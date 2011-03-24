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

import java.io.File;
import java.util.ArrayList;
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
    /** The logger holds all caught exceptions for recording in the log file.*/
    public static final ArrayList<Exception> logger = new ArrayList<Exception>();

    /** The load/save thread that is continually running unless explicitly paused or stopped.*/
    protected LoadSaveThread loadSaveThread;
    /** The error file that keeps track of all errors and their occurrences.*/
    protected File logFile;
    /** The reference to the view of the application.*/
    private ViewPanel viewPanel;
	/** The currently open operation XML file.*/
	public File operationFile = null;
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
     * Load a given operation XML file to edit.
     */
    public void load()
    {

    }

	/**
	 * Save the currently open operation XML file.
	 */
	public void save()
	{
		
	}

	/**
	 * Save the currently open operation XML file as a new file.
	 */
	public void saveAs()
	{

	}

	/**
	 * Write the logger file, if it exists.
	 */
	public void writeLoggerFile()
	{
		// TODO write the logger file stuff
		/*if(isWritingLog || logger.isEmpty())
			return;

		try
		{
			isWritingLog = true;

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

			Date date = new Date();
			out.write("------------------------------------\n");
			out.write("Date: " + FULL_TIME_FORMAT.format(date) + "\n");

			for(int i = 0; i < logger.size(); ++i)
			{
				Exception ex = logger.get(i);

				// To file
				ex.printStackTrace(out);

				// To console
				ex.printStackTrace(System.err);

				// To server
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
				if(includeProbInReport && problem != null)
				{
					dataSB.append('&');
					dataSB.append(URLEncoder.encode("problem", "UTF-8"));
					dataSB.append('=');

					try
					{
						Document doc = new Document(problem.toXml());
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

				try
				{
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

			out.write("------------------------------------\n\n\n");
			out.flush();
			out.close();

			System.out.println(logger.size() + " exceptions written to " + logFile.getAbsolutePath());

			logger.clear();
		}
		catch(IOException ex)
		{
			System.err.println("Unable to write error log file: " + ex.getMessage());
		}
		finally
		{
			isWritingLog = false;
		}*/
	}

	/**
	 * Retrieve the current operation XML file that is open.
	 *
	 * @return The open operation XML file.
	 */
	public File getOperationFile()
	{
		return operationFile;
	}
}
