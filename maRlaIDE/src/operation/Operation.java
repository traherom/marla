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
package operation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JLabel;
import org.jdom.Element;
import problem.Changeable;
import problem.DataColumn;
import problem.DataNotFoundException;
import problem.DataSet;
import problem.DataSource;
import problem.DuplicateNameException;
import problem.InternalMarlaException;
import problem.MarlaException;
import r.RProcessor;
import r.RProcessorException;
import resource.ConfigurationException;

/**
 * Operation to perform on a parent object that implements
 * the DataAccess interface. The root parent will be a DataSet,
 * which then gets refined down by a chain of Operations.
 *
 * @author Ryan Morehart
 */
public abstract class Operation extends JLabel implements DataSource, Changeable
{
	/**
	 * Denotes when this operation is in the middle of loading from XML,
	 * blocks calculations and such from altering the structure until load is
	 * complete.
	 */
	private boolean isLoading = false;
	/**
	 * "Unique" internal ID for this operation
	 */
	private Integer internalID = null;
	/**
	 * Operation name.
	 */
	private String name;
	/**
	 * Actual values from computation
	 */
	private final DataSet data;
	/**
	 * Commands to perform on this dataset
	 */
	private final List<Operation> solutionOps = new ArrayList<Operation>();
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
	public static List<String> getAvailableOperationsList() throws OperationException, ConfigurationException
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
	public static Map<String, List<String>> getAvailableOperationsCategorized() throws OperationException, ConfigurationException
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

		// And sort 'em
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
	public static Operation createOperation(String opName) throws MarlaException
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
		name = newName;
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
	public static Operation fromXml(Element opEl) throws MarlaException
	{
		String opName = opEl.getAttributeValue("type");

		try
		{
			// Create the correct type of Operation
			Class opClass = Class.forName(opName);
			Operation newOp = (Operation) opClass.newInstance();
			newOp.isLoading = true;

			String id = opEl.getAttributeValue("id");
			if(id != null)
				newOp.internalID = Integer.valueOf(id);

			int x = Integer.parseInt(opEl.getAttributeValue("x"));
			int y = Integer.parseInt(opEl.getAttributeValue("y"));
			int height = Integer.parseInt(opEl.getAttributeValue("height"));
			int width = Integer.parseInt(opEl.getAttributeValue("width"));
			newOp.setBounds(x, y, width, height);

			// Restore remark
			newOp.setRemark(opEl.getChildText("remark"));

			// Allow it to do its custom thing
			newOp.fromXmlExtra(opEl);

			// And restore the answers
			for(Object questionEl : opEl.getChildren("question"))
			{
				OperationInformation.fromXml((Element) questionEl, newOp);
			}

			// Operations that chain off of here
			for(Object opChildEl : opEl.getChildren("operation"))
			{
				Operation newChildOp = Operation.fromXml((Element) opChildEl);
				newOp.addOperation(newChildOp);
				newChildOp.markDirty();
				newChildOp.checkDisplayName();
			}

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
	 * @param opEl JDOM Element with all data for Operation
	 */
	protected void fromXmlExtra(Element opEl) throws MarlaException
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
	 * Assigns this Operation to a new parent.
	 * @param newParent Parent DataSet/Operation we're a part of
	 */
	public final void setParentData(DataSource newParent) throws MarlaException
	{
		// If we're already a part of this parent, ignore request
		if(parent == newParent)
			return;

		// Tell our old parent we're removing ourselves
		if(parent != null)
		{
			DataSource oldParent = parent;
			parent = null;
			oldParent.removeOperation(this);
		}

		// Assign ourselves to the new guy
		parent = newParent;
		if(parent != null)
			parent.addOperation(this);

		markDirty();
		markUnsaved();
	}

	/**
	 * Returns the parent this Operation derives from
	 * @return Next higher set of data or null if there is none.
	 */
	public final DataSource getParentData()
	{
		return parent;
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
	public final int getOperationIndex(Operation op)
	{
		return solutionOps.indexOf(op);
	}

	@Override
	public final boolean isUniqueColumnName(String name) throws MarlaException
	{
		checkCache();
		return data.isUniqueColumnName(name);
	}

	@Override
	public final int getColumnIndex(String colName) throws MarlaException
	{
		checkCache();
		return data.getColumnIndex(colName);
	}

	@Override
	public final DataColumn getColumn(String colName) throws MarlaException
	{
		checkCache();
		return data.getColumn(colName);
	}

	@Override
	public final DataColumn getColumn(int index) throws MarlaException
	{
		checkCache();
		return data.getColumn(index);
	}

	@Override
	public final int getColumnLength() throws MarlaException
	{
		checkCache();
		return data.getColumnLength();
	}

	@Override
	public final String[] getColumnNames() throws MarlaException
	{
		checkCache();
		return data.getColumnNames();
	}

	@Override
	public final int getOperationCount()
	{
		return solutionOps.size();
	}

	/**
	 * Adds a new column to the result of the Operation
	 * @param colName Name of the new column to add
	 * @return Newly created DataColumn
	 */
	protected final DataColumn addColumn(String colName) throws DuplicateNameException
	{
		return data.addColumn(colName);
	}

	/**
	 * Copies a parent column into this operation's result
	 * @param colName Name of the column to copy
	 * @return Newly created DataColumn copy
	 */
	protected final DataColumn copyColumn(String colName) throws DuplicateNameException, DataNotFoundException, MarlaException
	{
		return copyColumn(parent.getColumn(colName));
	}

	/**
	 * Copies a parent column into this operation's result
	 * @param col DataColumn to copy into this operation
	 * @return Newly created DataColumn copy
	 */
	protected final DataColumn copyColumn(DataColumn col) throws DuplicateNameException, DataNotFoundException, MarlaException
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
			Operation newOp = Operation.createOperation(name);

			// Copy remark
			newOp.setRemark(remark);

			// Copy our child operations
			for(Operation op : solutionOps)
			{
				newOp.addOperation(op.clone());
			}

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
	public final void checkCache() throws MarlaException
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
	private synchronized void refreshCache() throws OperationException, RProcessorException, MarlaException
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
			// Compute new columns and save the way we do so (R commands) for use by toString()
			RProcessor proc = RProcessor.getInstance();
			proc.setRecorderMode(RProcessor.RecordMode.CMDS_ONLY);
			data.clearColumns();
			inRecompute = true;
			computeColumns(proc);
			operationRecord = proc.fetchInteraction();
			proc.setRecorderMode(RProcessor.RecordMode.DISABLED);

			// We're clean!
			isCacheDirty = false;

			// Children aren't though. Dirty, dirty children
			for(Operation op : solutionOps)
				op.markDirty();
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
	protected abstract void computeColumns(RProcessor proc) throws MarlaException;


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
	public final boolean isInfoRequired() throws MarlaException
	{
		return !questions.isEmpty();
	}

	/**
	 * Returns true if the Operation has questions/prompts for the user that are
	 * unanswered. getRequiredInfoPrompt() returns the actual list of data needed
	 * @return true if additional information is required
	 */
	public final boolean isInfoUnanswered() throws MarlaException
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
	public final List<OperationInformation> getRequiredInfoPrompt() throws MarlaException
	{
		// Converts the hashmap to an immutable list
		List<OperationInformation> info = new ArrayList<OperationInformation>(questions.size());
		Set<String> names = questions.keySet();
		for(String key : names)
		{
			info.add(questions.get(key));
		}

		return info;
	}

	/**
	 * Clears any set answers to required information
	 */
	public final void clearRequiredInfo() throws OperationInfoRequiredException
	{
		// Clear all the question answers
		Set<String> names = questions.keySet();
		for(String key : names)
		{
			questions.get(key).clearAnswer();
		}
	}

	/**
	 * Returns true if this operation has graphical output. The path to the graphic
	 * file can be obtained via getPlot(). An exception is thrown if an error occurs
	 * determining answer.
	 * @return true if there is available graphical output via getPlot(), false otherwise
	 */
	public abstract boolean hasPlot() throws MarlaException;

	/**
	 * Returns the path to the graphical plot this operation generated. An exception
	 * is thrown if an error occurs creating the plot.
	 * @return Path to plot, null if there is none associated with this operation.
	 */
	public String getPlot() throws MarlaException
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

		// Actually an operation?
		if(!(other instanceof Operation))
			return false;

		Operation otherOp = (Operation) other;

		// Different derivative operation types?
		if(this.getClass() != otherOp.getClass())
			return false;

		// Two operations of the same type and same parent must be
		// _different_. This allows multiple operations of the same
		// type to fall under the  the same parent
		if(parent == otherOp.parent)
			return false;

		// Remarks different
		if(remark != null && !remark.equals(otherOp.remark))
			return false;

		// Questions and answers different?
		if(!questions.equals(otherOp.questions))
			return false;

		// Well, are our children all the same then?
		if(!solutionOps.equals(otherOp.solutionOps))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 31 * hash + (this.solutionOps != null ? this.solutionOps.hashCode() : 0);
		hash = 31 * hash + (this.questions != null ? this.questions.hashCode() : 0);
		return hash;
	}

	@Override
	public Integer getID()
	{
		return internalID;
	}

	@Override
	public Integer generateID()
	{
		// Only generate if it's not already done
		if(internalID == null)
		{
			internalID = hashCode() + new Random().nextInt();
		}

		return internalID;
	}

	/**
	 * Produces the XML Element for this operation. Derivative classes
	 * should override toXmlExtra(Element) if they want to save additional
	 * information.
	 * XML elements to the returned Element.
	 * @return XML Element with all settings for this Operation
	 */
	@Override
	public final Element toXml() throws MarlaException
	{
		Element opEl = new Element("operation");
		opEl.setAttribute("type", this.getClass().getName());

		if(internalID != null)
			opEl.setAttribute("id", internalID.toString());

		Rectangle rect = getBounds();
		opEl.setAttribute("x", Integer.toString((int) rect.getX()));
		opEl.setAttribute("y", Integer.toString((int) rect.getY()));
		opEl.setAttribute("height", Integer.toString((int) rect.getHeight()));
		opEl.setAttribute("width", Integer.toString((int) rect.getWidth()));

		// Remark
		Element remarkEl = new Element("remark");
		remarkEl.addContent(remark);
		opEl.addContent(remarkEl);

		// Add Ops
		for(Operation op : solutionOps)
		{
			opEl.addContent(op.toXml());
		}

		// Add question answers
		Set<String> keys = questions.keySet();
		for(String key : keys)
		{
			opEl.addContent(questions.get(key).toXml());
		}

		// Extra info?
		Element extraEl = toXmlExtra();
		if(extraEl != null)
			opEl.addContent(extraEl);

		return opEl;
	}

	/**
	 * May be overridden by derivative classes to save additional information
	 * need by their operation type
	 * @return null if no extra information, JDOM Element otherwise
	 */
	protected Element toXmlExtra() throws MarlaException
	{
		return null;
	}

	/**
	 * Marks the Operation as having had something change about it and it
	 * needing to recompute its values.
	 */
	public void markDirty()
	{
		// Mark as dirty but don't actually recompute yet
		isCacheDirty = true;

		// Tell all children they need to recompute
		for(Operation op : solutionOps)
			op.markDirty();
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
	public String getRCommands() throws MarlaException
	{
		return getRCommands(true);
	}

	@Override
	public final String getRCommands(boolean chain) throws MarlaException
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
	public final String toHTML() throws MarlaException
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
	public final void exportFile(String filePath) throws MarlaException
	{
		checkCache();
		data.exportFile(filePath);
	}

	@Override
	public final String toRFrame() throws MarlaException
	{
		checkCache();
		return data.toRFrame();
	}

	@Override
	public final String getName()
	{
		return name;
	}

	@Override
	public final int getColumnCount() throws MarlaException
	{
		checkCache();
		return data.getColumnCount();
	}

	@Override
	public final Operation addOperation(Operation op) throws MarlaException
	{
		// Tell the operation to set us as the parent
		op.setParentData(this);

		if(!solutionOps.contains(op))
		{
			// They weren't already assigned to us, so stick them on our list
			solutionOps.add(op);
			markUnsaved();
		}

		return op;
	}

	@Override
	public final Operation removeOperation(Operation op) throws MarlaException
	{
		// Tell operation to we're not its parent any more
		op.setParentData(null);

		// Remove them from our list if still needed
		if(solutionOps.remove(op))
		{
			markUnsaved();
		}

		return op;
	}

	@Override
	public final Operation removeOperation(int index) throws MarlaException
	{
		return removeOperation(solutionOps.get(index));
	}

	@Override
	public final Operation getOperation(int index)
	{
		return solutionOps.get(index);
	}

	@Override
	public List<Operation> getAllChildOperations()
	{
		List<Operation> myOps = new ArrayList<Operation>();

		// Copy my operations over, plus ask each child to get their own
		// children. Append whatever they return
		for(Operation op : solutionOps)
		{
			myOps.add(op);
			myOps.addAll(op.getAllChildOperations());
		}

		return myOps;
	}

	@Override
	public List<Operation> getAllLeafOperations()
	{
		List<Operation> myLeaves = new ArrayList<Operation>();

		// If I have children, then copy their leaves
		// Otherwise _I_ am a leaf, so I should return myself
		if(!solutionOps.isEmpty())
		{
			for(Operation op : solutionOps)
				myLeaves.addAll(op.getAllLeafOperations());
		}
		else
			myLeaves.add(this);

		return myLeaves;
	}
}