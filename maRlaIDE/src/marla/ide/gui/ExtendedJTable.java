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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
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
import javax.swing.table.TableCellEditor;
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
	/** The color to paint the table grids.*/
	private static final Color TABLE_GRID_COLOR = new Color(217, 217, 217);
	/** The color to paint even rows.*/
	private static final Color EVEN_ROW_COLOR = new Color(237, 240, 242);
	/** The color to paint odd rows.*/
	private static final Color ODD_ROW_COLOR = Color.WHITE;

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
		setGridColor(TABLE_GRID_COLOR);
	}

	/**
	 * Paint the table header going clear to the far right of the viewport.
	 */
	private static void paintHeader(Graphics g, JTable table, int x, int width)
	{
		TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
		Component component = renderer.getTableCellRendererComponent(
				table, "", false, false, -1, 2);

		component.setBounds(0, 0, width, table.getTableHeader().getHeight());

		((JComponent) component).setOpaque(false);
		new CellRendererPane().paintComponent(g, component, null, x, 0, width, table.getTableHeader().getHeight(), true);
	}

	/**
	 * Invalidate, repaint, and resize the table after model changes have been made.
	 */
	public void refreshTable()
	{
		invalidate();
		repaint();
		getTableHeader().resizeAndRepaint();
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
        refreshTable();
    }

	/**
	 * Add stripes between cells and behind non-opaque cells.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		// Paint background stripes
		final Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int h = getHeight() - insets.top - insets.bottom;
		final int x = insets.left;
		int y = insets.top;
		int localRowHeight = 16;
		final int nItems = getRowCount();
		for(int i = 0; i < nItems; i++, y += localRowHeight)
		{
			localRowHeight = getRowHeight(i);
			g.setColor(i % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
			g.fillRect(x, y, w, localRowHeight);
		}

		final int nRows = nItems + (insets.top + h - y) / localRowHeight;
		for(int i = nItems; i < nRows; i++, y += localRowHeight)
		{
			g.setColor(i % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
			g.fillRect(x, y, w, localRowHeight);
		}
		final int remainder = insets.top + h - y;
		if(remainder > 0)
		{
			g.setColor(remainder % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
			g.fillRect(x, y, w, remainder);
		}

		// Paint the component
		setOpaque(false);
		super.paintComponent(g);
		setOpaque(true);
	}

	/**
	 * Add background stripes behind rendered cells.
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col)
	{
		Component c;
		if(getValueAt(row, col) != null)
		{
			c = super.prepareRenderer(renderer, row, col);
		}
		else
		{
			c = super.prepareRenderer(new DefaultTableCellRenderer(), row, col);
		}

		if(!isCellSelected(row, col))
		{
			c.setForeground(Color.BLACK);
			c.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
		}
		return c;
	}

	/**
	 * Add background stripes behind edited cells.  Selects everything within
	 * a cell when it is selected.
	 */
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int col)
	{
		final Component c = super.prepareEditor(editor, row, col);
		if(!isCellSelected(row, col))
		{
			c.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
		}
		return c;
	}

	/*
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

	/*
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
	 * Force the table to fill the viewport's height.
	 */
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		final Component c = getParent();
		if(!(c instanceof JViewport))
		{
			return false;
		}
		return ((JViewport) c).getHeight() > getPreferredSize().height;
	}
	
	/**
	 * The corner looks tacky without this, so we place a "column" just above the scroll pane in the viewport
	 * @param table
	 * @return 
	 */
	public static JScrollPane createCorneredJScrollPane(final JTable table)
	{
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setViewport(new ExtendedViewport(table));
		scrollPane.getViewport().setView(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER,
							 new JComponent()
								{
									@Override
									protected void paintComponent(Graphics g)
									{
										paintHeader(g, table, 0, getWidth());
									}
								});
		return scrollPane;
	}

	/**
	 * The extended viewport, which ensures row paintings are drawn clear to the
	 * far right of the viewport.
	 */
	private static class ExtendedViewport extends JViewport
	{
		/** The table to wrap within this viewport.*/
		private final JTable table;

		/**
		 * Construct an extended viewport wrapped around the given table.
		 * 
		 * @param table The table to wrap this viewport around.
		 */
		public ExtendedViewport(JTable table)
		{
			this.table = table;
			setOpaque(false);
			initListeners();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			paintStripedBackground(g);
			paintVerticalGridLines(g);
			super.paintComponent(g);
		}

		private void paintStripedBackground(Graphics g)
		{
			int rowAtPoint = table.rowAtPoint(g.getClipBounds().getLocation());
			// Get the y-coordinate of the first row to paint
			// If there are no rows in the table, start painting at the top of the bounds
			int topY = rowAtPoint < 0 ? g.getClipBounds().y : table.getCellRect(rowAtPoint, 0, true).y;

			// Start current row at 0 if there are no rows in the table
			int currentRow = rowAtPoint < 0 ? 0 : rowAtPoint;
			while(topY < g.getClipBounds().y + g.getClipBounds().height)
			{
				int bottomY = topY + table.getRowHeight();
				g.setColor(currentRow % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
				g.fillRect(g.getClipBounds().x, topY, g.getClipBounds().width, bottomY);
				topY = bottomY;
				currentRow++;
			}
		}

		private void paintVerticalGridLines(Graphics g)
		{
			int x = 0;
			for(int i = 0; i < table.getColumnCount(); i++)
			{
				TableColumn column = table.getColumnModel().getColumn(i);

				x += column.getWidth();
				g.setColor(TABLE_GRID_COLOR);

				g.drawLine(x - 1, g.getClipBounds().y, x - 1, getHeight());
			}
		}

		/**
		 * The listener causes the table to repaint whenever a column is resized.
		 */
		private void initListeners()
		{
			for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			{
				table.getColumnModel().getColumn(i).addPropertyChangeListener(new PropertyChangeListener()
				{
					@Override
					public void propertyChange(PropertyChangeEvent evt)
					{
						repaint();
					}
				});
			}
		}
	}
}
