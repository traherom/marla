/*
 * The maRla Project - Graphical problem solver for statistics and probability problems.
 * Copyright (C) 2010 Cedarville University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package r;

import gui.Domain.PromptType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import problem.DataColumn;
import problem.DataNotFoundException;
import problem.IncompleteInitializationException;
import problem.MarlaException;
import problem.Operation;
import problem.OperationException;
import problem.OperationInfoRequiredException;

/**
 * @author Ryan Morehart
 */
public class OperationXML extends Operation
{
	/**
	 * Path to the XML file that specifies the operations
	 */
	private static String operationFilePath = null;
	/**
	 * Storage location for parsed operation XML file
	 */
	private static Element operationXML = null;
	/**
	 * Parser version this file was meant to be used under
	 */
	private static int parserVersion = Integer.MIN_VALUE;
	/**
	 * Configuration information for an instantiated operation
	 */
	private Element opConfig = null;
	/**
	 * Used to store the name of the plot we (might) create. Unused
	 * if the XML specification itself doesn't contain a <plot />
	 * element.
	 */
	private String plotPath = null;
	/**
	 * Saves the answer from the GUI to any questions we asked
	 */
	private Map<String, Object[]> questionAnswers = null;

	/**
	 * Types of queries we support from XML
	 */
	private enum QueryType
	{
		CHECKBOX, STRING, NUMBER, COLUMN, COMBO
	};

	/**
	 * Types of commands we support from XML
	 */
	private enum CommandType
	{
		CMD, SET, SAVE, LOOP, PLOT
	};

	/**
	 * Ways to process results from R
	 */
	private enum SaveType
	{
		DOUBLE, STRING, DOUBLE_ARRAY, STRING_ARRAY
	};

	/**
	 * Different things we can loop over
	 */
	private enum LoopType
	{
		PARENT, DOUBLE_ARRAY, STRING_ARRAY
	};

	/**
	 * Saves the passed XML file path and loads it from disk
	 * @param xmlPath Path to the operation XML file
	 * @throws OperationXMLException Thrown when the version of the XML file is inappropriate
	 */
	public static void loadXML(String xmlPath) throws OperationXMLException
	{
		try
		{
			operationFilePath = xmlPath;
			reloadXML();
		}
		catch(IncompleteInitializationException ex)
		{
			throw new OperationXMLException("Operation XML path not specified", ex);
		}
	}

	/**
	 * Loads the XML
	 * @throws IncompleteInitializationException XML path not yet set by loadXML()
	 * @throws OperationXMLException Thrown when the version of the XML file is inappropriate
	 */
	public static void reloadXML() throws IncompleteInitializationException, OperationXMLException
	{
		try
		{
			// Make sure we know where we're looking for that there XML
			if(operationFilePath == null)
				throw new IncompleteInitializationException("XML file for operations has not been specified");

			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(operationFilePath);
			operationXML = doc.getRootElement();

			// TODO Check version. Maybe have to use old versions of parsers someday?
			parserVersion = Integer.parseInt(operationXML.getAttributeValue("version"));
			if(parserVersion != 1)
				throw new OperationXMLException("Version " + parserVersion + " of operational XML cannot be parsed.");
		}
		catch(JDOMException ex)
		{
			throw new OperationXMLException("An error while processing the operation XML file", ex);
		}
		catch(IOException ex)
		{
			throw new OperationXMLException("An error occurred in accessing the operation XML file", ex);
		}
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations.
	 * @return ArrayList of the names of all available XML operations
	 * @throws OperationXMLException Thrown when multiple operations with the same name are detected
	 */
	public static List<String> getAvailableOperations() throws OperationXMLException
	{
		if(operationXML == null)
			throw new OperationXMLException("XML file has not been loaded yet.");

		List<String> opNames = new ArrayList<String>();
		for(Object opEl : operationXML.getChildren("operation"))
		{
			Element op = (Element) opEl;
			String name = op.getAttributeValue("name");

			// Only allow a name to appear once
			if(opNames.contains(name))
				throw new OperationXMLException("Multiple XML operations with the name '" + name + "' found");

			opNames.add(name);
		}
		
		return opNames;
	}

	/**
	 * Creates a new instance of an operation with the given name. Names come
	 * from the list in getAvailableOperations(). It then locates the corresponding
	 * XML Element and passes it off to createOperation(Element). It is more
	 * efficient to directly just call that.
	 * @param opName Name of the operation to load from the XML file
	 * @return New operation that will perform the specified computations
	 * @throws OperationXMLException Thrown when the given operation name cannot be found
	 *		in the XML file or when XML has not yet been loaded.
	 */
	public static OperationXML createOperation(String opName) throws OperationXMLException, RProcessorException
	{
		OperationXML newOp = new OperationXML();
		newOp.setConfiguration(findConfiguration(opName));
		return newOp;
	}

	/**
	 * Locates the named operation in the XML file and returns the associated Element
	 * @param opName XML operation to find in the file, as specified by its "name" attribute.
	 * @return Element holding the configuration information for the operation
	 * @throws OperationXMLException Unable to locate the corresponding XML operation
	 */
	protected static Element findConfiguration(String opName) throws OperationXMLException
	{
		if(operationXML == null)
			throw new OperationXMLException("XML file has not been loaded yet.");

		Element op = null;
		for(Object opEl : operationXML.getChildren("operation"))
		{
			op = (Element) opEl;
			if(op.getAttributeValue("name").equals(opName))
				return op;
		}

		// Couldn't find what they wanted
		throw new OperationXMLException("Unable to locate operation '" + opName + "'");
	}

	/**
	 * Exists basically to allow Operation to load us from a save file. Should rarely
	 * be used externally otherwise, instead use createOperation(). Before this may be
	 * used opConfig needs to be set, which can only occur through fromXMLExtra(Element)
	 * or createOperation()
	 */
	public OperationXML() throws RProcessorException
	{
		super("Unconfigured");
		opConfig = null;
	}

	/**
	 * Creates a new operation with the given computational... stuff
	 * @param newOpConfig JDOM XML Element that contains the needed configuration information
	 */
	protected void setConfiguration(Element newOpConfig)
	{
		opConfig = newOpConfig;
		setOperationName(opConfig.getAttributeValue("name"));
	}

	/**
	 * Performs the appropriate operations according to whatever the XML says. Fun!
	 * @throws CalcException Thrown as a result of other functions performing calculations
	 * @throws RProcessorParseException Thrown if the R processor could not parse the R output
	 *		as it was instructed to. Likely a programming error.
	 * @throws RProcessorException Error working with the R process itself (permissions or closed
	 *		pipes, for example).
	 */
	@Override
	protected void computeColumns(RProcessor proc) throws RProcessorException, RProcessorParseException, OperationXMLException, OperationInfoRequiredException, MarlaException
	{
		// Ensure any requirements were met already
		if(isInfoRequired() && questionAnswers == null)
			throw new OperationInfoRequiredException("Required info has not been set yet", this);

		// Clear out old plot
		this.plotPath = null;

		// Process away
		Element compEl = opConfig.getChild("computation");
		processSequence(proc, compEl);
	}

	private void processSequence(RProcessor proc, Element compEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Walk through each command/control structure sequentially
		for(Object elObj : compEl.getChildren())
		{
			Element el = (Element) elObj;
			CommandType type = CommandType.valueOf(el.getName().toUpperCase());
			switch(type)
			{
				case CMD:
					processCmd(proc, el);
					break;

				case SET:
					processSet(proc, el);
					break;

				case SAVE:
					processSave(proc, el);
					break;

				case LOOP:
					processLoop(proc, el);
					break;

				case PLOT:
					processPlot(proc, el);
					break;

				default:
					throw new OperationXMLException("Unknown computation command '" + type + "'");
			}
		}
	}

	private void processCmd(RProcessor proc, Element cmdEl) throws RProcessorException
	{
		proc.execute(cmdEl.getTextTrim());
	}

	private void processSet(RProcessor proc, Element cmdEl) throws OperationXMLException, OperationInfoRequiredException, RProcessorException, MarlaException
	{
		Object[] answer = null;

		try
		{
			answer = questionAnswers.get(cmdEl.getAttributeValue("name"));
			if(answer == null)
				throw new OperationXMLException("XML specification does not mark '" + cmdEl.getAttributeValue("name") + "' as required information but uses it in computation.");
		}
		catch(NullPointerException ex)
		{
			throw new OperationInfoRequiredException("Required info is somehow null. Tell devs.", this);
		}

		// Which setVariable() should we call?
		String rVar = cmdEl.getAttributeValue("rvar");
		QueryType varType = (QueryType) answer[0];
		switch(varType)
		{
			case COMBO: // A combo just gets returned as a string anyway
			case STRING:
				proc.setVariable(rVar, (String) answer[1]);
				break;

			case NUMBER:
				proc.setVariable(rVar, (Double) answer[1]);
				break;

			case COLUMN:
				try
				{
					proc.setVariable(rVar, getParentData().getColumn((String) answer[1]));
				}
				catch(DataNotFoundException ex)
				{
					throw new OperationInfoRequiredException("A DataColumn with the given name ('" + answer[1] + "') could not be found.", this);
				}
				break;

			default:
				throw new OperationXMLException("Unable to set '" + varType + "' yet.");
		}
	}

	private void processSave(RProcessor proc, Element cmdEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Get the command we will execute for the value
		String cmd = cmdEl.getTextTrim();

		// Get Column, create if needed
		String colName = null;
		String dynamicColumnCmd = cmdEl.getAttributeValue("dynamic_column");
		if(dynamicColumnCmd != null)
		{
			colName = proc.executeString(dynamicColumnCmd);
		}
		else
		{
			colName = cmdEl.getAttributeValue("column");
		}

		DataColumn col = null;
		try
		{
			col = getColumn(colName);
		}
		catch(DataNotFoundException ex)
		{
			// The column doesn't exist yet, create it
			col = addColumn(colName);
		}

		SaveType type = SaveType.valueOf(cmdEl.getAttributeValue("type", "double").toUpperCase());
		switch(type)
		{
			case DOUBLE: // Saves either the given R variable or command result into the given column
				col.setMode(DataColumn.DataMode.NUMERICAL);
				col.add(proc.executeDouble(cmd));
				break;

			case DOUBLE_ARRAY: // Saves either the given R variable or command result into the given column
				// Get the value
				col.setMode(DataColumn.DataMode.NUMERICAL);
				col.addAll(proc.executeDoubleArray(cmd));
				break;

			case STRING:
				col.setMode(DataColumn.DataMode.STRING);
				col.add(proc.executeString(cmd));
				break;

			case STRING_ARRAY:
				col.setMode(DataColumn.DataMode.STRING);
				col.addAll(proc.executeStringArray(cmd));
				break;

			default:
				throw new OperationXMLException("Save type of '" + type + "' is unrecognized.");
		}
	}

	private void processLoop(RProcessor proc, Element loopEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Make up the loop we're going to work over and pass iteration back to processSequence()
		String nameVar = loopEl.getAttributeValue("nameVar");
		String indexVar = loopEl.getAttributeValue("indexVar");
		String valueVar = loopEl.getAttributeValue("valueVar");

		LoopType type = LoopType.valueOf(loopEl.getAttributeValue("type").toUpperCase());
		switch(type)
		{
			case PARENT: // Loop over every column in parent
				for(int i = 0; i < getParentData().getColumnCount(); i++)
				{
					// Assign the loop key and value
					if(nameVar != null)
						proc.setVariable(nameVar, getParentData().getColumn(i).getName());
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, getParentData().getColumn(i));

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			case DOUBLE_ARRAY: // Loop over an R vector, setting each element as the index var
				ArrayList<Double> doubleVals = proc.executeDoubleArray(loopEl.getAttributeValue("loopVar"));
				for(int i = 0; i < doubleVals.size(); i++)
				{
					// Assign the loop index
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, doubleVals.get(i));

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			case STRING_ARRAY: // Loop over an R vector, setting each element as the index var
				ArrayList<String> stringVals = proc.executeStringArray(loopEl.getAttributeValue("loopVar"));
				for(int i = 0; i < stringVals.size(); i++)
				{
					// Assign the loop index
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, stringVals.get(i));

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			default:
				throw new OperationXMLException("Loop type '" + type + "' not recognized.");
		}
	}

	private void processPlot(RProcessor proc, Element cmdEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// An operation may only have one plot in it
		if(plotPath != null)
			throw new RProcessorParseException("An operation may only have one plot in it");

		// Plot away
		plotPath = proc.startGraphicOutput();
		processSequence(proc, cmdEl);
		proc.stopGraphicOutput();
	}

	/**
	 * Prompts user for the data specified by <query /> XML in operations. Query
	 * takes the following attributes:
	 *		type - checkbox, number, string, column, combo
	 *		name - name to reference this value as after it is returned to setRequiredInfo()
	 *		prompt - Question to actually present the user with
	 */
	@Override
	public ArrayList<Object[]> getRequiredInfoPrompt() throws MarlaException
	{
		ArrayList<Object[]> questions = new ArrayList<Object[]>();

		for(Object queryElObj : opConfig.getChildren("query"))
		{
			Element queryEl = (Element) queryElObj;
			QueryType type = QueryType.valueOf(queryEl.getAttributeValue("type").toUpperCase());
			switch(type)
			{
				case CHECKBOX:
					questions.add(new Object[]
							{
								queryEl.getAttributeValue("prompt"), PromptType.CHECKBOX
							});
					break;

				case STRING: // Number and string both present a box, it varies
				case NUMBER: // how we handle them after it's returned
					questions.add(new Object[]
							{
								queryEl.getAttributeValue("prompt"), PromptType.TEXT
							});
					break;

				case COLUMN:
					// Build a list of the column names
					questions.add(new Object[]
							{
								queryEl.getAttributeValue("prompt"), PromptType.COMBO, getParentData().getColumnNames()
							});
					break;

				default:
					throw new RuntimeException(new OperationXMLException("The query command type '" + type + "' is not yet handled."));
			}
		}

		return questions;
	}

	/**
	 * Saves the returned values into a HashMap with keys from the name given in the query
	 * XML. First element in the HashMap stores the type that the value actually represents
	 * @param values ArrayList of Objects that answer the questions.
	 */
	@Override
	public void setRequiredInfo(ArrayList<Object> values) throws OperationException, MarlaException
	{
		// It would be an error to try to set when none is asked for
		if(!isInfoRequired())
			throw new OperationException("This operation does not require info, should not be set");

		questionAnswers = new HashMap<String, Object[]>();
		@SuppressWarnings("unchecked")
		List<Element> queryEls = opConfig.getChildren("query");
		for(int i = 0; i < queryEls.size(); i++)
		{
			Element queryEl = queryEls.get(i);
			Object[] temp = new Object[2];
			temp[0] = QueryType.valueOf(queryEl.getAttributeValue("type").toUpperCase());
			temp[1] = values.get(i);
			questionAnswers.put(queryEl.getAttributeValue("name"), temp);
		}

		markChanged();
		markUnsaved();
	}

	@Override
	public String getPlot() throws MarlaException
	{
		checkCache();
		return plotPath;
	}

	@Override
	public boolean equals(Object other)
	{
		// Operation checks apply here too
		if(!super.equals(other))
			return false;

		// Actually an XML operation?
		if(!(other instanceof OperationXML))
			return false;

		OperationXML otherOp = (OperationXML) other;
		if(!opConfig.equals(otherOp.opConfig))
			return false;

		// Can only test if it's not null
		if(questionAnswers != null)
		{
			if(questionAnswers.equals(otherOp.questionAnswers))
				return false;
		}
		else
		{
			// Other side had better be null too then...
			if(otherOp.questionAnswers != null)
				return false;
		}

		return true;
	}

	@Override
	public Operation clone()
	{
		OperationXML op = (OperationXML) super.clone();
		// TODO op.questionAnswers = (HashMap<String, Object[]>) (questionAnswers == null ? null : questionAnswers.clone());
		return op;
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		hash = 31 * hash + (this.opConfig != null ? this.opConfig.hashCode() : 0);
		hash = 31 * hash + (this.questionAnswers != null ? this.questionAnswers.hashCode() : 0);
		return hash;
	}

	/**
	 * Saves the XML operation name
	 * @return Element with the XML operation name
	 */
	@Override
	protected Element toXmlExtra()
	{
		Element el = new Element("xmlop");
		el.setAttribute("name", opConfig.getAttributeValue("name"));

		// Answers to prompts, if they exist
		if(questionAnswers != null)
		{
			Set<String> keys = questionAnswers.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext())
			{
				String key = it.next();
				Object[] answer = questionAnswers.get(key);

				Element answerEl = new Element("answer");
				answerEl.setAttribute("key", key);
				answerEl.setAttribute("type", answer[0].toString());
				answerEl.setAttribute("answer", answer[1].toString());

				el.addContent(answerEl);
			}
		}

		return el;
	}

	/**
	 * Loads opConfig with the appropriate XML execution configuration.
	 * @param opEl Element with the needed name of the XML operation to use
	 */
	@Override
	protected void fromXmlExtra(Element opEl)
	{
		try
		{
			Element xmlEl = opEl.getChild("xmlop");

			// Load the correct XML specification
			setConfiguration(findConfiguration(xmlEl.getAttributeValue("name")));

			// Question answers
			if(!xmlEl.getChildren("answer").isEmpty())
			{
				questionAnswers = new HashMap<String, Object[]>();
				for(Object answer : xmlEl.getChildren("answer"))
				{
					Element answerEl = (Element) answer;

					String key = answerEl.getAttributeValue("key");
					Object[] an = new Object[2];
					an[0] = QueryType.valueOf(answerEl.getAttributeValue("type"));

					// Covert the actual answer if needed
					if(an[0] == QueryType.CHECKBOX)
					{
						an[1] = Boolean.parseBoolean(answerEl.getAttributeValue("answer"));
					}
					else
					{
						an[1] = answerEl.getAttributeValue("answer");
					}

					questionAnswers.put(key, an);
				}
			}
		}
		catch(OperationXMLException ex)
		{
			throw new RuntimeException("Unable to load operation '" + opEl.getAttributeValue("name") + "' from XML", ex);
		}
	}
}
