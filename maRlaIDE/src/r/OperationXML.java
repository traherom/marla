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
import problem.DataSet;
import problem.IncompleteInitialization;
import problem.Operation;
import problem.Problem;

/**
 * <p>Creates operations based on an XML specification. The XML file should be similar
 * to the following. A full example implementing a t-test is listed at the bottom.</p>
 * <code style="white-space: pre; font-family: monospace;">
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 * &lt;operations&gt;
 *   &lt;operation name="&lt;operation name&gt;"&gt;
 *     &lt;query type="&lt;column|checkbox|string|number|combo&gt;" name="&lt;internal name&gt;" prompt="&lt;User prompt&gt;" /&gt;
 *     &lt;computation&gt;
 *       &lt;set rvar="&lt;variable name&gt;" name="&lt;internal name&gt;" /&gt;
 *       
 *       &lt;cmd&gt;R command&lt;/cmd&gt;
 *       ...
 *       &lt;cmd&gt;R command&lt;/cmd&gt;
 *       
 *       &lt;loop type="parent|double_array|string_array" [keyVar="&lt;R variable name&gt;"] [valueVar="&lt;R variable name&gt;"] [rvar=""]&gt;
 *         &lt;cmd&gt;R command&lt;/cmd&gt;
 *         ...
 *       &lt;/loop&gt;
 *       
 *       &lt;save type="double|string|double_array|string_array" [column="&lt;Column name&gt;"] [dynamic_column="&lt;R command&gt;"] [rvar="&lt;R command&gt;"]&gt;[R command]&lt;/save&gt;
 *     &lt;/computation&gt;
 *   &lt;/operation&gt;
 * &lt;/operations&gt;
 * </code>
 * </p>
 *
 * <p>Each &lt;operation />&gt; takes a name that will be displayed to the user and can be used to
 * create that operation. It must be unique. If multiple with the same name exist an
 * exception will be thrown.</p>
 *
 * <p>Inside of an operation there can be multiple <query /> elements. These queries will be presented
 * to the user with the given "prompt" when they add the operation. Each query requires a "prompt",
 * a "type", and a "name". Prompt may be any arbitrary string to display to the user. The type
 * should be one of "column", "checkbox", "string", "number", or "combo".</p>
 * 
 * <p>A column type allows the selection of one of the columns in the parent data set and/or
 * operation. A combo requires that <option />s be specified. For example:
 * <code style="white-space: pre; font-family: monospace;">
 * &lt;query type="combo" name="test_type" prompt="Select the test to perform"&gt;
 *   &lt;option&gt;Two sample&lt;/option&gt;
 *   &lt;option&gt;Paired&lt;/option&gt;
 * &lt;/query&gt;
 * </code>
 * </p>
 *
 * <p>The user's response will be returned back and saved under the name given. In the computation
 * section of the operation specification these values can be sent to R to perform work.</p>
 *
 * <p>There then is a &lt;computation /&gt; section inside the operation. This section allows four types of
 * elements, all of which may be specified in any number of times. Elements will be executed in the
 * order they are encountered.</p>
 *
 * <p>The first element type is the simplest, commands. A &lt;cmd /&gt; takes a single R command and executes
 * it. An exception will be thrown if more than one command is placed here. If a newline is needed
 * in a string then use \n, not a hard line break.</p>
 *
 * <p>Second, a &lt;set /&gt; element allows for an R variable to be set from one of the user-prompted
 * values. The attribute "rvar" specifies the R variable name to use and "name" should reference the
 * same name as was used in the original &lt;query /&gt;. For example, to ask a user to select a column
 * and then save it for use:
 * <code style="white-space: pre; font-family: monospace;">
 * &lt;operation&gt;
 *   &lt;query type="column" name="selected_column" prompt="Select a column" /&gt;
 *   &lt;computation&gt;
 *     &lt;set rvar="col" name="selected_column" /&gt;
 *   &lt;computation&gt;
 * &lt;/operation&gt;
 * </code>
 * </p>
 *
 * <p>Third, &lt;loop /&gt;s allow repetition. Each loop must specify its "type" as either "parent",
 * "double_array", or "string_array". Parent loops go through every column of the parent data set
 * and/or operation, assigning them in turn to the R variable given in "valueVar". A "keyVar" may
 * also be specified for any loop type which will keep an index (1 based) of the loop number.
 * Array loops take the R command given in "rvar" and iterate over the elements in it, setting
 * each in turn as the value of valueVar. A loop may then use any of the other elements inside
 * itself, including other loops.</p>
 *
 * <p>Finally, to set the actual values for the operation, &lt;save /&gt; elements may be specified. The
 * result to save may be given either via an "rvar" attribute or as a contain R command. For
 * example, both of the following do the same thing:
 * <code style="white-space: pre; font-family: monospace;">
 * &lt;comuptation&gt;
 *   &lt;cmd&gt;testing = 75.6&lt;/cmd&gt;
 *   &lt;save type="double" column="ex" rvar="testing" /&gt;
 *   &lt;save type="double" column="ex"&gt;testing&lt;/save&gt;
 * &lt;/computation&gt;
 * </code>
 * </p>
 * <p>The "type" tells the operation how to process the result and may be one of "double", "string",
 * "double_array", or "string_array". The "column" attribute gives the name of the result
 * column to save into. Instead of plain "column", "dynamic_column" maybe used, which may
 * contain any R command that results in a single string.</p>
 *
 * <p>Below is an example of implementing a t-test:
 * <code style="white-space: pre; font-family: monospace;">
 * &lt;operations&gt;
 *   &lt;operation name="t-test"&gt;
 *     &lt;computation&gt;
 *       &lt;loop type="parent" indexVar="col"&gt;
 *         &lt;cmd&gt;t = t.test(col)&lt;/cmd&gt;
 *         &lt;save type="double" column="t"&gt;t$statistic&lt;/save&gt;
 *         &lt;save type="double" column="df"&gt;t$parameter&lt;/save&gt;
 *         &lt;save type="double" column="p-value"&gt;t$p.value&lt;/save&gt;
 *         &lt;save type="double" column="mean"&gt;t$estimate&lt;/save&gt;
 *         &lt;save type="double" column="CI"&gt;t$conf.int[1]&lt;/save&gt;
 *         &lt;save type="double" column="CI"&gt;t$conf.int[2]&lt;/save&gt;
 *         &lt;save type="double" column="alpha"&gt;attr(t$conf.int, 'conf.level')&lt;/save&gt;
 *       &lt;/loop&gt;
 *     &lt;/computation&gt;
 *   &lt;/operation&gt;
 * &lt;/operations&gt;
 * </code>
 * </p>
 *
 * The resultant operation does the following internally:
 * <code style="white-space: pre; font-family: monospace;">
 * test.csv
 *   Import.me: 10.0, 11.0, 12.0, 13.0
 *     Column.: 20.0, 21.0, 22.0, 23.0
 *       Hello: 30.0, 31.0, 32.0, 33.0
 * col = c(10.0, 11.0, 12.0, 13.0 )
 * t = t.test(col)
 * t$statistic
 * t$parameter
 * t$p.value
 * t$estimate
 * t$conf.int[1]
 * t$conf.int[2]
 * attr(t$conf.int, 'conf.level')
 * col = c(20.0, 21.0, 22.0, 23.0 )
 * t = t.test(col)
 * t$statistic
 * ... (repeat above)
 * attr(t$conf.int, 'conf.level')
 * col = c(30.0, 31.0, 32.0, 33.0 )
 * t = t.test(col)
 * t$statistic
 * ... (repeat again)
 * attr(t$conf.int, 'conf.level')
 * Result:
 * t-test
 *         Import.me t: 17.81572
 *        Import.me df: 3.0
 *   Import.me p-value: 3.856172E-4
 *      Import.me mean: 11.5
 *        Import.me CI: 9.44574, 13.55426
 *     Import.me alpha: 0.95
 *           Column. t: 33.30766
 *          Column. df: 3.0
 *     Column. p-value: 5.948823E-5
 *        Column. mean: 21.5
 *          Column. CI: 19.44574, 23.55426
 *       Column. alpha: 0.95
 *             Hello t: 48.79959
 *            Hello df: 3.0
 *       Hello p-value: 1.894812E-5
 *          Hello mean: 31.5
 *            Hello CI: 29.44574, 33.55426
 *         Hello alpha: 0.95
 * </code>
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
		CMD, SET, SAVE, LOOP
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

		// Force it to check for duplicate names
		getAvailableOperations();
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations.
	 * @return HashMap of the names and operations.
	 * @throws OperationXMLException Thrown when multiple operations with the same name are detected
	 */
	public static HashMap<String, Element> getAvailableOperations() throws OperationXMLException
	{
		HashMap<String, Element> opNames = new HashMap<String, Element>();
		for(Object opEl : operationXML.getChildren("operation"))
		{
			Element op = (Element) opEl;
			String name = op.getAttributeValue("name");

			// Only allow a name to appear once
			if(opNames.containsKey(name))
				throw new OperationXMLException("Multiple operations with the name '" + name + "' found");

			opNames.put(name, op);
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
	 * @throws OperationXMLException
	 */
	public static OperationXML createOperation(String opName) throws OperationXMLException
	{
		if(operationXML == null)
			throw new OperationXMLException("XML file has not been loaded yet.");

		Element op = null;
		for(Object opEl : operationXML.getChildren("operation"))
		{
			op = (Element) opEl;
			if(op.getAttributeValue("name").equals(opName))
				return createOperation(op);
		}

		// Couldn't find what they wanted
		throw new OperationXMLException("Unable to locate operation '" + opName + "'");
	}

	/**
	 * Creates a new instance of an operation with the given XML configuration.
	 * @param op JDOM XML Element that contains all the configuration information the an operation
	 * @return Newly created, functional operation
	 */
	public static OperationXML createOperation(Element op)
	{
		return new OperationXML(op);
	}

	/**
	 * Creates a new operation with the given computational... stuff
	 * @param config JDOM XML Element that contains the needed configuration information
	 */
	private OperationXML(Element config)
	{
		super(config.getAttributeValue("name"));
		opConfig = config;
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
			throw new OperationXMLException("Required info has not been set yet");

		// Process away
		Element compEl = opConfig.getChild("computation");
		processSequence("", compEl);
	}

	private void processSequence(String savePrepend, Element compEl) throws RProcessorException, RProcessorParseException, CalcException
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

				default:
					throw new OperationXMLException("Unknow computation command '" + type + "'");
			}
		}
	}

	private void processCmd(Element cmdEl) throws RProcessorException
	{
		proc.execute(cmdEl.getTextTrim());
	}

	private void processSet(Element cmdEl) throws RProcessorException
	{
		// What type of setVariable() should we call?
		String rVar = cmdEl.getAttributeValue("rvar");
		Object[] answer = questionAnswers.get(cmdEl.getAttributeValue("name"));
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
					throw new OperationXMLException("A DataColumn with the given name ('" + answer[1] + "') could not be found.");
				}
				break;

			default:
				throw new OperationXMLException("Unable to set '" + varType + "' yet.");
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
				throw new OperationXMLException("Commands of type '" + type + "' are not yet handled.");
		}
	}

	private void processLoop(String savePrepend, Element loopEl) throws RProcessorException, RProcessorParseException, CalcException
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
				throw new OperationXMLException("Loop type '" + type + "' not handled yet.");
		}
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
					throw new OperationXMLException("The query command type '" + type + "' is not yet handled.");
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

	public static void main(String[] args) throws Exception
	{
		try
		{
			Problem p = new Problem();
			DataSet ds1 = DataSet.importFile("test.csv");
			DataSet ds2 = DataSet.importFile("test.csv");
			p.addData(ds1);
			p.addData(ds2);

			OperationXML.loadXML("ops.xml");

			OperationXML testOpXML = OperationXML.createOperation("Summary");
			long startXML = System.currentTimeMillis();
			ds2.addOperation(testOpXML);
			long endXML = System.currentTimeMillis();

			OperationSummary testOpHC = new OperationSummary();
			long startHC = System.currentTimeMillis();
			ds1.addOperation(testOpHC);
			long endHC = System.currentTimeMillis();

			System.out.println("Hardcoded:");
			System.out.println(testOpHC);
			
			System.out.println("$$$$$$$$$$$$$$$$$$\nXML:");
			System.out.println(testOpXML);

			System.out.println("Time HC: " + (endHC - startHC));
			System.out.println("Time XML: " + (endXML - startXML));
		}
		finally
		{
			RProcessor.getInstance().close();
		}
	}
}
