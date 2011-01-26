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
import problem.DataColumn.DataMode;
import problem.DataNotFoundException;
import problem.DataSource;
import problem.IncompleteInitializationException;
import problem.MarlaException;
import problem.Operation;
import problem.OperationException;
import problem.OperationInfoRequiredException;
import r.RProcessor.RecordMode;

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
	private Map<String, Object> questionAnswers = null;
	/**
	 * Saves the RecordMode we should be saving in. XML ops have to perform 
	 * extra R calls to do their work, we avoid saving those to the record
	 * by switching back and forth from RecordMode.DISABLED and this
	 */
	private RecordMode intendedRecordMode = null;

	/**
	 * Types of commands we support from XML
	 */
	private enum CommandType
	{
		CMD, SET, SAVE, LOOP, IF, PLOT
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
	 * Configures the defaults for XML operations based on the given XML configuration
	 * @param configEl XML configuration element with settings as attributes
	 */
	public static void setConfig(Element configEl) throws OperationXMLException
	{
		// Extract information from configuration XML and set appropriately
		loadXML(configEl.getAttributeValue("xml"));
	}

	/**
	 * Creates an XML element that could be passed back to setConfig to configure
	 * the LatexExporter defaults the same as currently
	 * @param configEl XML configuration element upon which to add information
	 * @return XML element with configuration data set
	 */
	public static Element getConfig(Element configEl)
	{
		configEl.setAttribute("xml", operationFilePath);
		return configEl;
	}

	/**
	 * Saves the passed XML file path and loads it from disk. An exception may
	 * be thrown  when the version of the XML file is inappropriate or other parse errors
	 * occur.
	 * @param xmlPath Path to the operation XML file
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
			throw new OperationXMLException("Operation XML path not specified or null", ex);
		}
	}

	/**
	 * Loads the XML. If the XML path has not been set by loadXML then an
	 * IncompleteInitializationException is thrown. If the XML is contains
	 * parse errors an exception will be thrown.
	 */
	public static void reloadXML() throws IncompleteInitializationException, OperationXMLException
	{
		try
		{
			System.out.println("Loading XML operations from '" + operationFilePath + "'");
			
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
	 * that will perform the calculations. An exception is thrown if multiple operations
	 * with the same name are detected.
	 * @return ArrayList of the names of all available XML operations
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

			try
			{
				// Ensure that we're supposed to actually list this one
				if(!Boolean.parseBoolean(op.getAttributeValue("list", "true")))
					continue;
			}
			catch(NumberFormatException ex)
			{
				throw new OperationXMLException("Operation '" + name + "': Invalid value '" + op.getAttributeValue("list") + "' for list attribute");
			}

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
	 * efficient to directly just call that. An exception is thrown if the
	 * operation could not be loaded from the XML.
	 * @param opName Name of the operation to load from the XML file
	 * @return New operation that will perform the specified computations
	 */
	public static OperationXML createOperation(String opName) throws OperationXMLException, RProcessorException
	{
		OperationXML newOp = new OperationXML();
		newOp.setConfiguration(findConfiguration(opName));
		return newOp;
	}

	/**
	 * Locates the named operation in the XML file and returns the associated Element.
	 * An exception is thrown if an operation with the given name cannot be found in the XML.
	 * @param opName XML operation to find in the file, as specified by its "name" attribute.
	 * @return Element holding the configuration information for the operation
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
	 */
	@Override
	protected void computeColumns(RProcessor proc) throws RProcessorException, RProcessorParseException, OperationXMLException, OperationInfoRequiredException, MarlaException
	{
		// Ensure any requirements were met already
		if(isInfoRequired() && questionAnswers == null)
			throw new OperationInfoRequiredException("Required info has not been set yet", this);

		// Clear out old plot
		this.plotPath = null;

		// Get computation element
		Element compEl = opConfig.getChild("computation");
		if(compEl == null)
			throw new OperationXMLException("Computation element not specified");

		// Process away. Only record the R commands we explicitly say to
		intendedRecordMode = proc.setRecorder(RecordMode.DISABLED);
		processSequence(proc, compEl);

		// Restore recording mode
		proc.setRecorder(intendedRecordMode);
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

				case IF:
					processIf(proc, el);
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
		proc.setRecorder(intendedRecordMode);
		proc.execute(cmdEl.getTextTrim());
		proc.setRecorder(RecordMode.DISABLED);
	}

	private void processSet(RProcessor proc, Element setEl) throws OperationXMLException, OperationInfoRequiredException, RProcessorException, MarlaException
	{
		// What answer are we looking for here?
		String promptKey = setEl.getAttributeValue("name");
		if(promptKey == null)
			throw new OperationXMLException("XML specification does not mark '" + promptKey + "' as required information but uses it in computation.");

		// Read the answer
		Object answerVal = questionAnswers.get(promptKey);
		
		// Record the set calls
		proc.setRecorder(intendedRecordMode);

		// All of them will save to here
		String rVar = setEl.getAttributeValue("rvar");

		// Find out what type of query it was so we know where setVar to call
		PromptType promptType = getPromptType(promptKey);
		switch(promptType)
		{
			case COMBO: // A combo just gets returned as a string anyway
			case STRING:
				proc.setVariable(rVar, (String) answerVal);
				break;

			case NUMBER:
				proc.setVariable(rVar, (Double) answerVal);
				break;

			case CHECKBOX:
				proc.execute(rVar + " = " + answerVal.toString().toUpperCase());
				break;

			case COLUMN:
				// Look for the modifier to only save the string
				String useType = setEl.getAttributeValue("use");
				if(useType == null || useType.equals("values"))
				{
					// Save all the values in the column
					try
					{
						proc.setVariable(rVar, getParentData().getColumn((String) answerVal));
					}
					catch(DataNotFoundException ex)
					{
						throw new OperationInfoRequiredException("A DataColumn with the given name '" + answerVal + "' could not be found.", this);
					}
				}
				else if(useType.equals("name"))
				{
					// Just save the column name
					proc.setVariable(rVar, (String) answerVal);
				}
				else
				{
					throw new OperationXMLException("Invalid setting '" + useType + "' for use attribute");
				}
				break;

			default:
				throw new OperationXMLException("Unable to set '" + promptType + "' yet.");
		}

		// Go back to old record mode
		proc.setRecorder(RecordMode.DISABLED);
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

		// Process the command we're saving
		proc.setRecorder(intendedRecordMode);

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

		// Disable again
		proc.setRecorder(RecordMode.DISABLED);
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
					proc.setRecorder(intendedRecordMode);
					if(nameVar != null)
						proc.setVariable(nameVar, getParentData().getColumn(i).getName());
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, getParentData().getColumn(i));
					proc.setRecorder(RecordMode.DISABLED);

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			case DOUBLE_ARRAY: // Loop over an R vector, setting each element as the index var
				List<Double> doubleVals = proc.executeDoubleArray(loopEl.getAttributeValue("loopVar"));
				for(int i = 0; i < doubleVals.size(); i++)
				{
					// Assign the loop index
					proc.setRecorder(intendedRecordMode);
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, doubleVals.get(i));
					proc.setRecorder(RecordMode.DISABLED);

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			case STRING_ARRAY: // Loop over an R vector, setting each element as the index var
				List<String> stringVals = proc.executeStringArray(loopEl.getAttributeValue("loopVar"));
				for(int i = 0; i < stringVals.size(); i++)
				{
					// Assign the loop index
					proc.setRecorder(intendedRecordMode);
					if(indexVar != null)
						proc.setVariable(indexVar, new Double(i + 1));
					if(valueVar != null)
						proc.setVariable(valueVar, stringVals.get(i));
					proc.setRecorder(RecordMode.DISABLED);

					// Now do what the XML says
					processSequence(proc, loopEl);
				}
				break;

			default:
				throw new OperationXMLException("Loop type '" + type + "' not recognized.");
		}
	}

	private void processIf(RProcessor proc, Element ifEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Check if the expression passes
		boolean ifExprResult = false;
		String type = ifEl.getAttributeValue("type");
		if(type.equals("vartype"))
		{
			// Get the type of the variable given in "rvar"
			String var = ifEl.getAttributeValue("rvar");
			String strResult = proc.execute("str(" + var + ")");
			String varType = strResult.substring(1, 4);

			// And ensure we match the expected value
			String expected = ifEl.getAttributeValue("expected");
			if(expected.equals("numeric") && varType.equals("num"))
				ifExprResult = true;
			else if(expected.equals("string") && varType.equals("chr"))
				ifExprResult = true;
			else
				ifExprResult = false;
		}
		else if(type.equals("expr"))
		{
			try
			{
				// Custom expression, pass to R and return that
				String expr = ifEl.getAttributeValue("expr");
				ifExprResult = proc.executeBoolean(expr);
			}
			catch(RProcessorParseException ex)
			{
				throw new OperationXMLException("If expression did not return a single boolean value", ex);
			}
		}
		else
		{
			throw new OperationXMLException("If type '" + type + "' not recognized.");
		}

		// Run then then/else blocks as appropriate
		if(ifExprResult)
		{
			// Is there a then?
			Element thenEl = ifEl.getChild("then");
			if(thenEl != null)
				processSequence(proc, thenEl);
		}
		else
		{
			// Is there an else?
			Element elseEl = ifEl.getChild("else");
			if(elseEl != null)
				processSequence(proc, elseEl);
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

	@Override
	public boolean isInfoRequired() throws MarlaException
	{
		return !opConfig.getChildren("query").isEmpty();
	}

	/**
	 * Prompts user for the data specified by <query /> XML in operations. Query
	 * takes the following attributes:
	 *		type - checkbox, number, string, column, combo
	 *		name - name to reference this value as after it is returned to setRequiredInfo()
	 *		prompt - Question to actually present the user with
	 */
	@Override
	public List<Object[]> getRequiredInfoPrompt() throws MarlaException
	{
		List<Object[]> questions = new ArrayList<Object[]>();

		// Build the array for each of the questions
		for(Object queryElObj : opConfig.getChildren("query"))
		{
			Element queryEl = (Element) queryElObj;

			// Ask the GUI for the right type of value
			PromptType type = PromptType.valueOf(queryEl.getAttributeValue("type").toUpperCase());
			if(type == PromptType.COLUMN)
			{
				// What type of column should we present to the user?
				String colType = queryEl.getAttributeValue("column_type", "all");
				DataSource parent = getParentData();
				List<String> columnNames = new ArrayList<String>();

				// Build a list of the column names of the correct type
				if(colType.equals("numeric"))
				{
					// Numeric columns
					for(int i = 0; i < parent.getColumnCount(); i++)
					{
						DataColumn currCol = parent.getColumn(i);
						if(currCol.getMode() == DataMode.NUMERICAL)
							columnNames.add(currCol.getName());
					}
				}
				else if(colType.equals("string"))
				{
					// String columns
					for(int i = 0; i < parent.getColumnCount(); i++)
					{
						DataColumn currCol = parent.getColumn(i);
						if(currCol.getMode() == DataMode.STRING)
							columnNames.add(currCol.getName());
					}
				}
				else if(colType.equals("all"))
				{
					// All columns
					for(int i = 0; i < parent.getColumnCount(); i++)
					{
						DataColumn currCol = parent.getColumn(i);
						columnNames.add(currCol.getName());
					}
				}
				else
				{
					throw new OperationXMLException("Invalid column type '" + colType + "' specified for query");
				}

				// Actually add the question to the array
				questions.add(new Object[]
						{
							PromptType.COLUMN, queryEl.getAttributeValue("name"), queryEl.getAttributeValue("prompt"), columnNames.toArray()
						});
			}
			else if(type == PromptType.COMBO)
			{
				// Build list of options
				List<String> options = new ArrayList<String>();
				for(Object opObj : queryEl.getChildren("option"))
				{
					Element opEl = (Element)opObj;
					options.add(opEl.getText());
				}

				// Set question
				questions.add(new Object[]
						{
							PromptType.COMBO, queryEl.getAttributeValue("name"), queryEl.getAttributeValue("prompt"), options.toArray()
						});
			}
			else
			{
				// No processing needed
				questions.add(new Object[]
						{
							type, queryEl.getAttributeValue("name"), queryEl.getAttributeValue("prompt")
						});
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
	public void setRequiredInfo(List<Object> val) throws OperationException, MarlaException
	{
		// It would be an error to try to set when none is asked for
		if(!isInfoRequired())
			throw new OperationException("This operation does not require info, should not be set");

		// Create new answer map to fill with new answers
		questionAnswers = new HashMap<String, Object>();

		// Go through each asked for element to determine the name associated with it
		List<Object[]> prompt = getRequiredInfoPrompt();
		for(int i = 0; i < prompt.size(); i++)
		{
			questionAnswers.put((String)prompt.get(i)[1], val.get(i));
		}

		// We need to recompute
		markChanged();
		markUnsaved();
	}


	private Element getPromptEl(String key) throws OperationXMLException
	{
		for(Object queryElObj : opConfig.getChildren("query"))
		{
			Element queryEl = (Element) queryElObj;
			if(key.equals(queryEl.getAttributeValue("name")))
				return queryEl;
		}

		throw new OperationXMLException("Unable to find a prompt with the key '" + key + "'");
	}

	private PromptType getPromptType(String key) throws OperationXMLException
	{
		return getPromptType(getPromptEl(key));
	}

	private PromptType getPromptType(Element promptEl) throws OperationXMLException
	{
		return PromptType.valueOf(promptEl.getAttributeValue("type").toUpperCase());
	}

	@Override
	public boolean hasPlot() throws MarlaException
	{
		// Check if plot="true" is set for this op
		return Boolean.parseBoolean(opConfig.getAttributeValue("plot", "false"));
	}

	@Override
	public String getPlot() throws MarlaException
	{
		// Only bother if we have a plot
		if(!hasPlot())
			return null;

		// Ensure it's computed then return
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

		// Compare answers
		if(questionAnswers == null && otherOp.questionAnswers == null)
			return true; // Nobody has answers
		else if(questionAnswers == null && otherOp.questionAnswers != null)
			return false; // We don't have answers, they do. Yes, the test doesn't need the second part
		else
			return questionAnswers.equals(otherOp.questionAnswers);
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
	protected Element toXmlExtra() throws MarlaException
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
				String promptKey = it.next();
				Object answer = questionAnswers.get(promptKey);

				Element answerEl = new Element("answer");
				answerEl.setAttribute("key", promptKey);
				answerEl.setAttribute("type", getPromptType(promptKey).toString());
				answerEl.setAttribute("answer", answer.toString());

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
				questionAnswers = new HashMap<String, Object>();
				for(Object answerObj : xmlEl.getChildren("answer"))
				{
					Element answerEl = (Element) answerObj;

					String key = answerEl.getAttributeValue("key");
					
					// Covert the actual answer if needed
					Object answer = null;
					PromptType type = getPromptType(key);
					switch(type)
					{
						case NUMBER:
							answer = Double.parseDouble(answerEl.getAttributeValue("answer"));
							break;
							
						case CHECKBOX:
							answer = Boolean.parseBoolean(answerEl.getAttributeValue("answer"));
							break;

						default:
							answer = answerEl.getAttributeValue("answer");
					}

					// Save to the new operation
					questionAnswers.put(key, answer);
				}
			}
		}
		catch(OperationXMLException ex)
		{
			throw new RuntimeException("Unable to load operation '" + opEl.getAttributeValue("name") + "' from XML", ex);
		}
	}
}
