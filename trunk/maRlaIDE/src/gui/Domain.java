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

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import latex.LatexException;
import latex.LatexExporter;
import problem.DataSet;
import problem.MarlaException;
import operation.Operation;
import operation.OperationInfoRequiredException;
import problem.Problem;
import r.RProcessorException;
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
	/** The desktop object for common desktop operations.*/
	protected Desktop desktop;
	/** The load/save thread that is continually running unless explicitly paused or stopped.*/
	protected LoadSaveThread loadSaveThread;
	/** The user can only have one problem open a time, so here is our problem object reference.*/
	protected Problem problem = null;
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

		// If the Desktop object is supported, get the reference
		if (Desktop.isDesktopSupported ())
		{
			desktop = Desktop.getDesktop ();
		}
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
	}

	/**
	 * Allows the user to specify a new file to save the problem as.
	 */
	protected void saveAs()
	{
		if (problem != null)
		{
			// Construct the file-based open chooser dialog
			viewPanel.saveChooserDialog.setDialogTitle("Save Problem As");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter(viewPanel.marlaFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ()));
			viewPanel.saveChooserDialog.setSelectedFile(new File (problem.getFileName ()));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog(viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".marla");
				}
				// ensure the file is a valid backup file
				if (!file.toString ().endsWith (".marla"))
				{
					JOptionPane.showMessageDialog(viewPanel, "The extension for the file must be .marla.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".marla"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
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
					break;
				}
				else
				{
					continue;
				}
			}
		}
	}

	/**
	 * Export the current problem to a PDF file through Latex's commands.
	 */
	protected void exportToPdf()
	{
		if (problem != null)
		{
			// Construct the file-based open chooser dialog
			viewPanel.saveChooserDialog.setDialogTitle("Export to PDF");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter(viewPanel.pdfFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf(".")) + ".pdf"));
			viewPanel.saveChooserDialog.setSelectedFile(new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf(".")) + ".pdf"));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog(viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".pdf");
				}
				// ensure the file is a valid backup file
				if (!file.toString ().endsWith (".pdf"))
				{
					JOptionPane.showMessageDialog(viewPanel, "The extension for the file must be .pdf.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".pdf"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
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
					try
					{
						// Ensure all operations have been fulfilled, info wise
						for(int i = 0; i < problem.getDataCount(); i++)
						{
							List<Operation> ops = problem.getData(i).getAllLeafOperations();
							for(Operation op : ops)
								ensureRequirementsMet(op);
						}
						
						LatexExporter exporter = new LatexExporter (problem);
						File genFile = new File (exporter.generatePDF(file.getPath()));
						if (desktop != null)
						{
							desktop.open (genFile);
						}
					}
					catch (IOException ex)
					{
						Domain.logger.add (ex);
					}
					catch (MarlaException ex)
					{
						Domain.logger.add (ex);
					}
					break;
				}
				else
				{
					continue;
				}
			}
		}
	}

	/**
	 * Export the current problem for Latex.
	 */
	protected void exportForLatex()
	{
		if (problem != null)
		{
			// Construct the file-based open chooser dialog
			viewPanel.saveChooserDialog.setDialogTitle("Export for LaTeX");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter(viewPanel.latexFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf(".")) + ".rnw"));
			viewPanel.saveChooserDialog.setSelectedFile(new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf(".")) + ".rnw"));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog(viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".rnw");
				}
				// ensure the file is a valid backup file
				if (!file.toString ().endsWith (".rnw"))
				{
					JOptionPane.showMessageDialog(viewPanel, "The extension for the file must be .rnw.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".tex"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
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
					try
					{
						// Ensure all operations have been fulfilled, info wise
						for(int i = 0; i < problem.getDataCount(); i++)
						{
							List<Operation> ops = problem.getData(i).getAllLeafOperations();
							for(Operation op : ops)
								ensureRequirementsMet(op);
						}

						LatexExporter exporter = new LatexExporter (problem);
						File genFile = new File (exporter.cleanExport (file.getPath()));
						if (desktop != null)
						{
							desktop.open (genFile);
						}
					}
					catch (IOException ex)
					{
						Domain.logger.add (ex);
					}
					catch (MarlaException ex)
					{
						Domain.logger.add (ex);
					}
					break;
				}
				else
				{
					continue;
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
			viewPanel.openChooserDialog.resetChoosableFileFilters ();
			viewPanel.openChooserDialog.setFileFilter(viewPanel.marlaFilter);
			viewPanel.openChooserDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String curDir = lastGoodDir;
			if (problem != null)
			{
				curDir = problem.getFileName ();
			}
			viewPanel.openChooserDialog.setSelectedFile (new File (""));
			viewPanel.openChooserDialog.setCurrentDirectory(new File (curDir));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.openChooserDialog.showOpenDialog(viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.openChooserDialog.getSelectedFile();
				if (!file.isFile () || !file.toString ().endsWith (".marla"))
				{
					JOptionPane.showMessageDialog(viewPanel, "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = viewPanel.openChooserDialog.getSelectedFile().toString().lastIndexOf(".");
					if (lastIndex == -1)
					{
						lastIndex = viewPanel.openChooserDialog.getSelectedFile().toString().length();
					}
					viewPanel.openChooserDialog.setSelectedFile (new File (viewPanel.openChooserDialog.getSelectedFile ().toString ().substring (0, lastIndex) + ".marla"));
					response = viewPanel.openChooserDialog.showOpenDialog(viewPanel);
					continue;
				}
				
				if (problem != null)
				{
					viewPanel.closeProblem();
				}

				if (file.isDirectory())
				{
					lastGoodDir = file.toString ();
				}
				else
				{
					lastGoodDir = file.toString ().substring (0, file.toString ().lastIndexOf(File.separatorChar));
				}
				problem = Problem.load(file.toString ());

				viewPanel.openProblem();
				break;
			}
		}
		catch (MarlaException ex)
		{
			JOptionPane.showMessageDialog(viewPanel, ex.getMessage(), "Error loading save file", JOptionPane.WARNING_MESSAGE);
		}
		catch(FileNotFoundException ex)
		{
			JOptionPane.showMessageDialog(viewPanel, "Unable to locate ", "Error loading save file", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void ensureRequirementsMet(Operation op) throws MarlaException
	{
		// Iteratively attempts to tell operation to recompute itself until
		// no operation that it depends on has missing requirements
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
