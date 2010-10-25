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
package problem;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import javax.swing.JLabel;
import org.jdom.Element;

/**
 * Contains a simple dataset that essentially amounts to a table
 * with column names. Datasets may be named for easier identification.
 *
 * @author Ryan Morehart
 */
public class DataSet extends JLabel
{

	/**
	 * Dataset name.
	 */
	protected String name = null;
	/**
	 * Actual values in dataset. All values are assumed to be doubles as
	 * a majority of statistics problems go at least somewhat into decimals.
	 */
	protected ArrayList<DataColumn> columns = new ArrayList<DataColumn>();
	/**
	 * Problem this dataset belongs to. Lets us tell the parent when
	 * we've been updated in some way.
	 */
	private ProblemPart parent = null;
	/**
	 * Commands to perform on this dataset
	 */
	protected ArrayList<Operation> solutionOps = new ArrayList<Operation>();

	/**
	 * Creates a blank dataset with the given name.
	 *
	 * @param name New dataset name
	 */
	public DataSet(String name)
	{
		this(null, name);
	}

	/**
	 * Creates a blank dataset with the given name.
	 * @param parent The problem set this dataset is used by
	 * @param name New dataset name
	 */
	public DataSet(ProblemPart parent, String name)
	{
		super(name);
		this.parent = parent;
		this.name = name;
		this.columns = new ArrayList<DataColumn>();
	}

	/**
	 * Create a deep copy of a dataset.
	 * @param copy Dataset to be copied.
	 * @param parent Parent for the copy to use
	 */
	public DataSet(DataSet copy, ProblemPart parent)
	{
		super(copy.name);
		this.parent = parent;
		name = copy.name;
		for(DataColumn dc : copy.columns)
		{
			columns.add(new DataColumn(dc, this));
		}
	}

	/**
	 * Imports the given file in as a dataset. Currently only
	 * CSV files are supported. Column names must be given at the top
	 * of each, well, column. A dataset name is derived from those.
	 * @param filePath Absolute or relative path to file to import.
	 * @return New DataSet containing the imported values
	 */
	public static DataSet importFile(String filePath) throws FileNotFoundException
	{
		File file = new File(filePath);
		BufferedReader is = new BufferedReader(new FileReader(file));

		// Read top line of file so we can look for column headers
		String firstLine;
		try
		{
			firstLine = is.readLine();
		}
		catch(IOException ex)
		{
			throw new InputMismatchException("File is empty");
		}

		String[] lineEntries = firstLine.split(",|;");

		// Is this row parsable as names?
		DataSet newData = new DataSet(file.getName());
		try
		{
			// Do these work as numbers?
			Double.parseDouble(lineEntries[0]);
			// Yes, so we have to make our own column headers
			for(int i = 0; i < lineEntries.length; i++)
			{
				DataColumn col = newData.addColumn("Column " + i);
				col.add(Double.parseDouble(lineEntries[i]));
			}
		}
		catch(NumberFormatException e)
		{
			// Well, we certainly can't use the columns as numbers
			for(int i = 0; i < lineEntries.length; i++)
			{
				newData.addColumn(lineEntries[i]);
			}
		}

		try
		{
			while(is.ready())
			{
				lineEntries = is.readLine().split(",|;");
				for(int i = 0; i < lineEntries.length; i++)
				{
					try
					{
						newData.getColumn(i).add(Double.parseDouble(lineEntries[i]));
					}
					catch(NumberFormatException e)
					{
						// Whatever, maybe a blank in it, maybe we're reading
						// a blank line
					}
				}
			}
			return newData;
		}
		catch(IOException ex)
		{
			// All out o' file, no biggie
		}

		return newData;
	}

	/**
	 * Assigns this DataSet to a new parent. Should only be called by
	 * the new parent Problem, as that needs to actually insert the
	 * operation into its array. The package private access is intentional.
	 *
	 * @param newParent Problem this dataset belongs to
	 */
	void setParentProblem(ProblemPart newParent)
	{
		parent = newParent;
	}

	/**
	 * Returns the ProblemPart that this dataset belongs to. Return
	 * is null if there is no parent for this problem.
	 * @return Parent problem, null if none
	 */
	public ProblemPart getParentProblem()
	{
		return parent;
	}

	/**
	 * Always returns null, as DataSets do not have higher data sources.
	 * @return null, indicating top of data hierarchy
	 */
	public DataSet getParentData()
	{
		return null;
	}

	/**
	 * Gets the current dataset name
	 * @return Dataset name
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the dataset name
	 * @param newName
	 */
	@Override
	public void setName(String newName)
	{
		markChanged();
		super.setName(newName);
		name = newName;
	}

	/**
	 * Adds another column to this dataset
	 * @param colName Name for new column
	 * @return Newly created data column
	 */
	public DataColumn addColumn(String colName)
	{
		markChanged();
		DataColumn newCol = new DataColumn(this, colName);
		columns.add(newCol);
		return newCol;
	}

	/**
	 * Adds a column to this DataSet
	 * @param column Column to assign to this DataSet
	 * @return Column that was added to data (same as passed in)
	 */
	public DataColumn addColumn(DataColumn column)
	{
		markChanged();
		columns.add(column);
		column.setParent(this);
		return column;
	}

	/**
	 * Inserts the given DataColumn into the specified location in the DataSet.
	 * Shifts current elements right to make room. IE, if a DataSet has
	 * columns 0, 1, 2, 3, then in insert of column 4 at index 1 makes
	 * the new setup 0, 4, 1, 2, 3.
	 * @param index Position to insert the column at
	 * @param column Column to assign to this DataSet
	 * @return Column that was added (same as passed in)
	 */
	public DataColumn addColumn(int index, DataColumn column)
	{
		markChanged();
		columns.add(index, column);
		column.setParent(this);
		return column;
	}

	/**
	 * Removes given column in the dataset.
	 * @param col Column to remove from the dataset
	 * @return The removed column
	 */
	public DataColumn removeColumn(DataColumn col)
	{
		return removeColumn(columns.indexOf(col));
	}

	/**
	 * Remove the column at the given index.
	 * All columns to the right shift right, index-wise.
	 * @param index Location to remove from DataSet
	 * @return Removed DataColumn, with the parent no longer set to this dataset
	 */
	public DataColumn removeColumn(int index)
	{
		markChanged();
		DataColumn col = columns.remove(index);
		col.setParent(null);
		return col;
	}

	/**
	 * Returns the column in the dataset with the given name.
	 *
	 * @param colName List of values in that column. Column manipulations will
	 *					be reflected in the dataset itself unless a copy is made.
	 * @throws DataNotFound Unable to find the requested column to return
	 * @return The DataColumn requested
	 */
	public DataColumn getColumn(String colName) throws DataNotFound
	{
		return getColumn(getColumnIndex(colName));
	}

	/**
	 * Returns the column index (as would be passed to getColumn(int))
	 * of the column with the given name
	 * @param colName Column name to search for
	 * @return index of corresponding column
	 * @throws DataNotFound Thrown if a column with the given name can't be found
	 */
	public int getColumnIndex(String colName) throws DataNotFound
	{
		for(int i = 0; i < columns.size(); i++)
		{
			if(columns.get(i).getName().equals(colName))
				return i;
		}

		throw new DataNotFound("Unable to locate data column named '" + colName + "'");
	}

	/**
	 * Returns the column requested by index
	 * @param index Index of the column to access
	 * @return DataColumn at the given index
	 */
	public DataColumn getColumn(int index)
	{
		return columns.get(index);
	}

	/**
	 * Returns the number of columns in this DataSet
	 * @return Number of columns in DataSet
	 */
	public int getColumnCount()
	{
		return columns.size();
	}

	/**
	 * Returns the length of the <em>longest</em> column in this dataset
	 * @return Length of the longest column in this dataset. 0 if there are none
	 */
	public int getColumnLength()
	{
		int max = 0;
		for(DataColumn c : columns)
		{
			int len = c.size();
			if(max < len)
				max = len;
		}
		return max;
	}

	/**
	 * Returns a list of column names.
	 * Alex is too stupid to do it on his own.
	 * @return All column names in this dataset
	 */
	public String[] getColumnNames()
	{
		String[] names = new String[columns.size()];
		for(int i = 0; i < names.length; i++)
		{
			names[i] = columns.get(i).getName();
		}
		return names;
	}

	/**
	 * Returns the same result as if you called getColumn() for each column
	 * and combined them into a single DataSet. Shortcut if all values are
	 * desired.
	 * @return DataSet with all values "solved" (if an operation is being performed)
	 */
	public DataSet getAllColumns()
	{
		return new DataSet(this, null);
	}

	/**
	 * Tells the problem we belong to that we've changed. Used by DataColumns
	 * under us to notify encapsulating problem.
	 */
	public void markChanged()
	{
		// Tell all children to recompute
		for(Operation op : solutionOps)
		{
			op.refreshCache();
		}

		if(parent != null)
			parent.markChanged();
	}

	/**
	 * Quick way to check if something may need to be recomputed
	 * @return true if it is possible that the values in this DataSet are different
	 */
	public boolean isChanged()
	{
		return parent.isChanged();
	}

	/**
	 * Add an operation to this data object. If you want to chain operations
	 * together, then you must append the operation to another operation.
	 * Multiple operations added to a single dataset are independent and
	 * the results have no effect on one another!
	 *
	 * The return of the newly added operation allows chains to be built
	 * quickly.
	 *
	 * @param op Operation to add to perform on DataSet
	 * @return Newly added operation
	 */
	public Operation addOperation(Operation op)
	{
		markChanged();
		op.setParentData(this);
		solutionOps.add(op);
		return op;
	}

	/**
	 * Removes an operation from the data
	 * @param op Operation to remove from data
	 * @return The removed Operation
	 */
	public Operation removeOperation(Operation op)
	{
		markChanged();
		solutionOps.remove(op);
		op.setParentData(null);
		return op;
	}

	/**
	 * Get the Operation at the specified index
	 * @param index Index of Operation to retrieve
	 * @return Operation at index
	 */
	public Operation getOperation(int index)
	{
		return solutionOps.get(index);
	}

	/**
	 * Returns the number of top-level operations working on this
	 * DataSet
	 * @return Number of Operations in DataSet
	 */
	public int getOperationCount()
	{
		return solutionOps.size();
	}

	/**
	 * Returns a two dimensional array with all values in this dataset.
	 * Column labels are lost but columns are in the order they were
	 * inserted in
	 * @return Table of all values in dataset
	 */
	public Double[][] toArray()
	{
		Double[][] allColumns = new Double[columns.size()][];
		for(int i = 0; i < columns.size(); i++)
		{
			allColumns[i] = new Double[columns.get(i).size()];

			Object[] vals = columns.get(i).toArray();
			for(int j = 0; j < vals.length; j++)
			{
				allColumns[i][j] = (Double) vals[j];
			}
		}
		return allColumns;
	}

	/**
	 * Outputs this DataSet as a constructed R data frame
	 * @return DataSet as a string two dimensional array
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for(DataColumn col : columns)
		{
			sb.append(col.toString());
			sb.append('\n');
		}

		sb.append(Operation.sanatizeName(this.name));
		sb.append(" = data.frame(");

		for(DataColumn col : columns)
		{
			sb.append(Operation.sanatizeName(col.getName()));
			sb.append(", ");
		}
		if(columns.size() > 0)
			sb.replace(sb.length() - 2, sb.length(), "");

		sb.append(')');

		return sb.toString();
	}

	/**
	 * Returns a JDOM Element that encapsulates this DataSet's
	 * name, columns, and child operations
	 * @return JDOM Element of this DataSet
	 */
	public Element toXml()
	{
		Element dataEl = new Element("data");
		dataEl.setAttribute("name", name);
		dataEl.setAttribute("id", Integer.toString(hashCode()));

		Rectangle rect = getBounds();
		dataEl.setAttribute("x", Integer.toString((int)rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int)rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int)rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int)rect.getWidth()));

		// Add columns
		for(DataColumn col : columns)
		{
			dataEl.addContent(col.toXml());
		}

		// Add Ops
		for(Operation op : solutionOps)
		{
			dataEl.addContent(op.toXml());
		}

		return dataEl;
	}

	/**
	 * Creates new DataSet with information in JDOM Element
	 * @param dataEl JDOM Element with the information to construct DataSet
	 * @return Constructed and initialized DataSet
	 */
	public static DataSet fromXml(Element dataEl)
	{
		DataSet newData = new DataSet(dataEl.getAttributeValue("name"));

		int x = Integer.parseInt(dataEl.getAttributeValue("x"));
		int y = Integer.parseInt(dataEl.getAttributeValue("y"));
		int height = Integer.parseInt(dataEl.getAttributeValue("height"));
		int width = Integer.parseInt(dataEl.getAttributeValue("width"));
		newData.setBounds(x, y, height, width);

		for(Object colEl : dataEl.getChildren("column"))
		{
			newData.addColumn(DataColumn.fromXml((Element) colEl));
		}

		for(Object opEl : dataEl.getChildren("operation"))
		{
			newData.addOperation(Operation.fromXml((Element) opEl));
		}

		return newData;
	}

	/**
	 * A DataSet is equal if all solution ops, columns, and name are the same
	 * @param other Object to compare against
	 * @return True if the the given object is the same as this one
	 */
	@Override
	public boolean equals(Object other)
	{
		// Ourself?
		if(other == this)
			return true;

		// Actually a dataset?
		if(!(other instanceof DataSet))
			return false;

		DataSet otherDS = (DataSet) other;
		if(!name.equals(otherDS.name))
			return false;
		if(!columns.equals(otherDS.columns))
			return false;
		if(!solutionOps.equals(otherDS.solutionOps))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 97 * hash + (this.columns != null ? this.columns.hashCode() : 0);
		hash = 97 * hash + (this.solutionOps != null ? this.solutionOps.hashCode() : 0);
		return hash;
	}
}
