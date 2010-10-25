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

package gui;

import javax.swing.table.AbstractTableModel;

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
    private String[] columnNames =
    {
    };
    /** The data array keeps track of rows in this table.*/
    private Object[][] data =
    {
    };

	/**
	 * Add a new column to the end of the columns list with the given name.
	 *
	 * @param name The name for the new column.
	 */
	public void addColumn(String name)
	{
		// Create a new column including the new column name
		String[] newColumnNames = new String[columnNames.length + 1];
		for (int i = 0; i < newColumnNames.length; ++i)
		{
			if (i < newColumnNames.length - 1)
			{
				newColumnNames[i] = columnNames[i];
			}
			else
			{
				newColumnNames[i] = name;
			}
		}

		columnNames = newColumnNames;
		
		// Create a new data array including the new column
        Object[][] newData = new Object[data.length][columnNames.length];
        for (int i = 0; i < data.length; ++i)
        {
            for (int j = 0; j < columnNames.length - 1; ++j)
            {
				if (data[i][j] != null)
				{
					newData[i][j] = data[i][j];
				}
				else
				{
					newData[i][j] = 0.0;
				}
            }
        }
		
        data = newData;
		// Add default data values for new column
		for (int i = 0; i < data.length; ++i)
		{
			data[i][columnNames.length - 1] = 0.0;
		}
		
		fireTableDataChanged();
	}

	/**
	 * Remove the column at the given index.
	 *
	 * @param index The index of the column to remove.
	 */
	public void removeColumn(int index)
	{
		// Create a new array of columns without the column at index, and move
		// old values into this new array
		String[] newColumnNames = new String[columnNames.length - 1];
		for (int i = 0; i < columnNames.length; ++i)
		{
			int refIndex = i;
			if (i >= index)
			{
				refIndex += 1;
			}

			if (refIndex < columnNames.length)
			{
				newColumnNames[i] = columnNames[refIndex];
			}
		}

		columnNames = newColumnNames;
		
		// Create the new data array without the column at index
        Object[][] newData = new Object[data.length][columnNames.length];
        for (int i = 0; i < newData.length; ++i)
        {
            for (int j = 0; j < columnNames.length; ++j)
            {
				newData[i][j] = data[i][j];
            }
        }

        data = newData;
        fireTableDataChanged();
	}

    /**
     * Adds a row to the table filled with data from the passed in array.
     * 
     * @param row The row to be placed in the table.
     */
    public void addRow(Object[] row)
    {
        // Create a new data array with one more row and fill it with the old data
        Object[][] newData = new Object[data.length + 1][columnNames.length];
        for (int i = 0; i < data.length; ++i)
        {
            for (int j = 0; j < columnNames.length; ++j)
            {
				if (data[i][j] != null)
				{
					newData[i][j] = data[i][j];
				}
				else
				{
					newData[i][j] = 0.0;
				}
            }
        }

        // Fill the new row
        for (int i = 0; i < columnNames.length; ++i)
        {
			if (row[i] != null)
			{
				newData[newData.length - 1][i] = row[i];
			}
			else
			{
				newData[newData.length - 1][i] = 0.0;
			}
        }
        data = newData;
        fireTableRowsUpdated (data.length, data.length);
    }

    /**
     * Removes the specified row from the table.
     * 
     * @param index The index to be removed from the table.
     */
    public void removeRow(int index)
    {
        // Create a new data array with one more row and fill it with the old data
        Object[][] newData = new Object[data.length - 1][columnNames.length];
        for (int i = 0; i < newData.length; ++i)
        {
            for (int j = 0; j < columnNames.length; ++j)
            {
                int refIndex = i;
                if (i >= index)
                {
                    refIndex += 1;
                }
                newData[i][j] = data[refIndex][j];
            }
        }

        data = newData;
        fireTableRowsUpdated (data.length, data.length);
    }

    /**
     * Removes all rows from the table.
     */
    public void removeAllRows()
    {
        data = new Object[][]
                {
                };
    }

    /**
     * Retrieves the column count.
     *
     * @return The column count.
     */
    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    /**
     * Retrieves the row count.
     *
     * @return The row count.
     */
    @Override
    public int getRowCount()
    {
        return data.length;
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
        return columnNames[col];
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
            return data[row][col];
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            return null;
        }
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
        data[row][col] = value;
        fireTableCellUpdated (row, col);
    }

	/**
	 * Set the name of the column.
	 * 
	 * @param name The name to set the column to.
	 * @param col The column index.
	 */
	public void setColumn(String name, int col)
	{
		columnNames[col] = name;
	}

    /**
     * Set the row object.
     *
     * @param rowObject The row object to be set.
     * @param row The index of the row.
     */
    public void setRow(Object[] rowObject, int row)
    {
        data[row] = rowObject;
    }
}
