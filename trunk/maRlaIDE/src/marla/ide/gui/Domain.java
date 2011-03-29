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

package marla.ide.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import marla.ide.latex.LatexExporter;
import marla.ide.problem.MarlaException;
import marla.ide.operation.Operation;
import marla.ide.operation.OperationInfoRequiredException;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import marla.ide.problem.DataSource;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.Problem;
import marla.ide.resource.BuildInfo;
import marla.ide.resource.Configuration.ConfigType;
import marla.ide.resource.ConfigurationException;
import marla.ide.resource.LoadSaveThread;

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
	public static final String VERSION = "0.5";
	/** The pre-release version name of the application.*/
	public static final String PRE_RELEASE = "Beta";
	/** The location of the application as it runs.*/
	public static final String CWD = System.getProperty ("user.dir");
	/** The name of the operating system being used.*/
	public static final String OS_NAME = System.getProperty ("os.name");
	/** The home directory for the current user.*/
	public static final String HOME_DIR = System.getProperty ("user.home");
	/** The full time format for debug output.*/
	public static final SimpleDateFormat FULL_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
	/** The logger holds all caught exceptions for recording in the log file.*/
	public static final List<Throwable> logger = new ArrayList<Throwable> ();
	/** Debug mode */
	public static boolean debug = false;
	/** First run of maRla */
	public static boolean firstRun = false;
	/** Domain object currently created. Only one allowed, ever */
	public static Domain currDomain = null;
	/**
	 * Server to send maRla exceptions to
	 */
	private static String errorServerURL = null;
	/**
	 * Whether to send stack traces to marla servers
	 */
	private static boolean sendErrorReport = true;
	/**
	 * Whether to include, if applicable, the problem XML in the
	 * error report
	 */
	private static boolean includeProbInReport = true;
	/** The last good problem directory.*/
	public String lastGoodDir = HOME_DIR;
	/** The last good file that was a CSV file.*/
	public String lastGoodCsvFile = lastGoodDir;
	/** The error file that keeps track of all errors and their occurrences.*/
	protected File logFile;
	/** Denotes if the log is being written. Prevents double writing */
	protected boolean isWritingLog = false;
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
		if(currDomain != null)
			throw new InternalMarlaException("Multiple domain instances created");

		currDomain = this;

		this.viewPanel = viewPanel;

		// If the Desktop object is supported, get the reference
		if (Desktop.isDesktopSupported ())
		{
			desktop = Desktop.getDesktop ();
		}
		final Domain domain = this;
		Problem.setDomain (domain);

		logFile = new File ("log.dat");
	}

	/**
	 * Gets the current instance of Domain (null if there is none)
	 * @return Current Domain
	 */
	public static Domain getInstance()
	{
		return currDomain;
	}

	/**
	 * Gets the currently set server to send error reports to
	 * @return Current error server
	 */
	public static String getErrorServer()
	{
		return errorServerURL;
	}

	/**
	 * Sets the URL to send error reports to. URL is checked and must respond properly
	 * @param newServer URL to send reports to
	 * @return Previously set value for server
	 */
	public static String setErrorServer(String newServer)
	{
		String old = errorServerURL;

		try
		{
			// Ensure it's valid. Don't allow it to follow redirects, helps with the CU proxy login
			URL url = new URL(newServer);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setInstanceFollowRedirects(false);

			// Read in page
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder page = new StringBuilder();
			String line = null;
			while((line = rd.readLine()) != null)
				page.append(line);
			rd.close();

			// Check response
			if(!page.toString().equals("maRla"))
				throw new ConfigurationException("URL given for error server is invalid", ConfigType.ErrorServer);
		}
		catch(UnknownHostException ex)
		{
			// We likely just timed out instantly and there's no network
			// connection. Accept though, for when the network comes back online
			System.out.println("Accepting setting for error sever, unable to check");
		}
		catch(MalformedURLException ex)
		{
			throw new ConfigurationException("URL given for error server ('" + newServer + "') appears invalid", ConfigType.ErrorServer, ex);
		}
		catch(IOException ex)
		{
			throw new ConfigurationException("URL given for error server could not be reached", ConfigType.ErrorServer, ex);
		}

		// Must be fine
		errorServerURL = newServer;
		return old;
	}

	/**
	 * Gets whether log writer will send exception reports to maRla servers
	 * @return true if exceptions are sent, false if they are only logged locally
	 */
	public static boolean getSendReport()
	{
		return sendErrorReport;
	}

	/**
	 * Sets whether the log file writer should also send exceptions
	 * to maRla servers
	 * @param send true to send, false otherwise
	 * @return Previously set value for sending
	 */
	public static boolean setSendReport(boolean send)
	{
		boolean old = sendErrorReport;
		sendErrorReport = send;
		return old;
	}

	/**
	 * Gets the current debug mode
	 * @return Current debug mode. True if debugging, false otherwise
	 */
	public static boolean isDebugMode()
	{
		return debug;
	}

	/**
	 * Sets the current debug mode
	 * @param newMode New debug mode
	 * @return Previous debug mode. True if debugging, false otherwise
	 */
	public static boolean isDebugMode(boolean newMode)
	{
		boolean old = debug;
		debug = newMode;
		return old;
	}

	/**
	 * Returns whether this is the first time maRla has run
	 * @return true if first run, false otherwise
	 */
	public static boolean isFirstRun()
	{
		return firstRun;
	}

	/**
	 * Sets whether this is the first run of maRla
	 * @param newMode New value for first run
	 * @return true if first run was set, false otherwise
	 */
	public static boolean isFirstRun(boolean newMode)
	{
		boolean old = firstRun;
		firstRun = newMode;
		return old;
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
			MainFrame.progressFrame.setLocationRelativeTo(currDomain.getMainFrame());
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

	/**
	 * Gets whether log writer will include the current problem in its reports
	 * to maRla servers
	 * @return true if the problem is included, false otherwise
	 */
	public static boolean getReportIncludesProblem()
	{
		return includeProbInReport;
	}

	/**
	 * Sets whether reports sent to the maRla servers include the XML for the
	 * current problem
	 * @param include true to include the current problem in the report, false
	 *		otherwise
	 * @return Previously set value for including problem
	 */
	public static boolean setReportIncludesProblem(boolean include)
	{
		boolean old = includeProbInReport;
		includeProbInReport = include;
		return old;
	}

	/**
	 * Writes any current log file out to disk and clears the logger (so
	 * that the same exceptions won't be written again)
	 */
	public void flushLog()
	{
		if(isWritingLog)
			return;
		
		try
		{
			isWritingLog = true;
			flushLog(logger, debug, logFile, errorServerURL, (includeProbInReport ? problem : null));
			logger.clear();
		}
		finally
		{
			isWritingLog = false;
		}
	}

	/**
	 * Writes the given log to the various sources
	 * @param log Exception log to write
	 * @param toConsole if true, sends trace to standard out
	 * @param logFile if not null, writes tace to given file
	 * @param errorServer if not null, sends trace to given error server
	 * @param currProb if not null, sends given problem along with report to server
	 */
	public static void flushLog(List<Throwable> log, boolean toConsole, File logFile, String errorServer, Problem currProb)
	{
		if(log.isEmpty())
			return;

		PrintWriter out = null;
		if(logFile != null)
		{
			try
			{
				out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

				Date date = new Date();
				out.write("Date: " + FULL_TIME_FORMAT.format(date) + "\n");
			}
			catch(IOException ex)
			{
				out = null;
				System.err.println("Unable to write error log file: " + ex.getMessage());
			}
		}

		for(int i = 0; i < logger.size(); ++i)
		{
			Throwable ex = logger.get(i);

			if(toConsole)
				ex.printStackTrace(System.err);

			if(out != null)
				ex.printStackTrace(out);

			if(errorServer != null)
				sendExceptionToServer(errorServer, ex, currProb);
		}

		if(out != null)
		{
			out.flush();
			out.close();
		}
	}

	/**
	 * Sends the given exception to the error server
	 */
	private static void sendExceptionToServer(String server, Throwable ex, Problem prob)
	{
		try
		{
			// Construct data
			StringBuilder dataSB = new StringBuilder();

			// "security" key
			dataSB.append(URLEncoder.encode("secret", "UTF-8"));
			dataSB.append('=');
			dataSB.append(URLEncoder.encode("badsecurity", "UTF-8"));

			// Send version number
			dataSB.append('&');
			dataSB.append(URLEncoder.encode("version", "UTF-8"));
			dataSB.append('=');
			dataSB.append(URLEncoder.encode(BuildInfo.revisionNumber, "UTF-8"));

			// Exception message
			dataSB.append('&');
			dataSB.append(URLEncoder.encode("msg", "UTF-8"));
			dataSB.append('=');
			dataSB.append(URLEncoder.encode(ex.getMessage(), "UTF-8"));

			// Stack trace
			ByteArrayOutputStream trace = new ByteArrayOutputStream();
			ex.printStackTrace(new PrintStream(trace));
			dataSB.append('&');
			dataSB.append(URLEncoder.encode("trace", "UTF-8"));
			dataSB.append('=');
			dataSB.append(URLEncoder.encode(trace.toString(), "UTF-8"));

			// Problem, if applicable
			if(prob != null)
			{
				dataSB.append('&');
				dataSB.append(URLEncoder.encode("problem", "UTF-8"));
				dataSB.append('=');

				try
				{
					Document doc = new Document(prob.toXml());
					Format formatter = Format.getPrettyFormat();
					formatter.setEncoding("UTF-8");
					XMLOutputter xml = new XMLOutputter(formatter);
					dataSB.append(URLEncoder.encode(xml.outputString(doc), "UTF-8"));
				}
				catch(MarlaException ex2)
				{
					dataSB.append(URLEncoder.encode("Unable to get XML: " + ex2.toString(), "UTF-8"));
				}
			}

			// Send data
			URL url = new URL(errorServerURL);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(dataSB.toString());
			wr.flush();

			// Check for success
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String response = null;
			String line = null;
			while((line = rd.readLine()) != null)
			{
				// Only the first line actually counts as the return
				// but we get the rest for debugging
				if(response == null)
					response = line;
				else
					System.out.println(line);
			}
			wr.close();
			rd.close();

			if(response.equals("success"))
				System.out.println("Exception sent to maRla development team");
			else
				System.out.println("Unable to send exception to development team: " + response);
		}
		catch(IOException ex2)
		{
			// Too bad, but ignore
			System.out.println("Unable to send exception to development team: " + ex2.getMessage());
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
	 * Retrieves a list of Operations in the View panel that does are not attached to a data set.
	 *
	 * @return The list of operations not attached to a data set.
	 */
	public ArrayList<Operation> getUnattachedOperations()
	{
		ArrayList<Operation> ops = new ArrayList<Operation> ();

		for (int i = 0; i < viewPanel.workspacePanel.getComponentCount(); ++i)
		{
			Component comp = viewPanel.workspacePanel.getComponent(i);
			if (comp instanceof Operation && ((Operation) comp).getParentData() == null)
			{
				ops.add ((Operation) comp);
			}
		}

		return ops;
	}

	/**
	 * Adds the given operation to the workspace
	 * @param op Operation to add to workspace
	 */
	public void addUnattachedOperation(Operation op)
	{
		viewPanel.workspacePanel.add(op);
		op.setText("<html>" + op.getDisplayString(false) + "</html>");
		op.setSize(op.getPreferredSize());
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
				problem.save ();
			}
			catch (MarlaException ex)
			{
				logger.add(ex);
				JOptionPane.showMessageDialog(getTopWindow(), ex.getMessage(), "Unable to Save", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Returns true if the problem is being edited, false if it is a new problem.
	 *
	 * @return True for editing, false for new problem.
	 */
	public boolean isEditing()
	{
		if (viewPanel.NEW_PROBLEM_WIZARD_DIALOG.newProblem != null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Tells the view to rebuild and repaint the given tree.
	 * @param dataSet
	 */
	public void rebuildTree(DataSource dataSet)
	{
		viewPanel.rebuildTree(dataSet);
	}

	/**
	 * Tells the view to rebuild and repaint the entire workspace.
	 */
	public void rebuildWorkspace()
	{
		viewPanel.rebuildWorkspace();
	}

	/**
	 * Mark the View as unsaved.
	 */
	public void markUnsaved()
	{
		if (isEditing ())
		{
			viewPanel.saveButton.setEnabled (true);
		}
	}

	/**
	 * Mark the View as saved.
	 */
	public void markSaved()
	{
		viewPanel.saveButton.setEnabled (false);
	}

	/**
	 * Allows the user to specify a new file to save the problem as.
	 */
	protected void saveAs()
	{
		if (problem != null)
		{
			// Construct the file-based open chooser dialog
			viewPanel.saveChooserDialog.setDialogTitle ("Save Problem As");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter (viewPanel.marlaFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode (JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ()));
			viewPanel.saveChooserDialog.setSelectedFile (new File (problem.getFileName ()));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile ();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".marla");
				}
				// ensure the file is a valid backup file
				if (!file.toString ().endsWith (".marla"))
				{
					JOptionPane.showMessageDialog (getTopWindow(), "The extension for the file must be .marla.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".marla"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
				if (file.exists ())
				{
					response = JOptionPane.showConfirmDialog (getTopWindow(), "The selected file already exists.\n"
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
					problem.setFileName (file.toString ());
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
			viewPanel.saveChooserDialog.setDialogTitle ("Export to PDF");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter (viewPanel.pdfFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode (JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf (".")) + ".pdf"));
			viewPanel.saveChooserDialog.setSelectedFile (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf (".")) + ".pdf"));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile ();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".pdf");
				}
				final File finalFile = file;
				// ensure the file is a valid backup file
				if (!finalFile.toString ().endsWith (".pdf"))
				{
					JOptionPane.showMessageDialog (viewPanel, "The extension for the file must be .pdf.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".pdf"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
				if (finalFile.exists ())
				{
					response = JOptionPane.showConfirmDialog (getTopWindow(), "The selected file already exists.\n"
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
					Domain.setProgressVisible(true);
					Domain.setProgressIndeterminate(true);
					Domain.setProgressString("");
					Domain.setProgressStatus("Beginning PDF export...");

					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							String filePath = null;
							try
							{
								// Ensure all operations have been fulfilled, info wise
								for (int i = 0; i < problem.getDataCount (); i++)
								{
									List<Operation> ops = problem.getData (i).getAllLeafOperations ();
									for (Operation op : ops)
									{
										ensureRequirementsMet (op);
									}
								}

								LatexExporter exporter = new LatexExporter (problem);
								File genFile = new File (exporter.exportPDF (finalFile.getPath ()));
								filePath = genFile.getCanonicalPath();
								if (desktop != null)
								{
									Domain.setProgressStatus("Opening PDF...");

									desktop.open (genFile);
									try
									{
										Thread.sleep(3000);
									}
									catch (InterruptedException ex) {}
								}
							}
							catch (IOException ex)
							{
								if (filePath != null)
								{
									Domain.setProgressIndeterminate(false);
									JOptionPane.showMessageDialog(getTopWindow(), "The file was exported successfully.\nLocation: " + filePath, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									Domain.logger.add (ex);
									JOptionPane.showMessageDialog(getTopWindow(), ex.getMessage(), "PDF Export Failed", JOptionPane.ERROR_MESSAGE);
								}
							}
							catch (MarlaException ex)
							{
								Domain.logger.add (ex);
								JOptionPane.showMessageDialog(getTopWindow(), ex.getMessage(), "PDF Export Failed", JOptionPane.ERROR_MESSAGE);
							}
							finally
							{
								Domain.setProgressVisible(false);
								Domain.setProgressIndeterminate(false);
							}
						}
					}).start();
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
			viewPanel.saveChooserDialog.setDialogTitle ("Export for LaTeX");
			viewPanel.saveChooserDialog.resetChoosableFileFilters ();
			viewPanel.saveChooserDialog.setFileFilter (viewPanel.latexFilter);
			viewPanel.saveChooserDialog.setFileSelectionMode (JFileChooser.FILES_ONLY);
			viewPanel.saveChooserDialog.setCurrentDirectory (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf (".")) + ".rnw"));
			viewPanel.saveChooserDialog.setSelectedFile (new File (problem.getFileName ().substring (0, problem.getFileName ().lastIndexOf (".")) + ".rnw"));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.saveChooserDialog.getSelectedFile ();
				// ensure an extension is on the file
				if (file.getName ().indexOf (".") == -1)
				{
					file = new File (viewPanel.saveChooserDialog.getSelectedFile ().toString () + ".rnw");
				}
				final File finalFile = file;
				// ensure the file is a valid backup file
				if (!finalFile.toString ().endsWith (".rnw"))
				{
					JOptionPane.showMessageDialog (getTopWindow(), "The extension for the file must be .rnw.", "Invalid Extension", JOptionPane.WARNING_MESSAGE);
					viewPanel.saveChooserDialog.setSelectedFile (new File (viewPanel.saveChooserDialog.getSelectedFile ().toString ().substring (0, viewPanel.saveChooserDialog.getSelectedFile ().toString ().lastIndexOf (".")) + ".tex"));
					response = viewPanel.saveChooserDialog.showSaveDialog (viewPanel);
					continue;
				}
				// Ensure the problem name given does not match an already existing file
				boolean continueAllowed = true;
				if (finalFile.exists ())
				{
					response = JOptionPane.showConfirmDialog (getTopWindow(), "The selected file already exists.\n"
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
					Domain.setProgressVisible(true);
					Domain.setProgressIndeterminate(true);
					Domain.setProgressString("");
					Domain.setProgressStatus("Beginning LaTeX export...");

					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							String filePath = null;
							try
							{
								// Ensure all operations have been fulfilled, info wise
								for (int i = 0; i < problem.getDataCount (); i++)
								{
									List<Operation> ops = problem.getData (i).getAllLeafOperations ();
									for (Operation op : ops)
									{
										ensureRequirementsMet (op);
									}
								}

								LatexExporter exporter = new LatexExporter (problem);
								File genFile = new File (exporter.cleanExport (finalFile.getPath ()));
								filePath = genFile.getCanonicalPath();
								if (desktop != null)
								{
									desktop.open (genFile);
									try
									{
										Thread.sleep(3000);
									}
									catch (InterruptedException ex) {}
								}
								else
								{
									throw new IOException ();
								}
							}
							catch (IOException ex)
							{
								if (filePath != null)
								{
									Domain.setProgressIndeterminate(false);
									JOptionPane.showMessageDialog(getTopWindow(), "The file was exported successfully.\nLocation: " + filePath, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									Domain.logger.add (ex);
									JOptionPane.showMessageDialog(getTopWindow(), ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
								}
							}
							catch (MarlaException ex)
							{
								Domain.logger.add (ex);
								JOptionPane.showMessageDialog(getTopWindow(), ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
							}
							finally
							{
								Domain.setProgressVisible(false);
								Domain.setProgressIndeterminate(false);
							}
						}
					}).start();
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
			viewPanel.openChooserDialog.setFileFilter (viewPanel.marlaFilter);
			viewPanel.openChooserDialog.setFileSelectionMode (JFileChooser.FILES_ONLY);
			String curDir = lastGoodDir;
			if (problem != null)
			{
				curDir = problem.getFileName ();
			}
			viewPanel.openChooserDialog.setSelectedFile (new File (""));
			viewPanel.openChooserDialog.setCurrentDirectory (new File (curDir));
			// Display the chooser and retrieve the selected file
			int response = viewPanel.openChooserDialog.showOpenDialog (viewPanel);
			while (response == JFileChooser.APPROVE_OPTION)
			{
				File file = viewPanel.openChooserDialog.getSelectedFile ();
				if (!file.isFile () || !file.toString ().endsWith (".marla"))
				{
					JOptionPane.showMessageDialog (getTopWindow(), "The specified file does not exist.", "Does Not Exist", JOptionPane.WARNING_MESSAGE);
					int lastIndex = viewPanel.openChooserDialog.getSelectedFile ().toString ().lastIndexOf (".");
					if (lastIndex == -1)
					{
						lastIndex = viewPanel.openChooserDialog.getSelectedFile ().toString ().length ();
					}
					viewPanel.openChooserDialog.setSelectedFile (new File (viewPanel.openChooserDialog.getSelectedFile ().toString ().substring (0, lastIndex) + ".marla"));
					response = viewPanel.openChooserDialog.showOpenDialog (viewPanel);
					continue;
				}

				if (problem != null)
				{
					viewPanel.closeProblem (false);
				}

				if (file.isDirectory ())
				{
					lastGoodDir = file.toString ();
				}
				else
				{
					lastGoodDir = file.toString ().substring (0, file.toString ().lastIndexOf (File.separatorChar));
				}
				problem = Problem.load (file.toString ());

				viewPanel.openProblem (false);
				break;
			}
		}
		catch (MarlaException ex)
		{
			Domain.logger.add(ex);
			JOptionPane.showMessageDialog (getTopWindow(), ex.getMessage (), "Error Loading Save File", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void ensureRequirementsMet(Operation op)
	{
		// Iteratively attempts to tell operation to recompute itself until
		// no operation that it depends on has missing requirements
		boolean isSolved = false;
		while (!isSolved)
		{
			try
			{
				op.checkCache ();
				isSolved = true;
			}
			catch (OperationInfoRequiredException ex)
			{
				viewPanel.getRequiredInfoDialog (ex.getOperation (), true);
			}
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
		else if (viewPanel.SETTINGS_DIALOG.isVisible())
		{
			return viewPanel.SETTINGS_DIALOG;
		}
		else if (viewPanel.NEW_PROBLEM_WIZARD_DIALOG.isVisible())
		{
			return viewPanel.NEW_PROBLEM_WIZARD_DIALOG;
		}
		else
		{
			return viewPanel;
		}
	}

	/**
	 * Gets the current window frame for maRla
	 * @return Current MainFrame
	 */
	public MainFrame getMainFrame()
	{
		return viewPanel.mainFrame;
	}
}
