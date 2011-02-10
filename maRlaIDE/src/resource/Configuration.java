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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import latex.LatexExporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import problem.MarlaException;
import r.OperationXML;
import r.RProcessor;

/**
 * @author Ryan Morehart
 */
public class Configuration
{
	/**
	 * Sets configuration parameters based on command line
	 * @param args Command line parameters, as given to main() by the VM
	 */
	public static void processCmdLine(String[] args) throws MarlaException
	{
		if(args.length >= 1)
		{
			if(args[0].equals("debug"))
				RProcessor.getInstance().setDebugMode(RProcessor.RecordMode.FULL);
			else if(args[0].equals("output"))
				RProcessor.getInstance().setDebugMode(RProcessor.RecordMode.OUTPUT_ONLY);
			else if(args[0].equals("cmds"))
				RProcessor.getInstance().setDebugMode(RProcessor.RecordMode.CMDS_ONLY);
			else if(args[0].equals("quiet"))
				RProcessor.getInstance().setDebugMode(RProcessor.RecordMode.DISABLED);
			else
				throw new MarlaException("Unrecognized command line parameter '" + args[0] + "'");
		}
	}

	/**
	 * Loads maRla configuration from the default location, config.xml in the
	 * current directory
	 */
	public static void load() throws MarlaException
	{
		load("config.xml");
	}

	/**
	 * Loads maRla configuration from the specified config file
	 * @param configPath XML file to load data from
	 */
	public static void load(String configPath) throws MarlaException
	{
		Element configXML = null;
		boolean configLoaded = false;

		try
		{
			// Load the XML
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(configPath);
			configXML = doc.getRootElement();
			configLoaded = true;

			// Tell various components about their settings
			RProcessor.setConfig(configXML.getChild("rprocessor"));
			LatexExporter.setConfig(configXML.getChild("latex"));
			OperationXML.setConfig(configXML.getChild("xmlops"));

			System.out.println("Using config file at '" + configPath + "'");
		}
		catch(JDOMException ex)
		{
			configLoaded = false;
		}
		catch(IOException ex)
		{
			configLoaded = false;
		}

		// Unable to load config, so make sure we can find R and pdflatex
		if(!configLoaded)
		{
			RProcessor.setRLocation(findR());
			LatexExporter.setPdflatexPath(findPdflatex());
		}
	}

	/**
	 * Saves current maRla configuration to the default location, config.xml
	 * in the current directory
	 */
	public static void save() throws MarlaException
	{
		save("config.xml");
	}

	/**
	 * Saves current maRla configuration to the given location
	 * @param configPath Location to save data to
	 */
	public static void save(String configPath) throws MarlaException
	{
		// Build document
		Element root = new Element("marla");
		root.addContent(RProcessor.getConfig(new Element("rprocessor")));
		root.addContent(LatexExporter.getConfig(new Element("latex")));
		root.addContent(OperationXML.getConfig(new Element("xmlops")));
		Document doc = new Document(root);

		try
		{
			// Output to file
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(configPath));
			BufferedWriter outputStream = new BufferedWriter(os);

			Format formatter = Format.getPrettyFormat();
			formatter.setEncoding(os.getEncoding());
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);
		}
		catch(IOException ex)
		{
			throw new MarlaException("Problem occured writing to configuration file", ex);
		}
	}

	/**
	 * Checks if the given executable is on the system's PATH
	 * @param exeName executable to locate. On Windows this must include the extension
	 * @return true if the executable is on the path, false otherwise
	 */
	public static boolean isOnPath(String exeName)
	{
		// Determines if a given file is somewhere on the system PATH
		System.out.println("Looking for '" + exeName + "' on PATH");
		String[] pathDirs = System.getenv("PATH").split(";|:");
		for(String dirPath : pathDirs)
		{
			File exe = new File(dirPath + "/" + exeName);
			if(exe.exists())
				return true;
		}

		// Didn't find it
		return false;
	}

	public static String findR()
	{
		return findExecutable("R", "R.*|bin|usr|local|lib|Program Files.*|x64|x32", null);
	}

	public static String findPdflatex()
	{
		return findExecutable("pdflatex", "bin|usr|Program Files.*|[Mm]i[Kk][Tt]e[Xx].*", null);
	}

	/**
	 * Returns a path to the given executable or null if none is found
	 * @param exeName Name of the executable to find. Should not include the .exe portion
	 *	(although that will still function)
	 * @param dirSearch Regular expression of the directories to search. Any directory that
	 *	matches this pattern will be recursed into
	 * @param additional List of directories to manually add to the search
	 * @return Path to executable, as usable by createProcess(). Null if not found
	 */
	public static String findExecutable(String exeName, String dirSearch, List<File> additional)
	{
		// Save all possible files that match the requested name
		Pattern namePatt = Pattern.compile(exeName + "(\\.exe)?");
		Pattern dirPatt = Pattern.compile(dirSearch);
		List<File> checkPaths = new ArrayList<File>();

		// Add any additional paths the user specifically added
		// Put them first as they're more likely to be correct
		if(additional != null)
			checkPaths.addAll(additional);

		// Add all path locations
		//checkPaths.addAll(Arrays.asList(System.getenv("PATH").split(";|:")));

		// Loop to hit all the dives on Windows. On Linux/OSX this only happens once
		File[] roots = File.listRoots();
		for(int i = 0; i < roots.length; i++)
		{
			// Ensure this drive actually exists (for example, A: may be returned without being attached)
			if(!roots[i].isDirectory())
				continue;

			// The R folder has the version number, so look for anything similar
			Collection<File> driveSearchRes = FileUtils.listFiles(roots[i],
					new RegexFileFilter(namePatt), // Find files with these names
					new RegexFileFilter(dirPatt) // And recurse down directories with these names
					);
			checkPaths.addAll(driveSearchRes);
		}

		// Find one of our results that is executable
		System.out.println("Looking for " + exeName + " at ");
		for(File f : checkPaths)
		{
			System.out.println("\t" + f.getPath());
			if(f.canExecute())
				return f.getPath();
		}

		// Failed
		System.out.println("Unable to locate " + exeName);
		return null;
	}

	public static void main(String[] args)
	{
		System.out.println("R located at " + findR());
		System.out.println("pdflatex located at " + findPdflatex());
	}
}
