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
import gui.WorkspacePanel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
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
import problem.InternalMarlaException;
import problem.Problem;
import r.RProcessor;
import r.RProcessor.RecordMode;
import r.RProcessorException;

/**
 * @author Ryan Morehart
 */
public class Configuration
{
	/**
	 * Current instance oconfigXML.getChild("r")f Configuration
	 */
	private static Configuration instance = null;
	/**
	 * Saves where the last load() tried to load its configuration from.
	 * Useful for saving back to the same location
	 */
	private final String configPath;
	/**
	 * Parsed XML from config file
	 */
	private Element configXML = null;

	/**
	 * Possible elements that can be configured through this class
	 */
	public enum ConfigType {
			DebugMode,
			PdfTex, R, OpsXML, TexTemplate,
			UserName, ClassShort, ClassLong,
			MinLineWidth, LineSpacing,
			SendErrorReports, ReportWithProblem, ErrorServer
		};

	/**
	 * Creates new instance of the configuration pointed to the given configuration file
	 */
	private Configuration(String configPath)
	{
		this.configPath = configPath;
	}

	/**
	 * Gets the current instance of the Configuration. Creates a new one if needed
	 * @return Working instance of Configuration class
	 */
	public static Configuration getInstance()
	{
		if(instance == null)
		{
			instance = new Configuration(locateConfig());
		}

		return instance;
	}

	/**
	 * Loads configuration from XML file. No attempt is made to search
	 * for missing config elements.
	 * @return true if the configuration file was loaded successfully
	 */
	private boolean reloadXML()
	{
		try
		{
			System.out.println("Using config file at '" + configPath + "'");

			// Load the XML
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(configPath);
			configXML = doc.getRootElement();
			return true;
		}
		catch(JDOMException ex)
		{
			return false;
		}
		catch(IOException ex)
		{
			return false;
		}
	}

	/**
	 * Loads configuration from all possible sources. Processing order is:
	 *		Command Line->XML file->Search->Defaults
	 * Items fulfilled at higher levels are skipped later
	 * @param Command line options to be parsed
	 * @return List of the config types that failed to be configured
	 */
	public List<ConfigType> configureAll(String[] args)
	{
		// We will assume all fail for now, then remove things that get configured
		// We make the array this way because Arrays.asList() returns a list that can't
		// use removeAll()
		List<ConfigType> unconfigured = new ArrayList<ConfigType>();
		unconfigured.addAll(Arrays.asList(ConfigType.values()));
	
		// Command line
		List<ConfigType> fixed = new ArrayList<ConfigType> (unconfigured.size ());
		for(ConfigType c : unconfigured)
		{
			if(configureFromCmdLine(c, args))
				fixed.add(c);
		}

		unconfigured.removeAll(fixed);

		// Try XML config
		fixed.clear();
		if(reloadXML())
		{
			for(ConfigType c : unconfigured)
			{
				if(configureFromXML(c))
					fixed.add(c);
			}
		}

		unconfigured.removeAll(fixed);

		// Try searching
		fixed.clear();
		for(ConfigType c : unconfigured)
		{
			if(configureFromSearch(c))
				fixed.add(c);
		}

		unconfigured.removeAll(fixed);

		// Try defaults
		fixed.clear();
		for(ConfigType c : unconfigured)
		{
			if(configureFromDefault(c))
				fixed.add(c);
		}

		unconfigured.removeAll(fixed);

		// Display the results
		System.out.println("Configuration:");
		for(ConfigType c : ConfigType.values())
		{
			System.out.print("\t" + c + ": ");
			try
			{
				System.out.println(get(c));
			}
			catch (MarlaException ex)
			{
				System.out.println("unset (" + ex.getMessage() + ")");
			}
		}

		// Return the failures
		return unconfigured;
	}

	/**
	 * Gets the given configuration item's value
	 * @param setting Setting to adjust
	 * @return Currentlyset value for configuration item, null if there was none
	 */
	public Object get(ConfigType setting) throws MarlaException
	{
		switch(setting)
		{
			case PdfTex:
				return LatexExporter.getPdfTexPath();

			case OpsXML:
				return OperationXML.getXMLPath();

			case R:
				return RProcessor.getRLocation();

			case DebugMode:
				return RProcessor.getDebugMode();

			case TexTemplate:
				return LatexExporter.getDefaultTemplate();

			case ClassLong:
				return Problem.getDefaultLongCourseName();

			case ClassShort:
				return Problem.getDefaultShortCourseName();

			case UserName:
				return Problem.getDefaultPersonName();

			case LineSpacing:
				return WorkspacePanel.getLineSpacing();

			case MinLineWidth:
				return WorkspacePanel.getMinLineWidth();

			case SendErrorReports:
				return Domain.getSendReport();

			case ReportWithProblem:
				return Domain.getReportIncludesProblem();

			case ErrorServer:
				return Domain.getErrorServer();

			default:
				throw new InternalMarlaException("Unhandled configuration exception type in get");
		}
	}

	/**
	 * Sets the given configuration item to the given value
	 * @param setting Setting to adjust
	 * @param val Value to assign to setting
	 * @return Previously set value for configuration item, null if there was none
	 */
	public Object set(ConfigType setting, Object val) throws MarlaException
	{
		Object previous = null;

		switch(setting)
		{
			case PdfTex:
				previous = LatexExporter.setPdfTexPath(val.toString());
				break;

			case OpsXML:
				OperationXML.loadXML(val.toString());
				break;

			case R:
				previous = RProcessor.setRLocation(val.toString());
				break;

			case DebugMode:
				try
				{
					RecordMode valCast = null;
					if(val instanceof RecordMode)
						valCast = (RecordMode) val;
					else
						valCast = RecordMode.valueOf (val.toString().toUpperCase());
					
					previous = RProcessor.setDebugMode(valCast);
				}
				catch(IllegalArgumentException ex)
				{
					throw new ConfigurationException("Invalid setting '" + val + "' for debug mode", ConfigType.DebugMode);
				}
				break;

			case TexTemplate:
				previous = LatexExporter.setDefaultTemplate(val.toString());
				break;

			case ClassLong:
				previous = Problem.setDefaultLongCourseName(val.toString());
				break;

			case ClassShort:
				previous = Problem.setDefaultShortCourseName(val.toString());
				break;

			case UserName:
				previous = Problem.setDefaultPersonName(val.toString());
				break;

			case LineSpacing:
				if(val instanceof Integer)
					previous = WorkspacePanel.setLineSpacing((Integer)val);
				else
					previous = WorkspacePanel.setLineSpacing(Integer.parseInt(val.toString()));
				break;

			case MinLineWidth:
				if(val instanceof Integer)
					previous = WorkspacePanel.setMinLineWidth((Integer)val);
				else
					previous = WorkspacePanel.setMinLineWidth(Integer.parseInt(val.toString()));
				break;

			case ErrorServer:
				previous = Domain.setErrorServer(val.toString());
				break;

			case SendErrorReports:
				if(val instanceof Boolean)
					previous = Domain.setSendReport((Boolean)val);
				else
					previous = Domain.setSendReport(Boolean.parseBoolean(val.toString().toLowerCase()));
				break;

			case ReportWithProblem:
				if(val instanceof Boolean)
					previous = Domain.setReportIncludesProblem((Boolean)val);
				else
					previous = Domain.setReportIncludesProblem(Boolean.parseBoolean(val.toString().toLowerCase()));
				break;

			default:
				throw new InternalMarlaException("Unhandled configuration exception type in name");
		}

		return previous;
	}

	/**
	 * Sets configuration parameters based loaded configuration file
	 * @param setting Setting to configure
	 * @return true if successfully configured, false otherwise
	 */
	private boolean configureFromXML(ConfigType setting)
	{
		boolean success = false;

		try
		{
			String val = configXML.getChildText(setting.toString());
			if(val != null)
			{
				set(setting, val);
				success = true;
			}
			else
				success = false;
		}
		catch(MarlaException ex)
		{
			success = false;
		}

		if(success)
			System.out.println("Configured " + setting + " from config file");
		
		return success;
	}

	/**
	 * Sets configuration parameters based on command line
	 * @param setting Setting to configure based on command line
	 * @param args Command line parameters, as given to main() by the VM
	 * @return true if successfully configured, false otherwise
	 */
	private boolean configureFromCmdLine(ConfigType setting, String[] args)
	{
		// Look for key in args
		String setName = "--" + setting.toString();
		for(String arg : args)
		{
			if(arg.startsWith(setName))
			{
				try
				{
					set(setting, arg.substring(arg.indexOf('=') + 1));

					System.out.println("Configured " + setting + " from command line");
					return true;
				}
				catch(MarlaException ex)
				{
					return false;
				}
			}
		}

		// Didn't find a match
		return false;
	}

	/**
	 * Attempts to search for the given setting's file (if applicable)
	 * @param setting Configuration setting to look for
	 * @return true if the item is configured successfully, false otherwise
	 */
	private boolean configureFromSearch(ConfigType setting)
	{
		boolean success = false;

		switch(setting)
		{
			case PdfTex:
				success = findAndSetPdfTex();
				break;

			case OpsXML:
				success = findAndSetOpsXML();
				break;

			case R:
				success = findAndSetR();
				break;

			case TexTemplate:
				success = findAndSetLatexTemplate();
				break;

			case ErrorServer:
				success = retreiveAndSetErrorServer();
				break;

			default:
				// Can't handle through a search
				success = false;
		}

		if(success)
			System.out.println("Configured " + setting + " from search");

		return success;
	}

	/**
	 * Sets configuration elements from those that have logical defaults
	 * @param setting Configuration setting to find a default for
	 * @return true if the item is configured successfully, false otherwise
	 */
	private boolean configureFromDefault(ConfigType setting)
	{
		boolean success = false;
		
		try
		{
			switch(setting)
			{
				case DebugMode:
					set(setting, RProcessor.RecordMode.DISABLED);
					success = true;
					break;

				case MinLineWidth:
					set(setting, 2);
					success = true;
					break;

				case LineSpacing:
					set(setting, 4);
					success = true;
					break;

				case SendErrorReports:
				case ReportWithProblem:
					set(setting, true);
					success = true;
					break;

				case ErrorServer:
					set(setting, "http://www.moreharts.com/marla/report.php");
					success = true;
					break;

				case ClassLong:
				case ClassShort:
				case UserName:
					set(setting, "");
					success = true;
					break;

				default:
					// No logical default for this setting
					success = false;
			}
		}
		catch(MarlaException ex)
		{
			success = false;
		}

		if(success)
			System.out.println("Configured " + setting + " with default");

		return success;
	}

	/**
	 * Locates maRla configuration file and returns the path to use. The path
	 * "found" may actually not exist, it will return the default if none exists
	 * currently
	 * @return Path to configuration file, which may or may not exist
	 */
	private static String locateConfig()
	{
		// Locate config file.
		File[] configPaths = new File[] {
			new File(System.getProperty("user.home") + "/" + ".marla/config.xml"),
			new File("config.xml")
		};

		for(int i = 0; i < configPaths.length; i++)
		{
			if(configPaths[i].exists())
				return configPaths[i].getPath();
		}

		// Doesn't exist, but pretend. It will write a new one here
		return configPaths[0].getPath();
	}

	/**
	 * Saves current maRla configuration to the location we loaded from
	 */
	public void save() throws MarlaException
	{
		// Build document
		Element rootEl = new Element("marla");
		Document doc = new Document(rootEl);

		for(ConfigType c : ConfigType.values())
		{
			try
			{
				Element el = new Element(c.toString());
				el.addContent(get(c).toString());
				rootEl.addContent(el);
			}
			catch(NullPointerException ex)
			{
				// Ignore, don't save
			}
		}

		try
		{
			// Create directories to file if needed
			System.out.println("Writing configuration to '" + configPath + "'");
			FileUtils.forceMkdir(new File(configPath).getAbsoluteFile().getParentFile());
			
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

	private static boolean findAndSetR()
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
				return true;
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
		return false;
	}

	private static boolean findAndSetOpsXML()
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
				return true;
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
		return false;
	}

	private static boolean findAndSetLatexTemplate()
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
				return true;
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		return false;
	}

	private static boolean findAndSetPdfTex()
	{
		List<String> possibilities = findFile("pdf(la)?tex(\\.exe)?", "bin|usr|Program Files.*|.*[Tt][Ee][Xx].*|local|20[0-9]{2}|.*darwin|Contents|Resources|Portable.*", null);

		// Test each possibility until one runs properly
		for(String exe : possibilities)
		{
			try
			{
				// Try to launch
				System.out.println("Checking '" + exe + "'");
				LatexExporter.setPdfTexPath(exe);
				
				// Must have launched successfully, use this one
				return true;
			}
			catch(ConfigurationException ex)
			{
				// Try the next one
			}
		}

		// Couldn't find one that worked
		return false;
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
	private static List<String> findFile(String fileName, String dirSearch, List<File> additional)
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
		@SuppressWarnings("unchecked")
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
			@SuppressWarnings("unchecked")
			Collection<File> driveSearchRes = FileUtils.listFiles(roots[i],
					new RegexFileFilter(namePatt), // Find files with these names
					new RegexFileFilter(dirPatt) // And recurse down directories with these names
					);
			checkPaths.addAll(driveSearchRes);
		}

		// Convert all to just the paths (not Files)
		List<String> files = new ArrayList<String>(checkPaths.size());
		System.out.println(checkPaths.size() + " possibilities found for " + fileName + ": ");
		for(File f : checkPaths)
		{
			System.out.println("\t" + f.getPath());
			files.add(f.getAbsolutePath());
		}
		
		return files;
	}

	private static boolean retreiveAndSetErrorServer()
	{
		try
		{
			// Get the wiki page with the latest URL
			URL url = new URL("http://code.google.com/p/marla/wiki/ReportServer");
			URLConnection conn = url.openConnection();

			// Read in page
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder page = new StringBuilder();
			String line = null;
			while((line = rd.readLine()) != null)
				page.append(line);
			rd.close();

			// Find the <server> markers
			Pattern server = Pattern.compile("\\|SERVER\\|(http://.+)\\|SERVER\\|");
			Matcher m = server.matcher(page.toString());
			if(!m.find())
				return false;
			
			// Set
			Domain.setErrorServer(m.group(1));

			return true;
		}
		catch(IOException ex)
		{
			return false;
		}
		catch(ConfigurationException ex)
		{
			return false;
		}
	}

	/**
	 * Gets a user-friendly name for the given setting
	 * @param setting Setting to find a name for
	 * @return User-friendly name
	 */
	public static String getName(ConfigType setting)
	{
		switch(setting)
		{
			case PdfTex:
				return "pdfTeX path";

			case OpsXML:
				return "Operation XML path";

			case R:
				return "R path";

			case TexTemplate:
				return "LaTeX export template path";

			default:
				return setting.toString();
		}
	}

	/**
	 * Convenience method to do the most common configuration stuff. Intended
	 * only for use when nothing will need manual configuration (pre-setup computer)
	 * @return true if all items are configured, false otherwise
	 */
	public static boolean load()
	{
		return Configuration.getInstance().configureAll(new String[]{}).isEmpty();
	}

	/**
	 * Loads (including searching if needed) and saves configuration to
	 * the given file. Useful for creating new configuration
	 */
	public static void main(String[] args) throws MarlaException
	{
		Configuration conf = Configuration.getInstance();
		List<ConfigType> screwedUp = conf.configureAll(args);

		System.out.println("Configuruation failed:");
		for(ConfigType c : screwedUp)
			System.out.println("\t" + c);

		conf.save();
	}
}
