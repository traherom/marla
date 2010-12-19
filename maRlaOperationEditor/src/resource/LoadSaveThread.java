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

package resource;

import gui.ViewPanel;
import gui.Domain;

/**
 * The thread which continually checks if changes have been made and need to be
 * saved.
 * 
 * @author Alex Laird
 */
public class LoadSaveThread extends Thread
{
    /** The main frame of the application.*/
    private ViewPanel viewPanel;
    /** The domain for the main frame.*/
    private Domain domain;
    /** If the thread is already in a load operation, a second load operation
     * may not be called on it until the first finishes.*/
    public boolean loading = false;
    /** If the thread is already in a save operation, a second save operation
     * may not be called on it until the first finishes.*/
    public boolean saving = false;
    /** Auto-save, if save is needed, every three minutes.*/
    private final long delay = 180000;
    /** Check if the thread should quit.*/
    private boolean wantToQuit;

    /**
     * Constructs the load/save thread with a reference to the main frame and
     * a reference to the local utility object.
     *
     * @param viewPanel A reference to the main frame of the application.
     * @param domain The domain for the main frame.
     */
    public LoadSaveThread(ViewPanel viewPanel, Domain domain)
    {
        this.viewPanel = viewPanel;
        this.domain = domain;
        wantToQuit = true;
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
                sleep (delay);
            }
            catch (InterruptedException ex)
            {
                Domain.logger.add (ex);
            }

            save ();
        }
    }

    /**
     * Calls the respective save methods if changes have been made.
     */
    public synchronized void save()
    {

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
