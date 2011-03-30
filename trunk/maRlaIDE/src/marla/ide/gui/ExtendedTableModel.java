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

package marla.ide.gui;

import javax.swing.table.AbstractTableModel;
import marla.ide.operation.Operation;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataSet;
import marla.ide.problem.DataSource;
import marla.ide.problem.DuplicateNameException;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.MarlaException;

/**
 * The table model used for the assignments table, which allows JComponents to
 * be displayed and used in the table--this allows Done column to allow
 * editing of the JCheckBox that is placed there.
 *
 * @author Alex Laird
 */
public class ExtendedTableModel extends AbstractTableModel
{
	/** The column names for this table.*/
	private DataSource data = null;

	/**
	 * Construct a table model with the given operation.
	 *
	 * @param data The data set to construct with.
	 */
	public ExtendedTableModel(Operation data)
	{
		this.data = (DataSource) data;
	}

	/**
	 * Construct a table model with the given data set.
	 *
	 * @param data The data set to construct with.
	 */
	public ExtendedTableModel(DataSet data)
	{
		this.data = (DataSource) data;
	}
	
	/**
	 * Set the operation for this table.
	 *
	 * @param data The operation to set as the data source.
	 */
	public void setData(Operation data)
	{
		this.data = (DataSource) data;

		fireTableDataChanged();
	}

	/**
	 * Set the data set for this table.
	 *
	 * @param data The data set to set as the data source.
	 */
	public void setData(DataSet data)
	{
		this.data = (DataSource) data;

		fireTableDataChanged();
	}

	/**
	 * Add a new column to the end of the columns list with the given name.
	 *
	 * @param name The name for the new column.
	 */
	public void addColumn(String name)
	{
		// Only a data set will recognize columns
		if(data instanceof DataSet)
		{
			// Create a new column including the new column name
			DataColumn newCol;
			try
			{
				newCol = ((DataSet) data).addColumn(name);
			}
			catch(DuplicateNameException ex)
			{
				throw new InternalMarlaException("Duplicate name for column", ex);
			}

			// Make the length the same as all the others
			int len = data.getColumnLength();
			for(int i = 0; i < len; i++)
			{
				newCol.add(0);
			}

			fireTableDataChanged();
		}
		else
		{

		}
	}

	/**
	 * Remove the column at the given index.
	 *
	 * @param index The index of the column to remove.
	 */
	public void removeColumn(int index)
	{
		// Only a data set will recognize columns
		if(data instanceof DataSet)
		{
			((DataSet) data).removeColumn(index);
			fireTableDataChanged();
		}
	}

	/**
	 * Adds a row to the table filled with data from the passed in array.
	 */
	public void addRow()
	{
		// Add 0 to the end of all columns
		for(int i = 0; i < data.getColumnCount(); i++)
		{
			data.getColumn(i).add(0);
		}

		int newLen = data.getColumnLength();
		fireTableRowsUpdated(newLen, newLen);
	}

	/**
	 * Removes the specified row from the table.
	 *
	 * @param index The index to be removed from the table.
	 */
	public void removeRow(int index)
	{
		// Remove bottom element of each column
		for(int i = 0; i < data.getColumnCount(); i++)
		{
			data.getColumn(i).remove(index);
		}

		int newLen = data.getColumnLength();
		fireTableRowsUpdated(newLen, newLen);
	}

	/**
	 * Removes all rows from the table.
	 */
	public void removeAllRows()
	{
		for(int i = 0; i < data.getColumnCount(); i++)
		{
			data.getColumn(i).clear();
		}

		fireTableDataChanged();
	}

	/**
	 * Retrieves the column count.
	 *
	 * @return The column count.
	 */
	@Override
	public int getColumnCount()
	{
		return data.getColumnCount();
	}

	/**
	 * Retrieves the row count.
	 *
	 * @return The row count.
	 */
	@Override
	public int getRowCount()
	{
		return data.getColumnLength();
	}

	/**
	 * Retrieves the name of the column at the given index.
	 *
	 * @param col The column index.
	 * @return The name of the column.
	 */
	@Override
	public String getColumnName(int col)
	{
		return data.getColumn(col).getName();
	}

	/**
	 * Retrieves the value at the specified row and column index in the data model.
	 *
	 * @param row The row index.
	 * @param col The colum index.
	 * @return The value at that location in the data model.
	 */
	@Override
	public Object getValueAt(int row, int col)
	{
		try
		{
			return data.getColumn(col).get(row);
		}
		catch(IndexOutOfBoundsException ex)
		{
			return null;
		}
	}

	/**
	 * Retrieves the array of objects in the given row.
	 *
	 * @param row The row to retrieve the data for.
	 * @return The row data.
	 */
	public Object[] getRowAt(int rowIndex)
	{
		// Build row
		Object[] row = new Object[data.getColumnCount()];
		for(int i = 0; i < row.length; i++)
		{
			row[i] = data.getColumn(i).get(rowIndex);
		}

		return row;
	}

	/**
	 * If the column is the first column and the list item type is an assignment,
	 * the cell is editable, otherwise it is not.
	 *
	 * @param row The row index.
	 * @param col The column index.
	 * @return True if the cell is editable, false otherwise.
	 */
	@Override
	public boolean isCellEditable(int row, int col)
	{
		return true;
	}

	/**
	 * Sets the value at the specified row, column location in the data model.
	 *
	 * @param value The value to be set.
	 * @param row The row index.
	 * @param col The column index.
	 */
	@Override
	public void setValueAt(Object value, int row, int col)
	{
		data.getColumn(col).set(row, value);
		fireTableCellUpdated(row, col);
	}

	/**
	 * Set the name of the column.
	 * 
	 * @param name The name to set the column to.
	 * @param col The column index.
	 */
	public void setColumn(String name, int col)
	{
		try
		{
			data.getColumn(col).setName(name);
		}
		catch(DuplicateNameException ex)
		{
			throw new InternalMarlaException("Column name not checked by setColumn", ex);
		}
		catch(MarlaException ex)
		{
			throw new InternalMarlaException("Should never occur", ex);
		}

		fireTableDataChanged();
	}

	/**
	 * Set the row object.
	 *
	 * @param rowObject The row object to be set.
	 * @param row The index of the row.
	 */
	public void setRow(Object[] rowObject, int row)
	{
		for(int i = 0; i < rowObject.length; i++)
		{
			data.getColumn(i).set(row, rowObject[i]);
		}

		fireTableRowsUpdated(row, row);
	}
}
