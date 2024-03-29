/**
 * The maRla Project - Graphical problem solver for statistical calculations.
 * Copyright © 2011 Cedarville University
 * http://marla.googlecode.com
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/**
 * An extended JTable with additional functionality, including stripped rows.
 *
 * @author Alex Laird
 */
public class ExtendedJTable extends JTable
{
	/** The color to paint even rows.*/
	private static final Color EVEN_ROW_COLOR = new Color(237, 240, 242);
	/** The color to paint odd rows.*/
	private static final Color ODD_ROW_COLOR = Color.WHITE;
	/** The color to paint the table grids.*/
	private static final Color TABLE_GRID_COLOR = new Color(217, 217, 217);
	/** The cell renderer.*/
	private static final CellRendererPane CELL_RENDER_PANE = new CellRendererPane();

	/**
	 * Construct the JTable with a given table model.
	 *
	 * @param model The model to set by default.
	 */
	public ExtendedJTable(TableModel model)
	{
		super(model);

		setTableHeader(new JTableHeader(getColumnModel())
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				// Cause the table header to go clear to the far right of the table
				JViewport viewport = (JViewport) table.getParent();
				if(viewport != null && table.getWidth() < viewport.getWidth())
				{
					int x = table.getWidth();
					int width = viewport.getWidth() - table.getWidth();
					paintHeader(g, getTable(), x, width);
				}
			}
		});
		getTableHeader().setReorderingAllowed(false);
		setOpaque(false);
		setGridColor(TABLE_GRID_COLOR);
		setIntercellSpacing(new Dimension(0, 0));
		setShowGrid(false);
	}

	/**
	 * Invalidate, repaint, and resize the table after model changes have been made.
	 */
	public void refreshTable()
	{
		invalidate();
		revalidate();
		repaint();
	}

	/**
	 * Sets the selected row in the table based on an index.
	 *
	 * @param index The index of the row to be set.
	 */
	public void setSelectedRow(int index)
	{
		if(index != -1)
		{
			getSelectionModel().setSelectionInterval(index, index);
		}
		else
		{
			getSelectionModel().removeSelectionInterval(getSelectedRow(), getSelectedRow());
		}
		refreshTable();
	}

	/**
	 * Paints the given JTable's table default header background at given
	 * x for the given width.
	 */
	private static void paintHeader(Graphics g, JTable table, int x, int width)
	{
		TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
		Component component = renderer.getTableCellRendererComponent(
				table, "", false, false, -1, 2);

		component.setBounds(0, 0, width, table.getTableHeader().getHeight());

		((JComponent) component).setOpaque(false);
		CELL_RENDER_PANE.paintComponent(g, component, null, x, 0,
										width, table.getTableHeader().getHeight(), true);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row,
									 int column)
	{
		Component component = super.prepareRenderer(renderer, row, column);
		// if the rendere is a JComponent and the given row isn't part of a
		// selection, make the renderer non-opaque so that striped rows show
		// through.
		if(component instanceof JComponent)
		{
			((JComponent) component).setOpaque(getSelectionModel().isSelectedIndex(row));
		}
		return component;
	}

	/**
	 *  Override to provide Select All editing functionality
	 */
	@Override
	public boolean editCellAt(int row, int column, EventObject e)
	{
		boolean result = super.editCellAt(row, column, e);

		selectAll(e);

		NewProblemWizardDialog.changingValue = getValueAt(row, column);
		return result;
	}

	/**
	 * Select the text when editing on a text related cell is started
	 */
	private void selectAll(EventObject e)
	{
		final Component editor = getEditorComponent();

		if(editor == null
		   || !(editor instanceof JTextComponent))
			return;

		if(e == null)
		{
			((JTextComponent) editor).selectAll();
			return;
		}

		//  Typing in the cell was used to activate the editor
		if(e instanceof KeyEvent)
		{
			((JTextComponent) editor).selectAll();
			return;
		}

		//  F2 was used to activate the editor
		if(e instanceof ActionEvent)
		{
			((JTextComponent) editor).selectAll();
			return;
		}

		//  A mouse click was used to activate the editor.
		//  Generally this is a double click and the second mouse click is
		//  passed to the editor which would remove the text selection unless
		//  we use the invokeLater()
		if(e instanceof MouseEvent)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					((JTextComponent) editor).selectAll();
				}
			});
		}
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
		TableCellRenderer renderer = super.getCellRenderer(row, column);
		((DefaultTableCellRenderer) renderer).setHorizontalAlignment(SwingConstants.LEFT);
		return renderer;
	}

	/**
	 * Creates a JViewport that draws a striped background corresponding to the
	 * row positions of the given JTable.
	 */
	private static class StripedViewport extends JViewport
	{
		private final JTable table;

		public StripedViewport(JTable table)
		{
			this.table = table;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			paintStripedBackground(g);
			paintVerticalGridLines(g);
			paintHorizontalGridLines(g);
			super.paintComponent(g);
		}

		private void paintStripedBackground(Graphics g)
		{
			Rectangle clipBounds = g.getClipBounds();
			int topY = clipBounds.y;
			int rowHeight = table.getRowHeight();
			int clipYRelativeToTable = getViewPosition().y + topY;
			int currentRow = clipYRelativeToTable / rowHeight;
			int bottomY = topY + rowHeight;

			// calculate the first value of the bottom 'y' taking in count that
			// first row may be partially displayed
			bottomY -= clipYRelativeToTable % rowHeight;

			while(topY < clipBounds.y + clipBounds.height)
			{
				g.setColor(getRowColor(currentRow++));
				g.fillRect(clipBounds.x, topY, clipBounds.width, bottomY);
				topY = bottomY;
				bottomY = topY + rowHeight;
			}
		}

		@Override
		public void setViewPosition(Point p)
		{
			super.setViewPosition(p);
			repaint();
		}

		private Color getRowColor(int row)
		{
			return row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR;
		}

		private void paintHorizontalGridLines(Graphics g)
		{
			// paint the column grid dividers for the non-existent columns.
			Rectangle clipBounds = g.getClipBounds();
			int topY = clipBounds.y;
			int rowHeight = table.getRowHeight();
			int clipYRelativeToTable = getViewPosition().y + topY;
			int bottomY = topY + rowHeight;

			// calculate the first value of the bottom 'y' taking in count that
			// first row may be partially displayed
			bottomY -= clipYRelativeToTable % rowHeight;
			for(int i = 0; i < table.getRowCount(); i++)
			{
				// increase the x position by the height of a row.
				topY = bottomY;
				g.setColor(TABLE_GRID_COLOR);
				g.drawLine(0, topY - 1, table.getColumnModel().getTotalColumnWidth() - 1, topY - 1);
				bottomY = topY + rowHeight;
			}
		}

		private void paintVerticalGridLines(Graphics g)
		{
			// paint the row grid dividers for the non-existent rows.
			Rectangle clipBounds = g.getClipBounds();
			int firstX = clipBounds.x;
			int clipXRelativeToTable = getViewPosition().x + firstX;
			int lastX = firstX;
			
			// calculate the first value of the first 'x' taking in count that
			// first column may be partially displayed
			lastX -= clipXRelativeToTable;
			g.setColor(TABLE_GRID_COLOR);
			for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			{
				TableColumn column = table.getColumnModel().getColumn(i);
				// increase the x position by the width of the current column.
				firstX = lastX;
				g.drawLine(firstX - 1, g.getClipBounds().y, firstX - 1, table.getRowCount() * table.getRowHeight() - 1);
				lastX = firstX + column.getWidth();
			}
			// draw the last column line
			g.drawLine(lastX - 1, g.getClipBounds().y, lastX - 1, table.getRowCount() * table.getRowHeight() - 1);
			
		}
	}

	public static JScrollPane createStripedJScrollPane(JTable table)
	{
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setViewport(new StripedViewport(table));
		scrollPane.getViewport().setView(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER,
							 createCornerComponent(table));
		return scrollPane;
	}

	/**
	 * Creates a component that paints the header background for use in a
	 * JScrollPane corner.
	 */
	private static JComponent createCornerComponent(final JTable table)
	{
		return new JComponent()
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				paintHeader(g, table, 0, getWidth());
			}
		};
	}
}
