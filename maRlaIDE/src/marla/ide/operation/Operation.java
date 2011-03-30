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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;
import marla.ide.problem.Changeable;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataNotFoundException;
import marla.ide.problem.DataSet;
import marla.ide.problem.DataSource;
import marla.ide.problem.DuplicateNameException;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.MarlaException;
import marla.ide.problem.Problem;
import marla.ide.problem.SubProblem;
import marla.ide.r.RProcessor;

/**
 * Operation to perform on a parent object that implements
 * the DataAccess interface. The root parent will be a DataSet,
 * which then gets refined down by a chain of Operations.
 *
 * @author Ryan Morehart
 */
public abstract class Operation extends DataSource implements Changeable
{
	/**
	 * Denotes when this operation is in the middle of loading from XML,
	 * blocks calculations and such from altering the structure until load is
	 * complete.
	 */
	private boolean isLoading = false;
	/**
	 * Index that this Operation's data starts at. Generally speaking, this would
	 * be 0 if we have no parent (or it has no data) or the number of columns
	 * that our parent contains. It could be found each time, but this helps
	 * speed.
	 */
	private int startIndex = 0;
	/**
	 * Actual values from computation
	 */
	private final DataSet data;
	/**
	 * Parent data that this operation works on
	 */
	private DataSource parent;
	/**
	 * Saves the answer from the GUI to any questions we asked
	 */
	private final Map<String, OperationInformation> questions = new HashMap<String, OperationInformation>();
	/**
	 * A user-specified remark about this operation. Why it's here, whatever.
	 * Intended to be used as a tool for analysis
	 */
	private String remark = "";
	/**
	 * Saves the R operations used the last time refreshCache() was called. This
	 * string can then be dumped out by getRCommands() to give an idea of how to perform
	 * the calculations
	 */
	private String operationRecord = null;
	/**
	 * True if the operation needs to recompute its values before returning results
	 */
	private boolean isCacheDirty = true;
	/**
	 * True if the operation is recalculating its results, used to allow it to
	 * work with its own columns and not cause infinite recursion. Not certain
	 * about the proper handling of threads here. TODO: ensure thread safety.
	 */
	private boolean inRecompute = false;
	/**
	 * List of Java Operation derivative classes that may be created by
	 * the GUI front end.
	 */
	private static Map<String, String> javaOps;
	/**
	 * Categorization of the Java operations
	 */
	private static Map<String, List<String>> javaOpCategories;

	/**
	 * Initializes the list of available Java-based (hard coded) operations.
	 * This list should contain key value pairs with the key being a friendly, user
	 * readable name and the value being the class string, as would be passed to Class.forName().
	 * For example, a mean operation in the the r package would be "Mean" => "r.OperationMean"
	 * Additionally, the categories Map must be filled with "Category" => {"op", "op", ...}
	 */
	static
	{
		javaOps = new HashMap<String, String>();
		javaOpCategories = new HashMap<String, List<String>>();
	}

	/**
	 * Returns a list of all the operations available, both from XML and Java. This is a
	 * hard coded list for now and eases adding new operations to the GUI (no need to edit the
	 * other package in a few places). An exception is thrown when multiple operations with
	 * the same name are detected
	 * @return ArrayList of the names of operations available. Each name will be unique. If an XML
	 *		operation with the same name as a Java operation exists then the XML version will
	 *		be used. Otherwise an OperationException is thrown.
	 */
	public static List<String> getAvailableOperationsList()
	{
		List<String> ops = new ArrayList<String>();

		// Java
		ops.addAll(javaOps.keySet());

		// XML operations
		for(String xmlOpName : OperationXML.getAvailableOperations())
		{
			if(!ops.contains(xmlOpName))
				ops.add(xmlOpName);
		}

		// And sort 'em
		Collections.sort(ops);

		return ops;
	}

	/**
	 * Returns a list of all the operations available, both from XML and Java. The
	 * Map is categorized such that it looks like "Category" => {"op1", "op2", ...}.
	 * An exception* is thrown when multiple operations with the same name are detected.
	 * @return Map of the categories of operations available pointing to the operations
	 *		in that category. Each name will be unique. If an XML operation with the
	 *		same name as a Java operation exists then the XML version will
	 *		be used. Otherwise an OperationException is thrown.
	 */
	public static Map<String, List<String>> getAvailableOperationsCategorized()
	{
		Map<String, List<String>> opCategories = new HashMap<String, List<String>>();

		// Hardcoded java operations
		for(String javaCat : javaOpCategories.keySet())
		{
			// Does this category already exist?
			if(!opCategories.containsKey(javaCat))
			{
				// Not yet, create new category
				opCategories.put(javaCat, javaOpCategories.get(javaCat));
			}
			else
			{
				// Add to existing category
				opCategories.get(javaCat).addAll(javaOpCategories.get(javaCat));
			}
		}

		// XML operations
		Map<String, List<String>> xmlOpsCategories = OperationXML.getAvailableOperationsCategorized();
		for(String xmlCat : xmlOpsCategories.keySet())
		{
			// Does this category already exist?
			if(!opCategories.containsKey(xmlCat))
			{
				// Not yet, create new category
				opCategories.put(xmlCat, xmlOpsCategories.get(xmlCat));
			}
			else
			{
				// Add to existing category
				opCategories.get(xmlCat).addAll(xmlOpsCategories.get(xmlCat));
			}
		}

		// And sort the operations inside the categories
		for(String cat : opCategories.keySet())
		{
			Collections.sort(opCategories.get(cat));
		}

		return opCategories;
	}

	/**
	 * Creates a new Operation via the given name. Operations are first searched for in
	 * the currently loaded XML operations list, then in the Java-based list. An exception
	 * is thrown if an operation matching the name cannot be found and/or instantiated.
	 * @param opName Name of operation to search for, usually taken from getAvailableOperations().
	 * @return Newly created operation of the given type
	 */
	public static Operation createOperation(String opName)
	{
		// Locate the operation
		Operation op = null;
		try
		{
			// Try first in XML, then the hardcoded list
			op = OperationXML.createOperation(opName);
		}
		catch(OperationXMLException ex)
		{
			try
			{
				// Try in the list of Java classes
				Class opClass = Class.forName(Operation.javaOps.get(opName));
				op = (Operation) opClass.newInstance();
			}
			catch(IllegalAccessException ex2)
			{
				throw new OperationException("Illegal access during creation of operation '" + opName + "'", ex2);
			}
			catch(InstantiationException ex2)
			{
				throw new OperationException("Unable to create instance of operation '" + opName + "'", ex2);
			}
			catch(ClassNotFoundException ex2)
			{
				throw new OperationException("Unable to locate operation '" + opName + "' for loading", ex2);
			}
		}

		return op;
	}

	/**
	 * Sets the text name for the JLabel
	 * @param newName Text for JLabel
	 */
	protected Operation(String newName)
	{
		setOperationName(newName);

		try
		{
			data = new DataSet(this, "internal");
		}
		catch(DuplicateNameException ex)
		{
			throw new InternalMarlaException("DataSet reported it had a duplicate name when it shouldn't. Report to developers.", ex);
		}
	}

	/**
	 * Sets the name of the operation, only used internally
	 * @param newName New name for the operation
	 */
	protected final void setOperationName(String newName)
	{
		// And save the op name
		setName(newName);
	}

	/**
	 * Sets the remark for this Operation. Arbitrary, intended to be used
	 * as an analysis comment
	 * @param newRemark Remark to save for Operation
	 * @return previously set remark
	 */
	public final String setRemark(String newRemark)
	{
		String oldRemark = remark;
		remark = newRemark;

		markUnsaved();

		return oldRemark;
	}

	/**
	 * Gets the currently set remark for the Operation
	 * @return  Current remark, null if there is none
	 */
	public final String getRemark()
	{
		return remark;
	}

	/**
	 * Checks if this operation has a remark associated with it
	 * @return true if it has a remark, false otherwise
	 */
	public final boolean hasRemark()
	{
		return !remark.isEmpty();
	}

	/**
	 * Creates the appropriate derivative Operation from the given JDOM XML. Class
	 * must be specified as an attribute ("type") of the Element supplied. An exception
	 * is thrown if the operation could not be created. The inner exception has more information.
	 * @param opEl JDOM Element with the information to construct Operation
	 * @return Constructed and initialized operation
	 */
	public static Operation fromXml(Element opEl)
	{
		String opName = opEl.getAttributeValue("type");

		try
		{
			// Create the correct type of Operation
			Class opClass = Class.forName(opName);
			Operation newOp = (Operation) opClass.newInstance();
			newOp.isLoading = true;

			// Base DataSource stuff
			newOp.fromXmlBase(opEl);

			// Restore remark
			newOp.setRemark(opEl.getChildText("remark"));

			// Allow it to do its custom thing
			newOp.fromXmlExtra(opEl.getChild("extra"));

			// And restore the answers
			for(Object questionEl : opEl.getChildren("question"))
				OperationInformation.fromXml((Element) questionEl, newOp);

			newOp.isLoading = false;
			return newOp;
		}
		catch(IllegalAccessException ex)
		{
			throw new OperationException("Illegal access during creation of operation '" + opName + "'", ex);
		}
		catch(InstantiationException ex)
		{
			throw new OperationException("Unable to create instance of operation '" + opName + "'", ex);
		}
		catch(ClassNotFoundException ex)
		{
			throw new OperationException("Unable to locate operation '" + opName + "' for loading", ex);
		}
	}

	/**
	 * May be overridden by derivative classes in order to reload extra. Thrown if the
	 * save XML contains incorrect data.
	 * information saved for their type of Operation
	 * @param extraEl JDOM Element with all data for Operation
	 */
	protected void fromXmlExtra(Element extraEl)
	{
	}

	@Override
	public boolean isLoading()
	{
		if(isLoading)
			return true;
		else if(parent != null)
			return parent.isLoading();
		else
			return false;
	}

	/**
	 * Allows derivative operation to set whether the operation is in
	 * the process of loading.
	 * @param newLoadingVal True if the operation is loading, false if not
	 */
	protected final void isLoading(boolean newLoadingVal)
	{
		isLoading = newLoadingVal;
	}

	/**
	 * Assigns this Operation to a new parent.
	 * @param newParent Parent DataSet/Operation we're a part of
	 */
	public final void setParentData(DataSource newParent)
	{
		if(newParent != null)
			setParentData(newParent.getOperationCount(), newParent);
		else
			setParentData(-1, newParent);
	}
	
	/**
	 * Assigns this Operation to a new parent.
	 * @param index Index at which to insert ourselves into the parent
	 * @param newParent Parent DataSet/Operation we're a part of
	 */
	public final void setParentData(int index, DataSource newParent)
	{
		// If we're already a part of this parent or its ourselves, ignore request
		if(parent == newParent || newParent == this)
			return;

		// Tell our old parent we're removing ourselves
		if(parent != null)
		{
			// Remove ourselves from any SubProblems
			List<SubProblem> oldList = getSubProblems();
			List<SubProblem> currSubs = new ArrayList<SubProblem>(oldList.size());
			for(SubProblem sub : getSubProblems())
				currSubs.add(sub);
			for(SubProblem sub : currSubs)
				sub.removeStep(this);
			
			DataSource oldParent = parent;
			parent = null;
			oldParent.removeOperation(this);
		}

		// Assign ourselves to the new guy
		parent = newParent;
		if(parent != null)
			parent.addOperation(index, this);

		// Fill our questions from the new parent if possible
		for(OperationInformation q : getRequiredInfoPrompt())
			q.autoAnswer();

		markDirty();
		markUnsaved();
	}

	@Override
	public final DataSource getParentData()
	{
		return parent;
	}

	@Override
	public final Problem getParentProblem()
	{
		if(parent != null)
			return parent.getParentProblem();
		else
			return null;
	}

	@Override
	public final DataSource getRootDataSource()
	{
		if(parent != null)
			return parent.getRootDataSource();
		else
			return this;
	}

	/**
	 * Returns a human-readable description of the operation. Intended for
	 * helping the user determine what an operation does
	 * @return String to display to user
	 */
	public abstract String getDescription();

	/**
	 * Runs up the chain and finds what index from the root DataSet this
	 * operation falls under
	 * @return Index of the "branch" off point
	 */
	public final int getIndexFromDataSet()
	{
		DataSource currOp = this;
		Operation prevOp = null;
		while(!(currOp instanceof DataSet))
		{
			prevOp = (Operation)currOp;
			currOp = prevOp.getParentData();
		}

		return currOp.getOperationIndex(prevOp);
	}

	@Override
	public final boolean isUniqueColumnName(String name)
	{
		checkCache();

		// Unique to us?
		if(!data.isUniqueColumnName(name))
			return false;
		else if(parent != null)
			return parent.isUniqueColumnName(name);
		else
			return true;
	}

	@Override
	public final int getColumnIndex(String colName)
	{
		checkCache();

		// Is it within our parent?
		int parentIndex = -1;
		if(parent != null)
			parentIndex = data.getColumnIndex(colName);

		if(parentIndex != -1)
			return parentIndex;
		else
			return data.getColumnIndex(colName);
	}

	/**
	 * Returns the columns that this operation added to the dataset. If
	 * the operation modified a column that is NOT included here.
	 * @return List of added columns
	 */
	public final List<DataColumn> getNewColumns()
	{
		checkCache();

		List<DataColumn> ourCols = new ArrayList<DataColumn>();
		for(int i = 0; i < data.getColumnCount(); i++)
			ourCols.add(data.getColumn(i));
		
		return ourCols;
	}

	@Override
	public final DataColumn getColumn(String colName)
	{
		checkCache();

		DataColumn dc = null;

		if(parent != null)
		{
			try
			{
				dc = parent.getColumn(colName);
			}
			catch(DataNotFoundException ex)
			{
				// We'll look in ourselves
			}
		}
		
		if(dc == null)
			return data.getColumn(colName);
		else
			return dc;
	}

	@Override
	public List<DataColumn> getColumns()
	{
		List<DataColumn> cols = new ArrayList<DataColumn>();
		cols.addAll(data.getColumns());
		
		// Get parent columns
		if(parent != null)
			cols.addAll(parent.getColumns());
			
		return Collections.unmodifiableList(cols);
	}

	@Override
	public final DataColumn getColumn(int index)
	{
		checkCache();

		// Is it within us or our parent?
		if(index < startIndex)
			return parent.getColumn(index);
		else
			return data.getColumn(index - startIndex);
	}

	@Override
	public final int getColumnLength()
	{
		checkCache();

		int pLen = 0;
		if(parent != null)
			pLen = parent.getColumnLength();

		int ourLen = data.getColumnLength();

		if(pLen > ourLen)
			return pLen;
		else
			return ourLen;
	}

	/**
	 * Performs the same function as getColumnLength() but only for "new"
	 * columns that this operation added
	 * @return Maximum length of the newly added columns. -1 if there are no
	 * new columns
	 */
	public final int getNewColumnLength()
	{
		checkCache();

		int max = -1;
		List<DataColumn> newCols = getNewColumns();
		for(DataColumn dc : newCols)
		{
			if(max < dc.size())
				max = dc.size();
		}

		return max;
	}

	@Override
	public final String[] getColumnNames()
	{
		checkCache();

		String[] parentNames = new String[0];
		if(parent != null)
			parentNames = parent.getColumnNames();

		String[] ourNames = data.getColumnNames();

		String[] allNames = new String[parentNames.length + ourNames.length];
		System.arraycopy(parentNames, 0, allNames, 0, parentNames.length);
		System.arraycopy(ourNames, 0, allNames, parentNames.length, ourNames.length);

		return allNames;
	}

	/**
	 * Adds a new column to the result of the Operation
	 * @param colName Name of the new column to add
	 * @return Newly created DataColumn
	 */
	protected final DataColumn addColumn(String colName)
	{
		return data.addColumn(colName);
	}

	/**
	 * Copies a parent column into this operation's result
	 * @param colName Name of the column to copy
	 * @return Newly created DataColumn copy
	 */
	protected final DataColumn copyColumn(String colName)
	{
		return copyColumn(parent.getColumn(colName));
	}

	/**
	 * Copies a parent column into this operation's result
	 * @param col DataColumn to copy into this operation
	 * @return Newly created DataColumn copy
	 */
	protected final DataColumn copyColumn(DataColumn col)
	{
		return data.copyColumn(col);
	}

	/**
	 * Duplicates an operation. Derivative classes should override this
	 * if additional information needs to be copied.
	 * @return Newly created duplicate Operation
	 */
	@Override
	public Operation clone()
	{
		try
		{
			// Create an operation with the same type
			Operation newOp = Operation.createOperation(getName());
			newOp.isLoading = true;

			// Copy remark
			newOp.setRemark(remark);

			// Copy our child operations
			//for(Operation op : solutionOps)
			//	newOp.addOperation(op.clone());

			// Copy configuration questions
			List<OperationInformation> myConf = getRequiredInfoPrompt();
			List<OperationInformation> newConf = newOp.getRequiredInfoPrompt();
			for(int i = 0; i < myConf.size(); i++)
			{
				OperationInformation myConfCurr = myConf.get(i);
				OperationInformation newConfCurr = newConf.get(i);

				if(myConfCurr.isAnswered())
					newConfCurr.setAnswer(myConfCurr.getAnswer());
			}

			newOp.isLoading = false;
			return newOp;
		}
		catch(MarlaException ex)
		{
			throw new InternalMarlaException("Unable to clone Operation. See internal exception.", ex);
		}
	}

	/**
	 * Refreshes the cache if needed
	 */
	public final void checkCache()
	{
		if(isCacheDirty && !inRecompute)
			refreshCache();
	}

	/**
	 * Retrieves the computation status of the Operation.
	 * @return true if the Operation needs to recompute values before it can
	 *		return anything, false otherwise
	 */
	public final boolean isDirty()
	{
		return isCacheDirty;
	}

	/**
	 * Recalculates columns and saves the R operations needed for computation
	 */
	private synchronized void refreshCache()
	{
		if(parent == null)
			throw new OperationException("No parent for operation to get data from");

		// Don't if everything is still loading
		if(parent.isLoading())
			return;

		// Our parent must be computed
		if(parent instanceof Operation)
			((Operation)parent).checkCache();

		// We must be fully answered
		if(isInfoUnanswered())
			throw new OperationInfoRequiredException("More information required for computation", this);

		try
		{
			// Get data from parent to allow us to pull stuff down quickly
			data.clearColumns();
			startIndex = parent.getColumnCount();

			// Compute new columns and save the way we do so (R commands) for use by toString()
			RProcessor proc = RProcessor.getInstance();
			proc.setRecorderMode(RProcessor.RecordMode.CMDS_ONLY);
			inRecompute = true;
			computeColumns(proc);
			operationRecord = proc.fetchInteraction();
			proc.setRecorderMode(RProcessor.RecordMode.DISABLED);

			// Children are dirty. Dirty, dirty children
			markDirty();

			// But we're clean!
			isCacheDirty = false;
		}
		finally
		{
			// Well we're certainly not recomputing any more
			inRecompute = false;
		}
	}

	/**
	 * Overridden by child operations to actually perform the task. When the
	 * column/other data is requested the deriving class should place the
	 * result of the appropriate operation on the dataset above in the data
	 * DataSet.
	 *
	 * Caching is performed by Operation. Concrete Operation derivatives
	 * should not implement their own caching unless a specific need
	 * arises.
	 * @param proc RProcessor to use for computations
	 */
	protected abstract void computeColumns(RProcessor proc);

	/**
	 * Removes all questions currently attached to this operation
	 */
	protected final void clearQuestions()
	{
		questions.clear();
		markUnsaved();
	}

	/**
	 * Adds the given information to the information this operation
	 * expects. Intended to be called by derivative classes
	 * @param info Question/information required from the user
	 */
	protected final void addQuestion(OperationInformation info)
	{
		// Ensure it's valid
		if(info.getOperation() != this)
			throw new InternalMarlaException("Attempt to add information that did not point to same operation");

		questions.put(info.getName(), info);
		markUnsaved();
	}

	/**
	 * Returns the information with the given name
	 * @param name
	 */
	public final OperationInformation getQuestion(String name)
	{
		return questions.get(name);
	}

	/**
	 * Returns true if the Operation has questions/prompts for the user,
	 * regardless of whether they are answered or not. getRequiredInfoPrompt()
	 * returns the actual list of data needed
	 * @return true if additional information is required
	 */
	public final boolean isInfoRequired()
	{
		return !questions.isEmpty();
	}

	/**
	 * Returns true if the Operation has questions/prompts for the user that are
	 * unanswered. getRequiredInfoPrompt() returns the actual list of data needed
	 * @return true if additional information is required
	 */
	public final boolean isInfoUnanswered()
	{
		if(questions.isEmpty())
			return false;

		// Check if any are unanswered
		Set<String> names = questions.keySet();
		for(String key : names)
		{
			if(!questions.get(key).isAnswered())
				return true;
		}

		// No problems were found, we're all good to go
		return false;
	}

	/**
	 * Deriving classes should override this to prompt the user for the
	 * information they need.
	 * @return Object[] ArrayList of questions to ask the user. The Object[] array
	 *			is two dimensional, the first element is a verbatim string to
	 *			ask the user and the second is a constant on the question type.
	 *			If the question type requires for information (for example, a
	 *			combo selection box), then the third element in Object[] will
	 *			be whatever is needed.
	 */
	public final List<OperationInformation> getRequiredInfoPrompt()
	{
		// Converts the hashmap to an immutable list
		List<OperationInformation> info = new ArrayList<OperationInformation>(questions.size());
		Set<String> names = questions.keySet();
		for(String key : names)
		{
			info.add(questions.get(key));
		}

		return Collections.unmodifiableList(info);
	}

	/**
	 * Clears any set answers to required information
	 */
	public final void clearRequiredInfo()
	{
		// Clear all the question answers
		Set<String> names = questions.keySet();
		for(String key : names)
		{
			questions.get(key).clearAnswer();
		}
	}

	@Override
	public final List<Operation> getAllLeafOperations()
	{
		List<Operation> leaves = super.getAllLeafOperations();

		// If we have no children, then we're a leaf
		if(leaves.isEmpty())
			leaves.add(this);
		
		return leaves;
	}

	/**
	 * Returns true if this operation has graphical output. The path to the graphic
	 * file can be obtained via getPlot(). An exception is thrown if an error occurs
	 * determining answer.
	 * @return true if there is available graphical output via getPlot(), false otherwise
	 */
	public abstract boolean hasPlot();

	/**
	 * Returns the path to the graphical plot this operation generated. An exception
	 * is thrown if an error occurs creating the plot.
	 * @return Path to plot, null if there is none associated with this operation.
	 */
	public String getPlot()
	{
		// Check derivative implementation
		if(hasPlot())
			throw new InternalMarlaException("Operation indictates it has a plot but has not overriden getPlot()");

		return null;
	}

	/**
	 * An Operation is equal all the operations tied to it are the same
	 * @param other Object to compare against
	 * @return True if the the given object is the same as this one
	 */
	@Override
	public boolean equals(Object other)
	{		
		// Ourselves?
		if(other == this)
			return true;

		// Do DataSource checks
		if(!super.equals(other))
			return false;

		// Actually an operation?
		if(!(other instanceof Operation))
			return false;

		Operation otherOp = (Operation) other;

		// Different derivative operation types?
		if(this.getClass() != otherOp.getClass())
			return false;

		// Two operations of the same type and same parent must be _different_
		// This allows multiple operations of the same type to fall under the
		// the same parent and avoids detchment problems
		if(parent == otherOp.parent || (parent == null && otherOp.parent != null) || (parent != null && otherOp.parent == null))
			return false;

		// Remarks different
		if(remark != null && !remark.equals(otherOp.remark))
			return false;

		// Questions and answers different?
		if(!questions.equals(otherOp.questions))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		hash = 31 * hash + (this.questions != null ? this.questions.hashCode() : 0);
		return hash;
	}

	/**
	 * Produces the XML Element for this operation. Derivative classes
	 * should override toXmlExtra(Element) if they want to save additional
	 * information.
	 * XML elements to the returned Element.
	 * @return XML Element with all settings for this Operation
	 */
	@Override
	public final Element toXml()
	{
		Element opEl = new Element("operation");
		opEl.setAttribute("type", this.getClass().getName());

		// Remark
		Element remarkEl = new Element("remark");
		remarkEl.addContent(remark);
		opEl.addContent(remarkEl);

		// Add all the DataSource stuff
		super.toXml(opEl);

		// Add question answers
		Set<String> keys = questions.keySet();
		for(String key : keys)
			opEl.addContent(questions.get(key).toXml());

		// Extra info?
		Element extraEl = new Element("extra");
		opEl.addContent(extraEl);
		toXmlExtra(extraEl);

		return opEl;
	}

	/**
	 * May be overridden by derivative classes to save additional information
	 * need by their operation type
	 * @param extraEl Element which toXmlExtra should attach its information to
	 * @return null if no extra information, JDOM Element otherwise
	 */
	protected Element toXmlExtra(Element extraEl)
	{
		return null;
	}

	/**
	 * Marks the Operation as having had something change about it and it
	 * needing to recompute its values.
	 */
	@Override
	public final void markDirty()
	{
		// Mark as dirty but don't actually recompute yet
		isCacheDirty = true;
		super.markDirty();
	}

	/**
	 * Notes that something changed about the problem, tell parent we're not
	 * saved any more
	 */
	@Override
	public void markUnsaved()
	{
		if(parent != null)
			parent.markUnsaved();
	}

	@Override
	public String getRCommands()
	{
		return getRCommands(true);
	}

	@Override
	public final String getRCommands(boolean chain)
	{
		checkCache();

		StringBuilder sb = new StringBuilder();

		// Get the operations needed for the parent, if desired
		if(chain && parent != null)
			sb.append(parent.getRCommands(chain));

		// Ourselves
		sb.append(operationRecord);

		return operationRecord.toString();
	}

	@Override
	public final String toHTML()
	{
		// We only output ourselves as a DataSet if we don't have a plot
		if(hasPlot())
			throw new InternalMarlaException("This operation generates a plot, it must be displayed with getPlot()");

		checkCache();

		// Just display the results as a normal DataSet
		return DataSet.toHTML(this);
	}

	/**
	 * Returns the calculated result of this operation.
	 * @return String of the R commands used to do computations
	 */
	@Override
	public final String toString()
	{
		try
		{
			// We only output ourselves as a DataSet if we don't have a plot
			if(hasPlot())
				throw new InternalMarlaException("This operation generates a plot, it must be displayed with getPlot()");

			checkCache();

			// Just display the results as a normal DataSet
			return DataSet.toString(this);
		}
		catch(MarlaException ex)
		{
			throw new InternalMarlaException("Unable to do toString() because the values could not be computed.", ex);
		}
	}

	@Override
	public final void exportFile(String filePath)
	{
		checkCache();
		data.exportFile(filePath);
	}

	@Override
	public final String toRFrame()
	{
		checkCache();
		return DataSet.toRFrame(this);
	}

	@Override
	public final int getColumnCount()
	{
		checkCache();
		
		int total = 0;
		if(parent != null)
			total = parent.getColumnCount();

		return total + data.getColumnCount();
	}
}
