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
package marla.ide.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import marla.ide.operation.OperationInformation.PromptType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataColumn.DataMode;
import marla.ide.problem.DataNotFoundException;
import marla.ide.problem.DataSet;
import marla.ide.problem.DataSource;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.Problem;
import marla.ide.r.RProcessor;
import marla.ide.r.RProcessor.RecordMode;
import marla.ide.r.RProcessorException;
import marla.ide.r.RProcessorParseException;
import marla.ide.resource.Configuration.ConfigType;
import marla.ide.resource.ConfigurationException;

/**
 * Performs statistical operations though R as directed by an XML template
 * @author Ryan Morehart
 */
@SuppressWarnings("serial")
public class OperationXML extends Operation
{
	/**
	 * Path to the XML file that specifies the maRla-supplied operations
	 */
	private static String primaryOpsPath = null;
	/**
	 * Path to additional XML file that specify extra operations
	 */
	private static List<String> additionalOpsPaths = null;
	/**
	 * Storage location for parsed operation XML file
	 */
	private static Element operationXML = null;
	/**
	 * Configuration information for an instantiated operation
	 */
	private Element opConfig = null;
	/**
	 * Stores configuration data for a dynamic name/label for the operation
	 */
	private Element displayNameEl = null;
	/**
	 * Cache for the dynamic name that we build for the user. Because we read
	 * it from the XML we don't want to recalculate every time
	 */
	private String dynamicNameLong = null;
	/**
	 * Cache for the abbrevitaded version of the dynamic name that we build for the user.
	 */
	private String dynamicNameShort = null;
	/**
	 * Used to store the name of the plot we (might) create. Unused
	 * if the XML specification itself doesn't contain a <plot />
	 * element.
	 */
	private String plotPath = null;
	/**
	 * Saves the RecordMode we should be saving in. XML ops have to perform 
	 * extra R calls to do their work, we avoid saving those to the record
	 * by switching back and forth from RecordMode.DISABLED and this
	 */
	private RecordMode intendedRecordMode = null;

	/**
	 * Sets additional paths, beyond the primary, that OperationXML will load
	 * operations from
	 * @param newPath Path to main XML operation file
	 * @return Previously set path
	 */
	public static String setPrimaryXMLPath(String newPath)
	{
		String old = primaryOpsPath;

		try
		{
			clearXMLOps();
			primaryOpsPath = newPath;
			loadXML();
			return old;
		}
		catch(OperationXMLException ex)
		{
			// Revert back
			primaryOpsPath = old;
			throw new ConfigurationException("Primary operation XML file '" + newPath + "' appears to have errors", ConfigType.PrimaryOpsXML, ex);
		}
		catch(ConfigurationException ex)
		{
			// Revert back
			primaryOpsPath = old;
			throw ex;
		}
	}

	/**
	 * Gets the current main XML operation file path
	 * @return currently set path to the primary XML file
	 */
	public static String getPrimaryXMLPath()
	{
		return primaryOpsPath;
	}
	
	/**
	 * Sets additional paths, beyond the primary, that OperationXML will load
	 * operations from
	 * @param newPaths Path(s) to user-created XML operation files
	 * @return Previously set user paths
	 */
	public static List<String> setUserXMLPaths(List<String> newPaths)
	{
		List<String> old = additionalOpsPaths;

		try
		{
			clearXMLOps();
			additionalOpsPaths = newPaths;
			loadXML();
			return old;
		}
		catch(OperationXMLException ex)
		{
			// Revert back
			additionalOpsPaths = old;
			throw new ConfigurationException("User operation XML file(s) appear to have errors", ConfigType.UserOpsXML, ex);
		}
		catch(ConfigurationException ex)
		{
			// Revert back
			additionalOpsPaths = old;
			throw ex;
		}
	}

	/**
	 * Gets the current "user" (additional) XML operation file paths
	 * @return paths to the XML file(s) that contains operations
	 */
	public static List<String> getUserXMLPaths()
	{
		return additionalOpsPaths;
	}

	/**
	 * Removes any currently loaded XML operation files
	 */
	public static void clearXMLOps()
	{
		operationXML = null;
	}

	/**
	 * Loads the XML operations from disk using the set paths (user and primary)
	 * if they are not already. To force a reload, first call clearXMLOps()
	 */
	public static void loadXML()
	{
		if(operationXML == null)
		{
			if(primaryOpsPath != null)
				loadXML(primaryOpsPath, true);

			if(additionalOpsPaths != null)
				loadXML(additionalOpsPaths);
		}
	}

	/**
	 * Clears any current XML operations and loads the ones given
	 * @param xmlPaths Path(s) to operation XML files
	 */
	private static void loadXML(List<String> xmlPaths)
	{
		// Load each path listed
		for(String path : xmlPaths)
		{
			if(path != null && !path.isEmpty())
				loadXML(path, false);
		}
	}

	/**
	 * Loads the XML. If the XML path has not been set by loadXML then an
	 * IncompleteInitializationException is thrown. If the XML is contains
	 * parse errors an exception will be thrown.
	 * @param xmlPath Path to the operation XML file to include
	 * @param isPrimary true if this file is the primary (base). false if it's secondary/user supplied
	 */
	private static void loadXML(String xmlPath, boolean isPrimary)
	{
		try
		{
			// Make sure we know where we're looking
			if(xmlPath == null)
			{
				if(isPrimary)
					throw new ConfigurationException("XML file for operations has not been specified", ConfigType.PrimaryOpsXML);
				else
					throw new ConfigurationException("Null name for user XML operations given", ConfigType.UserOpsXML);
			}

			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(xmlPath);

			// It is an operation file, right?
			Element root = doc.getRootElement();
			if(!root.getName().equals("operations"))
				throw new ConfigurationException("XML file '" + xmlPath + "' does not appear to contain operations", ConfigType.PrimaryOpsXML);

			// Just save it or bring it into the combined doc?
			if(isPrimary)
				operationXML = root;
			else if(operationXML != null)
			{
				// Check each additional operation to see if it overrides a current one
				List<String> currNames = getAvailableOperations();
				List<Element> toBeRemoved = new ArrayList<Element>();
				for(Object newOpObj : root.getChildren("operation"))
				{
					Element newOpEl = (Element)newOpObj;
					String newOpName = newOpEl.getAttributeValue("name");

					// Remove from document if it's not named at all
					if(newOpName == null)
					{
						toBeRemoved.add(newOpEl);
						continue;
					}

					// See if the current operations already contain an operation
					// with the same name
					// Overwrite the XML for the current one by removing
					// the current element, wherever that might be.
					// We will then add the new one to the end
					for(Object currOpObj : operationXML.getChildren("operation"))
					{
						Element currOpEl = (Element)currOpObj;
						if(newOpName.equals(currOpEl.getAttributeValue("name")))
							toBeRemoved.add(currOpEl);
					}
				}

				// Now remove the duplicates/unnamed elements. Couldn't do it above because we
				// were looping over the tree
				for(Element opEl : toBeRemoved)
					opEl.getParentElement().removeContent(opEl);

				// Add all the new operations to the end
				operationXML.addContent(root.cloneContent());
			}
			else
				throw new InternalMarlaException("User XML given before primary XML");
		}
		catch(JDOMException ex)
		{
			throw new OperationXMLException("Operation XML file '" +xmlPath + "' contains XML error(s)", ex);
		}
		catch(IOException ex)
		{
			if(isPrimary)
				throw new ConfigurationException("Unable to read the primary operation XML file '" + xmlPath + "'", ConfigType.PrimaryOpsXML, ex);
			else
				throw new ConfigurationException("Unable to read the user operation XML file '" + xmlPath + "'", ConfigType.UserOpsXML, ex);
		}
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations.
	 * @return ArrayList of the names of all available XML operations
	 */
	public static List<String> getAvailableOperations()
	{
		// Attempt to load operations if it hasn't been done yet
		if(operationXML == null)
			loadXML();

		List<String> opNames = new ArrayList<String>();
		for(Object opEl : operationXML.getChildren("operation"))
		{
			Element op = (Element) opEl;

			String name = op.getAttributeValue("name");
			if(name == null)
				throw new OperationXMLException("No name supplied for operation in XML file");

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

			// Only allow a name to appear once (remove old if it does)
			int dupeIndex = opNames.indexOf(name);
			if(dupeIndex != -1)
				opNames.remove(dupeIndex);

			opNames.add(name);
		}

		return opNames;
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations. An exception is thrown if multiple operations
	 * with the same name are detected.
	 * @return ArrayList of the names of all available XML operations
	 */
	public static Map<String, List<String>> getAvailableOperationsCategorized()
	{
		// Attempt to load operations if it hasn't been done yet
		if(operationXML == null)
			loadXML();

		Map<String, List<String>> opsCategorized = new HashMap<String, List<String>>();
		for(Object opEl : operationXML.getChildren("operation"))
		{
			Element op = (Element) opEl;

			// Operation name and category
			String name = op.getAttributeValue("name");
			if(name == null)
				throw new OperationXMLException("No name supplied for operation in XML file");

			String cat = op.getAttributeValue("category", "Uncategorized");

			try
			{
				// Ensure that we're supposed to actually list this one
				if(!Boolean.parseBoolean(op.getAttributeValue("list", "true")))
					continue;
			}
			catch(NumberFormatException ex)
			{
				throw new OperationXMLException(name, "Invalid value '" + op.getAttributeValue("list") + "' for list attribute");
			}

			// Add to the categorized list.  Does this category already exist?
			if(!opsCategorized.containsKey(cat))
			{
				// Not yet, create new category
				List<String> newCat = new ArrayList<String>();
				newCat.add(name);
				opsCategorized.put(cat, newCat);
			}
			else
			{
				// Add to existing category
				opsCategorized.get(cat).add(name);
			}
		}

		return opsCategorized;
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
	public static OperationXML createOperation(String opName)
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
	protected static Element findConfiguration(String opName)
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
	public OperationXML()
	{
		super("Unconfigured");
	}

	/**
	 * Copy constructor
	 * @param org Original operation to copy
	 */
	private OperationXML(OperationXML org)
	{
		super(org);
		
		// TODO decide if clone is actually needed. For our undo/redo purposes,
		// probably not. I mean, it doesn't change ever. However, that's not
		// expected copy constructor behavior
		opConfig = (Element)org.opConfig.clone();
		if(org.displayNameEl != null)
			
		
		// TODO decide if clone is actually needed. For our undo/redo purposes,
		// probably not. I mean, it doesn't change ever. However, that's not
		// expected clone() behavior
		opConfig = (Element) opConfig.clone();
		if(displayNameEl != null)
			displayNameEl = (Element) displayNameEl.clone();
	}

	/**
	 * Returns the XML that powers this operation
	 * @return XML which operation follows, null if not set
	 */
	protected Element getConfiguration()
	{
		return opConfig;
	}

	/**
	 * Creates a new operation with the given computational... stuff
	 * @param newOpConfig JDOM XML Element that contains the needed configuration information
	 */
	protected void setConfiguration(Element newOpConfig)
	{
		// Ignore if it's the same
		if(opConfig == newOpConfig)
			return;

		opConfig = newOpConfig;

		// Obviously we'll need to update
		markDirty();

		setOperationName(opConfig.getAttributeValue("name"));

		// Parse all the questions
		clearQuestions();
		@SuppressWarnings("unchecked")
		List<Element> queries = opConfig.getChildren("query");

		// Build the array for each of the questions
		for(Element queryEl : queries)
		{
			String name = queryEl.getAttributeValue("name");
			if(name == null)
				throw new OperationXMLException("Name not supplied for operation query");

			String prompt = queryEl.getAttributeValue("prompt");
			if(prompt == null)
				throw new OperationXMLException("Prompt not supplied for operation's '" + name + "' query");

			// Ask the GUI for the right type of value
			// For columns, if we don't have a parent then we just say we want a generic
			// string basically. This case should only be hit when an operation is being
			// restored from a save file
			PromptType type = PromptType.valueOf(queryEl.getAttributeValue("type").toUpperCase());
			if(type == PromptType.COLUMN)
			{
				// What type of column should we present to the user?
				String colType = queryEl.getAttributeValue("column_type", "all");
				List<String> columnNames = new ArrayList<String>();

				OperationInformation opQuery = null;

				// Select correct type
				if(colType.equals("numeric"))
					opQuery = new OperationInfoColumn(this, name, prompt, DataMode.NUMERIC);
				else if(colType.equals("string"))
					opQuery = new OperationInfoColumn(this, name, prompt, DataMode.STRING);
				else if(colType.equals("all"))
					opQuery = new OperationInfoColumn(this, name, prompt);
				else
					throw new OperationXMLException("Invalid column type '" + colType + "' specified for query");

				// Actually add the question to the array
				addQuestion(opQuery);
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
				addQuestion(new OperationInfoFixedOptionCombo(this, name, prompt, options));
			}
			else if(type == PromptType.NUMERIC)
			{
				// Are limits imposed?
				Double min = Double.MIN_VALUE;
				String minStr = queryEl.getAttributeValue("min");
				if(minStr != null)
					min = Double.valueOf(minStr);

				Double max = Double.MAX_VALUE;
				String maxStr = queryEl.getAttributeValue("max");
				if(maxStr != null)
					max = Double.valueOf(maxStr);

				addQuestion(new OperationInfoNumeric(this, name, prompt, min, max));
			}
			else if(type == PromptType.STRING)
			{
				// No processing needed
				addQuestion(new OperationInfoString(this, name, prompt));
			}
			else if(type == PromptType.CHECKBOX)
			{
				// No processing needed
				addQuestion(new OperationInfoCheckbox(this, name, prompt));
			}
			else if(type == PromptType.FIXED)
			{
				// Pull out the default value
				addQuestion(new OperationInfoFixed(this, name, prompt, queryEl.getAttributeValue("value")));
			}
			else
			{
				throw new OperationXMLException("Unhandled operation prompt type '" + type + "'");
			}
		}

		// Dynamic name set if specified, purely cosmetic
		displayNameEl = opConfig.getChild("displayname");
		checkDisplayName();
	}

	@Override
	public void checkDisplayName()
	{
		if(updateDynamicName() && !isLoading())
		{
			// We actually did change from what was being used
			DataSource root = getRootDataSource();
			if(Problem.getDomain() != null && root instanceof DataSet)
				Problem.getDomain().rebuildTree((DataSet)root);
		}
	}

	/**
	 * Performs the appropriate operations according to whatever the XML says. Fun!
	 */
	@Override
	protected void computeColumns(RProcessor proc)
	{
		// Ensure any requirements were met already
		if(isInfoUnanswered())
			throw new OperationInfoRequiredException("Required info has not been set yet", this);

		// Clear out old plot
		this.plotPath = null;

		// Get computation element
		Element compEl = opConfig.getChild("computation");
		if(compEl == null)
			throw new OperationXMLException("Computation element not specified");

		try
		{
			// Process away. Only record the R commands we explicitly say to
			intendedRecordMode = proc.setRecorderMode(RecordMode.DISABLED);
			processSequence(proc, compEl);
		}
		catch(OperationXMLException ex)
		{
			// Add the operation name to exception for easier debugging
			ex.addName(getName());
			throw ex;
		}
		finally
		{
			// Restore recording mode
			proc.setRecorderMode(intendedRecordMode);
		}
	}

	/**
	 * Main processor for a sequence of XML commands. Analyzes each in turn and
	 * passes them off to the appropriate parser
	 * @param proc R process to work through, passes it to children
	 * @param compEl Element with commands to run through
	 */
	private void processSequence(RProcessor proc, Element compEl)
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
			else if(cmdName.equals("plot"))
				processPlot(proc, el);
			else if(cmdName.equals("error"))
				processError(proc, el);
			else if(cmdName.equals("load"))
				processLoad(proc, el);
			else
				throw new OperationXMLException("Unrecognized command element '" + cmdName + "'");
		}
	}

	/**
	 * Passes a single R command off to the processor. 
	 * @param proc R process to work through
	 * @param cmdEl Element containing the R command to run
	 */
	private void processCmd(RProcessor proc, Element cmdEl)
	{
		try
		{
			proc.setRecorderMode(intendedRecordMode);
			proc.execute(cmdEl.getTextTrim());
		}
		catch(RProcessorException ex)
		{
			throw new OperationXMLException("Command given in XML appears to be invalid", ex);
		}
		finally
		{
			proc.setRecorderMode(RecordMode.DISABLED);
		}
	}

	/**
	 * Sets an R variable with the user's answer to a given prompt
	 * @param proc R process to set variable to
	 * @param setEl Element containing the information about set
	 */
	private void processSet(RProcessor proc, Element setEl)
	{
		// What answer are we looking for here?
		String promptKey = setEl.getAttributeValue("name");
		if(promptKey == null)
			throw new OperationXMLException("XML specification does not mark '" + promptKey + "' as required information but uses it in computation.");

		// Read the answer
		OperationInformation answer = getQuestion(promptKey);

		// Record the set calls
		proc.setRecorderMode(intendedRecordMode);

		// All of them will save to here
		String rVar = setEl.getAttributeValue("rvar");

		// Find out what type of query it was so we know where setVar to call
		PromptType promptType = answer.getType();
		switch(promptType)
		{
			case COMBO: // No processing needed for these
			case STRING:
			case FIXED:
			case NUMERIC:
			case CHECKBOX:
				proc.setVariable(rVar, answer.getAnswer());
				break;

			case COLUMN:
				// Look for the modifier to only save the string
				String useType = setEl.getAttributeValue("use");
				if(useType == null || useType.equals("values"))
				{
					// Save all the values in the column
					proc.setVariable(rVar, getParentData().getColumn((String)answer.getAnswer()));
				}
				else if(useType.equals("name"))
				{
					// Just save the column name
					proc.setVariable(rVar, (String)answer.getAnswer());
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

	/**
	 * Saves a given R variable to a column. Intended for saving the results of
	 * a calculations
	 * @param proc R process to pull data from
	 * @param cmdEl Element containing the information about save
	 */
	private void processSave(RProcessor proc, Element cmdEl)
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

		String processAs = cmdEl.getAttributeValue("type", "auto");
		if(processAs.equals("auto"))
		{
			try
			{
				col.setMode(DataColumn.DataMode.NUMERIC);
				col.addAll(proc.parseDoubleArray(result));
			}
			catch(RProcessorParseException ex)
			{
				col.setMode(DataMode.STRING);
				col.addAll(proc.parseStringArray(result));
			}
		}
		else if(processAs.equals("numeric"))
		{
			col.setMode(DataColumn.DataMode.NUMERIC);
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

	/**
	 * Processes the same sequence of commands repeatedly, setting the given R
	 * variables with the current state of the loop
	 * @param proc R process to work with
	 * @param loopEl Element containing loop information
	 */
	private void processLoop(RProcessor proc, Element loopEl)
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
				if(keyVar != null)
					proc.setVariable(keyVar, new Double(i + 1));
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
				if(keyVar != null)
					proc.setVariable(keyVar, new Double(i + 1));
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

	/**
	 * Conditional. Based on the given expression (various kinds available),
	 * executes either its then or else block.
	 * @param proc R process to perform checks through
	 * @param ifEl Element containing conditional specification
	 */
	private void processIf(RProcessor proc, Element ifEl)
	{
		// Figure out what type of if it is and check if it's true or false
		boolean ifExprResult = false;

		String expr = ifEl.getAttributeValue("expr");
		String expectedVarType = ifEl.getAttributeValue("vartype");
		String colExists = ifEl.getAttributeValue("colexists");

		if(expr != null)
		{
			try
			{
				// Custom expression, pass to R and return that
				ifExprResult = proc.executeBoolean(expr);
			}
			catch(RProcessorParseException ex)
			{
				throw new OperationXMLException("If expression did not return a single boolean value", ex);
			}
		}
		else if(expectedVarType != null)
		{
			// Get the type of the variable given in "rvar"
			String var = ifEl.getAttributeValue("rvar");
			String strResult = proc.execute("str(" + var + ")");
			String realVarType = strResult.substring(1, 4);

			// And ensure we match the expected value
			if(expectedVarType.equals("numeric") && realVarType.equals("num"))
				ifExprResult = true;
			else if(expectedVarType.equals("string") && realVarType.equals("chr"))
				ifExprResult = true;
			else
				ifExprResult = false;
		}
		else if(colExists != null)
		{
			// Does a column with that name exist yet?
			if(getColumnIndex(colExists) != -1)
				ifExprResult = true;
			else
				ifExprResult = false;
		}
		else
		{
			throw new OperationXMLException("If type not recognized.");
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

	/**
	 * Starts a plot with R and processes the sequence of commands inside. At
	 * the end of the sequence, the plot is finished and the results saved 
	 * as the plot for the operation. May only be executed once in an operation
	 * @param proc R process to work get plot from
	 * @param cmdEl Element containing plot commands
	 */
	private void processPlot(RProcessor proc, Element cmdEl)
	{
		// An operation may only have one plot in it
		if(plotPath != null)
			throw new OperationXMLException("An operation may only have one plot in it");

		// Plot away
		plotPath = proc.startGraphicOutput();
		processSequence(proc, cmdEl);
		proc.stopGraphicOutput();
	}

	/**
	 * Terminates the operation processing, sending the given message as
	 * an exception, likely to be shown to the user.
	 * @param proc R process to work with if needed
	 * @param errorEl Element containing message to pass to user
	 */
	private void processError(RProcessor proc, Element errorEl)
	{
		// The operation wants us to throw an error to the user
		String msg = errorEl.getAttributeValue("msg", "");
		if(msg != null)
			throw new OperationXMLException(msg);
		else
			throw new OperationXMLException("No message supplied for error");
	}

	/**
	 * Loads a library into R. If it is not installed, it attempts to install
	 * it automatically.
	 * @param proc R process to load library into
	 * @param loadEl Element containing library to load
	 */
	private void processLoad(RProcessor proc, Element loadEl)
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

		if(!proc.loadLibrary(libToLoad))
			throw new OperationXMLException("Unable to load and/or install library '" + libToLoad + "' into R");
	}

	@Override
	public String getDisplayString(boolean abbrv)
	{
		if(abbrv)
			return dynamicNameShort;
		else
			return dynamicNameLong;
	}

	@Override
	public String getDescription()
	{
		// Return operation description if there is one, otherwise
		// just return the name of the operation
		String desc = opConfig.getChildText("description");
		if(desc != null)
			return desc;
		else
			return getName();
	}

	/**
	 * Sets the XML operation's name to the latest version, based on the data
	 * given in the longname element and the answers to queries
	 * @return true if the dynamic name actually changed
	 */
	private boolean updateDynamicName()
	{
		String oldLongName = dynamicNameLong;

		// Only bother if we aren't still initializing
		if(opConfig == null)
			return false;

		if(displayNameEl != null)
		{
			StringBuilder shortName = new StringBuilder();
			StringBuilder longName = new StringBuilder();

			for(Object partObj : displayNameEl.getContent())
			{
				// We only deal with elements (stuff we need to replace/handle)
				// and text, which we stick in verbatim. Ignore everything else, such as comments
				if(partObj instanceof Element)
				{
					Element partEl = (Element) partObj;
					if(partEl.getName().equals("response"))
					{
						// Pull data from one of the query answers unless they haven't been answered
						Object val = null;
						if(!isInfoUnanswered())
						{
							String resName = partEl.getAttributeValue("name");
							OperationInformation q = getQuestion(resName);
							if(q != null)
								val = q.getAnswer();
							else
								throw new OperationXMLException(getName(), "Display name asks for question '" + resName + "' but it does not exist");
						}
						else
						{
							val = partEl.getAttributeValue("default");
							if(val == null)
								throw new OperationXMLException(getName(), "No default given for filler in display name");
						}

						// Append to names
						longName.append(val);
						shortName.append(DataSet.shortenString(val.toString(), 5));
					}
					else
					{
						throw new OperationXMLException("Invalid element '" + partEl.getName() + "' in long name XML");
					}
				}
				else if(partObj instanceof Text)
				{
					Text partText = (Text) partObj;
					shortName.append(partText.getText());
					longName.append(partText.getText());
				}
			}

			// Make it visible externally, not interal though
			dynamicNameLong = longName.toString();
			dynamicNameShort = shortName.toString();
		}
		else
		{
			// Just use the plain old name
			dynamicNameLong = opConfig.getAttributeValue("name");
			dynamicNameShort = DataSet.shortenString(dynamicNameLong, 5);
		}

		// Did the name change?
		return !dynamicNameLong.equals(oldLongName);
	}

	@Override
	public boolean hasPlot()
	{
		// Check if plot="true" is set for this op
		return Boolean.parseBoolean(opConfig.getAttributeValue("plot", "false"));
	}

	@Override
	public String getPlot()
	{
		// Only bother if we have a plot
		if(!hasPlot())
			return null;

		// Ensure it's computed then return
		checkCache();
		return plotPath;
	}

	@Override
	public OperationXML clone()
	{
		return new OperationXML(this);
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

		// Config we work with is the same?
		OperationXML otherOp = (OperationXML) other;
		if(!opConfig.equals(otherOp.opConfig))
			return false;
		
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		hash = 31 * hash + (this.opConfig != null ? this.opConfig.hashCode() : 0);
		return hash;
	}

	/**
	 * Saves the XML operation name
	 * @param extraEl Element to attach name to
	 * @return Element with the XML operation name
	 */
	@Override
	protected Element toXmlExtra(Element extraEl)
	{
		extraEl.setAttribute("name", opConfig.getAttributeValue("name"));
		return extraEl;
	}

	/**
	 * Loads opConfig with the appropriate XML execution configuration.
	 * @param extraEl Element with the needed name of the XML operation to use
	 */
	@Override
	protected void fromXmlExtra(Element extraEl)
	{
		// Load the correct XML specification
		setConfiguration(findConfiguration(extraEl.getAttributeValue("name")));
	}
}
