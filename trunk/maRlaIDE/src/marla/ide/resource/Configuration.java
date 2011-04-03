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

package marla.ide.resource;

import marla.ide.gui.Domain;
import marla.ide.gui.MainFrame;
import marla.ide.gui.WorkspacePanel;
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
import marla.ide.latex.LatexExporter;
import marla.ide.operation.OperationXMLException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import marla.ide.problem.MarlaException;
import marla.ide.operation.OperationXML;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.Problem;
import marla.ide.r.RProcessor;
import marla.ide.r.RProcessor.RecordMode;
import marla.ide.r.RProcessorException;

/**
 * Configures most aspects of maRla by calling out to the appropriate settings
 * methods in other classes
 * @author Ryan Morehart
 */
public class Configuration
{
	/**
	 * Configuration elements which are required for maRla to run.
	 */
	public static ConfigType[] requiredConfig = new ConfigType[] {ConfigType.R, ConfigType.PrimaryOpsXML};
	/**
	 * Current instance oconfigXML.getChild("r")f Configuration
	 */
	private static Configuration instance = null;
	/**
	 * Controls whether detailed messages about where things are configured from
	 * are displayed
	 */
	private boolean detailedConfigStatus = true;
	/**
	 * Cache of wiki settings page
	 */
	private static String pageCache = null;
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
			DebugMode, FirstRun,
			BrowseLocation,
			WindowX, WindowY, WindowHeight, WindowWidth,
			PdfTex, R, PrimaryOpsXML, UserOpsXML, TexTemplate,
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
	 * @param args Command line options to be parsed
	 * @return List of the config types that failed to be configured
	 */
	public List<ConfigType> configureAll(String[] args)
	{
		// Set up stuff for incrementing progress bar nicely
		ConfigType[] vals = ConfigType.values();
		int currProgress = 10;
		int max = 80;
		int valCount = vals.length;
		int incr = max;
		if(valCount > 0)
			incr = (int)Math.floor((max - currProgress) / valCount);
		else
			incr = max;

		// Add any that fail to configure to here
		List<ConfigType> unconfigured = new ArrayList<ConfigType>();

		for(ConfigType c : vals)
		{
			if(!configureFromBest(c, args))
			{
				// Unable to configure
				unconfigured.add(c);
			}
			
			currProgress += incr;
			Domain.setProgressString(currProgress + "%");
			Domain.setProgressValue(currProgress);
		}

		if(unconfigured.isEmpty())
		{
			Domain.setProgressStatus("Configuration complete");
		}
		else
		{
			Domain.setProgressStatus(unconfigured.size() + " unconfigured");
		}

		if(detailedConfigStatus)
		{
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
		}

		// Never whine about anything _not_ required
		List<ConfigType> requiredAndMissing = new ArrayList<ConfigType>();
		for(ConfigType c : unconfigured)
		{
			for(ConfigType r : requiredConfig)
			{
				if(r == c)
					requiredAndMissing.add(c);
			}
		}


		// Return the failures
		return requiredAndMissing;
	}

	/**
	 * Gets the given configuration item's value
	 * @param setting Setting to adjust
	 * @return Currently set value for configuration item, null if there was none
	 */
	public Object get(ConfigType setting)
	{
		switch(setting)
		{
			case PdfTex:
				return LatexExporter.getPdfTexPath();

			case PrimaryOpsXML:
				// The first one is always the primary
				return OperationXML.getPrimaryXMLPath();

			case UserOpsXML:
				// Combine the path(s) into a single string
				List<String> paths = OperationXML.getUserXMLPaths();
				if(paths != null)
				{
					StringBuilder sb = new StringBuilder();
					for(int i = 1; i < paths.size(); i++)
					{
						sb.append(paths.get(i));
						if(i != paths.size() - 1)
							sb.append('|');
					}
					return sb.toString();
				}
				else
					return null;

			case R:
				return RProcessor.getRLocation();

			case DebugMode:
				return Domain.isDebugMode();
				
			case FirstRun:
				return Domain.isFirstRun();

			case BrowseLocation:
				return Domain.lastBrowseLocation();

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

			case WindowX:
				if(Domain.getInstance() != null)
					return Domain.getInstance().getMainFrame().getX();
				else
					return null;

			case WindowY:
				if(Domain.getInstance() != null)
					return Domain.getInstance().getMainFrame().getY();
				else
					return null;

			case WindowHeight:
				if(Domain.getInstance() != null)
					return Domain.getInstance().getMainFrame().getHeight();
				else
					return null;

			case WindowWidth:
				if(Domain.getInstance() != null)
					return Domain.getInstance().getMainFrame().getWidth();
				else
					return null;

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
	public Object set(ConfigType setting, Object val)
	{
		Object previous = null;
		MainFrame frame = null;
		int x;
		int y;
		int height;
		int width;

		switch(setting)
		{
			case PdfTex:
				previous = LatexExporter.setPdfTexPath(val.toString());
				break;

			case PrimaryOpsXML:
				OperationXML.clearXMLOps();
				previous = OperationXML.setPrimaryXMLPath(val.toString());
				break;

			case UserOpsXML:
				previous = get(setting);

				OperationXML.clearXMLOps();

				// Break apart if needed
				OperationXML.setUserXMLPaths(Arrays.asList(val.toString().split("\\|")));
				break;

			case R:
				previous = RProcessor.setRLocation(val.toString());
				break;

			case DebugMode:
				Boolean mode = true;
				if(val instanceof Boolean)
					mode = (Boolean) val;
				else
					mode = Boolean.valueOf(val.toString());
				
				previous = Domain.isDebugMode(mode);

				// Toggle our own output
				detailedConfigStatus = mode;

				// Also set RProcessor correctly
				if(mode)
					RProcessor.setDebugMode(RecordMode.FULL);
				else
					RProcessor.setDebugMode(RecordMode.DISABLED);
				
				break;

			case FirstRun:
				Boolean first = true;
				if(val instanceof Boolean)
					first = (Boolean) val;
				else
					first = Boolean.valueOf(val.toString());

				previous = Domain.isFirstRun(first);
				break;

			case BrowseLocation:
				previous = Domain.lastBrowseLocation(val.toString());
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

			case WindowX:
				try
				{
					frame = Domain.getInstance().getMainFrame();
					y = frame.getY();
					if(val instanceof Integer)
						x = (Integer)val;
					else
						x = Integer.parseInt(val.toString());
					frame.setLocation(x, y);
					MainFrame.progressFrame.setLocationRelativeTo(frame);
				}
				catch(NullPointerException ex)
				{
					throw new ConfigurationException("No window currently available to set", setting);
				}
				break;

			case WindowY:
				try
				{
					frame = Domain.getInstance().getMainFrame();
					x = frame.getX();
					if(val instanceof Integer)
						y = (Integer)val;
					else
						y = Integer.parseInt(val.toString());
					frame.setLocation(x, y);
					MainFrame.progressFrame.setLocationRelativeTo(frame);
				}
				catch(NullPointerException ex)
				{
					throw new ConfigurationException("No window currently available to set", setting);
				}
				break;

			case WindowHeight:
				try
				{
					frame = Domain.getInstance().getMainFrame();
					x = frame.getX();
					y = frame.getY();
					width = frame.getWidth();
					if(val instanceof Integer)
						height = (Integer)val;
					else
						height = Integer.parseInt(val.toString());
					frame.setBounds(x, y, width, height);
					MainFrame.progressFrame.setLocationRelativeTo(frame);
				}
				catch(NullPointerException ex)
				{
					throw new ConfigurationException("No window currently available to set", setting);
				}
				break;

			case WindowWidth:
				try
				{
					frame = Domain.getInstance().getMainFrame();
					x = frame.getX();
					y = frame.getY();
					height = frame.getHeight();
					if(val instanceof Integer)
						width = (Integer)val;
					else
						width = Integer.parseInt(val.toString());
					frame.setBounds(x, y, width, height);
					MainFrame.progressFrame.setLocationRelativeTo(frame);
				}
				catch(NullPointerException ex)
				{
					throw new ConfigurationException("No window currently available to set", setting);
				}
				break;

			default:
				throw new InternalMarlaException("Unhandled configuration exception type in name");
		}

		return previous;
	}

	/**
	 * Runs down the chain of possibilities, configuring the given setting
	 * from the "best" source (command line->xml->search->default)
	 * @param setting Setting to configure
	 * @param args The command line. If null, will be skipped entirely
	 * @return true if setting was configured, false otherwise
	 */
	public boolean configureFromBest(ConfigType setting, String[] args)
	{
		String dispStr = getName(setting);

		Domain.setProgressStatus("Configuring " + dispStr + " from command line...");
		if(args != null && configureFromCmdLine(setting, args))
			return true;

		Domain.setProgressStatus("Configuring " + dispStr + " from XML...");
		if(configPath != null && configXML == null)
			reloadXML();
		if(configXML != null && configureFromXML(setting))
			return true;

		Domain.setProgressStatus("Searching for " + dispStr + "...");
		if(configureFromSearch(setting))
			return true;

		Domain.setProgressStatus("Using default for " + dispStr + "...");
		if(configureFromDefault(setting))
			return true;

		// Rats
		return false;
	}

	/**
	 * Sets configuration parameters based loaded configuration file
	 * @param setting Setting to configure
	 * @return true if successfully configured, false otherwise
	 */
	public boolean configureFromXML(ConfigType setting)
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

		if(detailedConfigStatus && success)
			System.out.println("Configured " + setting + " from config file");

		return success;
	}

	/**
	 * Sets configuration parameters based on command line
	 * @param setting Setting to configure based on command line
	 * @param args Command line parameters, as given to main() by the VM
	 * @return true if successfully configured, false otherwise
	 */
	public boolean configureFromCmdLine(ConfigType setting, String[] args)
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

					if(detailedConfigStatus)
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
	public boolean configureFromSearch(ConfigType setting)
	{
		boolean success = false;

		switch(setting)
		{
			case PdfTex:
				success = findAndSetPdfTex();
				break;

			case PrimaryOpsXML:
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

		if(detailedConfigStatus && success)
			System.out.println("Configured " + setting + " from search");

		return success;
	}

	/**
	 * Sets configuration elements from those that have logical defaults
	 * @param setting Configuration setting to find a default for
	 * @return true if the item is configured successfully, false otherwise
	 */
	public boolean configureFromDefault(ConfigType setting)
	{
		boolean success = false;

		try
		{
			switch(setting)
			{
				case DebugMode:
					set(setting, false);
					success = true;
					break;

				case FirstRun:
					set(setting, true);
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

				case UserOpsXML:
					set(setting, "");
					success = true;
					break;

				case BrowseLocation:
					set(setting, System.getProperty("user.home"));
					success = true;
					break;

				case WindowHeight:
				case WindowWidth:
				case WindowX:
				case WindowY:
					// Leave this to whatever it defaults to automatically
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

		if(detailedConfigStatus && success)
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
	public void save()
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

	/**
	 * Looks for R in typical binary locations on Windows, Linux, and OSX. Once
	 * located, it sets the correct values
	 * @return true if R is found, false otherwise
	 */
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

	/**
	 * Looks for primary operation XML file in typical install locations
	 * on Windows, Linux, and OSX, as well as close to the run directory. Once
	 * located, it sets the correct values
	 * @return true if ops XML is found, false otherwise
	 */
	private static boolean findAndSetOpsXML()
	{
		List<String> possibilities = findFile("ops\\.xml", "config|xml|test|etc|ma[rR]la|Program Files.*", null);

		// Test each possibility until one loads
		for(String path : possibilities)
		{
			try
			{
				// Try to load
				System.out.println("Checking '" + path + "'");
				OperationXML.clearXMLOps();
				OperationXML.setPrimaryXMLPath(path);
				OperationXML.loadXML();

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

	/**
	 * Looks for Latex template in typical install locations on Windows,
	 * Linux, and OSX, as well as close to the working directory. Once
	 * located, it sets the correct values
	 * @return true if template is found, false otherwise
	 */
	private static boolean findAndSetLatexTemplate()
	{
		List<String> possibilities = findFile("export_template\\.xml", "config|xml|test|etc|ma[rR]la|Program Files.*", null);

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

	/**
	 * Looks for pdfTeX in typical binary locations on Windows, Linux, and OSX. Once
	 * located, it sets the correct values
	 * @return true if pdfTeX is found, false otherwise
	 */
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
	public static List<String> findFile(String fileName, String dirSearch, List<File> additional)
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

	/**
	 * Fetches the wiki setting page and searches it for the given tag
	 * @param tag Tag to search for on wiki page
	 * @return String found between the tag markers
	 */
	public static String fetchSettingFromServer(String tag)
	{
		try
		{
			if(pageCache == null)
			{
				// Get the wiki page with the latest URL
				URL url = new URL("http://code.google.com/p/marla/wiki/CurrentSettings");
				URLConnection conn = url.openConnection();

				// Read in page
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder page = new StringBuilder();
				String line = null;
				while((line = rd.readLine()) != null)
					page.append(line);
				rd.close();

				// Cache
				pageCache = page.toString();
			}

			// Find the markers
			Pattern server = Pattern.compile("\\|" + tag + "\\|(.+)\\|" + tag + "\\|");
			Matcher m = server.matcher(pageCache.toString());
			if(!m.find())
				return null;

			return m.group(1);
		}
		catch(IOException ex)
		{
			return null;
		}
	}

	/**
	 * Gets the current error server report URL from the settings page. 
	 * @return true if server is found, false otherwise
	 */
	private static boolean retreiveAndSetErrorServer()
	{
		try
		{
			// Get setting from server
			String server = fetchSettingFromServer("SERVER");
			if(server != null)
			{
				Domain.setErrorServer(server);
				return true;
			}
			else
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

			case PrimaryOpsXML:
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
	 * only for use when nothing will need manual configuration (pre-setup computer).
	 * Assumes no command line parameters
	 * @return true if all items are configured, false otherwise
	 */
	public static boolean load()
	{
		return Configuration.getInstance().configureAll(new String[]{}).isEmpty();
	}

	/**
	 * Loads given command line configuration and saves to
	 * file. Useful for creating new configuration
	 */
	public static void main(String[] args)
	{
		Configuration conf = Configuration.getInstance();

		for(ConfigType c : ConfigType.values())
		{
			if(conf.configureFromCmdLine(c, args))
				System.out.println("Configured " + c);
		}

		conf.save();
	}
}
