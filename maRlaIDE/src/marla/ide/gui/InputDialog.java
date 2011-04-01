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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * The Input Dialog.
 *
 * @author Alex Laird
 */
public class InputDialog extends EscapeDialog
{
	private JPanel inputPanel;
	private JPanel confirmPanel;
	private JTextArea inputTextArea;
	private JLabel iconLabel;
	private JLabel messageLabel;
	private JButton okButton;
	private JButton cancelButton;
	private JScrollPane scrollPane;
	private String returnValue = "";

	/**
	 * Construct the input dialog.
	 *
	 * @param viewPanel A reference to the view panel.
	 */
	public InputDialog(ViewPanel viewPanel)
	{
		super (viewPanel);

		initComponents();
		setIconImage(new ImageIcon (getClass ().getResource ("/marla/ide/images/logo.png")).getImage ());
	}

	private void initComponents()
	{
		inputPanel = new JPanel();
		confirmPanel = new JPanel();
		inputTextArea = new JTextArea();
		messageLabel = new JLabel();
		iconLabel = new JLabel("");
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModal(true);

		messageLabel.setFont(ViewPanel.FONT_BOLD_12);
		inputTextArea.setFont(ViewPanel.FONT_PLAIN_12);
		okButton.setFont(ViewPanel.FONT_PLAIN_12);
		cancelButton.setFont(ViewPanel.FONT_PLAIN_12);
		iconLabel.setIcon (new ImageIcon(getClass().getResource("/marla/ide/images/question.png")));
		okButton.addActionListener (new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				returnValue = inputTextArea.getText ();
				dispose();
			}
		});
		cancelButton.addActionListener (new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				dispose ();
			}
		});
		inputTextArea.setLineWrap(true);
		inputTextArea.setWrapStyleWord(true);
		inputTextArea.setRows(3);
		scrollPane = new JScrollPane ();
		scrollPane.setViewportView (inputTextArea);

		inputPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.ipadx = 5;
		gbc.ipady = 5;

		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridheight = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;
		inputPanel.add(iconLabel, gbc);
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.gridheight = 1;
		inputPanel.add(messageLabel, gbc);
		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		inputPanel.add(scrollPane, gbc);

		confirmPanel.add(okButton);
		confirmPanel.add(cancelButton);

		setLayout(new GridBagLayout());
		GridBagConstraints dbc = new GridBagConstraints();
		dbc.fill = GridBagConstraints.BOTH;
		dbc.insets = new Insets(0, 5, 0, 5);

		dbc.gridx = 0;
		dbc.gridy = 0;
		dbc.weightx = 1;
		dbc.weighty = 1;
		add(inputPanel, dbc);
		dbc.gridx = 0;
		dbc.gridy = 1;
		dbc.weightx = 0;
		dbc.weighty = 0;
		add(confirmPanel, dbc);

		pack();
	}

	/**
	 * Launch the input dialog.
	 *
	 * @param parent The component to set the dialog relative to.
	 * @param title Set the title for the dialog.
	 * @param message Set the message for th dialog.
	 * @param inputText Set the initial input message for the dialog.
	 * @return The new string from the input dialog.
	 */
	protected String launchInputDialog(JComponent parent, String title, String message, String inputText)
	{
		setLocationRelativeTo(parent);
		setTitle(title);
		messageLabel.setText(message);
		inputTextArea.setText(inputText);
		returnValue = inputText;

		// Pack and show the input dialog
		pack();
		setLocationRelativeTo(viewPanel);
		inputTextArea.requestFocus();
		inputTextArea.selectAll();
		setVisible(true);

		return returnValue;
	}
}
