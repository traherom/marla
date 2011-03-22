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

import gui.Domain;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import problem.MarlaException;

/**
 * @author Ryan Morehart
 */
public class Updater implements Runnable
{
	/**
	 * URL to download updater from
	 */
	private final String updateURL;
	/**
	 * Becomes true when maRla exits
	 */
	private static boolean hasExited = false;

	private Updater(String updateLocation)
	{
		updateURL = updateLocation;
	}

	/**
	 * Tell updater that maRla has exited and can be updated
	 */
	public static void notifyExit()
	{
		hasExited = true;
	}

	/**
	 * Checks for and updates components of maRla if possible
	 */
	public static void checkForUpdates() throws MarlaException
	{
		// Are there updates?
		String latestRev = Configuration.fetchSettingFromServer("REV");
		if(latestRev == null)
		{
			System.out.println("Unable to check for updates");
			return;
		}

		// Are we current?
		int rev = Integer.parseInt(latestRev);
		if(rev <= Integer.parseInt(BuildInfo.revisionNumber))
		{
			System.out.println("maRla is up-to-date");
			return;
		}
		else
		{
			System.out.println("maRla is out of date. Updating to revision " + rev + "");
		}

		// Start download of update jar in background
		Updater up = new Updater("http://marla.googlecode.com/files/update.jar");
		Thread upThread = new Thread(up);
		upThread.setDaemon(true);
		upThread.start();
	}

	/**
	 * Gets the give file and returns the path to it
	 * @param urlLocation URL to fetch
	 * @return path to downloaded file
	 */
	private static String fetchFile(String urlLocation) throws MarlaException
	{
		try
		{
			// Get the wiki page with the latest URL
			URL url = new URL(urlLocation);
			URLConnection conn = url.openConnection();

			// Read in and write to disk as we go
			File tempFile = File.createTempFile("marla", ".jar");
			OutputStream os = new FileOutputStream(tempFile);
			IOUtils.copy(conn.getInputStream(), os);

			return tempFile.getAbsolutePath();
		}
		catch(IOException ex)
		{
			throw new MarlaException("Unable to fetch file '" + urlLocation + "' from server", ex);
		}
	}

	@Override
	public void run()
	{
		try
		{
			String updateFile = fetchFile(updateURL);

			// Wait for exit
			// TODO way to change this into waitfor on parent?
			while(!hasExited)
				Thread.sleep(3000);
			Thread.sleep(10);

			runUpdateJar(updateFile);
		}
		catch(MarlaException ex)
		{
			Domain.logger.add(ex);
		}
		catch(InterruptedException ex)
		{
			Domain.logger.add(ex);
		}
	}

	/**
	 * Runs update jar, if applicable
	 */
	public void runUpdateJar(String updateJar)
	{
		try
		{
			System.out.print("Begining maRla update...");

			// Run it!
			ProcessBuilder proc = new ProcessBuilder("java -jar " + updateJar);
			proc.start();

			// Remove update file
			new File(updateJar).delete();

			System.out.println("complete");
		}
		catch(IOException ex)
		{
			Domain.logger.add(ex);
			ex.printStackTrace();
		}
	}
}
