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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import problem.DataSet;
import operation.Operation;
import problem.DataSource;
import problem.SubProblem;

/**
 * Paints the panel for the workspace with proper lines for all shown data sets and operations.
 *
 * @author Alex Laird
 */
public class WorkspacePanel extends JPanel
{
	/** A reference to the view panel.*/
	private ViewPanel viewPanel;
	/** Increment value in sub problem line spacing.*/
	private final int SUB_INC = 5;

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
		try
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
				if (dataSet.getParent () != this)
				{
					add (dataSet);
				}
				for (int j = 0; j < dataSet.getOperationCount(); ++j)
				{
					Operation topOp = dataSet.getOperation(j);
					int x1 = (dataSet.getX () + dataSet.getX () + dataSet.getWidth ()) / 2;
					int y1 = dataSet.getY () + dataSet.getHeight ();
					int x2 = (topOp.getX () + dataSet.getOperation (j).getX () + topOp.getWidth ()) / 2;
					int y2 = topOp.getY ();
					List<SubProblem> subProblems = topOp.getSubProblems();
					if (!subProblems.isEmpty())
					{
						int decNeg = SUB_INC;
						int decX = x2;
						int incPos = SUB_INC;
						int incX = x2;
						for (int k = 0; k < subProblems.size (); ++k)
						{
							g2.setPaint(subProblems.get(k).getColor());
							if (k % 2 == 0)
							{
								x2 = decX - decNeg;
								decNeg -= SUB_INC;
								g2.draw (new Line2D.Double (x1, y1, x2, y2));
							}
							else
							{
								x2 = incX + incPos;
								incPos += SUB_INC;
								g2.draw (new Line2D.Double (x1, y1, x2, y2));
							}
						}
					}
					else
					{
						g2.setPaint(Color.DARK_GRAY);
						g2.draw (new Line2D.Double (x1, y1, x2, y2));
					}

					if (topOp.getParent () != this)
					{
						add (topOp);
					}
					List<Operation> operations = topOp.getAllChildOperations();
					if (!operations.isEmpty())
					{
						Operation op = operations.get (0);
						connectOperations (g2, op);

						x1 = (topOp.getX () + topOp.getX () + topOp.getWidth ()) / 2;
						y1 = topOp.getY () + topOp.getHeight ();
						x2 = (op.getX () + op.getX () + op.getWidth ()) / 2;
						y2 = op.getY ();
						subProblems = op.getSubProblems();
						if (!subProblems.isEmpty())
						{
							int decNeg = SUB_INC;
							int decX = x2;
							int incPos = SUB_INC;
							int incX = x2;
							for (int k = 0; k < subProblems.size (); ++k)
							{
								g2.setPaint(subProblems.get(k).getColor());
								if (k % 2 == 0)
								{
									x1 = decX - decNeg;
									x2 = decX - decNeg;
									decNeg -= SUB_INC;
									g2.draw (new Line2D.Double (x1, y1, x2, y2));
								}
								else
								{
									x1 = incX + incPos;
									x2 = incX + incPos;
									incPos += SUB_INC;
									g2.draw (new Line2D.Double (x1, y1, x2, y2));
								}
							}
						}
						else
						{
							g2.setPaint(Color.DARK_GRAY);
							g2.draw (new Line2D.Double (x1, y1, x2, y2));
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			Domain.logger.add(ex);
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
		if (operation.getParent () == null || (operation.getParent () != null && operation.getParent () != this))
		{
			add (operation);
		}
		if (operation.getOperationCount() > 0)
		{
			connectOperations (g2, operation.getOperation(0));

			int x1 = (operation.getX () + operation.getX () + operation.getWidth ()) / 2;
			int y1 = operation.getY () + operation.getHeight ();
			int x2 = (operation.getOperation (0).getX () + operation.getOperation (0).getX () + operation.getOperation (0).getWidth ()) / 2;
			int y2 = operation.getOperation (0).getY ();
			List<SubProblem> subProblems = operation.getSubProblems();
			if (subProblems.size() > 1)
			{
				int decNeg = SUB_INC;
				int decX = x2;
				int incPos = SUB_INC;
				int incX = x2;
				for (int k = 0; k < subProblems.size (); ++k)
				{
					g2.setPaint(subProblems.get(k).getColor());
					if (k % 2 == 0)
					{
						x1 = decX - decNeg;
						x2 = decX - decNeg;
						decNeg -= SUB_INC;
						g2.draw (new Line2D.Double (x1, y1, x2, y2));
					}
					else
					{
						x1 = incX + incPos;
						x2 = incX + incPos;
						incPos += SUB_INC;
						g2.draw (new Line2D.Double (x1, y1, x2, y2));
					}
				}
			}
			else
			{
				g2.setPaint(Color.DARK_GRAY);
				g2.draw (new Line2D.Double (x1, y1, x2, y2));
			}
		}
	}
}
