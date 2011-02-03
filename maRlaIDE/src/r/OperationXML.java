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
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import problem.DataColumn;
import problem.DataColumn.DataMode;
import problem.DataNotFoundException;
import problem.DataSet;
import problem.DataSource;
import problem.DuplicateNameException;
import problem.MarlaException;
import problem.Operation;
import problem.OperationException;
import problem.OperationInfoRequiredException;
import problem.Problem;
import r.RProcessor.RecordMode;

/**
 * @author Ryan Morehart
 */
public class OperationXML extends Operation
{
	/**
	 * Path to the XML file that specifies the operations
	 */
	private static String operationFilePath = "ops.xml";
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
	 * Stores configuration data for a dynamic name/label for the operation
	 */
	private Element longNameEl = null;
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
	 * the OperationXML defaults the same as currently
	 * @param configEl XML configuration element upon which to add information
	 * @return XML element with configuration data set
	 */
	public static Element getConfig(Element configEl)
	{
		configEl.setAttribute("xml", operationFilePath);
		return configEl;
	}

	/**
	 * Loads the XML operations from disk using the default/last used file.
	 * An exception may be thrown when the version of the XML file is
	 * inappropriate or other parse errors occur.
	 */
	public static void loadXML() throws OperationXMLException
	{
		loadXML(operationFilePath);
	}

	/**
	 * Loads the XML. If the XML path has not been set by loadXML then an
	 * IncompleteInitializationException is thrown. If the XML is contains
	 * parse errors an exception will be thrown.
	 * @param xmlPath Path to the operation XML file
	 */
	public static void loadXML(String xmlPath) throws OperationXMLException
	{
		try
		{
			// Save for future use again
			operationFilePath = xmlPath;

			System.out.println("Loading XML operations from '" + operationFilePath + "'");

			// Make sure we know where we're looking for that there XML
			if(operationFilePath == null)
				throw new OperationXMLException("XML file for operations has not been specified");

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
			throw new OperationXMLException("Operation XML file '" + operationFilePath + "' contains XML error(s)", ex);
		}
		catch(IOException ex)
		{
			throw new OperationXMLException("An error occurred accessing the operation XML file '" + operationFilePath + "'", ex);
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
		// Attempt to load operations if it hasn't been done yet
		if(operationXML == null)
			loadXML();

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
		// Attempt to load operations if it hasn't been done yet
		if(operationXML == null)
			loadXML();

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
	}

	/**
	 * Creates a new operation with the given computational... stuff
	 * @param newOpConfig JDOM XML Element that contains the needed configuration information
	 */
	protected void setConfiguration(Element newOpConfig) throws OperationXMLException
	{
		opConfig = newOpConfig;
		setOperationName(opConfig.getAttributeValue("name"));

		// Dynamic name set if specified, purely cosmetic
		longNameEl = opConfig.getChild("longname");
		updateDynamicName();
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
		intendedRecordMode = proc.setRecorderMode(RecordMode.DISABLED);
		processSequence(proc, compEl);

		// Restore recording mode
		proc.setRecorderMode(intendedRecordMode);
	}

	private void processSequence(RProcessor proc, Element compEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Walk through each command/control structure sequentially
		for(Object elObj : compEl.getChildren())
		{
			Element el = (Element) elObj;

			String cmdName = el.getName();
			if(cmdName.equals("cmd"))
				processCmd(proc, el);
			else if(cmdName.equals("set"))
				processSet(proc, el);
			else if(cmdName.equals("save"))
				processSave(proc, el);
			else if(cmdName.equals("loop"))
				processLoop(proc, el);
			else if(cmdName.equals("if"))
				processIf(proc, el);
			else if(cmdName.equals("copy"))
				processCopy(proc, el);
			else if(cmdName.equals("plot"))
				processPlot(proc, el);
			else if(cmdName.equals("load"))
				processLoad(proc, el);
			else
				throw new OperationXMLException("Unrecognized command element '" + cmdName + "'");
		}
	}

	private void processCmd(RProcessor proc, Element cmdEl) throws RProcessorException
	{
		proc.setRecorderMode(intendedRecordMode);
		proc.execute(cmdEl.getTextTrim());
		proc.setRecorderMode(RecordMode.DISABLED);
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
		proc.setRecorderMode(intendedRecordMode);

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

			case NUMERIC:
				proc.setVariable(rVar, (Double) answerVal);
				break;

			case CHECKBOX:
				proc.execute(rVar + " = " + answerVal.toString().toUpperCase());
				break;

			case COLUMN:
				// Ensure the column actually exists in the parent data
				DataColumn parentCol = null;
				try
				{
					parentCol = getParentData().getColumn((String) answerVal);
				}
				catch(DataNotFoundException ex)
				{
					throw new OperationInfoRequiredException("The column '" + answerVal + "' has been removed", ex, this);
				}

				// Look for the modifier to only save the string
				String useType = setEl.getAttributeValue("use");
				if(useType == null || useType.equals("values"))
				{
					// Save all the values in the column
					proc.setVariable(rVar, parentCol);
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
		proc.setRecorderMode(RecordMode.DISABLED);
	}

	private void processSave(RProcessor proc, Element cmdEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Get the command we will execute for the value
		String cmd = cmdEl.getTextTrim();

		// Get Column, create if needed
		String colName = cmdEl.getAttributeValue("column");
		if(colName == null)
		{
			// Rats, dynamic one?
			String dynamicColumnCmd = cmdEl.getAttributeValue("r_column");
			if(dynamicColumnCmd == null)
				throw new OperationXMLException("No column name supplied for save");

			colName = proc.executeString(dynamicColumnCmd);
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
		proc.setRecorderMode(intendedRecordMode);
		String result = proc.execute(cmd);

		String processAs = cmdEl.getAttributeValue("type", "numeric");
		if(processAs.equals("numeric"))
		{
			col.setMode(DataColumn.DataMode.NUMERICAL);
			col.addAll(proc.parseDoubleArray(result));
		}
		else if(processAs.equals("string"))
		{
			col.setMode(DataMode.STRING);
			col.addAll(proc.parseStringArray(result));
		}
		else
			throw new OperationXMLException("Save type of '" + processAs + "' is unrecognized.");
		
		// Disable recorder again
		proc.setRecorderMode(RecordMode.DISABLED);
	}

	private void processLoop(RProcessor proc, Element loopEl) throws RProcessorException, RProcessorParseException, OperationXMLException, MarlaException
	{
		// Make up the loop we're going to work over and pass iteration back to processSequence()
		String indexVar = loopEl.getAttributeValue("index_var");
		String keyVar = loopEl.getAttributeValue("key_var");
		String valueVar = loopEl.getAttributeValue("value_var");

		String loopType = loopEl.getAttributeValue("type", "numeric");
		if(loopType.equals("parent"))
		{
			// Loop over every column in parent
			for(int i = 0; i < getParentData().getColumnCount(); i++)
			{
				// Assign the loop key and value
				proc.setRecorderMode(intendedRecordMode);
				if(keyVar != null)
					proc.setVariable(keyVar, getParentData().getColumn(i).getName());
				if(indexVar != null)
					proc.setVariable(indexVar, i + 1);
				if(valueVar != null)
					proc.setVariable(valueVar, getParentData().getColumn(i));
				proc.setRecorderMode(RecordMode.DISABLED);

				// Now do what the XML says
				processSequence(proc, loopEl);
			}
		}
		else if(loopType.equals("numeric"))
		{
			// Loop over an R vector, setting each element as the index var
			List<Double> doubleVals = proc.executeDoubleArray(loopEl.getAttributeValue("loop_var"));
			for(int i = 0; i < doubleVals.size(); i++)
			{
				// Assign the loop index
				proc.setRecorderMode(intendedRecordMode);
				if(indexVar != null)
					proc.setVariable(indexVar, new Double(i + 1));
				if(valueVar != null)
					proc.setVariable(valueVar, doubleVals.get(i));
				proc.setRecorderMode(RecordMode.DISABLED);

				// Now do what the XML says
				processSequence(proc, loopEl);
			}
		}
		else if(loopType.equals("string"))
		{
			// Loop over an R vector, setting each element as the index var
			List<String> stringVals = proc.executeStringArray(loopEl.getAttributeValue("loop_var"));
			for(int i = 0; i < stringVals.size(); i++)
			{
				// Assign the loop index
				proc.setRecorderMode(intendedRecordMode);
				if(indexVar != null)
					proc.setVariable(indexVar, new Double(i + 1));
				if(valueVar != null)
					proc.setVariable(valueVar, stringVals.get(i));
				proc.setRecorderMode(RecordMode.DISABLED);

				// Now do what the XML says
				processSequence(proc, loopEl);
			}
		}
		else
			throw new OperationXMLException("Loop type '" + loopType + "' not recognized.");
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
			throw new OperationXMLException("An operation may only have one plot in it");

		// Plot away
		plotPath = proc.startGraphicOutput();
		processSequence(proc, cmdEl);
		proc.stopGraphicOutput();
	}

	private void processCopy(RProcessor proc, Element copyEl) throws OperationXMLException, RProcessorParseException, RProcessorException, MarlaException
	{
		// What column(s) are we supposed to copy?
		if(Boolean.parseBoolean(copyEl.getAttributeValue("all", "false")))
		{
			// Copy all columns
			DataSource parent = getParentData();
			for(int i = 0; i < parent.getColumnCount(); i++)
			{
				copyColumn(parent.getColumn(i));
			}
		}
		else
		{
			// Single column copy
			String colName = copyEl.getAttributeValue("column");
			if(colName == null)
			{
				// Rats, dynamic one?
				String dynamicColumnName = copyEl.getAttributeValue("r_column");
				if(dynamicColumnName == null)
					throw new OperationXMLException("No column name supplied for copy");

				colName = proc.executeString(dynamicColumnName);
			}

			try
			{
				// Copy it over, with the same name and data
				copyColumn(colName);
			}
			catch(DataNotFoundException ex)
			{
				throw new OperationXMLException("Unable to locate column '" + colName + "' for copy");
			}
			catch(DuplicateNameException ex)
			{
				throw new OperationXMLException("A column copy must occur before any saves to a column of the same name");
			}
		}
	}

	private void processLoad(RProcessor proc, Element loadEl) throws OperationXMLException, RProcessorParseException, RProcessorException, MarlaException
	{
		// Find what library the operation wants to load
		String libToLoad = loadEl.getAttributeValue("library");
		if(libToLoad == null)
		{
			// Ok, try for a dynamic version then
			String dynamicName = loadEl.getAttributeValue("r_library");
			if(dynamicName == null)
				throw new OperationXMLException("No library specified for load");

			// Actually find the string in that variable
			libToLoad = proc.executeString(dynamicName);
		}

		Boolean loaded = false;
		try
		{
			// Attempt to load.
			loaded = proc.executeBoolean("library('" + libToLoad + "', logical.return=T)");
		}
		catch(RProcessorException ex)
		{
			// This is the "no package" error, right?
			if(ex.getMessage().contains("no package"))
				loaded = false;
			else
				throw ex;
		}

		// Install if needed and retry the load
		if(!loaded)
		{
			try
			{
				proc.execute("install.packages('" + libToLoad + "', repos='http://cran.r-project.org')");
				proc.execute("library('" + libToLoad + "')");
			}
			catch(RProcessorException ex)
			{
				throw new OperationXMLException("Unable to load library '" + libToLoad + "' and could not install it", ex);
			}
		}
	}

	@Override
	public boolean isInfoRequired() throws MarlaException
	{
		return !opConfig.getChildren("query").isEmpty();
	}

	/**
	 * Prompts user for the data specified by <query /> XML in operations. See
	 * documentation on wiki for more information
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
			// For columns, if we don't have a parent then we just say we want a generic
			// string basically. This case should only be hit when an operation is being
			// restored from a save file
			PromptType type = PromptType.valueOf(queryEl.getAttributeValue("type").toUpperCase());
			if(type == PromptType.COLUMN && getParentData() != null)
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
					Element opEl = (Element) opObj;
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
		// and ensure it is the proper type
		List<Object[]> prompt = getRequiredInfoPrompt();
		for(int i = 0; i < prompt.size(); i++)
		{
			String answerKey = (String) prompt.get(i)[1];
			PromptType promptType = (PromptType) prompt.get(i)[0];
			switch(promptType)
			{
				case COMBO: // A combo just gets returned as a string anyway
				case COLUMN: // Ditto with column, just need its name
				case STRING:
					questionAnswers.put(answerKey, val.get(i).toString());
					break;

				case NUMERIC:
					questionAnswers.put(answerKey, Double.parseDouble(val.get(i).toString()));
					break;

				case CHECKBOX:
					questionAnswers.put(answerKey, Boolean.parseBoolean(val.get(i).toString()));
					break;

				default:
					throw new OperationXMLException("Unable to set '" + promptType + "' yet.");
			}
		}

		// Change the operation name so that the label is pretty
		updateDynamicName();

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

	/**
	 * Sets the XML operation's name to the latest version, based on the data
	 * given in the longname element and the answers to queries
	 */
	private void updateDynamicName() throws OperationXMLException
	{
		if(longNameEl != null)
		{
			StringBuilder newName = new StringBuilder(getName().length());

			for(Object partObj : longNameEl.getContent())
			{
				// We only deal with elements (stuff we need to replace/handle)
				// and text, which we stick in verbatim. Ignore everything else, such as comments
				if(partObj instanceof Element)
				{
					Element partEl = (Element) partObj;
					if(partEl.getName().equals("response"))
					{
						// Pull data from one of the query answers unless they haven't been answered
						if(questionAnswers != null)
							newName.append(questionAnswers.get(partEl.getAttributeValue("name")).toString());
						else
							newName.append(partEl.getAttributeValue("default"));
					}
					else
					{
						throw new OperationXMLException("Invalid element '" + partEl.getName() + "' in long name XML");
					}
				}
				else if(partObj instanceof Text)
				{
					Text partText = (Text) partObj;
					newName.append(partText.getText());
				}
			}

			// Make it visible externally, not interal though
			super.setText(newName.toString());
		}
		else
		{
			// Just use the plain old name
			super.setText(opConfig.getAttributeValue("name"));
		}

		// Tell the problem to rebuild the displayed tree, rearranging as needed
		DataSource rootDS = getRootDataSource();
		if(rootDS instanceof DataSet && Problem.getDomain() != null)
			Problem.getDomain().rebuildTree((DataSet)rootDS);
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
	protected void fromXmlExtra(Element opEl) throws MarlaException
	{
		Element xmlEl = opEl.getChild("xmlop");

		// Load the correct XML specification
		setConfiguration(findConfiguration(xmlEl.getAttributeValue("name")));

		// Put togother the "response" to give to setRequiredInfo()
		// based on what it asks for and what our save file has in it.
		// Only do it though if we actually need to
		List<Object[]> requiredInfo = getRequiredInfoPrompt();
		if(requiredInfo.size() > 0)
		{
			List<Object> answersFromXML = new ArrayList<Object>();

			// Answer each question
			for(Object[] question : requiredInfo)
			{
				// Could use XPath to find the answer, but we'd need to bring
				// in jaxen (or some other engine). To avoid bloating for such
				// a small need, we just loop
				String questionName = (String) question[1];
				for(Object answerObj : xmlEl.getChildren("answer"))
				{
					Element answerEl = (Element) answerObj;
					if(questionName.equals(answerEl.getAttributeValue("key")))
						answersFromXML.add(answerEl.getAttributeValue("answer"));
				}
			}

			// Ensure we answered everything. If we didn't, make the user answer again
			if(answersFromXML.size() != requiredInfo.size())
				return;

			// Save the answers to the operation itself
			setRequiredInfo(answersFromXML);
		}
	}
}
