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

import java.io.File;
import java.util.ArrayList;
import resource.LoadSaveThread;

/**
 *
 * @author Alex Laird
 */
public class Domain
{
    /** The name of the application.*/
    public static final String NAME = "The maRla Project - Operation Editor";
    /** The version number of the application.*/
    public static final String VERSION = "0.01";
    /** The pre-release version name of the application.*/
    public static final String PRE_RELEASE = "Alpha";
    /** The location of the application as it runs.*/
    public static final String CWD = System.getProperty("user.dir");
    /** The name of the operating system being used.*/
    public static final String OS_NAME = System.getProperty("os.name");
    /** The home directory for the current user.*/
    public static final String HOME_DIR = System.getProperty("user.home");
    /** The logger holds all caught exceptions for recording in the log file.*/
    public static final ArrayList<Exception> logger = new ArrayList<Exception>();

    /** The load/save thread that is continually running unless explicitly paused or stopped.*/
    protected LoadSaveThread loadSaveThread;
    /** The error file that keeps track of all errors and their occurrences.*/
    protected File logFile;
    /** The reference to the view of the application.*/
    private ViewPanel viewPanel;

    /**
     * Construct the domain with the view reference.
     *
     * @param viewPanel The view panel for this application.
     */
    public Domain(ViewPanel viewPanel)
    {
            this.viewPanel = viewPanel;
    }
    
    /**
     * Passes the reference to the load/save thread into this class.
     *
     * @param loadSaveThread The load/save thread to be used.
     */
    public void setLoadSaveThread(LoadSaveThread loadSaveThread)
    {
            this.loadSaveThread = loadSaveThread;
    }

    /**
     * 
     */
    public void load()
    {

    }
}
