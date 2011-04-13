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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JPanel;
import marla.ide.problem.DataSource;
import marla.ide.problem.SubProblem;
import marla.ide.resource.Configuration.ConfigType;
import marla.ide.resource.ConfigurationException;

/**
 * Paints the panel for the workspace with proper lines for all shown data sets and operations.
 *
 * @author Alex Laird
 */
public class WorkspacePanel extends JPanel
{
	/** Stroke width of lines on panel */
	private static int minLineWidth = 2;
	/** Spacing between lines on panel */
	private static int lineSpacing = 4;
	/** A reference to the view panel.*/
	private ViewPanel viewPanel;
	/**
	 * Increment value in sub problem line spacing. Set based on line width
	 * and line spacing
	 */
	private int lineInc = 0;

	/**
	 * Empty constructor for Bean display.
	 */
	public WorkspacePanel() {}
	
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
	 * Sets the minimum draw width for the lines on the workspace
	 * @param newMin Size in pixels for the lines. Must be 1 or greater
	 * @return Previously set line width
	 */
	public static int setMinLineWidth(int newMin) 
	{
		int oldMin = minLineWidth;

		if(newMin < 1)
			throw new ConfigurationException("Minimum line width must be 1 or greater", ConfigType.MinLineWidth);

		minLineWidth = newMin;
		return oldMin;
	}

	/**
	 * Gets the minimum draw width for the lines on the workspace in pixels
	 * @return Currently set width
	 */
	public static int getMinLineWidth()
	{
		return minLineWidth;
	}

	/**
	 * Sets the spacing between lines on the workspace
	 * @param newSpace Size in pixels between the lines. Must be 0 or greater
	 * @return Previously set line spacing
	 */
	public static int setLineSpacing(int newSpace) 
	{
		int oldSpace = lineSpacing;

		if(newSpace < 0)
			throw new ConfigurationException("Line spacing must be 0 or greater", ConfigType.LineSpacing);

		lineSpacing = newSpace;
		return oldSpace;
	}

	/**
	 * Gets the line spacing for the workspace in pixels
	 * @return Currently set spacing
	 */
	public static int getLineSpacing()
	{
		return lineSpacing;
	}

	/**
	 * Returns the component located at the given coordinates, ignoring the component
	 * specified in ignore.  ignore can be passed as null if the user doesn't care.
	 * If no component is found at the given point, null is returned.
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @param ignore The component to ignore, if any.
	 * @return The component at the given coordinates, or null if none exists.
	 */
	public Component getComponentAt(int x, int y, Component ignore)
	{
		for (Component comp : getComponents())
		{
			if (comp.getBounds().contains(x, y) &&
					comp != this &&
					comp != ignore)
			{
				return comp;
			}
		}

		return null;
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

			// Make the lines larger as we zoom in
			int lineWidth = ViewPanel.fontSize - 11;
			if(lineWidth < minLineWidth)
				lineWidth = minLineWidth;
			lineInc = lineWidth + lineSpacing;
			g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			// Work across each DataSource that we know about
			List<DataSource> data = viewPanel.domain.problem.getVisibleData();
			for(DataSource ds : data)
			{
				drawConnection(g2, ds);
			}

			// Always ensure low-level components are at the bottom of the workspace
			if(viewPanel.trashCan.getParent() == this)
			{
				setComponentZOrder(viewPanel.trashCan, getComponentCount() - 1);
			}
			if(viewPanel.statusLabel.getParent() == this)
			{
				setComponentZOrder(viewPanel.statusLabel, getComponentCount() - 1);
			}
			if(viewPanel.firstRunLabel.getParent() == this)
			{
				setComponentZOrder(viewPanel.firstRunLabel, getComponentCount() - 1);
			}
			if(viewPanel.draggingComponent != null && viewPanel.draggingComponent.getParent() == this)
			{
				setComponentZOrder(viewPanel.draggingComponent, 0);
			}
		}
		catch(Exception ex)
		{
			Domain.logger.addLast(ex);
		}
	}

	/**
	 * Draw a connection between the given data source and its parent.
	 *
	 * @param g2 The graphics reference.
	 * @param ds The data source to draw a line to.
	 */
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
		int endX = midEndX - subs.size() * lineInc / 2;
		if(!subs.isEmpty())
			endX += lineInc / 2;

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
				endX += lineInc;
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
