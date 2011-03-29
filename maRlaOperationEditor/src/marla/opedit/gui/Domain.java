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

package marla.opedit.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import marla.ide.resource.BuildInfo;
import marla.ide.resource.Configuration;
import marla.opedit.operation.OperationFile;
import marla.opedit.resource.LoadSaveThread;

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
	/** The full time format for debug output.*/
	public static final SimpleDateFormat FULL_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    /** The logger holds all caught exceptions for recording in the log file.*/
    public static final List<Throwable> logger = marla.ide.gui.Domain.logger;

    /** The load/save thread that is continually running unless explicitly paused or stopped.*/
    protected LoadSaveThread loadSaveThread;
    /** The error file that keeps track of all errors and their occurrences.*/
    protected File logFile;
	/** Denotes if the log is being written. Prevents double writing */
	protected boolean isWritingLog = false;
    /** The reference to the view of the application.*/
    private ViewPanel viewPanel;
	/** The currently open operation XML file.*/
	public OperationFile operationFile = null;
	/** True if the operation XML file has been changed, false otherwise.*/
	public boolean isChanged = false;

    /**
     * Construct the domain with the view reference.
     *
     * @param viewPanel The view panel for this application.
     */
    public Domain(ViewPanel viewPanel)
    {
		this.viewPanel = viewPanel;

		Configuration conf = Configuration.getInstance();
		conf.configureFromSearch(Configuration.ConfigType.ErrorServer);
		System.out.println("Error server found at " + marla.ide.gui.Domain.getErrorServer());
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
	 * Mark the View as unsaved.
	 */
	public static void markUnsaved()
	{
		if (ViewPanel.getInstance().currentFile != null)
		{
			ViewPanel.getInstance().saveButton.setEnabled (true);
		}
	}

	/**
	 * Mark the View as saved.
	 */
	public static void markSaved()
	{
		ViewPanel.getInstance().saveButton.setEnabled (false);
	}
	
	public void flushLog()
	{
		if(isWritingLog)
			return;

		try
		{
			isWritingLog = true;
			marla.ide.gui.Domain.flushLog(logger, true, logFile, marla.ide.gui.Domain.getErrorServer(), null);
			logger.clear();
		}
		finally
		{
			isWritingLog = false;
		}
	}

	/**
	 * Retrieve the current operation XML file that is open.
	 *
	 * @return The open operation XML file.
	 */
	public OperationFile getOperationFile()
	{
		return operationFile;
	}
}
