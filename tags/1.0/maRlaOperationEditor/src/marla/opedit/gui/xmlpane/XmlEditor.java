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
/*
 * Copyright 2006-2008 Kees de Kooter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marla.opedit.gui.xmlpane;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Frame displaying the XML textpane.
 *
 * @author kees
 *
 */
public class XmlEditor extends JFrame
{
	private static final long serialVersionUID = 2623631186455160679L;

	public static void main(String[] args)
	{
		XmlEditor xmlEditor = new XmlEditor();
		xmlEditor.setVisible(true);
	}

	public XmlEditor()
	{

		super("XML Text Editor Demo");
		setSize(800, 600);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout());

		XmlTextPane xmlTextPane = new XmlTextPane();
		panel.add(xmlTextPane);

		add(panel);
	}
}
