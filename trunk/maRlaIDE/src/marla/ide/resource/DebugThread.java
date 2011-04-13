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

package marla.ide.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import marla.ide.gui.Domain;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * Handles redirection of standard out to the given text area
 * @author Ryan Morehart
 */
public class DebugThread extends Thread
{
	/** Default console output location */
	private static final PrintStream stdOut = System.out;
	/** Text area to redirect debug text to, if enabled */
	private final JTextArea debugTextArea;
	/** Denotes whether to send output to debug console (true) or standard out (false). */
	private boolean doRedirect;
	/** Stream which will receive standard out */
	private final BufferedReader redirectedOutput;
	/** Check if the thread should quit.*/
	private boolean wantToQuit;

	public DebugThread(JTextArea textArea)
	{
		this.debugTextArea = textArea;

		// Setup stream to receive data from standard out
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis;
		try
		{
			pis = new PipedInputStream(pos);
		}
		catch(IOException ex)
		{
			doRedirect = false;
			pis = null;
			Domain.logger.add(ex);
			System.err.println("Unable to redirect output");
		}

		// If creation of the pipe failed, just leave output going to the console
		if(pis != null)
		{
			redirectedOutput = new BufferedReader(new InputStreamReader(pis));

			// Redirect standard out to go to both the console and our reading pipe
			System.setOut(new PrintStream(new TeeOutputStream(stdOut, pos), true));
		}
		else
			redirectedOutput = null;
	}

	/**
	 * Sets the quit state of the thread to true, so it will not execute its
	 * actions after each delay. It can be set back to running by calling run()
	 * at any time.
	 */
	public void stopRunning()
	{
		wantToQuit = true;
	}

	/**
	 * Turns on redirecting System.out output to the debug console in maRla
	 */
	public void enableDebugRedirect()
	{
		doRedirect = true;
		System.out.println("Sending output to debug pane");
	}

	/**
	 * Turns off redirecting System.out
	 */
	public void disableDebugRedirect()
	{
		// Send console output to the normal...console
		doRedirect = false;
		System.out.println("Sending output to console");
	}
	
	@Override
	public void run()
	{
		try
		{
			wantToQuit = false;
			
			while (!wantToQuit)
			{
				try
				{
					synchronized(redirectedOutput)
					{
						// Wait for more data/time to check for stuff to dump out
						redirectedOutput.wait(500);

						try
						{
							// Get all available data from stream
							boolean didOutput = false;
							while(redirectedOutput.ready())
							{
								String line = redirectedOutput.readLine();
								didOutput = true;

								// Send it to the right place
								if(doRedirect)
									debugTextArea.append(line + "\n");
							}

							// Scroll to end of text if applicable
							if(didOutput)
								debugTextArea.setCaretPosition(debugTextArea.getDocument().getLength());
						}
						catch(IOException ex)
						{
							Domain.logger.add(ex);
						}
					}
				}
				catch (InterruptedException ex)
				{
					Domain.logger.add (ex);
				}
			}
		}
		finally
		{
			// Ensure nothing hangs
			System.setOut(stdOut);
		}
	}
}
