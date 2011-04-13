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

import java.awt.Container;
import java.awt.Desktop;
import java.io.File;
import java.util.Queue;
import javax.swing.SwingUtilities;
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
    public static final String NAME = "maRla Operation Editor";
    /** The version number of the application.*/
    public static final String VERSION = "0.2";
    /** The pre-release version name of the application.*/
    public static final String PRE_RELEASE = "Beta";
    /** The location of the application as it runs.*/
    public static final String CWD = System.getProperty("user.dir");
    /** The name of the operating system being used.*/
    public static final String OS_NAME = System.getProperty("os.name");
    /** The home directory for the current user.*/
    public static final String HOME_DIR = System.getProperty("user.home");
    /** The logger holds all caught exceptions for recording in the log file.*/
    public static final Queue<Throwable> logger = marla.ide.gui.Domain.logger;
	/** If launched with a file to open, this will be the file set.*/
	public static File passedInFile = null;

    /** The load/save thread that is continually running unless explicitly paused or stopped.*/
    protected LoadSaveThread loadSaveThread;
    /** The error file that keeps track of all errors and their occurrences.*/
    protected File logFile;
	/** Denotes if the log is being written. Prevents double writing */
	protected boolean isWritingLog = false;
	/** The currently open operation XML file.*/
	public OperationFile operationFile = null;
	/** True if the operation XML file has been changed, false otherwise.*/
	public boolean isChanged = false;
	/** A reference to the desktop.*/
	protected Desktop desktop = null;

    /**
     * Construct the domain with the view reference.
     *
     * @param viewPanel The view panel for this application.
     */
    public Domain(ViewPanel viewPanel)
    {
		Configuration conf = Configuration.getInstance();
		conf.configureFromSearch(Configuration.ConfigType.ErrorServer);
		System.out.println("Server for error reporting: " + marla.ide.gui.Domain.getErrorServer());

		// If the Desktop object is supported, get the reference
		if (Desktop.isDesktopSupported ())
		{
			desktop = Desktop.getDesktop ();
		}
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
	 * Validates the undo/redo menu items in MainFrame to see if they should be
	 * enabled or disabled.
	 */
	public void validateUndoRedoMenuItems()
	{
		ViewPanel viewPanel = ViewPanel.getInstance();
		if (viewPanel.undoRedo.hasUndo())
		{
			viewPanel.mainFrame.undoMenuItem.setEnabled(true);
		}
		else
		{
			viewPanel.mainFrame.undoMenuItem.setEnabled(false);
		}
		if (viewPanel.undoRedo.hasRedo())
		{
			viewPanel.mainFrame.redoMenuItem.setEnabled(true);
		}
		else
		{
			viewPanel.mainFrame.redoMenuItem.setEnabled(false);
		}
	}

	/**
	 * Checks what window is the top-most being displayed right now and returns that.
	 *
	 * @return The top-most window displayed.
	 */
	public Container getTopWindow()
	{
		if (MainFrame.progressFrame.isVisible())
		{
			return MainFrame.progressFrame;
		}
		else if (ViewPanel.getInstance().answerDialog.isVisible())
		{
			return ViewPanel.getInstance().answerDialog;
		}
		else
		{
			return ViewPanel.getInstance();
		}
	}

	/**
	 * Marks that a change is beginning, so the step should be saved in undo/redo.
	 */
	public static void changeBeginning()
	{
		ViewPanel viewPanel = ViewPanel.getInstance();
		if (viewPanel != null && viewPanel.currentOperation != null)
		{
			ViewPanel.getInstance().undoRedo.addUndoStep (ViewPanel.getInstance().currentOperation.clone());
		}

		viewPanel.domain.validateUndoRedoMenuItems();
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

	/**
	 * Show or hide the progress bar. Must be set to false if you wish
	 * to set values manually.
	 *
	 * @param visible True for an indeterminate progress bar, false otherwise.
	 */
	public static void setProgressVisible(final boolean visible)
	{
		if(MainFrame.progressFrame != null)
		{
			MainFrame.progressFrame.setLocationRelativeTo(ViewPanel.getInstance().mainFrame);
			MainFrame.progressFrame.setVisible(visible);
		}
	}

	/**
	 * Enable or disable the indeterminate state of the progress bar. Must be
	 * set to false if you wish to set values manually.
	 *
	 * @param indeterminate True for an indeterminate progress bar, false otherwise.
	 */
	public static void setProgressIndeterminate(final boolean indeterminate)
	{
		if(MainFrame.progressFrame != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MainFrame.progressFrame.progressBar.setIndeterminate(indeterminate);
				}
			});
		}
	}

	/**
	 * Set the string shown within the progress bar.
	 *
	 * @param string The string shown within the progress bar.
	 */
	public static void setProgressString(final String string)
	{
		if(MainFrame.progressFrame != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MainFrame.progressFrame.progressBar.setString(string);
				}
			});
		}
	}

	/**
	 * Set the status label below the progress bar.
	 *
	 * @param status The status to be set.
	 */
	public static void setProgressStatus(String status)
	{
		if(MainFrame.progressFrame != null)
			MainFrame.progressFrame.statusLabel.setText(status);
	}

	/**
	 * The minimum value a progress can be.  Minimum has been set to 0 by default,
	 * so only use this accessor if you want to change that value.
	 *
	 * @param minValue The minimum value of the progress bar.
	 */
	public static void setProgressMinValue(int minValue)
	{
		if(MainFrame.progressFrame != null)
			MainFrame.progressFrame.progressBar.setMinimum(minValue);
	}

	/**
	 * The maximum value a progress can be.  Maximum has been set to 100 by default,
	 * so only use this accessor if you want to change that value.
	 *
	 * @param maxValue The maximum value of the progress bar.
	 */
	public static void setProgressMaxValue(int maxValue)
	{
		if(MainFrame.progressFrame != null)
			MainFrame.progressFrame.progressBar.setMaximum(maxValue);
	}

	/**
	 * The current value of the progress bar.  Make sure min and max values have
	 * been set first so the progress bar scales properly.  To make use of the
	 * scalable progress bar, ensure indeterminate is set to false.
	 *
	 * @param value The value of the progress bar.
	 */
	public static void setProgressValue(final int value)
	{
		if(MainFrame.progressFrame != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MainFrame.progressFrame.progressBar.setValue(value);
				}
			});
		}
	}
}
