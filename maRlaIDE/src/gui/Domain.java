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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jdom.JDOMException;
import problem.DataSet;
import problem.MarlaException;
import problem.Operation;
import problem.OperationInfoRequiredException;
import problem.Problem;
import resource.LoadSaveThread;

/**
 * Interactions that are related but not directly tied to the front-end of the
 * user interface.
 *
 * @author Alex Laird
 */
public class Domain
{
	/** The name of the application.*/
	public static final String NAME = "The maRla Project";
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
	/** The last good problem directory.*/
	public String lastGoodDir = HOME_DIR;
	/** The last good file that was a CSV file.*/
	public String lastGoodCsvFile = lastGoodDir;
	/** The error file that keeps track of all errors and their occurrences.*/
	protected File logFile;
	/** The load/save thread that is continually running unless explicitly paused or stopped.*/
	protected LoadSaveThread loadSaveThread;
	/** The user can only have one problem open a time, so here is our problem object reference.*/
	protected Problem problem = null;
	/** The current selected data set which operations are added to.*/
	protected DataSet currentDataSet = null;
	/** The current selected operation which.*/
	protected Operation currentOperation = null;
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

		Problem.setDomain(this);

		logFile = new File ("log.dat");
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
	 * Retrieves the reference to the currently open problem.
	 *
	 * @return The currently open problem object.
	 */
	public Problem getProblem()
	{
		return problem;
	}

	/**
	 * If a problem has been defined, this will call the problems save method,
	 * otherwise it does nothing.
	 */
	public void save()
	{
		if (problem != null)
		{
			try
			{
				problem.save();
			}
			catch(MarlaException ex)
			{
				// TODO. Unable to save
			}
		}
	}

	/**
	 * Tells the view to rebuild and repaint the tree.
	 *
	 * @param dataSet The data set to rebuild in the tree.
	 */
	public void rebuildTree(DataSet dataSet)
	{
		viewPanel.rebuildTree (dataSet);
		//viewPanel.workspacePanel.repaint ();
	}

	/**
	 * Allows the user to specify a new file to save the problem as.
	 */
	protected void saveAs()
	{
		if (problem != null)
		{
			// Construct the file-based open chooser dialog
			viewPanel.saveChooserDialog.setFileFilter(viewPanel.marlaFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ()));
			viewPanel.saveChooserDialog.setSelectedFile(new File (problem.getFileName ()));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog(viewPanel);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				boolean continueAllowed = true;
				File file = viewPanel.saveChooserDialog.getSelectedFile();
				// Ensure the problem name given does not match an already existing file
				if (file.exists ())
				{
					response = JOptionPane.showConfirmDialog(viewPanel, "The selected file already exists.\n"
							+ "Would you like to overwrite the existing file?",
							"Overwrite Existing File",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response != JOptionPane.YES_OPTION)
					{
						continueAllowed = false;
					}
				}

				if (continueAllowed)
				{
					problem.setFileName(file.toString ());
					save ();
				}
			}
		}
	}

	/**
	 * Presents a file chooser to allow the user to specify a problem to load.
	 */
	public void load()
	{
		try
		{
			// Construct the file-based open chooser dialog
			viewPanel.openChooserDialog.setFileFilter(viewPanel.marlaFilter);
			viewPanel.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String curDir = lastGoodDir;
			if (problem != null)
			{
				curDir = problem.getFileName ();
			}
			viewPanel.openChooserDialog.setCurrentDirectory(new File (curDir));
			viewPanel.openChooserDialog.setSelectedFile (new File (""));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.openChooserDialog.showOpenDialog(viewPanel);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				if (problem != null)
				{
					viewPanel.closeProblem();
				}
				
				problem = Problem.load(viewPanel.openChooserDialog.getSelectedFile().toString ());

				viewPanel.openProblem();
			}
		}
		catch (MarlaException ex)
		{
			JOptionPane.showMessageDialog(viewPanel, "The requested R package either cannot be located or is not installed.", "Missing Package", JOptionPane.WARNING_MESSAGE);
		}
		catch(FileNotFoundException ex)
		{
		}
		catch(IOException ex)
		{
		}
		catch(JDOMException ex)
		{
		}
	}

	public void ensureRequirementsMet(Operation op) throws MarlaException
	{
		// Iteratively attempts to tell operation to recompute itself until noone that
		// it depends on has missing requirements
		boolean isSolved = false;
		while(!isSolved)
		{
			try
			{
				op.checkCache();
				isSolved = true;
			}
			catch(OperationInfoRequiredException ex)
			{
				viewPanel.getRequiredInfoDialog(ex.getOperation());
			}
		}
	}
}
