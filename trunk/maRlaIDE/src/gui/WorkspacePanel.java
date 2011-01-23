/*
 * The maRla Project - Graphical problem solver for statistics and probability problems.
 * Copyright (C) 2010 Cedarville University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *buid
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import problem.DataSet;
import problem.Operation;

/**
 * 
 *
 * @author Alex Laird
 */
public class WorkspacePanel extends JPanel
{
	/** */
	ViewPanel viewPanel;
	/** */
	ArrayList<DataSet> dataSets = new ArrayList<DataSet> ();

	public WorkspacePanel(ViewPanel viewPanel)
	{
		this.viewPanel = viewPanel;
	}

	/**
	 *
	 * @param dataSet
	 */
	public void addDataSet (DataSet dataSet)
	{
		dataSets.add (dataSet);
	}

	/**
	 *
	 * @param dataSet
	 */
	public void removeDataSet (DataSet dataSet)
	{
		dataSets.remove (dataSet);
	}

	/**
	 * 
	 * @param g
	 */
	@Override
    protected void paintComponent(Graphics g)
	{
		super.paintComponent (g);

		if (!dataSets.isEmpty() && viewPanel.dataSetDragging == null && viewPanel.operationDragging == null)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = 0; i < dataSets.size (); ++i)
			{
				g2.setPaint (Color.DARK_GRAY);
				DataSet dataSet = dataSets.get (i);
				List<Operation> childOperations = dataSet.getAllChildOperations();
				for (int j = 0; j < childOperations.size (); ++j)
				{
					Operation firstOperation = null;
					Operation secondOperation = null;
					if (j == 0)
					{
						firstOperation = childOperations.get (j);
					}
					else
					{
						firstOperation = childOperations.get (j - 1);
						secondOperation = childOperations.get (j);
					}

					int x1 = -1;
					int x2 = -1;
					int y1 = -1;
					int y2 = -1;
					if (secondOperation == null)
					{
						x1 = (dataSet.getX () + dataSet.getX () + dataSet.getWidth ()) / 2;
						y1 = dataSet.getY () + dataSet.getHeight ();
						x2 = (firstOperation.getX () + firstOperation.getX () + firstOperation.getWidth ()) / 2;
						y2 = firstOperation.getY ();
					}
					else
					{
						x1 = (firstOperation.getX () + firstOperation.getX () + firstOperation.getWidth ()) / 2;
						y1 = firstOperation.getY () + firstOperation.getHeight();
						x2 = (secondOperation.getX () + secondOperation.getX () + secondOperation.getWidth ()) / 2;
						y2 = secondOperation.getY ();
					}
					g2.draw (new Line2D.Double (x1, y1, x2, y2));
				}
				g2.setPaint (Color.WHITE);
			}
		}
	}
}
