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

import java.util.ArrayList;
import marla.ide.gui.Domain;

/**
 * The thread which continually checks for background tasks that are in need
 * of completion; for instance, a flush of the error log.
 * 
 * @author Alex Laird
 */
public class BackgroundThread extends Thread
{
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
	/** The number of delay iterations to sit through before activating logger.*/
	private final int LOGGER_DELAY = 20;
	/** The number of logger delay iterations sat through.*/
	private int loggerDelayIndex = 0;
	/** Delay increments to leave a status message for in the workspace.*/
	private final int STATUS_DELAY = 3;
	/** The number of display iterations already sat through.  -1 if not waiting for a display to finish.*/
	private int statusDelayIndex = -1;
	/** The list of status messages that have not yet been displayed.*/
	private ArrayList<String> statusMessages = new ArrayList<String>();
	/** Check if the thread should quit.*/
	private boolean wantToQuit;

	/**
	 * Constructs the load/save thread with a reference to the main frame and
	 * a reference to the local utility object.
	 *
	 * @param domain The domain for the main frame.
	 */
	public BackgroundThread(Domain domain)
	{
		this.domain = domain;
		wantToQuit = true;
	}

	/**
	 * Add a status to the queue to be displayed to the user.
	 *
	 * @param message The status message that will be displayed the user.
	 */
	public void addStatus(String message)
	{
		statusMessages.add(message);
	}

	/**
	 * Clear the status messages queue.
	 */
	public void clearStatus()
	{
		statusMessages.clear();
		domain.setWorkspaceStatus("");
		statusDelayIndex = -1;
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

		while (!wantToQuit)
		{
			try
			{
				sleep (DELAY);
			}
			catch (InterruptedException ex)
			{
				Domain.logger.add (ex);
			}

			if (statusDelayIndex == -1)
			{
				// Display status messages from an undo/redo
				if (!statusMessages.isEmpty())
				{
					domain.setWorkspaceStatus(statusMessages.remove(statusMessages.size() - 1));
					statusDelayIndex = 0;
				}
			}
			else if (statusDelayIndex != STATUS_DELAY)
			{
				++statusDelayIndex;
			}
			else
			{
				domain.setWorkspaceStatus("");
				statusDelayIndex = -1;
			}

			if (loggerDelayIndex == LOGGER_DELAY)
			{
				// Write log file
				domain.flushLog();
				loggerDelayIndex = 0;
			}
			else
			{
				++loggerDelayIndex;
			}
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
