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
	/** A reference to the view panel.*/
	ViewPanel viewPanel;

	/**
	 * Construct the workspace panel with a reference to the view panel.
	 *
	 * @param viewPanel The reference to the view panel.
	 */
	public WorkspacePanel(ViewPanel viewPanel)
	{
		this.viewPanel = viewPanel;
	}

	/**
	 * Paint all data sets and operations properly with connecting lines in the workspace panel.
	 *
	 * @param g The graphics of the panel.
	 */
	@Override
    protected void paintComponent(Graphics g)
	{
		super.paintComponent (g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
							 RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint (Color.DARK_GRAY);

		// Iterate through each data set and draw to the first operation of each column
		for (int i = 0; i < viewPanel.domain.problem.getDataCount(); ++i)
		{
			DataSet dataSet = viewPanel.domain.problem.getData (i);
			for (int j = 0; j < dataSet.getOperationCount(); ++j)
			{
				int x1 = (dataSet.getX () + dataSet.getX () + dataSet.getWidth ()) / 2;
				int y1 = dataSet.getY () + dataSet.getHeight ();
				int x2 = (dataSet.getOperation (j).getX () + dataSet.getOperation (j).getX () + dataSet.getOperation (j).getWidth ()) / 2;
				int y2 = dataSet.getOperation (j).getY ();
				g2.draw (new Line2D.Double (x1, y1, x2, y2));

				List<Operation> operations = dataSet.getOperation (j).getAllChildOperations();
				if (operations.size () > 0)
				{
					connectOperations (g2, operations.get (0));

					x1 = (dataSet.getOperation (j).getX () + dataSet.getOperation (j).getX () + dataSet.getOperation (j).getWidth ()) / 2;
					y1 = dataSet.getOperation (j).getY () + dataSet.getOperation (j).getHeight ();
					x2 = (operations.get (0).getX () + operations.get (0).getX () + operations.get (0).getWidth ()) / 2;
					y2 = operations.get (0).getY ();
					g2.draw (new Line2D.Double (x1, y1, x2, y2));
				}
			}
		}
	}

	/**
	 * Recursively connect child operations as long as they exist.
	 *
	 * @param g2 The graphics object to draw lines with.
	 * @param operation The current operation to connect with its next child operation.
	 */
	private void connectOperations(Graphics2D g2, Operation operation)
	{
		if (operation.getOperationCount() > 0)
		{
			connectOperations (g2, operation.getOperation(0));

			int x1 = (operation.getX () + operation.getX () + operation.getWidth ()) / 2;
			int y1 = operation.getY () + operation.getHeight ();
			int x2 = (operation.getOperation (0).getX () + operation.getOperation (0).getX () + operation.getOperation (0).getWidth ()) / 2;
			int y2 = operation.getOperation (0).getY ();
			g2.draw (new Line2D.Double (x1, y1, x2, y2));
		}
	}
}
