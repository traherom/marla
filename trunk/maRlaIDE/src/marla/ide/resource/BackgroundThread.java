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
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.JTextArea;
import marla.ide.gui.Domain;
import marla.ide.problem.MarlaException;

/**
 * The thread which continually checks for background tasks that are in need
 * of completion; for instance, a flush of the error log.
 * 
 * @author Alex Laird
 */
public class BackgroundThread extends Thread
{
	/** Saves the standard out stream */
	public static final PrintStream stdOut = System.out;
	/** The domain for the main frame.*/
	private Domain domain;
	/** If the thread is already in a load operation, a second load operation
	 * may not be called on it until the first finishes.*/
	public boolean loading = false;
	/** If the thread is already in a save operation, a second save operation
	 * may not be called on it until the first finishes.*/
	public boolean saving = false;
	/** Delay before checking if a save and/or logger write is needed */
	private final long DELAY = 500;
	/** The next time (system clock in ms) that the logger will be flushed */
	private long nextLoggerUpdate = 0;
	/** The number of logger delay iterations sat through.*/
	private int loggerDelayIndex = 0;
	/** The next time (system clock in ms) that statuses for the user will be updated */
	private long nextStatusUpdate = 0;
	/** True if a status is visible to the user */
	private boolean statusIsVisible = false;
	/** The list of status messages that have not yet been displayed.*/
	private Deque<String> statusMessages = new ArrayDeque<String>(5);
	/** Stream which will receive standard out */
	private BufferedReader redirectedOutput = null;
	/** Text area to redirect debug text to, if enabled */
	private JTextArea debugTextArea = null;
	/** Check if the thread should quit.*/
	private boolean wantToQuit;

	/**
	 * Constructs the load/save thread with a reference to the main frame and
	 * a reference to the local utility object.
	 *
	 * @param domain The domain for the main frame.
	 * @param debugTextArea Text area to redirect debug text to, if enabled
	 */
	public BackgroundThread(Domain domain, JTextArea debugTextArea)
	{
		this.domain = domain;
		this.debugTextArea = debugTextArea;
		wantToQuit = true;
	}

	/**
	 * Add a status to the queue to be displayed to the user.
	 *
	 * @param message The status message that will be displayed the user.
	 */
	public void addStatus(String message)
	{
		statusMessages.addLast(message);
	}

	/**
	 * Clear the status messages queue.
	 */
	public void clearStatus()
	{
		statusMessages.clear();
		domain.setWorkspaceStatus("");
		statusIsVisible = false;
	}

	/**
	 * Turns on redirecting System.out output to the debug console in maRla
	 */
	public void enableDebugRedirect()
	{
		try
		{
			// Redirect System.out/err to a stream that goes to the pane
			PipedOutputStream pos = new PipedOutputStream();
			PrintStream paneStream = new PrintStream(pos);
			System.setOut(paneStream);

			// Watch input stream (what the console was told to do) and write it to the textpane
			redirectedOutput = new BufferedReader(new InputStreamReader(new PipedInputStream(pos)));
		}
		catch(IOException ex)
		{
			throw new MarlaException("Unable to enable output redirection", ex);
		}
	}

	/**
	 * Turns off redirecting System.out
	 */
	public void disableDebugRedirect()
	{
		// Send console output to the normal...console
		System.setOut(stdOut);
		System.out.println("Sending debug output to console");

		if(redirectedOutput != null)
		{
			try
			{
				redirectedOutput.close();
			}
			catch(IOException ex)
			{
				Domain.logger.addLast(ex);
			}

			redirectedOutput = null;
		}
	}

	/**
	 * Returns the check delay time in milliseconds.
	 *
	 * @return Check delay.
	 */
	public long getDelay()
	{
		return DELAY;
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
	 * Starts the save thread and checks every delay interval to see if changes
	 * have been made and settings should be saved to the file.
	 */
	@Override
	public void run()
	{
		wantToQuit = false;

		try
		{
			while (!wantToQuit)
			{
				try
				{
					if(redirectedOutput != null)
					{
						synchronized(redirectedOutput)
						{
							// Wait for more data/time to check for stuff to dump out
							redirectedOutput.wait(DELAY);
						}
					}
					else
					{
						// Not redirecting, do a normal sleep
						sleep(DELAY);
					}
				}
				catch (InterruptedException ex)
				{
					Domain.logger.add (ex);
				}


				// Redirect output to text area?
				if(redirectedOutput != null)
				{
					try
					{
						// Get all available data from stream
						while(redirectedOutput.ready())
						{
							String line = redirectedOutput.readLine();
							debugTextArea.append(line + "\n");
						}

						// Scroll to end of text
						debugTextArea.setCaretPosition(debugTextArea.getDocument().getLength());
					}
					catch(IOException ex)
					{
						// Nothing to write right now
						Domain.logger.addLast(ex);
					}
				}
				
				// Time, used to check each time to see if it is ready to update
				long currTime = System.currentTimeMillis();

				// Status messages to flash to user
				if (nextStatusUpdate <= currTime)
				{
					// Hide current message?
					if(statusIsVisible)
					{
						domain.setWorkspaceStatus("");
						statusIsVisible = false;

						// Next time to update
						nextStatusUpdate = currTime + 500;
					}

					// Display status messages from an undo/redo
					if (!statusMessages.isEmpty())
					{
						domain.setWorkspaceStatus(statusMessages.removeFirst());
						statusIsVisible = true;

						// Take longer to update. IE, leave message up longer
						nextStatusUpdate = currTime + 1000;
					}
				}

				// Write log?
				if (nextLoggerUpdate <= currTime)
				{
					// Write log file in separate thread
					new Thread(new Runnable() {
						@Override
						public void run()
						{
							domain.flushLog();
						}
					}).start();

					// Next time to check for a flush if needed
					nextLoggerUpdate = currTime + 1000;
				}
			}
		}
		catch(Exception ex)
		{
			Domain.logger.addLast(ex);
			System.err.println("Background thread died unexectedly: " + ex.getMessage());
		}
		finally
		{
			disableDebugRedirect();
		}
	}

	/**
	 * Calls the respective save methods if changes have been made.
	 */
	public synchronized void save()
	{
		if (domain.getProblem () != null)
		{
			if (domain.getProblem ().isChanged ())
			{
				domain.save ();
			}
		}
	}

	/**
	 * Calls the overarching load method in the utility to load all GUI
	 * elements.
	 */
	public synchronized void load()
	{
		if (!loading)
		{
			loading = true;
			domain.load ();
			loading = false;
		}
	}
}
