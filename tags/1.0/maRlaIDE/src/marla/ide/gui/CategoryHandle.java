/*
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
	/** A reference to the view panel.*/
	private ViewPanel viewPanel;
	/** True if the category is selected, false otherwise.*/
	private boolean selected;
	/** The open icon for the category.*/
	ImageIcon open;
	/** The close icon for the category.*/
	ImageIcon close;
	/** The label for the category.*/
	JLabel label;
	/** The clickable target to open or close the category.*/
	Rectangle target;

	/**
	 * Create the category handle header, which contains the name of the category
	 * and the plus/minus button to collapse/uncollapse each category.
	 *
	 * @param viewPanel A reference to the view panel.
	 * @param text The name of the category.
	 * @param listener The listener for the mouse click.
	 */
	public CategoryHandle(ViewPanel viewPanel, String text, MouseListener listener) throws IOException
	{
		this.viewPanel = viewPanel;
		addMouseListener(listener);
		selected = false;
		setOpaque(false);
		setBorder(BorderFactory.createRaisedBevelBorder());

		open = new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "open_plus.png"));
		close = new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "close_minus.png"));
		label = new JLabel (text);
		label.setIcon(open);
		label.setFont (ViewPanel.FONT_PLAIN_12);

		setLayout (new GridLayout(1,1));
		add(label);
		target = new Rectangle (0, 0, 500, 20);
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
			if (viewPanel.showFirst)
			{
				viewPanel.showFirst = false;
				viewPanel.refreshTip();
			}
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
