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
import javax.swing.JPanel;
import problem.DataSource;
import problem.Problem;
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

			// Work across each DataSource that we know about
			Problem prob = viewPanel.domain.problem;
			List<DataSource> data = prob.getAllData();
			for(DataSource ds : data)
			{
				// Ensure we're part of the workspace
				if(ds.getParent() != this)
					add(ds);

				drawConnection(g2, ds);
			}
		}
		catch(Exception ex)
		{
			Domain.logger.add(ex);
		}
	}

	private void drawConnection(Graphics2D g2, DataSource ds)
	{		
		// Stuff we'll need to reference a lot
		DataSource parentDS = ds.getParentData();
		if(parentDS == null)
			return;

		// If we're the only child of our parent, draw straight downward
		boolean isStraight = false;
		if(parentDS.getOperationCount() == 1)
			isStraight = true;

		List<SubProblem> subs = ds.getSubProblems();
		
		// Figure out where lines will end at
		int endY = ds.getY();

		// Back the start of the X's up a bit if we
		// have multiple subproblems
		int midEndX = ds.getX() + (ds.getWidth() / 2);
		int endX = midEndX - subs.size() * SUB_INC / 2;

		// Figure out where lines will start from
		int startY = parentDS.getY() + parentDS.getHeight();

		int midStartX = parentDS.getX() + (parentDS.getWidth() / 2);
		int startX = midStartX;
		if(isStraight)
		{
			// If we're vertically straight, the lines should go straight
			// down. Otherwise, start from our parent mid no matter what
			startX = endX;
		}

		if(!subs.isEmpty())
		{
			// Draw each subproblem line
			for(SubProblem sub : subs)
			{
				g2.setPaint(sub.getColor());
				g2.draw(new Line2D.Double(startX, startY, endX, endY));

				// Move lines
				endX += SUB_INC;
				if(isStraight)
					startX = endX;
			}
		}
		else
		{
			g2.setPaint(Color.DARK_GRAY);
			g2.draw(new Line2D.Double(startX, startY, endX, endY));
		}
	}
}
