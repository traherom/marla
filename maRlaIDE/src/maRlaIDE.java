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

// Intentionlly in the default package. This makes the title
// appear correctly(ish) on OS X

import gui.Domain;
import gui.MainFrame;
import gui.ProgressFrame;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import resource.BuildInfo;

/**
 * The launcher for maRla IDE.
 *
 * @author Alex Laird
 */
public class maRlaIDE
{
	/**
	 * The method responsible for constructing the visual frame and maintaining
	 * the thread as long as the frame is open.
	 *
	 * @param args The command-line arguments.
	 */
	public static void main(final String args[])
	{
		// Define UI characteristics before the applicaiton is instantiated
		try
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch(ClassNotFoundException ex)
		{
			Domain.logger.add(ex);
		}
		catch(InstantiationException ex)
		{
			Domain.logger.add(ex);
		}
		catch(IllegalAccessException ex)
		{
			Domain.logger.add(ex);
		}
		catch(UnsupportedLookAndFeelException ex)
		{
			Domain.logger.add(ex);
		}

		// Build info message
		System.out.println("Starting " + Domain.NAME + " " + Domain.VERSION + " " + Domain.PRE_RELEASE);
		System.out.println("Revision " + BuildInfo.revisionNumber + ", built " + BuildInfo.timeStamp);

		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					new MainFrame(new ProgressFrame()).setVisible(args, true);
				}
				catch(Exception ex)
				{
					UIManager.put("OptionPane.font", new Font("Verdana", Font.PLAIN, 12));
					UIManager.put("OptionPane.messageFont", new Font("Verdana", Font.PLAIN, 12));
					UIManager.put("OptionPane.buttonFont", new Font("Verdana", Font.PLAIN, 12));

					/** The option pane which can be customized to have yes/no, ok/cancel, or just ok buttons in it.*/
					final JOptionPane optionPane = new JOptionPane();
					JButton okButton = new JButton("Ok");
					okButton.setBackground(new Color(245, 245, 245));
					okButton.setFont(new Font("Verdana", Font.PLAIN, 12));
					okButton.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							optionPane.setValue(new Integer(JOptionPane.OK_OPTION));
						}
					});

					System.out.println("Error: " + ex.getClass());
					System.out.println("Message: " + ex.getMessage() + "\n--\nTrace:");
					Object[] trace = ex.getStackTrace();
					for(int j = 0; j < trace.length; ++j)
					{
						System.out.println("  " + trace[j].toString());
					}
					System.out.println();

					optionPane.setOptions(new Object[]
							{
								okButton
							});
					optionPane.setMessage("A fatal error occured while launching The maRla Project.\n"
										  + "Please contact the developer.");
					optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
					JDialog optionDialog = optionPane.createDialog("Fatal Error");
					optionDialog.setVisible(true);

					System.exit(1);
				}
			}
		});
	}
}
