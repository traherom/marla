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
 * Paints the panel for the workspace with proper lines for all shown data sets and operations.
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

		if (!dataSets.isEmpty() && viewPanel.draggingComponent == null)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
								 RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setPaint (Color.DARK_GRAY);
			for (int i = 0; i < dataSets.size (); ++i)
			{
				DataSet dataSet = dataSets.get (i);
				for (int j = 0; j < dataSet.getOperationCount(); ++j)
				{
					int x1 = (dataSet.getX () + dataSet.getX () + dataSet.getWidth ()) / 2;
					int y1 = dataSet.getY () + dataSet.getHeight ();
					int x2 = (dataSet.getOperation (j).getX () + dataSet.getOperation (j).getX () + dataSet.getOperation (j).getWidth ()) / 2;
					int y2 = dataSet.getOperation (j).getY ();
					g2.draw (new Line2D.Double (x1, y1, x2, y2));

					List<Operation> operations = dataSet.getOperation (j).getAllChildOperations();
					for (int k = 0; k < operations.size (); ++k)
					{
						if (k == 0)
						{
							x1 = (dataSet.getOperation (j).getX () + dataSet.getOperation (j).getX () + dataSet.getOperation (j).getWidth ()) / 2;
							y1 = dataSet.getOperation (j).getY () + dataSet.getOperation (j).getHeight ();
							x2 = (operations.get (k).getX () + operations.get (k).getX () + operations.get (k).getWidth ()) / 2;
							y2 = operations.get (k).getY ();
						}
						else
						{
							x1 = (operations.get (k - 1).getX () + operations.get (k - 1).getX () + operations.get (k - 1).getWidth ()) / 2;
							y1 = operations.get (k - 1).getY () + operations.get (k - 1).getHeight ();
							x2 = (operations.get (k).getX () + operations.get (k).getX () + operations.get (k).getWidth ()) / 2;
							y2 = operations.get (k).getY ();
						}
						g2.draw (new Line2D.Double (x1, y1, x2, y2));
					}

					//List<Operation> operations = dataSet.getOperation (j).getAllChildOperations();
					for (int k = 0; k < operations.size(); ++k)
					{
						if (j == 0)
						{
							x1 = (dataSet.getOperation (j).getX () + dataSet.getOperation (j).getX () + dataSet.getOperation (j).getWidth ()) / 2;
							y1 = dataSet.getOperation (j).getY () + dataSet.getOperation (j).getHeight();
							x2 = (operations.get (k).getX () + operations.get (k).getX () + operations.get (k).getWidth ()) / 2;
							y2 = operations.get (k).getY ();
						}
						else
						{
							x1 = (operations.get (k - 1).getOperation (j).getX () + operations.get (k - 1).getX () + operations.get (k - 1).getWidth ()) / 2;
							y1 = operations.get (k - 1).getOperation (j).getY () + operations.get (k - 1).getHeight();
							x2 = (operations.get (k).getX () + operations.get (k).getX () + operations.get (k).getWidth ()) / 2;
							y2 = operations.get (k).getY ();
						}
						g2.draw (new Line2D.Double (x1, y1, x2, y2));
					}
				}
			}
		}
	}
}
