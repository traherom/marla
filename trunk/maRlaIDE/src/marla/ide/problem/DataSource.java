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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import marla.ide.operation.Operation;
import java.util.List;
import java.util.Random;
import javax.swing.JLabel;
import marla.ide.problem.DataColumn.DataMode;
import org.jdom.Element;

/**
 * Tie for any source of that contains columns
 * @author Ryan Morehart
 */
public abstract class DataSource extends JLabel implements Loadable, Changeable
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
	 * Denotes if this DataSource should not actually be shown
	 * to the user on the workspace
	 */
	private boolean isHidden = false;

	/**
	 * Sets basic options for DataSource display
	 */
	public DataSource()
	{
		setOpaque(true);
		setBackground(new Color(255, 255, 255, 0));
	}
	
	/**
	 * Copy constructor for part of a DataSource. Our child ops need to be
	 * copied over by callers!
	 * @param org Original DataSource to copy
	 */
	protected DataSource(DataSource org)
	{
		this();
		
		// Easy stuff
		name = org.name;
		isHidden = org.isHidden;
		internalID = org.internalID;
		
		setBounds(org.getBounds());
		
		// We don't worry about subproblems attached to us, our cloner can
		// reattech them if they wish
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
	 * Sets the DataSource to it-s normal color. Convenience item for resetting
	 * after drags, hovers, etc
	 */
	public abstract void setDefaultColor();

	/**
	 * Returns true if the DataSource is meant to be hidden
	 * @return true if hidden, false otherwise
	 */
	public final boolean isHidden()
	{
		return isHidden;
	}
	/**
	 * Sets if the DataSource is meant to be hidden
	 * @return Previously set hidden value
	 */
	public final boolean isHidden(boolean newHidden)
	{
		boolean old = isHidden;
		isHidden = newHidden;
		
		// Remove all subproblems if we're hidden now
		if(isHidden)
		{
			removeAllSubProblems();
			for(Operation op : getAllChildOperations())
				op.removeAllSubProblems();
		}

		return old;
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
		changeBeginning(null);
		name = newName;
		checkDisplayName();
		markUnsaved();
	}

	/**
	 * Returns a string that could be displayed to the user
	 * @param abbrv If true, abbreviate the returned string in order
	 * @return String suitable for display to user
	 */
	public abstract String getDisplayString(boolean abbrv);

	/**
	 * Returns the column with the given name in the DataSource
	 * @param colName List of values in that column. Column manipulations will
	 *					be reflected in the dataset itself unless a copy is made.
	 * @return The DataColumn requested
	 */
	public abstract DataColumn getColumn(String colName);

	/**
	 * Returns a list of all the columns in the DataSource
	 * @return The DataColumns in the current DataSource
	 */
	public abstract List<DataColumn> getColumns();

	/**
	 * Returns the column index (as would be passed to getColumn(int))
	 * of the column with the given name
	 * @param colName Column name to search for
	 * @return index of corresponding column, -1 if not found
	 */
	public abstract int getColumnIndex(String colName);

	/**
	 * Returns the column requested by index
	 * @param index Index of the column to access
	 * @return DataColumn at the given index
	 */
	public abstract DataColumn getColumn(int index);

	/**
	 * Returns the number of columns in this DataSet
	 * @return Number of columns in DataSet
	 */
	public abstract int getColumnCount();

	/**
	 * Returns the length of the <em>longest</em> column in this dataset
	 * @return Length of the longest column in this dataset. -1 if there are none
	 */
	public abstract int getColumnLength();

	/**
	 * Returns a list of column names.
	 * @return All column names in this dataset
	 */
	public abstract String[] getColumnNames();

	/**
	 * Returns the DataSource data as a two-dimensional array. Data is returned
	 * col x row. That is, [1][2] would access element 2 of column 1.
	 * @return Data in the source in col-row format.
	 */
	public final Object[][] toArray()
	{
		int rowCount = getColumnLength();
		int colCount = getColumnCount();

		// Col x Row
		Object[][] arr = new Object[colCount][];
		for(int col = 0; col < colCount; col++)
		{
			DataColumn currCol = getColumn(col);

			arr[col] = new Object[rowCount];

			// Copy vals over
			for(int row = 0; row < currCol.size(); row++)
				arr[col][row] = currCol.get(row);

			// Fill remainder with ""
			for(int row = currCol.size(); row < rowCount; row++)
				arr[col][row] = "";
		}

		return arr;
	}

	/**
	 * Add given operation to the end of DataSource. See addOperation(index, op)
	 * @param op Operation to add to perform on DataSet
	 * @return Newly added operation
	 */
	public final Operation addOperation(Operation op)
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
	public final Operation addOperation(int index, Operation op)
	{
		// No adding ourselves
		if(op == this)
			return op;

		// Tell the operation to set us as the parent
		op.setParentData(index, this);

		if(!solutionOps.contains(op))
		{
			// They weren't already assigned to us, so stick them on our list
			solutionOps.add(index, op);

			// Add all our current SubProblems to the new child
			for(SubProblem sub : getSubProblems())
			{
				op.addSubProblem(sub);
				for(Operation childOp : op.getAllChildOperations())
					childOp.addSubProblem(sub);
			}
			
			markUnsaved();
		}

		return op;
	}

	/**
	 * Removes an operation from the data
	 * @param op Operation to remove from data
	 * @return The removed Operation
	 */
	public final Operation removeOperation(Operation op)
	{	
		// Tell operation to we're not its parent any more
		op.setParentData(null);

		// Remove them from our list if still needed
		if(solutionOps.remove(op))
			markUnsaved();

		return op;
	}

	/**
	 * Removes an operation from the data
	 * @param index Index of the operation to remove
	 * @return The removed Operation
	 */
	public final Operation removeOperation(int index)
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
	 * @param chain True if R commands from higher up the chain should be included
	 * @return String of R commands
	 */
	public abstract String getRCommands(boolean chain);

	/**
	 * Outputs this DataSource as the string of R commands needed to perform
	 * the calculations for itself.
	 * @return String of R commands
	 */
	public abstract String getRCommands();

	/**
	 * Outputs this DataSet as a constructed R data frame and returns the
	 * variable the data frame is stored in.
	 * @return R variable the data frame is in
	 */
	public abstract String toRFrame();

	/**
	 * Returns a JDOM Element that encapsulates this DataSet's
	 * name, columns, and child operations
	 * @return JDOM Element of this DataSet
	 */
	public abstract Element toXml();

	/**
	 * Takes the DataSource-specific information and bundles it into
	 * an the given XML element
	 * @return XML element in which the data was placed
	 */
	protected Element toXml(Element dataEl)
	{
		dataEl.setAttribute("name", getName());
		dataEl.setAttribute("id", getUniqueID().toString());

		Rectangle rect = getBounds();
		dataEl.setAttribute("x", Integer.toString((int) rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int) rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int) rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int) rect.getWidth()));

		dataEl.setAttribute("hidden", Boolean.toString(isHidden));

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
	protected final DataSource fromXmlBase(Element dsEl)
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

		isHidden = Boolean.parseBoolean(dsEl.getAttributeValue("hidden", "false"));

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
	public abstract void exportFile(String filePath);

	/**
	 * Ensures the given name is unique within the DataSet
	 * @param name Name to check for in existing columns
	 * @return true if the name is unique, false otherwise
	 */
	public abstract boolean isUniqueColumnName(String name);

	/**
	 * Tell the DataSource that some aspect of it has changed
	 */
	@Override
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

	/**
	 * Disconnects this DataSource from every subproblem it is a part of
	 */
	public final void removeAllSubProblems()
	{
		for(int i = 0; i < subProblems.size(); i++)
		{
			SubProblem sub = subProblems.remove(i);
			sub.removeStep(this);
		}
		
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

	/**
	 * Represents the given DataSet as an HTML table
	 * @return String of the data inside the given DataSource
	 */
	public String toHTML()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table>\n");

		// DataSource name
		sb.append("\t<tr><td style='text-align: center' colspan='");
		sb.append(getColumnCount() + 1);
		sb.append("'>");
		sb.append(getName());
		sb.append("</td></tr>\n");

		// Column names
		sb.append("\t<tr><td></td>");
		for(int i = 0; i < getColumnCount(); i++)
		{
			sb.append("<td>");
			sb.append(getColumn(i).getName());
			sb.append("</td>");
		}
		sb.append("</tr>\n");

		// Data. Truncate if needed
		int len = getColumnLength();
		if(len > 50)
			len = 50;

		for(int i = 0; i < len; i++)
		{
			sb.append("\t<tr><td>");
			sb.append(i + 1);
			sb.append("</td>");
			for(int j = 0; j < getColumnCount(); j++)
			{
				sb.append("<td style='text-align: center'>");
				// Ensure this column extends this far
				DataColumn dc = getColumn(j);
				if(dc.size() > i)
					sb.append(dc.get(i));
				else
					sb.append("&nbsp;");
				sb.append("</td>");
			}
			sb.append("</tr>\n");
		}

		// If we truncated tell the user
		if(len < getColumnLength())
		{
			sb.append("<tr><td colspan='");
			sb.append(getColumnCount() + 1);
			sb.append("' style='text-align: center'>-First ");
			sb.append(len);
			sb.append(" rows shown-</td></tr>");
		}

		sb.append("</table>");

		throw new InternalMarlaException("naj");

		//return sb.toString();
	}

	/**
	 * Represents the given DataSet as an easily readable string
	 * @return String of the data inside the given DataSource
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		// Only actually do this if we have columns
		if(getColumnCount() > 0)
		{
			// Make a table of all the strings, then use
			// that to determine the correct width for each column
			int colCount = 1 + getColumnCount();
			int rowCount = 1 + getColumnLength();
			String[][] table = new String[rowCount][];
			int[] colWidth = new int[colCount];

			// Header row
			table[0] = new String[colCount];
			table[0][0] = "";
			for(int col = 1; col < colCount; col++)
			{
				table[0][col] = getColumn(col - 1).getName();

				// Change width if needed
				if(colWidth[col] < table[0][col].length())
					colWidth[col] = table[0][col].length();
			}

			// Value rows
			for(int row = 1; row < rowCount; row++)
			{
				// Assign index column and change width if needed
				table[row] = new String[colCount];
				table[row][0] = "[" + row + "]";
				if(colWidth[0] < table[row][0].length())
					colWidth[0] = table[row][0].length();

				for(int col = 1; col < colCount; col++)
				{
					try
					{
						DataColumn dc = getColumn(col - 1);

						if(dc.getMode() == DataMode.NUMERIC)
							table[row][col] = dc.get(row - 1).toString();
						else
							table[row][col] = '"' + dc.get(row - 1).toString() + '"';

						// Change column width if needed
						if(colWidth[col] < table[row][col].length())
							colWidth[col] = table[row][col].length();
					}
					catch(IndexOutOfBoundsException ex)
					{
						table[row][col] = "";
					}
				}
			}

			// Output DataSet name
			sb.append("Dataset ");
			sb.append(getName());
			sb.append(":\n");

			// Print each row in the table
			for(int row = 0; row < table.length; row++)
			{
				for(int col = 0; col < table[row].length; col++)
				{
					sb.append(String.format("%-" + colWidth[col] + "s  ", table[row][col]));
				}

				// Done with the row
				sb.append('\n');
			}
		}
		else
		{
			// Output DataSet name
			sb.append(getName());
			sb.append('\n');
			sb.append("Empty\n");
		}
		
		return sb.toString();
	}
}
