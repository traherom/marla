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
package r;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;
import resource.ConfigurationException;

/**
 * @author Ryan Morehart
 */
public final class RProcessor
{
	/**
	 * Sent to R to force an output we can recognize as finishing the command set
	 */
	private final String SENTINEL_STRING_CMD = "print('---MARLA R OUTPUT END---')\n";
	/**
	 * Return from R for the command given in SENTINEL_STRING_CMD, we watch for this result
	 */
	private final String SENTINEL_STRING_RETURN = "[1] \"---MARLA R OUTPUT END---\"";
	/**
	 * Pattern used to recognize single R commands. Used by execute() to protect from
	 * hangs resulting from multiple commands being passed in. Does not allow
	 * strings with newlines in them, use \n instead.
	 */
	private final Pattern singleCmdPatt = Pattern.compile("^[^\\n;]+[\\n;]?$");
	/**
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern doublePatt = Pattern.compile("(?<=\\s)(-?[0-9]+(\\.[0-9]+)?(e[+-][0-9]+)?|NaN)(?=\\s|$)");
	/**
	 * Pattern used to recognize strings in R output, mainly for use with vectors
	 */
	private final Pattern stringPatt = Pattern.compile("\"(([^\\n]|\\\")+?)\"");
	/**
	 * Pattern used to recognize booleans in R output, mainly for use with vectors
	 */
	private final Pattern booleanPatt = Pattern.compile("(?<=\\s)(FALSE|TRUE)(?=\\s|$)");
	/**
	 * Path to the R executable, used if R has to be reloaded after it dies
	 */
	private static String rPath = "R";
	/**
	 * Single instance of RProcessor that we allow
	 */
	private static RProcessor singleRProcessor = null;
	/**
	 * The R process itself
	 */
	private Process rProc = null;
	/**
	 * The R process's output stream, where we get the results from
	 */
	private BufferedReader procOut = null;
	/**
	 * The R process's input stream, where we send commands to be run
	 */
	private BufferedOutputStream procIn = null;
	/**
	 * Synchronization variable
	 */
	private final Object processSync = new Object();
	/**
	 * Denotes the mode the RProcessor is
	 */
	private RecordMode recordMode = RecordMode.DISABLED;
	/**
	 * Denotes how/if the RProcessor should dump to console interactions with R
	 */
	private RecordMode debugOutputMode = RecordMode.DISABLED;
	/**
	 * Record of output returned from R
	 */
	private StringBuilder interactionRecord = new StringBuilder();
	/**
	 * Path of the most recently output graphic
	 */
	private String lastPngName = null;
	/**
	 * Stores the next value to use for the "unique" name generator
	 */
	private long uniqueValCounter = 0;

	/**
	 * Enumeration denoting the record mode the R processor can use
	 */
	public enum RecordMode
	{
		DISABLED, CMDS_ONLY, OUTPUT_ONLY, FULL
	};

	/**
	 * Creates a new R instance that can be fed commands
	 * @param newRPath R executable to run
	 */
	private RProcessor(String newRPath) throws RProcessorException, ConfigurationException
	{
		try
		{
			// Save new path for future use (IE, we need to restart)
			rPath = newRPath;

			// Start up R
			ProcessBuilder builder = new ProcessBuilder(rPath, "--slave", "--no-readline");
			builder.redirectErrorStream(true);
			rProc = builder.start();

			// Hook up streams. I swear the names of these streams are
			// confusing in the Java API. Input stream is the _output_ from the
			// process. It's input to us, I guess.
			procOut = new BufferedReader(new InputStreamReader(rProc.getInputStream()));
			procIn = (BufferedOutputStream) rProc.getOutputStream();

			// Set options and eat up an error about "no --no-readline"
			// option on Windows if needed.
			// show.error.messages=F?
			execute("options(error=dump.frames, warn=-1, device=png)");
		}
		catch(IOException ex)
		{
			throw new ConfigurationException("R could not be executed at '" + rPath + "'");
		}
	}

	/**
	 * Sets the default location to look for R
	 * @param newRPath New location of the R binary
	 * @return The previously assigned location of R
	 */
	public static String setRLocation(String newRPath) throws ConfigurationException, RProcessorException
	{
		String oldPath = rPath;
		rPath = newRPath;

		// Restart RProcessor if needed
		if(!oldPath.equals(newRPath))
			restartInstance();

		System.out.println("Using R binary at '" + rPath + "'");

		return oldPath;
	}

	/**
	 * Gets the currently set default location to look for R
	 * @return The assigned location of R
	 */
	public static String getRLocation()
	{
		return rPath;
	}

	/**
	 * Configures the RProcessor based on XML configuration, typically from a settings file
	 * @param configEl XML element containing needed data
	 */
	public static void setConfig(Element configEl) throws RProcessorException, ConfigurationException
	{
		// Extract information from configuration XML and set appropriately
		setRLocation(configEl.getAttributeValue("rpath"));

		String debugMode = configEl.getAttributeValue("debug");
		if(debugMode != null)
			RProcessor.getInstance().setDebugMode(RecordMode.valueOf(debugMode.toUpperCase()));
	}

	/**
	 * Creates an XML element that could be passed back to setConfig to configure
	 * the RProcessor the same way as before
	 * @param configEl XML configuration element upon which to add information
	 * @return XML element with configuration data set
	 */
	public static Element getConfig(Element configEl) throws RProcessorException
	{
		configEl.setAttribute("rpath", rPath);

		if(singleRProcessor != null)
			configEl.setAttribute("debug", singleRProcessor.getDebugMode().toString());

		return configEl;
	}

	/**
	 * Creates a new instance of R which can be fed commands. Assumes R is accessible on the path.
	 * If it isn't, RProcessor then searches for an installation alongside itself (in an
	 * R directory, so the R executable is at R/bin/R), then in common system install
	 * locations for Windows, Linux, and OSX.
	 * @return Instance of RProcessor that can be used for calculations
	 */
	public static RProcessor getInstance() throws RProcessorDeadException, ConfigurationException
	{
		try
		{
			if(singleRProcessor == null)
				singleRProcessor = new RProcessor(rPath);

			return singleRProcessor;
		}
		catch(RProcessorException ex)
		{
			System.err.println("No R installation found, dying.");
			throw new ConfigurationException("R installion not found", ex);
		}
	}

	/**
	 * Kills any existing instances of the RProcessor and starts a new one.
	 * @return Newly created RProcessor instance
	 */
	public static RProcessor restartInstance() throws RProcessorException, ConfigurationException
	{
		if(singleRProcessor != null)
		{
			// Make sure the previous one closed properly
			singleRProcessor.close();
			singleRProcessor = null;
		}

		return getInstance();
	}

	/**
	 * Ensures that R is killed cleanly if at all possible
	 */
	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		catch(Exception ex)
		{
			// Don't care, kill it forcibly
			rProc.destroy();
		}
		finally
		{
			super.finalize();
		}
	}

	/**
	 * Kills R process
	 */
	public void close()
	{
		try
		{
			// Only bother if we're not already dead
			if(!isRunning())
				return;

			// Tell R we're closing
			// We don't synchronize here because that would leave us
			// hanging if the main execute() was
			byte[] cmdArray = "q()".getBytes();
			procIn.write(cmdArray, 0, cmdArray.length);
			procIn.flush();

			// Close everything out
			procIn.close();
			procOut.close();
			rProc.waitFor();
		}
		catch(Exception ex)
		{
			// Oh well, it happens
		}
		finally
		{
			// Make the process die even if it didn't want to
			rProc.destroy();

			procIn = null;
			procOut = null;
			System.gc();
		}
	}

	/**
	 * Checks if the current R process is still running and accessible
	 * @return true if the process may be used, false otherwise
	 */
	public boolean isRunning()
	{
		if(rProc == null || procIn == null || procOut == null)
			return false;
		else
			return true;
	}

	/**
	 * Passes the given string onto R just as if you typed it at the command line. Only a single
	 * command may be executed by this command. If the user wants to run multiple commands as a
	 * group, use execute(ArrayList<String>).
	 * @param cmd R command to execute
	 * @return String output from R. Use one of the parse functions to processor further
	 */
	public String execute(String cmd) throws RProcessorException, RProcessorDeadException
	{
		return execute(cmd, false);
	}

	/**
	 * Passes the given string onto R just as if you typed it at the command line. Only a single
	 * command may be executed by this command. If the user wants to run multiple commands as a
	 * group, use execute(ArrayList<String>).
	 * @param cmd R command to execute
	 * @param ignoreErrors true if errors and warnings from R should be ignored and just
	 *		be returned with the rest of the output. If false, exceptions are thrown
	 *		on either occurrence
	 * @return String output from R. Use one of the parse functions to processor further
	 */
	public String execute(String cmd, boolean ignoreErrors) throws RProcessorException, RProcessorDeadException
	{
		// Ensure the processor is still running
		if(!isRunning())
			throw new RProcessorDeadException("R process has been closed.");

		// Check if there are multiple commands in the string.
		// Seriously, that's dangerous for us, could make us hang.
		Matcher m = singleCmdPatt.matcher(cmd);
		if(!m.matches())
			throw new RProcessorException("execute() may only be given one command at a time");

		// Start building up our nice command
		StringBuilder sentinelCmd = new StringBuilder(cmd.trim());
		sentinelCmd.append('\n');

		// Record and/or output if needed
		if(recordMode == RecordMode.CMDS_ONLY || recordMode == RecordMode.FULL)
			interactionRecord.append(sentinelCmd);
		if(debugOutputMode == RecordMode.CMDS_ONLY || debugOutputMode == RecordMode.FULL)
			System.out.print("> " + sentinelCmd);

		try
		{
			// Final R return stored here
			String results = null;
			
			// Indicate if R throws an error or warning
			boolean errorOccurred = false;

			// Only one thread may access the R input/output at one time
			synchronized(processSync)
			{
				// Send command with a sentinel at the end so we know when the output is done
				sentinelCmd.append(this.SENTINEL_STRING_CMD);
				byte[] cmdArray = sentinelCmd.toString().getBytes();
				procIn.write(cmdArray, 0, cmdArray.length);
				procIn.flush();

				// Get results back
				StringBuilder sb = new StringBuilder();
				String line = procOut.readLine();
				while(line != null && !line.equals(this.SENTINEL_STRING_RETURN))
				{
					sb.append(line);
					sb.append('\n');

					if(!ignoreErrors && (line.startsWith("Error") || line.startsWith("Warning")))
						errorOccurred = true;

					line = procOut.readLine();
				}

				// Convert to string
				results = sb.toString();
			}

			// Record interaction if needed
			if(recordMode == RecordMode.OUTPUT_ONLY || recordMode == RecordMode.FULL)
				interactionRecord.append(results);
			if(debugOutputMode == RecordMode.OUTPUT_ONLY || debugOutputMode == RecordMode.FULL)
				System.out.print(results);

			// Throw an error if we encountered an error or warning
			if(errorOccurred)
				throw new RProcessorException("R: " + results);

			// Return results, the caller is responsible for processing further
			return results;
		}
		catch(IOException ex)
		{
			// Unable to read/write to pipes. Try to kill off the process completely to allow
			// us to be restarted
			close();
			throw new RProcessorException("Unable to read or write to the R instance", ex);
		}
	}

	/**
	 * Calls execute(String) for each of the commands given in the cmds array. Commands will
	 * be automatically terminated with a newline if they does not have one.
	 * @param cmds List of R commands to execute
	 * @return ArrayList of Strings, where each entry is the output from one of the commands given.
	 */
	public List<String> execute(List<String> cmds) throws RProcessorException
	{
		List<String> output = new ArrayList<String>(cmds.size());

		for(String cmd : cmds)
		{
			output.add(execute(cmd));
		}
		return output;
	}

	/**
	 * Convenience function that executes the given command and parses the result as a single double
	 * value. An exception is thrown if there is not exactly one double in the output.
	 * @param cmd R command to execute
	 * @return Double value of the R call
	 */
	public Double executeDouble(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseDouble(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a vector
	 * of doubles. An exception is thrown if the output does not contain numerical values.
	 * @param cmd R command to execute
	 * @return ArrayList of doubles that the R command returned
	 */
	public List<Double> executeDoubleArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseDoubleArray(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a string. An
	 * exception is thrown if there is not exactly one string in the output.
	 * @param cmd R command to execute
	 * @return String value of the R call
	 */
	public String executeString(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseString(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a vector of
	 * strings. An exception is thrown if there are no strings in the output.
	 * @param cmd R command to execute
	 * @return ArrayList of strings that the R command returned
	 */
	public List<String> executeStringArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseStringArray(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a boolean. An
	 * exception is thrown if there is not exactly one boolean in the output.
	 * @param cmd R command to execute
	 * @return String value of the R call
	 */
	public Boolean executeBoolean(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseBoolean(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a vector of
	 * booleans. An exception is thrown if there are no booleans in the output.
	 * @param cmd R command to execute
	 * @return ArrayList of strings that the R command returned
	 */
	public List<Boolean> executeBooleanArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseBooleanArray(execute(cmd));
	}

	/**
	 * Runs the given command and saves it into a new, unique variable. The variable name used
	 * is returned as a string. Only one command may be given, an exception is thrown if this
	 * isn't true.
	 * @param cmd R command to execute
	 * @return R variable name that contains the results of the executed command
	 */
	public String executeSave(String cmd) throws RProcessorException
	{
		String varName = getUniqueName();
		execute(varName + " = " + cmd);
		return varName;
	}

	/**
	 * Takes the given R output and attempts to parse it as a single double value. An exception is
	 * thrown if there isn't exactly one numerical value in the output.
	 * @param rOutput R output, as returned by execute(String)
	 * @return Double value contained in the output
	 */
	public Double parseDouble(String rOutput) throws RProcessorParseException
	{
		ArrayList<Double> arr = parseDoubleArray(rOutput);

		if(arr.size() != 1)
			throw new RProcessorParseException("The R result was not a single double value");

		return arr.get(0);
	}

	/**
	 * Takes the given R output and attempts to parse it as a vector of doubles. An exception is
	 * thrown if there are no numerical values.
	 * @param rOutput R output, as returned by execute(String)
	 * @return ArrayList of Doubles from the output
	 */
	public ArrayList<Double> parseDoubleArray(String rOutput) throws RProcessorParseException
	{
		ArrayList<Double> vals = new ArrayList<Double>();

		try
		{
			Matcher m = doublePatt.matcher(rOutput);

			while(m.find())
			{
				vals.add(Double.valueOf(m.group()));
			}
		}
		catch(NumberFormatException ex)
		{
			// Should almost never be hit, as we recognized it with our regex
			throw new RProcessorParseException("The R result did not contain numeric values");
		}

		// We clearly weren't supposed to parse the output like this, it wasn't what we wanted
		if(vals.isEmpty())
			throw new RProcessorParseException("The R result is not a vector of doubles");

		return vals;
	}

	/**
	 * Takes the given R output and attempts to parse it as a single string value. An exception
	 * is thrown if there isn't exactly one string value in the output.
	 * @param rOutput R output, as returned by execute(String)
	 * @return String value contained in the output
	 */
	public String parseString(String rOutput) throws RProcessorParseException
	{
		ArrayList<String> arr = parseStringArray(rOutput);

		if(arr.size() != 1)
			throw new RProcessorParseException("The R result was not a single string value");

		return arr.get(0);
	}

	/**
	 * Takes the given R output and attempts to parse it as a vector of strings. An exception is
	 * thrown if the output contains no strings.
	 * @param rOutput R output, as returned by execute(String)
	 * @return ArrayList of Strings from the output
	 */
	public ArrayList<String> parseStringArray(String rOutput) throws RProcessorParseException
	{
		ArrayList<String> vals = new ArrayList<String>();

		Matcher m = stringPatt.matcher(rOutput);

		while(m.find())
		{
			vals.add(m.group(1));
		}

		// We clearly weren't supposed to parse the output like this, it wasn't what we wanted
		if(vals.isEmpty())
			throw new RProcessorParseException("The R result is not a vector of strings");

		return vals;
	}

	/**
	 * Takes the given R output and attempts to parse it as a single string value. An exception
	 * is thrown if there isn't exactly one string value in the output.
	 * @param rOutput R output, as returned by execute(String)
	 * @return String value contained in the output
	 */
	public Boolean parseBoolean(String rOutput) throws RProcessorParseException
	{
		List<Boolean> arr = parseBooleanArray(rOutput);

		if(arr.size() != 1)
			throw new RProcessorParseException("The R result was not a single boolean value");

		return arr.get(0);
	}

	/**
	 * Takes the given R output and attempts to parse it as a vector of strings. An exception is
	 * thrown if the output contains no strings.
	 * @param rOutput R output, as returned by execute(String)
	 * @return ArrayList of Strings from the output
	 */
	public List<Boolean> parseBooleanArray(String rOutput) throws RProcessorParseException
	{
		List<Boolean> vals = new ArrayList<Boolean>();

		try
		{
			Matcher m = booleanPatt.matcher(rOutput);

			while(m.find())
			{
				vals.add(Boolean.valueOf(m.group(1)));
			}
		}
		catch(NumberFormatException ex)
		{
			// Should almost never be hit, as we recognized it with our regex
			throw new RProcessorParseException("The R result did not contain boolean values");
		}

		// We clearly weren't supposed to parse the output like this, it wasn't what we wanted
		if(vals.isEmpty())
			throw new RProcessorParseException("The R result is not a vector of booleans");

		return vals;
	}

	/**
	 * Sets the given variable with the value given
	 * @param name R-conforming variable name
	 * @param val Value to store in the variable
	 * @return Name of the variable used
	 */
	public String setVariable(String name, Double val) throws RProcessorException
	{
		execute(name + "=" + val);

		return name;
	}

	/**
	 * Sets the given variable with the value given
	 * @param val Value to store in the variable
	 * @return Name of the variable used
	 */
	public String setVariable(Object val) throws RProcessorException
	{
		return setVariable(getUniqueName(), val);
	}

	/**
	 * Sets the given variable with the value given
	 * @param name R-conforming variable name
	 * @param val Value to store in the variable
	 * @return Name of the variable used
	 */
	public String setVariable(String name, Object val) throws RProcessorException
	{
		if(val instanceof Double)
			execute(name + " = " + val);
		else
			execute(name + " = \"" + val + '"');

		return name;
	}

	/**
	 * Sets a new unique variable with a vector of the values given
	 * @param vals Array of values to store in the variable
	 * @return Name of the variable used
	 */
	public String setVariable(List<Object> vals) throws RProcessorException
	{
		return setVariable(getUniqueName(), vals);
	}

	/**
	 * Sets the given variable with a vector of the values given. Values may be either
	 * Doubles or Strings (anything unrecognized is assumed to be a string).
	 * @param name R-conforming variable name
	 * @param vals Array of values to store in the variable
	 * @return Name of the variable used
	 */
	public String setVariable(String name, List<Object> vals) throws RProcessorException
	{
		// Builds an R command to set the given variable name with the values in the array
		StringBuilder cmd = new StringBuilder();
		cmd.append(name);
		cmd.append(" = c(");

		// Save as numerical or string vector as appropriate
		if(vals.get(0) instanceof Double)
		{
			for(Object val : vals)
			{
				cmd.append(val);
				cmd.append(", ");
			}
		}
		else
		{
			for(Object val : vals)
			{
				cmd.append('"');
				cmd.append(val);
				cmd.append("\", ");
			}
		}
		cmd.replace(cmd.length() - 2, cmd.length() - 1, "");
		cmd.append(")\n");

		// Run R command
		execute(cmd.toString());

		return name;
	}

	/**
	 * Creates a new graphic device with the necessary options for passing. An exception is thrown
	 * if the device creation fails.
	 * back to the GUI. Returns the path to the file that will hold the output.
	 * @return Path where the new graphics device will write to
	 */
	public String startGraphicOutput() throws RProcessorException
	{
		// Figure out path and request that it be removed once we close
		lastPngName = getUniqueName() + ".png";
		new File(lastPngName).deleteOnExit();

		// Tell R to start a new device
		execute("png(filename='" + lastPngName + "')");
		return lastPngName;
	}

	/**
	 * Stops the current graphic device, flushing it to disk.
	 * @return Path where the new graphic has been written to
	 */
	public String stopGraphicOutput() throws RProcessorException
	{
		String pngName = lastPngName;
		lastPngName = null;
		execute("dev.off()");
		return pngName;
	}

	/**
	 * Sets the recording mode for the processor
	 * @param mode RecordMode to place the processor in.
	 * @return The mode the RProcessor was in before the switch
	 */
	public RecordMode setRecorderMode(RecordMode mode)
	{
		RecordMode oldMode = recordMode;
		recordMode = mode;
		return oldMode;
	}

	/**
	 * Returns the current processor recording mode
	 * @return The mode the RProcessor is currently in
	 */
	public RecordMode getRecorderMode()
	{
		return recordMode;
	}

	/**
	 * Sets how much the processor should output to the console. Useful debugging operations
	 * @param mode RecordMode to place the processor in.
	 * @return The mode the RProcessor was in before the switch
	 */
	public RecordMode setDebugMode(RecordMode mode)
	{
		RecordMode oldMode = debugOutputMode;
		debugOutputMode = mode;
		return oldMode;
	}

	/**
	 * Returns how much the processor is outputting to the console
	 * @return The mode the RProcessor is currently in
	 */
	public RecordMode getDebugMode()
	{
		return debugOutputMode;
	}

	/**
	 * Retrieves the recorded input and output with R since the last fetch
	 * @return String of all the commands and their output executed since the last fetch
	 */
	public String fetchInteraction()
	{
		String sent = interactionRecord.toString();
		interactionRecord = new StringBuilder();
		return sent;
	}

	/**
	 * Returns a unique variable name for use in this R instance
	 * @return New unique name
	 */
	public String getUniqueName()
	{
		uniqueValCounter++;
		if(uniqueValCounter < 0)
			uniqueValCounter = 0;

		return "marlaUnique" + uniqueValCounter;
	}

	/**
	 * Allows for direct testing of the RProcessor execute() function
	 */
	public static void main(String[] args) throws Exception
	{
		RProcessor proc = null;
		try
		{
			proc = RProcessor.getInstance();
			proc.setDebugMode(RecordMode.FULL);
			proc.setRecorderMode(RecordMode.DISABLED);
			Scanner sc = new Scanner(System.in);

			while(proc.isRunning())
			{
				// Get next command to execute
				System.out.print("CMD: ");
				String line = sc.nextLine();

				// Execute
				try
				{
					proc.execute(line);
				}
				catch(RProcessorException ex)
				{
					System.out.println("Ex handler: " + ex.getMessage());
				}
			}
		}
		finally
		{
			proc.close();
		}
	}
}
