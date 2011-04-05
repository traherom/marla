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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;
import marla.ide.problem.DataColumn.DataMode;
import marla.ide.r.RProcessor;
import marla.ide.r.RProcessor.RecordMode;
import marla.ide.r.RProcessorParseException;

/**
 * Contains a simple dataset that essentially amounts to a table
 * with column names. Datasets may be named for easier identification.
 *
 * @author Ryan Morehart
 */
@SuppressWarnings("serial")
public final class DataSet extends DataSource
{
	/**
	 * Denotes if this class is loading from XML
	 */
	private boolean isLoading = false;
	/**
	 * Actual values in dataset. All values are assumed to be doubles as
	 * a majority of statistics problems go at least somewhat into decimals.
	 */
	private final List<DataColumn> columns = new ArrayList<DataColumn>();
	/**
	 * Problem this dataset belongs to. Lets us tell the parent when
	 * we've been updated in some way.
	 */
	private Changeable parent = null;

	/**
	 * Creates a blank dataset with the given name.
	 * @param name New dataset name
	 */
	public DataSet(String name)
	{
		this(null, name);
		setDefaultColor();
	}

	/**
	 * Creates a blank dataset with the given name.
	 * @param parent The problem set this dataset is used by
	 * @param name New dataset name
	 */
	public DataSet(Changeable parent, String name)
	{
		this.parent = parent;
		setDataName(name);
	}

	/**
	 * Copy constructor for DataSet. Parent is set to null
	 * @param org Original dataset to copy
	 */
	public DataSet(DataSet org)
	{
		super(org);
		
		isLoading = true;
		
		// Not the same parent, they'll add us where they want
		parent = null;
		
		// Copy all our columns
		for(DataColumn orgDC : org.columns)
			columns.add(new DataColumn(this, orgDC));
		
		isLoading = false;
	}
	
	public static Color getDefaultColor()
	{
		return new Color(143, 10, 43);
	}

	@Override
	public void setDefaultColor()
	{
		setForeground(new Color(143, 10, 43));
	}

	/**
	 * Imports the given R dataset in as a Java DataSet
	 * @param library Library to import from
	 * @param frame Dataset within the library to import
	 * @return New DataSet containing the imported values
	 */
	public static DataSet importFromR(String library, String frame)
	{
		// Ensure the library is imported
		RProcessor proc = RProcessor.getInstance();
		if(proc.loadLibrary(library))
			return fromRFrame(frame);
		else
			throw new MarlaException("The library '" + library + "' could not be loaded into R (or automatically installed)");
	}

	/**
	 * Imports the given file in as a dataset. Files may be either in table format or CSV,
	 * as parsed by R read.table() and read.csv(). Column names may be given at the top
	 * of each column. The dataset name is set from the file name.
	 * @param filePath Absolute or relative path to file to import.
	 * @return New DataSet containing the imported values
	 */
	public static DataSet importFile(String filePath)
	{
		try
		{
			// Open file ourselves to determine the type and settings we'll be handing R
			File file = new File(filePath);
			BufferedReader is = new BufferedReader(new FileReader(file));

			// Read top line of file so we can look for column headers
			// Basically just see if the top is parsable as numbers, in which case
			// we assume no header
			String line = is.readLine();
			Pattern splitPatt = Pattern.compile(",|;");
			Pattern trimPatt = Pattern.compile("^\\s*([\"']?)(.*)\\1\\s*$");
			String[] headers = splitPatt.split(line);
			boolean hasHeader = false;
			try
			{
				// Do these work as numbers?
				Double.parseDouble(headers[0]);
			}
			catch(NumberFormatException e)
			{
				// Well, we certainly can't use the columns as numbers, so they must be headers
				hasHeader = true;
			}

			// Create columns and add to our new DataSet
			DataSet ds = new DataSet(file.getName());
			Matcher cellMatcher = trimPatt.matcher("");
			for(int i = 0; i < headers.length; i++)
			{
				cellMatcher.reset(headers[i]);
				cellMatcher.find();
				String cell = cellMatcher.group(2);
				if(hasHeader)
				{
					ds.addColumn(cell.trim());
				}
				else
				{
					// Make up a name and add the number we read in accidentally
					DataColumn dc = ds.addColumn("Column " + (i + 1));
					dc.add(cell);
				}
			}

			// Read through the rest of the numbers
			line = is.readLine();
			while(line != null)
			{
				String[] row = splitPatt.split(line);

				for(int i = 0; i < row.length; i++)
				{
					cellMatcher.reset(row[i]);
					cellMatcher.find();
					String cell = cellMatcher.group(2);
					if(!cell.isEmpty())
						ds.getColumn(i).add(cell);
				}

				line = is.readLine();
			}

			// Set the DataColumn modes as appropriate
			for(int i = 0; i < ds.getColumnCount(); i++)
			{
				ds.getColumn(i).autodetectMode();
			}

			return ds;
		}
		catch(IOException ex)
		{
			throw new MarlaException("Error occured working with import file", ex);
		}
	}

	/**
	 * Exports this DataSet to a CSV file at the given path. Use R to perform the export.
	 * @param ds DataSource to export to CSV
	 * @param filePath CSV file to write to. File will be overwritten if needed.
	 */
	public static void exportFile(DataSource ds, String filePath)
	{
		try
		{
			// Column names
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			StringBuilder line = new StringBuilder();
			for(int i = 0; i < ds.getColumnCount(); i++)
			{
				line.append(ds.getColumn(i).getName());
				line.append(", ");
			}

			// Remove the final comma and terminate
			line.replace(line.length() - 2, line.length(), "");
			line.append("\n");

			// Go team, write
			out.write(line.toString());

			// Values
			int len = ds.getColumnLength();
			for(int i = 0; i < len; i++)
			{
				line = new StringBuilder();

				for(int j = 0; j < ds.getColumnCount(); j++)
				{
					DataColumn dc = ds.getColumn(j);

					// Actually more items in this column?
					if(i < dc.size())
					{
						if(dc.isNumeric())
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
		catch(IOException ex)
		{
			throw new MarlaException("An error occured working with the file", ex);
		}
	}

	@Override
	public void exportFile(String filePath)
	{
		exportFile(this, filePath);
	}

	/**
	 * Takes the given variable in R and builds the DataSet that represents it.
	 * The variable must be a data frame with numeric values.
	 * @param varName Variable name within the global RProcessor instance to work with
	 * @return New DataSet containing the loaded data
	 */
	public static DataSet fromRFrame(String varName)
	{
		DataSet ds = new DataSet(varName);

		// Get the column names
		RProcessor proc = RProcessor.getInstance();
		List<String> cols = proc.executeStringArray("colnames(" + varName + ")");
		for(String col : cols)
		{
			DataColumn dc = ds.addColumn(col);
			try
			{
				dc.setMode(DataMode.NUMERIC);
				dc.addAll(proc.executeDoubleArray(varName + "$" + col));
			}
			catch(RProcessorParseException ex)
			{
				// Doubles failed, probably strings
				dc.setMode(DataMode.STRING);
				dc.addAll(proc.executeStringArray(varName + "$" + col));
			}
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
	Problem setParentProblem(Problem newParent)
	{
		Problem oldParent = (Problem) parent;
		parent = newParent;
		return oldParent;
	}

	@Override
	public final Problem getParentProblem()
	{
		return (Problem) parent;
	}

	@Override
	public final DataSource getParentData()
	{
		return null;
	}

	@Override
	public final DataSource getRootDataSource()
	{
		return this;
	}

	@Override
	public String getDisplayString(boolean abbrv)
	{
		String longName = getName();
		if(abbrv && longName.length() > 4)
			return shortenString(longName, 7);
		else
			return longName;
	}

	/**
	 * Shortens the given string to the given length, using letters from the beginning and end
	 * @param longForm String to be shortened
	 * @param maxLen Maximum length of the shortened string
	 * @return Newly created short sting
	 */
	public static String shortenString(String longForm, int maxLen)
	{
		int longLen = longForm.length();

		// Don't bother if we're the right length already
		if(longLen <= maxLen)
			return longForm;

		if(maxLen < 4)
		{
			// No marker of the truncation
			int firstHalf = (int)Math.ceil(maxLen/ 2.0);
			int secondHalf = (int)Math.floor(maxLen / 2.0);
			return longForm.substring(0, firstHalf) + longForm.substring(longForm.length() - secondHalf);
		}
		else
		{
			int firstHalf = (int)Math.ceil((maxLen - 1) / 2.0);
			int secondHalf = (int)Math.floor((maxLen - 1) / 2.0);
			return longForm.substring(0, firstHalf) + "\u2026" + longForm.substring(longForm.length() - secondHalf);
		}
	}

	/**
	 * Sets the dataset name
	 * @param newName New name to call this DataSet by
	 */
	public final void setDataName(String newName)
	{
		// Make sure no other datasets have this name
		if(parent != null && parent instanceof ProblemPart)
		{
			ProblemPart prob = (ProblemPart) parent;

			for(int i = 0; i < prob.getDataCount(); i++)
			{
				if(newName.equalsIgnoreCase(prob.getData(i).getName()))
				{
					throw new DuplicateNameException("DataSet with name '" + newName + "' already exists.");
				}
			}
		}

		// And update the name
		setName(newName);

		markUnsaved();
	}

	@Override
	public final boolean isUniqueColumnName(String name)
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
		return addColumn(columns.size(), colName);
	}

	/**
	 * Inserts the given DataColumn into the specified location in the DataSet.
	 * Shifts current elements right to make room. IE, if a DataSet has
	 * columns 0, 1, 2, 3, then in insert of column 4 at index 1 makes
	 * the new setup 0, 4, 1, 2, 3.
	 * @param index Position to insert the column at
	 * @param colName Name of the new Column to create and add to set
	 * @return Column that was added (same as passed in)
	 */
	public DataColumn addColumn(int index, String colName)
	{
		// Ensure the name of this new column is ok
		if(!isUniqueColumnName(colName))
		{
			throw new DuplicateNameException("Data column with name '"
					+ colName + "' already exists in dataset '" + getName() + "'");
		}

		// Create
		DataColumn newColumn = new DataColumn(this, colName);
		columns.add(index, newColumn);
		markUnsaved();

		return newColumn;
	}

	/**
	 * Copies the values and name of an existing column into this DataSet
	 * @param oldCol Column to copy. Should not be part of this DataSet, as a DuplicateNameException will result
	 * @return Newly created column that is a part of this DataSet
	 */
	public DataColumn copyColumn(DataColumn oldCol)
	{
		return copyColumn(columns.size(), oldCol);
	}

	/**
	 * Copies the values and name of an existing column into this DataSet
	 * at the given column index
	 * @param index Column index to copy the column into
	 * @param oldCol Column to copy. Should not be part of this DataSet, as a DuplicateNameException will result
	 * @return Newly created column that is a part of this DataSet
	 */
	public DataColumn copyColumn(int index, DataColumn oldCol)
	{
		DataColumn newCol = addColumn(index, oldCol.getName());

		newCol.setMode(oldCol.getMode());
		newCol.addAll(oldCol);

		return newCol;
	}

	/**
	 * Removes all Columns from DataSet
	 */
	public void clearColumns()
	{
		columns.clear();
		markUnsaved();
	}

	/**
	 * Removes given column in the dataset.
	 * @param column Column to remove from the dataset
	 * @return The removed column
	 */
	public DataColumn removeColumn(DataColumn column)
	{
		// Remove them from our list
		if(columns.remove(column))
		{
			markUnsaved();
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
		DataColumn removedCol = columns.remove(index);
		markUnsaved();
		return removedCol;
	}

	@Override
	public DataColumn getColumn(String colName)
	{
		int index = getColumnIndex(colName);
		if(index != -1)
			return getColumn(index);
		else
			throw new DataNotFoundException("Unable to locate column '" + colName + "' in DataSet");
	}

	@Override
	public int getColumnIndex(String colName)
	{
		for(int i = 0; i < columns.size(); i++)
		{
			if(columns.get(i).getName().equals(colName))
				return i;
		}

		return -1;
	}

	@Override
	public DataColumn getColumn(int index)
	{
		return columns.get(index);
	}

	@Override
	public List<DataColumn> getColumns()
	{
		return Collections.unmodifiableList(columns);
	}

	@Override
	public int getColumnCount()
	{
		return columns.size();
	}

	@Override
	public int getColumnLength()
	{
		int max = -1;
		for(DataColumn c : columns)
		{
			int len = c.size();
			if(max < len)
				max = len;
		}
		return max;
	}

	@Override
	public String[] getColumnNames()
	{
		String[] names = new String[columns.size()];
		for(int i = 0; i < names.length; i++)
		{
			names[i] = columns.get(i).getName();
		}
		return names;
	}

	@Override
	public void checkDisplayName()
	{
		String currText = getText();
		if(!currText.equals(getName()))
		{
			// We actually did change from what was being used
			if(!isLoading() && Problem.getDomain() != null && parent instanceof ProblemPart)
				Problem.getDomain().rebuildTree(this);
		}
	}

	/**
	 * Tells the problem we belong to that we've changed
	 */
	@Override
	public void markUnsaved()
	{
		if(parent instanceof Problem)
			((ProblemPart)parent).markUnsaved();
	}

	@Override
	public void changeBeginning()
	{
		if(parent instanceof Problem)
			((ProblemPart)parent).changeBeginning();
	}

	@Override
	public String getRCommands()
	{
		return getRCommands(false);
	}

	/**
	 * This should only be called as part of an Operation chaining up here
	 * @param chain false if we should output the R frame for this data, false for blank.
	 * @return R commands to make an R data frame
	 */
	@Override
	public final String getRCommands(boolean chain)
	{
		if(!chain)
		{
			RProcessor proc = RProcessor.getInstance();
			RecordMode oldMode = proc.setRecorderMode(RecordMode.CMDS_ONLY);
			toRFrame();
			proc.setRecorderMode(oldMode);
			return proc.fetchInteraction();
		}
		else
			return "";
	}

	@Override
	public String toRFrame()
	{
		return toRFrame(this);
	}

	/**
	 * Takes the given DataSource and exports it to R as a complete data frame
	 * @param ds DataSource to work over
	 * @return Name of the R variable the frame is saved to
	 */
	public static String toRFrame(DataSource ds)
	{
		RProcessor proc = RProcessor.getInstance();

		// Save all of the columns to variables
		List<String> colVars = new ArrayList<String>();
		for(int i = 0; i < ds.getColumnCount(); i++)
		{
			DataColumn dc = ds.getColumn(i);
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

	@Override
	public Element toXml()
	{
		Element dataEl = new Element("data");

		// Add columns
		for(DataColumn col : columns)
		{
			// Column settings
			Element colEl = new Element("column");
			colEl.setAttribute("name", col.getName());
			colEl.setAttribute("mode", col.getMode().toString());

			// Each of the values
			for(Object d : col)
				colEl.addContent(new Element("value").addContent(d.toString()));

			// And put it with the rest of the data
			dataEl.addContent(colEl);
		}

		// Add all the DataSource stuff
		super.toXml(dataEl);

		return dataEl;
	}

	/**
	 * Creates new DataSet with information in JDOM Element
	 * @param dataEl JDOM Element with the information to construct DataSet
	 * @return Constructed and initialized DataSet
	 */
	public static DataSet fromXml(Element dataEl)
	{
		DataSet newData = new DataSet("initializing");
		newData.isLoading = true;

		// Load the DataSource information
		newData.fromXmlBase(dataEl);

		// Load columns
		for(Object colElObj : dataEl.getChildren("column"))
		{
			Element colEl = (Element) colElObj;

			// Create column
			DataColumn newCol = newData.addColumn(colEl.getAttributeValue("name"));
			newCol.setMode(DataMode.valueOf(colEl.getAttributeValue("mode")));

			// Stick in values
			for(Object el : colEl.getChildren("value"))
				newCol.add(((Element) el).getText());
		}

		newData.isLoading = false;
		return newData;
	}

	@Override
	public boolean isLoading()
	{
		if(isLoading)
			return true;
		else if(parent != null)
			return ((Loadable)parent).isLoading();
		else
			return false;
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

		if(!super.equals(other))
			return false;

		// Actually a dataset?
		if(!(other instanceof DataSet))
			return false;

		DataSet otherDS = (DataSet) other;
		if(!columns.equals(otherDS.columns))
			return false;
		
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		hash = 97 * hash + (this.columns != null ? this.columns.hashCode() : 0);
		return hash;
	}
}
