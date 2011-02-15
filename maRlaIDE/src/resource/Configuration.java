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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import latex.LatexExporter;
import operation.OperationXMLException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import problem.MarlaException;
import operation.OperationXML;
import r.RProcessor;
import r.RProcessor.RecordMode;
import r.RProcessorException;
import resource.ConfigurationException.ConfigType;

/**
 * @author Ryan Morehart
 */
public class Configuration
{
	private static Deque<ConfigurationException> errors = new ArrayDeque<ConfigurationException>();

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
	public static boolean load() throws MarlaException
	{
		return load("config.xml");
	}

	/**
	 * Loads maRla configuration from the specified config file. If any errors
	 * occur false is returned. Errors may be retrieved via getNextError()
	 * @param configPath XML file to load data from
	 * @return true if load succeeds, false otherwise
	 */
	public static boolean load(String configPath) throws MarlaException
	{
		Element configXML = null;
		boolean configLoaded = false;

		// Clear any errors
		errors.clear();

		try
		{
			System.out.println("Using config file at '" + configPath + "'");

			// Load the XML
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(configPath);
			configXML = doc.getRootElement();
			configLoaded = true;
		}
		catch(JDOMException ex)
		{
			configLoaded = false;
		}
		catch(IOException ex)
		{
			configLoaded = false;
		}

		// R
		try
		{
			boolean configSuccess = false;
			if(configLoaded)
			{
				try
				{
					// Tell various components about their settings
					Element rEl = configXML.getChild("r");
					RProcessor.setRLocation(rEl.getAttributeValue("rpath"));
					RProcessor.getInstance().setDebugMode(RecordMode.valueOf(rEl.getAttributeValue("debug", "disabled").toUpperCase()));

					configSuccess = true;
				}
				catch(NullPointerException ex)
				{
					configSuccess = false;
				}
			}

			if(!configSuccess)
			{
				findAndSetR();
			}
		}
		catch(ConfigurationException ex)
		{
			errors.push(ex);
		}

		// Latex template
		try
		{
			boolean configSuccess = false;

			if(configLoaded)
			{
				try
				{
					// Tell various components about their settings
					Element latexEl = configXML.getChild("latex");
					LatexExporter.setDefaultTemplate(latexEl.getAttributeValue("template"));
			
					configSuccess = true;
				}
				catch(NullPointerException ex)
				{
					configSuccess = false;
				}
			}

			if(!configSuccess)
			{
				findAndSetLatexTemplate();
			}
		}
		catch(ConfigurationException ex)
		{
			errors.push(ex);
		}

		// pdfTeX
		try
		{
			boolean configSuccess = false;

			if(configLoaded)
			{
				try
				{
					// Tell various components about their settings
					Element latexEl = configXML.getChild("latex");
					LatexExporter.setPdfTexPath(latexEl.getAttributeValue("pdftex"));

					configSuccess = true;
				}
				catch(NullPointerException ex)
				{
					configSuccess = false;
				}
			}

			if(!configSuccess)
			{
				findAndSetPdfTex();
			}
		}
		catch(ConfigurationException ex)
		{
			errors.push(ex);
		}

		// XML operations
		try
		{
			boolean configSuccess = false;
			
			if(configLoaded)
			{
				try
				{
					// Tell various components about their settings
					Element opsEl = configXML.getChild("ops");
					OperationXML.loadXML(opsEl.getAttributeValue("xml"));

					configSuccess = true;
				}
				catch(NullPointerException ex)
				{
					configSuccess = false;
				}
			}

			if(!configSuccess)
			{
				findAndSetOpsXML();
			}
		}
		catch(ConfigurationException ex)
		{
			errors.push(ex);
		}

		// Tell the caller if anything bad happened that we couldn't automatically correct
		return errors.isEmpty();
	}

	/**
	 * Returns the next error from the load or null if there are no more
	 * @return ConfigurationException giving details of error
	 */
	public static ConfigurationException getNextError()
	{
		if(!errors.isEmpty())
			return errors.pop();
		else
			return null;
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
		Element rootEl = new Element("marla");
		Document doc = new Document(rootEl);

		// R
		Element rEl = new Element("r");
		rootEl.addContent(rEl);
		if(RProcessor.getRLocation() != null)
			rEl.setAttribute("rpath", RProcessor.getRLocation());
		if(RProcessor.hasInstance())
			rEl.setAttribute("debug", RProcessor.getInstance().getDebugMode().toString());

		// Latex
		Element latexEl = new Element("latex");
		rootEl.addContent(latexEl);
		if(LatexExporter.getDefaultTemplate() != null)
			latexEl.setAttribute("template", LatexExporter.getDefaultTemplate());
		if(LatexExporter.getPdfTexPath() != null)
			latexEl.setAttribute("pdftex", LatexExporter.getPdfTexPath());

		// XML operations
		Element opsEl = new Element("ops");
		rootEl.addContent(opsEl);
		if(OperationXML.getXMLPath() != null)
			opsEl.setAttribute("xml", OperationXML.getXMLPath());

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
			File exeNoExt = new File(dirPath + "/" + exeName);
			File exeWithExt = new File(dirPath + "/" + exeName + ".exe");
			if(exeNoExt.exists() || exeWithExt.exists())
				return true;
		}

		// Didn't find it
		return false;
	}

	public static String findAndSetR() throws ConfigurationException
	{
		List<String> possibilities = findFile("R(\\.exe)?", "R.*|bin|usr|local|lib|Program Files.*|x64|i386|Library|Frameworks", null);

		// Test each possibility until one runs properly
		for(String exe : possibilities)
		{
			try
			{
				// Try to launch
				System.out.println("Checking '" + exe + "'");
				RProcessor.setRLocation(exe);
				RProcessor.getInstance();

				// Must have launched successfully, use this one
				return exe;
			}
			catch(RProcessorException ex)
			{
				// Try the next one
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		throw new ConfigurationException("Unable to find a suitable R installation", ConfigType.R);
	}

	public static String findAndSetOpsXML() throws ConfigurationException
	{
		List<String> possibilities = findFile("ops\\.xml", "config|xml|test", null);

		// Test each possibility until one loads
		for(String path : possibilities)
		{
			try
			{
				// Try to launch
				System.out.println("Checking '" + path + "'");
				OperationXML.loadXML(path);

				// Must have launched successfully, use this one
				return path;
			}
			catch(OperationXMLException ex)
			{
				// Try next one
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		throw new ConfigurationException("Unable to find a suitable operation XML file installation", ConfigType.OpsXML);
	}

	public static String findAndSetLatexTemplate() throws ConfigurationException
	{
		List<String> possibilities = findFile("export_template\\.xml", "config|xml|test", null);

		// Test each possibility until one loads
		for(String path : possibilities)
		{
			try
			{
				// Try to launch
				System.out.println("Checking '" + path + "'");
				LatexExporter.setDefaultTemplate(path);

				// Must have launched successfully, use this one
				return path;
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		throw new ConfigurationException("Unable to find a suitable LaTeX exporter template", ConfigType.TexTemplate);
	}

	public static String findAndSetPdfTex() throws ConfigurationException
	{
		List<String> possibilities = findFile("pdf(la)?tex(\\.exe)?", "bin|usr|Program Files.*|.*[Tt]e[Xx].*|local|20[0-9]{2}|.*darwin|Contents|Resources", null);

		// Test each possibility until one runs properly
		for(String exe : possibilities)
		{
			try
			{
				// Try to launch
				System.out.println("Checking '" + exe + "'");
				LatexExporter.setPdfTexPath(exe);
				
				// Must have launched successfully, use this one
				return exe;
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		throw new ConfigurationException("Unable to find a suitable pdfTeX installation", ConfigType.PdfTex);
	}

	/**
	 * Returns a path to the given executable or null if none is found
	 * @param fileName Name of the executable to find. Should not include the .exe portion
	 *	(although that will still function)
	 * @param dirSearch Regular expression of the directories to search. Any directory that
	 *	matches this pattern will be recursed into
	 * @param additional List of directories to manually add to the search
	 * @return Path to executable, as usable by createProcess(). Null if not found
	 */
	public static List<String> findFile(String fileName, String dirSearch, List<File> additional) throws ConfigurationException
	{
		// Check if it's on the path, in which case we don't bother searching
		//if(isOnPath(exeName))
		//	return exeName;

		System.out.println("Looking for '" + fileName + "', please wait");

		// Save all possible files that match the requested name
		Pattern namePatt = Pattern.compile(fileName);
		Pattern dirPatt = Pattern.compile(dirSearch);
		List<File> checkPaths = new ArrayList<File>();

		// Add any additional paths the user specifically added
		// Put them first as they're more likely to be correct
		if(additional != null)
			checkPaths.addAll(additional);

		// From current dir
		Collection<File> currDirSearchRes = FileUtils.listFiles(new File("."),
				new RegexFileFilter(namePatt), // Find files with these names
				new RegexFileFilter(dirPatt) // And recurse down directories with these names
				);
		checkPaths.addAll(currDirSearchRes);

		// Loop to hit all the drives on Windows.
		// On Linux/OSX this only happens once for '/'
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

		// Convert all to just the paths
		List<String> files = new ArrayList<String>(checkPaths.size());
		System.out.println(checkPaths.size() + " possibilities found for " + fileName + ": ");
		for(File f : checkPaths)
		{
			System.out.println("\t" + f.getPath());
			files.add(f.getPath());
		}
		
		return files;
	}

	public static void main(String[] args) throws MarlaException
	{
		if(args.length == 0)
			Configuration.load();
		else
			Configuration.load(args[0]);
	}
}
