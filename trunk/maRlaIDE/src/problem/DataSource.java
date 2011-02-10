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

import java.util.List;
import org.jdom.Element;

/**
 * Tie for any source of that contains columns
 * @author Ryan Morehart
 */
public interface DataSource
{
	/**
	 * Gets the current DataSource name
	 * @return DataSource name
	 */
	public String getName();

	/**
	 * Returns the column in the dataset with the given name.
	 *
	 * @param colName List of values in that column. Column manipulations will
	 *					be reflected in the dataset itself unless a copy is made.
	 * @return The DataColumn requested
	 */
	public DataColumn getColumn(String colName) throws DataNotFoundException, MarlaException;

	/**
	 * Returns the column index (as would be passed to getColumn(int))
	 * of the column with the given name
	 * @param colName Column name to search for
	 * @return index of corresponding column
	 */
	public int getColumnIndex(String colName) throws DataNotFoundException, MarlaException;

	/**
	 * Returns the column requested by index
	 * @param index Index of the column to access
	 * @return DataColumn at the given index
	 */
	public DataColumn getColumn(int index) throws DataNotFoundException, MarlaException;

	/**
	 * Returns the number of columns in this DataSet
	 * @return Number of columns in DataSet
	 */
	public int getColumnCount() throws MarlaException;

	/**
	 * Returns the length of the <em>longest</em> column in this dataset
	 * @return Length of the longest column in this dataset. 0 if there are none
	 */
	public int getColumnLength() throws MarlaException;

	/**
	 * Returns a list of column names.
	 * @return All column names in this dataset
	 */
	public String[] getColumnNames() throws MarlaException;

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
	public Operation addOperation(Operation op) throws MarlaException;;

	/**
	 * Removes an operation from the data
	 * @param op Operation to remove from data
	 * @return The removed Operation
	 */
	public Operation removeOperation(Operation op) throws MarlaException;

	/**
	 * Removes an operation from the data
	 * @param index Index of the operation to remove
	 * @return The removed Operation
	 */
	public Operation removeOperation(int index) throws MarlaException;

	/**
	 * Get the Operation at the specified index
	 * @param index Index of Operation to retrieve
	 * @return Operation at index
	 */
	public Operation getOperation(int index);

	/**
	 * Returns the number of top-level operations working on this
	 * DataSet
	 * @return Number of Operations in DataSet
	 */
	public int getOperationCount();

	/**
	 * Returns a flat list of every operation that is a child of this one,
	 * directly or indirectly. Easy way to get access to an entire subtree
	 * @return List of every operation below this one in the tree
	 */
	public List<Operation> getAllChildOperations();

	/**
	 * Returns the DataSource that is at the top of the chain
	 * @return DataSource without a parent (top of the chain)
	 */
	public DataSource getRootDataSource();

	/**
	 * Outputs this DataSource as the string of R commands needed to perform
	 * the calculations for itself. If chain is true then R commands from higher
	 * up the chain are also included in the string
	 * @throws chain True if R commands from higher up the chain should be included
	 * @return String of R commands
	 */
	public String getRCommands(boolean chain) throws MarlaException;

	/**
	 * Outputs this DataSet as a constructed R data frame and returns the
	 * variable the data frame is stored in.
	 * @return R variable the data frame is in
	 */
	public String toRFrame() throws MarlaException;

	/**
	 * Outputs this DataSource as an HTML table with the contained data
	 * @return String of the HTML table representing this DataSource
	 */
	public String toHTML() throws MarlaException;

	/**
	 * Returns a JDOM Element that encapsulates this DataSet's
	 * name, columns, and child operations
	 * @return JDOM Element of this DataSet
	 */
	public Element toXml() throws MarlaException;

	/**
	 * Exports this DataSource to a CSV file at the given path. Use R to perform the export.
	 * @param filePath CSV file to write to. File will be overwritten if needed.
	 */
	public void exportFile(String filePath) throws MarlaException;

	/**
	 * Ensures the given name is unique within the DataSet
	 * @param name Name to check for in existing columns
	 * @return true if the name is unique, false otherwise
	 */
	public boolean isUniqueColumnName(String name) throws MarlaException;

	/**
	 * Tell the DataSource that some aspect of it has changed
	 */
	public void markChanged();

	/**
	 * Tell the DataSource that some aspect of it has changed
	 */
	public void markUnsaved();
}
