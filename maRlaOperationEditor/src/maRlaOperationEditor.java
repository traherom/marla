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

// Intentionlly in the default package. This makes the title
// appear correctly(ish) on OS X

import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import marla.opedit.gui.Domain;
import marla.opedit.gui.MainFrame;

/**
 * The launcher for maRla Operation Editor.
 *
 * @author Alex Laird
 */
public class maRlaOperationEditor
{
	/**
	 * The method responsible for constructing the visual frame and maintaining
	 * the thread as long as the frame is open.
	 *
	 * @param args The command-line arguments.
	 */
	public static void main(final String args[])
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				Domain.logger.add(e);
			}
		});
		
		// Define UI characteristics before the application is instantiated
		try
		{
			if (System.getProperty ("os.name").toLowerCase().contains ("windows"))
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else
			{
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
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

		final marla.ide.gui.ProgressFrame progressFrame = new marla.ide.gui.ProgressFrame();

		progressFrame.setLocationRelativeTo(null);
		progressFrame.setTitle("Launching maRla Operation Editor");
		progressFrame.setVisible(true);
		progressFrame.progressBar.setValue(0);
		progressFrame.progressBar.setString("0%");
		progressFrame.statusLabel.setText("Loading framework...");

		progressFrame.progressBar.setValue(3);
		progressFrame.progressBar.setString("3%");

		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					new MainFrame(progressFrame).setVisible(args, true);
				}
				catch(Exception ex)
				{
					System.out.println("Error: " + ex.getClass());
					System.out.println("Message: " + ex.getMessage() + "\n--\nTrace:");
					Object[] trace = ex.getStackTrace();
					for(int j = 0; j < trace.length; ++j)
					{
						System.out.println("  " + trace[j].toString());
					}
					System.out.println();

					progressFrame.setAlwaysOnTop(false);
					marla.ide.gui.Domain.showErrorDialog(Domain.getTopWindow(), "A fatal error occured while launching the maRla Operation Editor.\nPlease contact the developer.", marla.ide.gui.Domain.prettyExceptionDetails(ex), "Fatal Error");

					System.exit(1);
				}
			}
		});

		progressFrame.progressBar.setValue(5);
		progressFrame.progressBar.setString("5%");
	}
}
