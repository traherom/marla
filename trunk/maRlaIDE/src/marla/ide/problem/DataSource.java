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
package marla.ide.problem;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import marla.ide.operation.Operation;
import java.util.List;
import java.util.Random;
import javax.swing.JLabel;
import org.jdom.Element;

/**
 * Tie for any source of that contains columns
 * @author Ryan Morehart
 */
public abstract class DataSource extends JLabel implements Loadable
{
	/**
	 * DataSource name.
	 */
	private String name = null;
	/**
	 * "Unique" internal ID for this operation
	 */
	private Integer internalID = null;
	/**
	 * Commands to perform on this DataSource
	 */
	private final List<Operation> solutionOps = new ArrayList<Operation>();
	/**
	 * SubProblems this DataSet is a part of
	 */
	private final List<SubProblem> subProblems = new ArrayList<SubProblem>();
	
	/**
	 * Empty constructor for normal initialization
	 */
	public DataSource()
	{
		// Blank
	}

	/**
	 * Helper for copy constructors in children
	 * @param copy DataSource to copy information from
	 */
	protected DataSource(DataSource copy)
	{
		name = copy.name;

		for(Operation op : copy.solutionOps)
			solutionOps.add(op.clone());
	}

	/**
	 * Creates an ID for this DataSource and saves it. If the DataSource
	 * already has an ID then the current one is used.
	 * @return ID assigned to DataSource
	 */
	public final Integer getUniqueID()
	{
		// Only generate if it's not already done
		if(internalID == null)
		{
			internalID = hashCode() + new Random().nextInt();
		}

		return internalID;
	}

	/**
	 * Allows an ID to be restored from a save file
	 * @param newID New ID to use for DataSource
	 */
	protected final void setUniqueID(Integer newID)
	{
		internalID = newID;
	}

	/**
	 * Gets the current DataSource name
	 * @return DataSource name
	 */
	@Override
	public final String getName()
	{
		return name;
	}

	/**
	 * Sets the name for the DataSource
	 * @param newName New name to set for the DataSource
	 */
	@Override
	public final void setName(String newName)
	{
		name = newName;
		checkDisplayName();
		markUnsaved();
	}

	/**
	 * Returns a string that could be displayed to the user
	 * @param abbrv If true, abreviate the returned string in order 
	 * @return String suitable for display to user
	 */
	public abstract String getDisplayString(boolean abbrv);

	/**
	 * Returns the column with the given name in the DataSource
	 * @param colName List of values in that column. Column manipulations will
	 *					be reflected in the dataset itself unless a copy is made.
	 * @return The DataColumn requested
	 */
	public abstract DataColumn getColumn(String colName) throws DataNotFoundException, MarlaException;

	/**
	 * Returns a list of all the columns in the DataSource
	 * @return The DataColumns in the current DataSource
	 */
	public abstract List<DataColumn> getColumns() throws MarlaException;

	/**
	 * Returns the column index (as would be passed to getColumn(int))
	 * of the column with the given name
	 * @param colName Column name to search for
	 * @return index of corresponding column, -1 if not found
	 */
	public abstract int getColumnIndex(String colName) throws MarlaException;

	/**
	 * Returns the column requested by index
	 * @param index Index of the column to access
	 * @return DataColumn at the given index
	 */
	public abstract DataColumn getColumn(int index) throws DataNotFoundException, MarlaException;

	/**
	 * Returns the number of columns in this DataSet
	 * @return Number of columns in DataSet
	 */
	public abstract int getColumnCount() throws MarlaException;

	/**
	 * Returns the length of the <em>longest</em> column in this dataset
	 * @return Length of the longest column in this dataset. -1 if there are none
	 */
	public abstract int getColumnLength() throws MarlaException;

	/**
	 * Returns a list of column names.
	 * @return All column names in this dataset
	 */
	public abstract String[] getColumnNames() throws MarlaException;

	/**
	 * Add given operation to the end of DataSource. See addOperation(index, op)
	 * @param op Operation to add to perform on DataSet
	 * @return Newly added operation
	 */
	public final Operation addOperation(Operation op) throws MarlaException
	{
		return addOperation(solutionOps.size(), op);
	}

	/**
	 * Add an operation to this data object. If you want to chain operations
	 * together, then you must append the operation to another operation.
	 * Multiple operations added to a single dataset are independent and
	 * the results have no effect on one another.
	 * @param index Insert at the given index
	 * @param op Operation to add to perform on DataSet
	 * @return Newly added operation
	 */
	public final Operation addOperation(int index, Operation op) throws MarlaException
	{
		// Tell the operation to set us as the parent
		op.setParentData(this);

		if(!solutionOps.contains(op))
		{
			// They weren't already assigned to us, so stick them on our list
			solutionOps.add(index, op);

			// Add all our current SubProblems to the new child
			for(SubProblem sub : getSubProblems())
				op.addSubProblem(sub);

			markUnsaved();
		}

		return op;
	}

	/**
	 * Removes an operation from the data
	 * @param op Operation to remove from data
	 * @return The removed Operation
	 */
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

	/**
	 * Removes an operation from the data
	 * @param index Index of the operation to remove
	 * @return The removed Operation
	 */
	public final Operation removeOperation(int index) throws MarlaException
	{
		return removeOperation(solutionOps.get(index));
	}

	/**
	 * Get the Operation at the specified index
	 * @param index Index of Operation to retrieve
	 * @return Operation at index
	 */
	public final Operation getOperation(int index)
	{
		return solutionOps.get(index);
	}

	/**
	 * Finds the index of the specified operation within the DataSource
	 * @param op Operation to locate within the DataSource
	 * @return index of the operation or -1 if not found
	 */
	public final int getOperationIndex(Operation op)
	{
		return solutionOps.indexOf(op);
	}

	/**
	 * Returns the number of top-level operations working on this
	 * DataSet
	 * @return Number of Operations in DataSet
	 */
	public final int getOperationCount()
	{
		return solutionOps.size();
	}

	/**
	 * Returns a flat list of every operation that is a child of this one,
	 * directly or indirectly. Easy way to get access to an entire subtree
	 * @return List of every operation below this one in the tree
	 */
	public final List<Operation> getAllChildOperations()
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

	/**
	 * Returns a flat list of every operation that is a child of this
	 * one--directly or indirectly--and has no child operations of its own.
	 * @return List of every operation below this one in the tree
	 */
	public List<Operation> getAllLeafOperations()
	{
		List<Operation> myLeaves = new ArrayList<Operation>();

		// If I have children, then copy their leaves
		for(Operation op : solutionOps)
			myLeaves.addAll(op.getAllLeafOperations());

		return myLeaves;
	}

	/**
	 * Returns the DataSource that is at the top of the chain
	 * @return DataSource without a parent (top of the chain)
	 */
	public abstract DataSource getRootDataSource();

	/**
	 * Outputs this DataSource as the string of R commands needed to perform
	 * the calculations for itself. If chain is true then R commands from higher
	 * up the chain are also included in the string
	 * @throws chain True if R commands from higher up the chain should be included
	 * @return String of R commands
	 */
	public abstract String getRCommands(boolean chain) throws MarlaException;

	/**
	 * Outputs this DataSource as the string of R commands needed to perform
	 * the calculations for itself.
	 * @return String of R commands
	 */
	public abstract String getRCommands() throws MarlaException;

	/**
	 * Outputs this DataSet as a constructed R data frame and returns the
	 * variable the data frame is stored in.
	 * @return R variable the data frame is in
	 */
	public abstract String toRFrame() throws MarlaException;

	/**
	 * Outputs this DataSource as an HTML table with the contained data
	 * @return String of the HTML table representing this DataSource
	 */
	public abstract String toHTML() throws MarlaException;

	/**
	 * Returns a JDOM Element that encapsulates this DataSet's
	 * name, columns, and child operations
	 * @return JDOM Element of this DataSet
	 */
	public abstract Element toXml() throws MarlaException;

	/**
	 * Takes the DataSource-specific information and bundles it into
	 * an the given XML element
	 * @return XML element in which the data was placed
	 */
	protected Element toXml(Element dataEl) throws MarlaException
	{
		dataEl.setAttribute("name", getName());
		dataEl.setAttribute("id", getUniqueID().toString());

		Rectangle rect = getBounds();
		dataEl.setAttribute("x", Integer.toString((int) rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int) rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int) rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int) rect.getWidth()));

		// Add Ops
		for(Operation op : solutionOps)
			dataEl.addContent(op.toXml());

		return dataEl;
	}

	/**
	 * Reads in DataSource-specific information from the given XML element
	 * @param dsEl XML Element containing DataSource information
	 * @return Reference to DataSource object
	 */
	protected final DataSource fromXmlBase(Element dsEl) throws MarlaException
	{
		setName(dsEl.getAttributeValue("name"));

		String id = dsEl.getAttributeValue("id");
		if(id != null)
			setUniqueID(Integer.valueOf(id));

		int x = Integer.parseInt(dsEl.getAttributeValue("x"));
		int y = Integer.parseInt(dsEl.getAttributeValue("y"));
		int height = Integer.parseInt(dsEl.getAttributeValue("height"));
		int width = Integer.parseInt(dsEl.getAttributeValue("width"));
		setBounds(x, y, width, height);


		for(Object opEl : dsEl.getChildren("operation"))
		{
			Operation newOp = Operation.fromXml((Element) opEl);
			addOperation(newOp);
			newOp.markDirty();
			newOp.checkDisplayName();
		}
		
		return this;
	}
	
	/**
	 * Exports this DataSource to a CSV file at the given path. Use R to perform the export.
	 * @param filePath CSV file to write to. File will be overwritten if needed.
	 */
	public abstract void exportFile(String filePath) throws MarlaException;

	/**
	 * Ensures the given name is unique within the DataSet
	 * @param name Name to check for in existing columns
	 * @return true if the name is unique, false otherwise
	 */
	public abstract boolean isUniqueColumnName(String name) throws MarlaException;

	/**
	 * Tell the DataSource that some aspect of it has changed
	 */
	public abstract void markUnsaved();

	/**
	 * Marks all our child operations as dirty
	 */
	public void markDirty()
	{
		// Tell all children they need to recompute
		for(Operation op : solutionOps)
			op.markDirty();
	}

	/**
	 * Ensures that the displayed name for the DataSource is the
	 * correct version and rebuilds the tree if needed
	 */
	public abstract void checkDisplayName();

	/**
	 * Returns the ProblemPart that this DataSource belongs to. Return
	 * is null if there is no parent for this source
	 * @return Parent problem, null if none
	 */
	public abstract Problem getParentProblem();

	/**
	 * Returns the parent this DataSource derives from
	 * @return Next higher set of data or null if there is none.
	 */
	public abstract DataSource getParentData();

	/**
	 * Gets the subproblems this DataSource is a part of
	 * @return SubProblem we are a solution to or an empty list if there is none
	 */
	public final List<SubProblem> getSubProblems()
	{
		return Collections.unmodifiableList(subProblems);
	}

	/**
	 * Adds this DataSource to the given SubProblem
	 * @param sub SubProblem to add DataSource to
	 */
	public final void addSubProblem(SubProblem sub)
	{
		// Don't bother if they're already part of us
		if(subProblems.contains(sub))
			return;

		// We'll need a unique ID
		getUniqueID();

		// Put it in sorted order so that when we are displayed
		// the lines stay consistent
		boolean added = false;
		for(int i = 0; i < subProblems.size(); i++)
		{
			if(subProblems.get(i).compareTo(sub) > 0)
			{
				// This one is the first one later than us, so place
				// ourselves just before him
				subProblems.add(i, sub);
				added = true;
				break;
			}
		}

		// If we didn't find something greater than us, just go to the end
		if(!added)
			subProblems.add(sub);

		sub.addStep(this);

		markUnsaved();
	}

	/**
	 * Removes this DataSource from the given SubProblem
	 * @param sub SubProblem to remove from this DataSource
	 */
	public final void removeSubProblem(SubProblem sub)
	{
		// Don't bother if they're already _not_ a part of us
		if(!subProblems.contains(sub))
			return;

		subProblems.remove(sub);
		sub.removeStep(this);
		markUnsaved();
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 31 * hash + (this.solutionOps != null ? this.solutionOps.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object other)
	{
		// Ourselves?
		if(other == this)
			return true;

		// Actually an operation?
		if(!(other instanceof DataSource))
			return false;

		DataSource otherDS = (DataSource)other;

		if(!name.equals(otherDS.name))
			return false;

		// Well, are our children all the same then?
		if(!solutionOps.equals(otherDS.solutionOps))
			return false;

		return true;
	}
}
