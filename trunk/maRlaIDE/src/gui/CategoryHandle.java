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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A handler for a collapsable JPanel.
 *
 * @author Alex Laird
 */
public class CategoryHandle extends JPanel
{
	private boolean selected;
	ImageIcon open;
	ImageIcon close;
	JLabel label;
	Rectangle target;
	final int OFFSET = 30;
	final int PAD = 5;

	/**
	 * Create the category handle header, which contains the name of the category
	 * and the plus/minus button to collapse/uncollapse each category.
	 *
	 * @param text The name of the category.
	 * @param listener The listener for the mouse click.
	 */
	public CategoryHandle(String text, MouseListener listener) throws IOException
	{
		addMouseListener(listener);
		selected = false;
		setOpaque(false);
		setBorder(BorderFactory.createRaisedBevelBorder());

		open = new ImageIcon(getClass().getResource("/images/open_plus.png").getFile());
		close = new ImageIcon(getClass().getResource("/images/close_minus.png").getFile());
		label = new JLabel (text);
		label.setIcon(open);
		label.setFont (ViewPanel.FONT_PLAIN_12);

		setLayout (new GridLayout(1,1));
		add(label);
		target = new Rectangle (0, 0, 500, 20);
		setMaximumSize (new Dimension(500, 20));
	}

	/**
	 * Toggle selection between plus and minus.
	 */
	public void toggleSelection()
	{
		selected = !selected;
		if (selected)
		{
			label.setIcon(close);
		}
		else
		{
			label.setIcon(open);
		}
	}

	/**
	 * The selected state of the category.
	 *
	 * @return True if the category is selected, false otherwise.
	 */
	public boolean isSelected()
	{
		return selected;
	}
}
