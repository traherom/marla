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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import javax.swing.JLabel;
import org.jdom.Element;
import problem.DataColumn.DataMode;
import r.RProcessor;
import r.RProcessor.RecordMode;
import r.RProcessorException;
import r.RProcessorParseException;

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
		this.columns = new ArrayList<DataColumn>();
		setName(name);
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

		for(Operation op : copy.solutionOps)
		{
			solutionOps.add(op.clone());
		}
	}

	/**
	 * Imports the given file in as a dataset. Files may be either in table format or CSV,
	 * as parsed by R read.table() and read.csv(). Column names may be given at the top
	 * of each column. The dataset name is set from the file name.
	 * @param filePath Absolute or relative path to file to import.
	 * @return New DataSet containing the imported values
	 * @throws FileNotFoundException The file we're supposed to be importing could not be found
	 * @throws CalcException Unable to compute values after everything was loaded up
	 * @throws RProcessorException Unable to bring in R processor to import file
	 * @throws RProcessorParseException R was unable to parse the file in some way
	 */
	public static DataSet importFile(String filePath) throws FileNotFoundException, CalcException, RProcessorException, RProcessorParseException
	{
		// Open file ourselves to determine the type and settings we'll be handing R
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

		// Is this a CSV?
		boolean isCSV = true;
		String[] lineEntries = null;
		lineEntries = firstLine.split(",|;");
		if(lineEntries.length == 1)
		{
			// Well, maybe it's a table then
			isCSV = false;
			lineEntries = firstLine.split("\\w+");
		}

		// Is this row parsable as names?
		boolean hasHeader = false;
		try
		{
			// Do these work as numbers?
			Double.parseDouble(lineEntries[0]);
		}
		catch(NumberFormatException e)
		{
			// Well, we certainly can't use the columns as numbers, so they must be headers
			hasHeader = true;
		}

		// Done with file
		try
		{
			is.close();
		}
		catch(IOException ex)
		{
			// Crap. Maybe this'll close it
			is = null;
		}

		// Now actually use R to pull in the file as appropriate
		StringBuilder cmd = new StringBuilder("read.");
		if(isCSV)
			cmd.append("csv(\"");
		else
			cmd.append("table(\"");

		// I swear to god this is right: \ to \\. The extra slashes are first
		// to get through the Java string, then through the regex, then to R
		cmd.append(filePath.replaceAll("\\\\", "\\\\\\\\"));

		cmd.append("\", header=");
		if(hasHeader)
			cmd.append('T');
		else
			cmd.append('F');
		cmd.append(')');

		RProcessor proc = RProcessor.getInstance();
		String varName = proc.executeSave(cmd.toString());

		// Read it back in
		DataSet ds = DataSet.fromRFrame(varName);
		ds.setName(file.getName());

		return ds;
	}

	/**
	 * Exports this DataSet to a CSV file at the given path. Use R to perform the export.
	 * @param filePath CSV file to write to. File will be overwritten if needed.
	 */
	public void exportFile(String filePath) throws IOException
	{
		// Column names
		BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
		StringBuilder line = new StringBuilder();
		for(DataColumn dc : columns)
		{
			line.append('"');
			line.append(dc.getName());
			line.append("\", ");
		}

		// Remove the final comma and terminate
		line.replace(line.length() - 2, line.length(), "");
		line.append("\n");

		// Go team, write
		out.write(line.toString());

		// Values
		int len = getColumnLength();
		for(int i = 0; i < len; i++)
		{
			line = new StringBuilder();

			for(DataColumn dc : columns)
			{
				// Actually more items in this column?
				if(i < dc.size())
				{
					if(dc.isNumerical())
					{
						line.append(dc.get(i));
					}
					else
					{
						line.append('"');
						line.append(dc.get(i));
						line.append('"');
					}
				}

				line.append(", ");
			}

			// Remove the final comma and terminate
			line.replace(line.length() - 2, line.length(), "");
			line.append("\n");

			// And write
			out.write(line.toString());
		}

		// All done
		out.close();
	}

	/**
	 * Takes the given variable in R and builds the DataSet that represents it.
	 * The variable must be a data frame with numeric values.
	 * @param varName Variable name within the global RProcessor instance to work with
	 * @return New DataSet containing the loaded data
	 */
	public static DataSet fromRFrame(String varName) throws RProcessorException, RProcessorParseException, DuplicateNameException, CalcException
	{
		DataSet ds = new DataSet(varName);

		// Get the column names
		RProcessor proc = RProcessor.getInstance();
		ArrayList<String> cols = proc.executeStringArray("colnames(" + varName + ")");
		for(String col : cols)
		{
			DataColumn dc = ds.addColumn(col);
			dc.addAll(proc.executeDoubleArray(varName + "$" + col));
		}

		return ds;
	}

	/**
	 * Assigns this DataSet to a new parent. Should only be called by
	 * the new parent Problem, as that needs to actually insert the
	 * operation into its array. The package private access is intentional.
	 * @param newParent Problem this dataset belongs to
	 * @return Old parent this DataSet used to belong to, null if there was none
	 */
	ProblemPart setParentProblem(ProblemPart newParent)
	{
		ProblemPart oldParent = parent;
		parent = newParent;
		return oldParent;
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
	 * @param newName New name to call this DataSet by
	 */
	@Override
	public final void setName(String newName)
	{
		// Make sure no other datasets have this name
		if(parent != null)
		{
			for(int i = 0; i < parent.getDataCount(); i++)
			{
				if(newName.equalsIgnoreCase(parent.getData(i).getName()))
				{
					throw new DuplicateNameException("DataSet with name '" + newName + "' already exists.");
				}
			}
		}

		markChanged();

		super.setText(newName);
		name = newName;
	}

	/**
	 * Ensures the given name is unique within the DataSet
	 * @param name Name to check for in existing columns
	 * @return true if the name is unique, false otherwise
	 */
	public boolean isUniqueColumnName(String name)
	{
		// Make sure no other columns have this name
		for(DataColumn dc : columns)
		{
			if(name.equalsIgnoreCase(dc.getName()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Adds another column to this dataset
	 * @param colName Name for new column
	 * @return Newly created data column
	 */
	public DataColumn addColumn(String colName)
	{
		return addColumn(new DataColumn(colName));
	}

	/**
	 * Adds a column to this DataSet
	 * @param column Column to assign to this DataSet
	 * @return Column that was added to data (same as passed in)
	 */
	public DataColumn addColumn(DataColumn column)
	{
		// Tell the column to set us as the parent
		column.setParent(this);

		if(!columns.contains(column))
		{
			// Ensure the name of this new column is ok
			if(!isUniqueColumnName(column.getName()))
			{
				throw new DuplicateNameException("Data column with name '"
						+ column.getName() + "' already exists in dataset '" + name + "'");
			}

			// They weren't already assigned to us, so stick them on our list
			columns.add(column);
			markChanged();
		}
		
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
	 * @throws CalcException Unable to recompute the data with this new column
	 */
	public DataColumn addColumn(int index, DataColumn column) throws CalcException
	{
		// Tell the column to set us as the parent
		column.setParent(this);

		if(!columns.contains(column))
		{
			// Ensure the name of this new column is ok
			if(!isUniqueColumnName(column.getName()))
			{
				throw new DuplicateNameException("Data column with name '"
						+ column.getName() + "' already exists in dataset '" + name + "'");
			}

			// They weren't already assigned to us, so stick them on our list
			columns.add(index, column);
			markChanged();
		}

		return column;
	}

	/**
	 * Removes given column in the dataset.
	 * @param column Column to remove from the dataset
	 * @return The removed column
	 */
	public DataColumn removeColumn(DataColumn column)
	{
		// Tell column to we're not its parent any more
		column.setParent(null);

		// Remove them from our list if still needed
		if(columns.remove(column))
		{
			markChanged();
		}

		return column;
	}

	/**
	 * Remove the column at the given index.
	 * All columns to the right shift right, index-wise.
	 * @param index Location to remove from DataSet
	 * @return Removed DataColumn, with the parent no longer set to this dataset
	 */
	public DataColumn removeColumn(int index)
	{
		return removeColumn(columns.get(index));
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
	 * Tells our child operations that their caches are dirty and need to be recomputed
	 */
	public void markChanged()
	{
		// Tell all children they need to recompute
		for(Operation op : solutionOps)
		{
			op.markChanged();
		}

		markUnsaved();
	}

	/**
	 * Tells the problem we belong to that we've changed. Used by DataColumns
	 * under us to notify encapsulating problem.
	 */
	public void markUnsaved()
	{
		if(parent != null)
			parent.markChanged();
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
		// Tell the operation to set us as the parent
		op.setParentData(this);

		if(!solutionOps.contains(op))
		{
			// They weren't already assigned to us, so stick them on our list
			solutionOps.add(op);
			markChanged();
		}

		return op;
	}

	/**
	 * Appends the given operation to the end of the "left" (first) chain
	 * on this dataset.
	 * @param op Operation to add to perform on DataSet
	 * @return Newly added operation
	 * @throws CalcException Unable to compute values with new operation attached
	 * @deprecated Instead call addOperation() on the appropriate Operation
	 */
	@Deprecated
	public Operation addOperationToEnd(Operation op) throws CalcException
	{
		if(solutionOps.isEmpty())
		{
			return addOperation(op);
		}
		else
		{
			return solutionOps.get(0).addOperationToEnd(op);
		}
	}

	/**
	 * Removes an operation from the data
	 * @param op Operation to remove from data
	 * @return The removed Operation
	 */
	public Operation removeOperation(Operation op)
	{
		// Tell column to we're not its parent any more
		op.setParentData(null);

		// Remove them from our list if still needed
		if(solutionOps.remove(op))
		{
			markChanged();
		}

		return op;
	}

	/**
	 * Removes an operation from the data
	 * @param index Index of the operation to remove
	 * @return The removed Operation
	 */
	public Operation removeOperation(int index)
	{
		return removeOperation(solutionOps.get(index));
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
	 * Outputs this DataSet as the string of R commands needed to turn it into a data frame
	 * @return R commands
	 */
	public String toRString() throws RProcessorException, RProcessorParseException
	{
		RProcessor proc = RProcessor.getInstance();
		RecordMode oldMode = proc.setRecorder(RecordMode.CMDS_ONLY);
		toRFrame();
		proc.setRecorder(oldMode);
		return proc.fetchInteraction();
	}

	/**
	 * Outputs this DataSet as a constructed R data frame and returns the
	 * variable the data frame is stored in.
	 * @return R variable the data frame is in
	 */
	public String toRFrame() throws RProcessorException, RProcessorParseException
	{
		RProcessor proc = RProcessor.getInstance();

		// Save all of the columns to variables
		ArrayList<String> colVars = new ArrayList<String>();
		for(DataColumn dc : columns)
		{
			String colName = proc.executeString("make.names('" + dc.getName() + "')");
			colVars.add(colName);
			proc.setVariable(colName, dc);
		}

		// Save to frame
		StringBuilder sb = new StringBuilder("data.frame(");
		for(String var : colVars)
		{
			sb.append(var);
			sb.append(", ");
		}
		if(colVars.size() > 0)
			sb.replace(sb.length() - 2, sb.length(), "");
		sb.append(")");
		String frameName = proc.executeSave(sb.toString());

		// And return back the variable it's in
		return frameName;
	}

	/**
	 * Outputs this DataSet as a human readable display
	 * @return DataSet as a string two dimensional array
	 */
	@Override
	public String toString()
	{
		return toString(this);
	}

	/**
	 * Represents the given DataSet as an easily readable string
	 * @param ds DataSet to create string for
	 * @return String of the data inside the DataSet with the given data
	 */
	public static String toString(DataSet ds)
	{
		StringBuilder sb = new StringBuilder();

		// Only actually do this if we have columns
		if(ds.getColumnCount() > 0)
		{
			// Make a table of all the strings, then use
			// that to determine the correct width for each column
			int colCount = 1 + ds.getColumnCount();
			int rowCount = 1 + ds.getColumnLength();
			String[][] table = new String[rowCount][];
			int[] colWidth = new int[colCount];

			// Header row
			table[0] = new String[colCount];
			table[0][0] = "";
			for(int col = 1; col < colCount; col++)
			{
				table[0][col] = ds.getColumn(col - 1).getName();

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

				// Assign each column's corresponding value to this row
				for(int col = 1; col < colCount; col++)
				{
					try
					{
						DataColumn dc = ds.getColumn(col - 1);
						if(dc.getMode() == DataMode.NUMERICAL)
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
			sb.append(ds.getName());
			sb.append('\n');

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
			sb.append(ds.getName());
			sb.append('\n');
			sb.append("Empty\n");
		}
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
		dataEl.setAttribute("x", Integer.toString((int) rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int) rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int) rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int) rect.getWidth()));

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
		newData.setBounds(x, y, width, height);

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
