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

package marla.opedit.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * An extended JTable with additional functionality, including stripped rows.
 *
 * @author Alex Laird
 */
public class ExtendedJTable extends JTable
{
	/** The alternating row colors.*/
	private Color rowColors[] = new Color[2];
	/** True if stripes should be drawn, false otherwise.*/
	private boolean drawStripes = false;

	/**
	 * Construct the JTable with a given table model.
	 *
	 * @param model The model to set by default.
	 */
	public ExtendedJTable(TableModel model)
	{
		super (model);
	}

	/**
	 * Invalidate, repaint, and resize the table after model changes have been made.
	 */
	public void refreshTableUI()
	{
		invalidate();
		repaint();
		getTableHeader().resizeAndRepaint();
	}

	/**
	 * Add a new column first to the data model, then using the JTable's add column default method.
	 *
	 * @param column The column to be added.
	 */
	@Override
	public void addColumn(TableColumn column)
	{
		((ExtendedTableModel) getModel()).addColumn(column.getHeaderValue().toString());
		super.addColumn(column);
	}

	/**
	 * Sets the selected row in the table based on an index.
	 *
	 * @param index The index of the row to be set.
	 */
	public void setSelectedRow(int index)
	{
		if (index != -1)
		{
			getSelectionModel ().setSelectionInterval (index, index);
		}
		else
		{
			getSelectionModel ().removeSelectionInterval (getSelectedRow (), getSelectedRow ());
		}
	}

	/**
	 * Add stripes between cells and behind non-opaque cells.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		if (!(drawStripes = isOpaque ()))
		{
			super.paintComponent (g);
			return;
		}

		// paint background stripes
		updateColors ();
		final Insets insets = getInsets ();
		final int w = getWidth () - insets.left - insets.right;
		final int h = getHeight () - insets.top - insets.bottom;
		final int x = insets.left;
		int y = insets.top;
		int localRowHeight = 16;
		final int nItems = getRowCount ();
		for (int i = 0; i < nItems; i++, y += localRowHeight)
		{
			localRowHeight = getRowHeight (i);
			g.setColor (rowColors[i & 1]);
			g.fillRect (x, y, w, localRowHeight);
		}

		final int nRows = nItems + (insets.top + h - y) / localRowHeight;
		for (int i = nItems; i < nRows; i++, y += localRowHeight)
		{
			g.setColor (rowColors[i & 1]);
			g.fillRect (x, y, w, localRowHeight);
		}
		final int remainder = insets.top + h - y;
		if (remainder > 0)
		{
			g.setColor (rowColors[nRows & 1]);
			g.fillRect (x, y, w, remainder);
		}

		// paint compoent
		setOpaque (false);
		super.paintComponent (g);
		setOpaque (true);
	}

	/**
	 * Add background stripes behind rendered cells.
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col)
	{
		Component c = null;
		if (getValueAt (row, col) != null)
		{
			c = super.prepareRenderer (renderer, row, col);
		}
		else
		{
			c = super.prepareRenderer (new DefaultTableCellRenderer (), row, col);
		}

		if (drawStripes && !isCellSelected (row, col))
		{
			c.setBackground (rowColors[row & 1]);
		}
		return c;
	}

	/**
	 * Retrieves the cell renderer for the specific cell.
	 *
	 * @param row The row to retrieve the renderer for.
	 * @param column The column to retrieve the renderer for.
	 * @return The cell renderer for the specific cell.
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		TableCellRenderer renderer = super.getCellRenderer (row, column);
		((DefaultTableCellRenderer) renderer).setHorizontalAlignment (SwingConstants.LEFT);
		return renderer;
	}

	/**
	 * Force the table to fill the viewport's height.
	 */
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		final Component c = getParent ();
		if (!(c instanceof JViewport))
		{
			return false;
		}
		return ((JViewport) c).getHeight () > getPreferredSize ().height;
	}

	/**
	 * Updates the colors accordingly for the cell for odd and even.
	 */
	private void updateColors()
	{
		rowColors[0] = Color.WHITE;
		rowColors[1] = new Color (237, 240, 242);
	}
}
