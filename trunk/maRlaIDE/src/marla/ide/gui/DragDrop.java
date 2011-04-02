/*
 * Get Organized - Organize your schedule, course assignments, and grades
 * Copyright (C) 2011 Alex Laird
 * alexdlaird@gmail.com
 * www.alexlaird.net
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

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import marla.ide.problem.MarlaException;
import marla.ide.operation.Operation;
import marla.ide.problem.DataSet;

/**
 * The drag and drop class handles the construction and destruction of drag/drop
 * events, but its functionality is limited to assignment and event objects.
 *
 * @author Alex Laird
 */
public class DragDrop implements DragGestureListener, DragSourceListener, DropTargetListener, Transferable
{
	/** A reference to the view panel.*/
	private ViewPanel viewPanel;
	/** The supported flavors for dragging and dropping.*/
	private static final DataFlavor[] supportedFlavors =
	{
		null
	};

	/** Set the main supported flavor for dragging and dropping.*/
	static
	{
		try
		{
			supportedFlavors[0] = new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType);
		}
		catch (Exception ex)
		{
		}
	}
	/** */
	private Object object;

	/**
	 * Construct the drag and drop class with a reference to the view panel.
	 *
	 * @param viewPanel The reference to the view panel.
	 */
	public DragDrop(ViewPanel viewPanel)
	{
		this.viewPanel = viewPanel;
	}

	// Transferable methods.
	@Override
	public Object getTransferData(DataFlavor flavor)
	{
		if (flavor.isMimeTypeEqual (DataFlavor.javaJVMLocalObjectMimeType))
		{
			return object;
		}
		else
		{
			return null;
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.isMimeTypeEqual (DataFlavor.javaJVMLocalObjectMimeType);
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent ev)
	{
		try
		{
			ev.startDrag (null, this, this);
		}
		catch (Exception ex)
		{
		}
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent ev)
	{
	}

	@Override
	public void dragEnter(DragSourceDragEvent ev)
	{
	}

	@Override
	public void dragExit(DragSourceEvent ev)
	{
	}

	@Override
	public void dragOver(DragSourceDragEvent ev)
	{
		object = ev.getSource ();
		viewPanel.dragFromPalette = true;
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent ev)
	{
	}

	@Override
	public void dragEnter(DropTargetDragEvent ev)
	{
	}

	@Override
	public void dragExit(DropTargetEvent ev)
	{
	}

	@Override
	public void dragOver(DropTargetDragEvent ev)
	{
		dropTargetDrag (ev);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent ev)
	{
		dropTargetDrag (ev);
	}

	void dropTargetDrag(DropTargetDragEvent ev)
	{
		ev.acceptDrag (ev.getDropAction ());

		viewPanel.dragInWorkspace (new MouseEvent (viewPanel.workspacePanel, 0, System.currentTimeMillis (), 1, ev.getLocation ().x, ev.getLocation ().y, 1, false));
	}

	@Override
	public void drop(DropTargetDropEvent ev)
	{
		ev.acceptDrop (ev.getDropAction ());
		try
		{
			DragSourceContext source = (DragSourceContext) ev.getTransferable ().getTransferData (supportedFlavors[0]);
			if (source.getComponent().getParent() == viewPanel.dataSetContentPanel)
			{
				viewPanel.setCursor(Cursor.getDefaultCursor());
				JLabel label = (JLabel) source.getComponent();
				label.setForeground(DataSet.getDefaultColor());
				DataSet dataSet = viewPanel.domain.problem.getData(label.getText());
				if (!dataSet.isHidden())
				{
					JOptionPane.showMessageDialog(viewPanel, "This data set already exists in the workspace, so it\ncannot be added again.", "Data Set Exits", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					dataSet.isHidden(false);
					dataSet.setLocation((int) ev.getLocation().getX() - viewPanel.xDragOffset, (int) ev.getLocation().getY() - viewPanel.yDragOffset);
					viewPanel.workspacePanel.add(dataSet);
					for (int i = 0; i < dataSet.getOperationCount(); ++i)
					{
						Operation op = dataSet.getOperation(i);
						for (Operation childOp : op.getAllChildOperations())
						{
							viewPanel.workspacePanel.add(childOp);
						}
						viewPanel.workspacePanel.add(op);
					}
					viewPanel.rebuildWorkspace();
				}
			}
			else
			{
				Operation operation = (Operation) source.getComponent ();

				viewPanel.domain.problem.markUnsaved ();

				try
				{
					viewPanel.drop (operation, true, ev.getLocation ());
				}
				catch (MarlaException ex)
				{
					JOptionPane.showMessageDialog (viewPanel.domain.getTopWindow(), "Unable to load the requested operation", "Missing Operation", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		catch (UnsupportedFlavorException ex)
		{
		}
		catch (IOException ex)
		{
		}

		ev.dropComplete (true);
		viewPanel.dragFromPalette = false;
	}
}