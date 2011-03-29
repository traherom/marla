/*
 * Get Organized - Organize your schedule, course assignments, and grades
 * Copyright © 2011 Laird Development
 * contact@getorganizedapp.com
 * www.getorganizedapp.com
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

package marla.opedit.gui;

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
    private String[] columnNames = {};
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
		String[] newColumns = new String[columnNames.length + 1];
		for (int i = 0; i < columnNames.length; ++i)
		{
			newColumns[i] = columnNames[i];
		}
		
		newColumns[newColumns.length - 1] = name;
		columnNames = newColumns;

		fireTableDataChanged ();
	}

	/**
	 * Remove all columns from the model.
	 */
	public void removeAllColumns()
	{
		columnNames = new String[0];
	}

    /**
     * Adds a row to the table filled with data from the passed in array.
     *
     * @param row The row to be placed in the table.
     */
    public void addRow(Object[] row)
    {
        // create a new data array with one more row and fill it with the old data
        Object[][] newData = new Object[data.length + 1][columnNames.length];
        for (int i = 0; i < data.length; ++i)
        {
            for (int j = 0; j < columnNames.length; ++j)
            {
                newData[i][j] = data[i][j];
            }
        }

        // fill the new row
        for (int i = 0; i < columnNames.length; ++i)
        {
            newData[newData.length - 1][i] = row[i];
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
        // create a new data array with one more row and fill it with the old data
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
        return false;
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
     * Set the row object.
     *
     * @param rowObject The row object to be set.
     * @param row The index of the row.
     */
    public void setRow(Object[] rowObject, int row)
    {
        data[row] = rowObject;
    }

	/**
	 * Retrieve the number of columns in the model.
	 *
	 * @return The number of columns in the model.
	 */
	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}
}
