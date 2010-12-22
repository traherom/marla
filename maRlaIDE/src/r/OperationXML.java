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
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import problem.CalcException;
import problem.DataColumn;
import problem.DataNotFound;
import problem.IncompleteInitialization;
import problem.Operation;
import problem.OperationException;
import problem.OperationInfoRequiredException;

/**
 * 
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
	private HashMap<String, Object[]> questionAnswers = null;

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
	 * @throws JDOMException A failure occurred during processing of the XML
	 * @throws IOException An error occurred reading the file
	 */
	public static void loadXML(String xmlPath) throws JDOMException, IOException
	{
		try
		{
			operationFilePath = xmlPath;
			reloadXML();
		}
		catch(IncompleteInitialization ex)
		{
			// This should be... impossible
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Loads the XML
	 * @throws JDOMException A failure occurred during processing of the XML
	 * @throws IOException An error occurred reading the file
	 * @throws IncompleteInitialization XML path not yet set by loadXML()
	 */
	public static void reloadXML() throws JDOMException, IOException, IncompleteInitialization
	{
		// Make sure we know where we're looking for that there XML
		if(operationFilePath == null)
			throw new IncompleteInitialization("");

		// Load file into JDOM
		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(operationFilePath);
		operationXML = doc.getRootElement();

		// TODO Check version. Maybe have to use old versions of parsers someday?
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations.
	 * @return ArrayList of the names of all available XML operations
	 * @throws OperationXMLException Thrown when multiple operations with the same name are detected
	 */
	public static ArrayList<String> getAvailableOperations() throws OperationXMLException
	{
		ArrayList<String> opNames = new ArrayList<String>();
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
	public static OperationXML createOperation(String opName) throws OperationXMLException
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
	public OperationXML()
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
		setName(opConfig.getAttributeValue("name"));
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
	protected void computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		// Ensure any requirements were met already
		if(isInfoRequired() && questionAnswers == null)
			throw new OperationInfoRequiredException("Required info has not been set yet", this);

		try
		{
			// Process away
			Element compEl = opConfig.getChild("computation");
			processSequence("", compEl);
		}
		catch(OperationXMLException ex)
		{
			throw new CalcException("An error exists in the XML for this operation", ex);
		}
	}

	private void processSequence(String savePrepend, Element compEl) throws RProcessorException, RProcessorParseException, CalcException, OperationXMLException
	{
		// Walk through each command/control structure sequentially
		for(Object elObj : compEl.getChildren())
		{
			Element el = (Element) elObj;
			CommandType type = CommandType.valueOf(el.getName().toUpperCase());
			switch(type)
			{
				case CMD:
					processCmd(el);
					break;

				case SET:
					processSet(el);
					break;

				case SAVE:
					processSave(savePrepend, el);
					break;

				case LOOP:
					processLoop(savePrepend, el);
					break;

				case PLOT:
					processPlot(savePrepend, el);
					break;

				default:
					throw new CalcException("Unknow computation command '" + type + "'");
			}
		}
	}

	private void processCmd(Element cmdEl) throws RProcessorException
	{
		proc.execute(cmdEl.getTextTrim());
	}

	private void processSet(Element cmdEl) throws RProcessorException, CalcException, OperationXMLException
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
					proc.setVariable(rVar, parent.getColumn((String) answer[1]));
				}
				catch(DataNotFound ex)
				{
					throw new OperationInfoRequiredException("A DataColumn with the given name ('" + answer[1] + "') could not be found.", this);
				}
				break;

			default:
				throw new CalcException("Unable to set '" + varType + "' yet.");
		}
	}

	private void processSave(String savePrepend, Element cmdEl) throws RProcessorException, RProcessorParseException, CalcException
	{
		// Get the value
		String cmd = cmdEl.getAttributeValue("rvar");
		if(cmd == null)
			cmd = cmdEl.getTextTrim();
		
		// Get Column, create if needed
		String colName = null;
		String dynamicColumnCmd = cmdEl.getAttributeValue("dynamic_column");
		if(dynamicColumnCmd != null)
		{
			colName = proc.executeString(dynamicColumnCmd);
		}
		else
		{
			colName = savePrepend + cmdEl.getAttributeValue("column");
		}

		DataColumn col = null;
		try
		{
			col = getColumn(colName);
		}
		catch(DataNotFound ex)
		{
			// The column doesn't exist yet, create it
			col = addColumn(colName);
		}

		SaveType type = SaveType.valueOf(cmdEl.getAttributeValue("type", "double").toUpperCase());
		switch(type)
		{
			case DOUBLE: // Saves either the given R variable or command result into the given column
				col.add(proc.executeDouble(cmd));
				break;

			case DOUBLE_ARRAY: // Saves either the given R variable or command result into the given column
				// Get the value
				col.addAll(proc.executeDoubleArray(cmd));
				break;

			default:
				throw new CalcException("Commands of type '" + type + "' are not yet handled.");
		}
	}

	private void processLoop(String savePrepend, Element loopEl) throws RProcessorException, RProcessorParseException, CalcException, OperationXMLException
	{
		// Make up the loop we're going to work over and pass iteration back to processSequence()
		String keyVar = loopEl.getAttributeValue("keyVar");
		String valueVar = loopEl.getAttributeValue("valueVar");

		LoopType type = LoopType.valueOf(loopEl.getAttributeValue("type").toUpperCase());
		switch(type)
		{
			case PARENT: // Loop over every column in parent
				for(int i = 0; i < parent.getColumnCount(); i++)
				{
					// Assign the loop key and value
					if(keyVar != null)
						proc.setVariable(keyVar, parent.getColumn(i).getName());
					if(valueVar != null)
						proc.setVariable(valueVar, parent.getColumn(i));

					// Now do what the XML says
					processSequence(parent.getColumn(i).getName() + savePrepend + " ", loopEl);
				}
				break;

			case DOUBLE_ARRAY: // Loop over an R vector, setting each element as the index var
				ArrayList<Double> doubleVals = proc.executeDoubleArray(loopEl.getAttributeValue("rvar"));
				for(int i = 0; i < doubleVals.size(); i++)
				{
					// Assign the loop index
					if(keyVar != null)
						proc.setVariable(keyVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, doubleVals.get(i));

					// Now do what the XML says
					processSequence(savePrepend + doubleVals.get(i) + " ", loopEl);
				}
				break;

			case STRING_ARRAY: // Loop over an R vector, setting each element as the index var
				ArrayList<String> stringVals = proc.executeStringArray(loopEl.getAttributeValue("rvar"));
				for(int i = 0; i < stringVals.size(); i++)
				{
					// Assign the loop index
					if(keyVar != null)
						proc.setVariable(keyVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, stringVals.get(i));

					// Now do what the XML says
					processSequence(savePrepend + stringVals.get(i) + " ", loopEl);
				}
				break;

			default:
				throw new CalcException("Loop type '" + type + "' not handled yet.");
		}
	}

	private void processPlot(String savePrepend, Element cmdEl) throws RProcessorException, RProcessorParseException, CalcException, OperationXMLException
	{
		// An operation may only have one plot in it
		if(plotPath != null)
			throw new RProcessorParseException("An operation may only have one plot in it");

		// Plot away
		plotPath = proc.startGraphicOutput();
		processSequence(savePrepend, cmdEl);
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
	public ArrayList<Object[]> getRequiredInfoPrompt()
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
								queryEl.getAttributeValue("prompt"), PromptType.COMBO, parent.getColumnNames()
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
	public void setRequiredInfo(ArrayList<Object> values)
	{
		markChanged();

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
	}

	@Override
	public String getPlot()
	{
		return plotPath;
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
			setConfiguration(findConfiguration(opEl.getAttributeValue("name")));
		}
		catch(OperationXMLException ex)
		{
			throw new RuntimeException("Unable to load operation '" + opEl.getAttributeValue("name") + "' from XML", ex);
		}
	}
}
