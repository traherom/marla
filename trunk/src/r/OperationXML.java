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
		PARENT, R_VECTOR
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
	}

	/**
	 * Returns a list of all the operations in the given XML file and the
	 * elements in the XML that describe those operations. The Element or
	 * the name can then be passed off to createOperation() to retrieve an object
	 * that will perform the calculations.
	 * @return HashMap of the names and operations.
	 */
	public static HashMap<String, Element> getAvailableOperations()
	{
		HashMap<String, Element> opNames = new HashMap<String, Element>();
		for(Object opEl : operationXML.getChildren("operation"))
		{
			Element op = (Element) opEl;
			opNames.put(op.getAttributeValue("name"), op);
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
		processSequence(compEl);
	}

	private void processSequence(Element compEl) throws RProcessorException, RProcessorParseException, CalcException
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
					processSave(el);
					break;

				case LOOP:
					processLoop(el);
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

	private void processSave(Element cmdEl) throws RProcessorException, RProcessorParseException, CalcException
	{
		String cmd = null;
		DataColumn col = null;

		SaveType type = SaveType.valueOf(cmdEl.getAttributeValue("type", "double").toUpperCase());
		switch(type)
		{
			case DOUBLE: // Saves either the given R variable or command result into the given column
				// Get the value
				cmd = cmdEl.getAttributeValue("rvar");
				if(cmd == null)
					cmd = cmdEl.getTextTrim();
				Double val = proc.executeDouble(cmd);

				// Save to column, create if needed
				try
				{
					col = getColumn(cmdEl.getAttributeValue("column"));
					col.add(val);
				}
				catch(DataNotFound ex)
				{
					// The column doesn't exist yet, create it
					col = addColumn(cmdEl.getAttributeValue("column"));
					col.add(val);
				}
				break;

			case DOUBLE_ARRAY: // Saves either the given R variable or command result into the given column
				// Get the value
				cmd = cmdEl.getAttributeValue("rvar");
				if(cmd == null)
					cmd = cmdEl.getTextTrim();
				ArrayList<Double> vals = proc.executeDoubleArray(cmd);

				// Save to column, create if needed
				try
				{
					col = getColumn(cmdEl.getAttributeValue("column"));
					col.addAll(vals);
				}
				catch(DataNotFound ex)
				{
					// The column doesn't exist yet, create it
					col = addColumn(cmdEl.getAttributeValue("column"));
					col.addAll(vals);
				}
				break;

			default:
				throw new OperationXMLException("Commands of type '" + type + "' are not yet handled.");
		}
	}

	private void processLoop(Element loopEl) throws RProcessorException, RProcessorParseException, CalcException
	{
		// Make up the loop we're going to work over and pass iteration back to processSequence()
		String indexVar = loopEl.getAttributeValue("indexVar");
		LoopType type = LoopType.valueOf(loopEl.getAttributeValue("type").toUpperCase());
		switch(type)
		{
			case PARENT: // Loop over every column in parent
				for(int i = 0; i < parent.getColumnCount(); i++)
				{
					// Assign the loop index
					proc.setVariable(indexVar, parent.getColumn(i));

					// Now do what the XML says
					processSequence(loopEl);
				}
				break;

			case R_VECTOR: // Loop over an R vector, setting each element as the index var
				ArrayList<Double> vector = proc.executeDoubleArray(loopEl.getAttributeValue("rvar"));
				for(Double val : vector)
				{
					// Assign the loop index
					proc.setVariable(indexVar, val);

					// Now do what the XML says
					processSequence(loopEl);
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
		for(int i = 0; i < values.size(); i++)
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
			DataSet ds = DataSet.importFile("test.csv");
			p.addData(ds);

			OperationXML.loadXML("ops.xml");

			OperationXML testOp = OperationXML.createOperation("xyprint");
			/*ArrayList<Object> responses = new ArrayList<Object>();
			responses.add("Import.me");
			responses.add("Column.");
			testOp.setRequiredInfo(responses);
			ds.addOperation(testOp);
			System.out.println(testOp);
*/
			//testOp = OperationXML.createOperation("nop");
			//ds.addOperation(testOp);
			//System.out.println(testOp);

			testOp = OperationXML.createOperation("count");
			ds.addOperation(testOp);
			System.out.println(testOp);
		}
		finally
		{
			RProcessor.getInstance().close();
		}
	}
}
