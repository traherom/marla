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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
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
	 * Enumeration denoting the record mode the R processor can use
	 */
	public enum RecordMode
	{
		DISABLED, CMDS_ONLY, OUTPUT_ONLY, FULL
	};
	/**
	 * Pattern used to recognize single R commands. Used by execute() to protect from
	 * hangs resulting from multiple commands being passed in. Does not allow
	 * strings with newlines in them, use \n instead.
	 */
	private final Pattern singleCmdPatt = Pattern.compile("^[^\\n;]+[\\n;]?$");
	/**
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern doublePatt = Pattern.compile("(?<=\\s)-?[0-9]+(\\.[0-9]+)?(e-?[0-9]+)?(?=\\s|$)");
	/**
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern stringPatt = Pattern.compile("\"(([^\\n]|\\\")+?)\"");
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
	 * The class that combines R's stdout and stderr streams
	 */
	private InputStreamCombine comboStream = null;
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
	 * Creates a new R instance that can be fed commands
	 * @param newRPath R executable to run
	 */
	private RProcessor(String newRPath) throws RProcessorException
	{
		try
		{
			// Save new path for future use (IE, we need to restart)
			rPath = newRPath;

			// Start up R
			rProc = Runtime.getRuntime().exec(new String[]
					{
						rPath, "--slave", "--no-readline"
					});

			// TODO: set this in domain or someplace, so that the command line can be used
			// to turn it on and off
			debugOutputMode = RecordMode.FULL;

			// Hook up streams
			//procOut = new BufferedReader(new InputStreamReader(rProc.getInputStream()));
			comboStream = new InputStreamCombine();
			procOut = new BufferedReader(comboStream);
			comboStream.addStream(rProc.getInputStream());
			comboStream.addStream(rProc.getErrorStream());

			procIn = (BufferedOutputStream) rProc.getOutputStream();

			// Set options and eat up an error about "no --no-readline"
			// option on Windows if needed.
			execute("options(error=dump.frames, device=png)");
		}
		catch(IOException ex)
		{
			throw new RProcessorException("Unable to initialize R processor", ex);
		}
	}

	/**
	 * Creates a new instance of R which can be fed commands. Assumes R is accessible on the path.
	 * If it isn't, RProcessor then searches for an installation alongside itself (in an
	 * R directory, so the R executable is at R/bin/R), then in common system install
	 * locations for Windows, Linux, and OSX.
	 * @return Instance of RProcessor that can be used for calculations
	 */
	public static RProcessor getInstance() throws RProcessorException
	{
		try
		{
			return getInstance(rPath);
		}
		catch(RProcessorException ex)
		{
			// Try to find it in all the common locations
			System.err.print("R not found on path:\n\t");
			System.err.println(System.getenv("PATH").replaceAll(";", "\n\t"));
			System.err.print("Looking for R in common locations: ");
			String[] commonLocs = new String[]
			{
				"R/bin/R", // Our own installation of it
				"/Library/Frameworks/R.framework/Resources/R",
				"/usr/lib/R/bin/R",
				"C:\\Program Files\\R\\bin\\x64\\R.exe",
				"C:\\Program Files\\R\\bin\\x32\\R.exe",
				"D:\\Program Files\\R\\bin\\x64\\R.exe",
				"D:\\Program Files\\R\\bin\\x32\\R.exe",
				"C:\\Program Files\\R\\bin\\R.exe",
				"D:\\Program Files\\R\\bin\\R.exe"
			};
			for(String s : commonLocs)
			{
				File f = new File(s);
				System.err.print("\n\t" + s);
				if(f.exists())
				{
					System.err.println("\nfound!");
					return getInstance(s);
				}
			}

			// Darn, no luck finding it
			System.err.println("\nNo installation found, dying.");
			throw ex;
		}
	}

	/**
	 * Creates a new instance of R at the given path which can be fed commands
	 * @param rPath Path to the R executable
	 * @return Instance of RProcessor that can be used for calculations
	 */
	public static RProcessor getInstance(String rPath) throws RProcessorException
	{
		if(singleRProcessor == null)
		{
			singleRProcessor = new RProcessor(rPath);
		}

		return singleRProcessor;
	}

	/**
	 * Kills any existing instances of the RProcessor and starts a new one.
	 * @return Newly created RProcessor instance
	 */
	public static RProcessor restartInstance() throws RProcessorException
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
			comboStream.close();
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
			comboStream = null;
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

		// Save R output to here
		StringBuilder results = new StringBuilder();

		try
		{
			// Only one thread may access the R input/output at one time
			synchronized(processSync)
			{
				// Send command with a sentinel at the end so we know when the output is done
				sentinelCmd.append(this.SENTINEL_STRING_CMD);
				byte[] cmdArray = sentinelCmd.toString().getBytes();
				procIn.write(cmdArray, 0, cmdArray.length);
				procIn.flush();

				// Get results back
				String line = procOut.readLine();
				while(line != null && !line.equals(this.SENTINEL_STRING_RETURN) && !line.startsWith("Error: "))
				{
					results.append(line);
					results.append('\n');
					line = procOut.readLine();
				}

				// If we ended the loop because of an error, read until we hit our sentinel anyway
				if(line.startsWith("Error: "))
				{
					// The last loop stopped before it added this
					results.append(line);
					results.append('\n');

					line = procOut.readLine();
					while(line != null && !line.equals(this.SENTINEL_STRING_RETURN))
					{
						results.append(line);
						results.append('\n');
						line = procOut.readLine();
					}

					// Record interaction if needed
					if(recordMode == RecordMode.OUTPUT_ONLY || recordMode == RecordMode.FULL)
						interactionRecord.append(results);
					if(debugOutputMode == RecordMode.OUTPUT_ONLY || debugOutputMode == RecordMode.FULL)
						System.out.print(results);

					// Throw an exception about this
					throw new RProcessorException(results.toString());
				}
			}
			
			// Record interaction if needed
			if(recordMode == RecordMode.OUTPUT_ONLY || recordMode == RecordMode.FULL)
				interactionRecord.append(results);
			if(debugOutputMode == RecordMode.OUTPUT_ONLY || debugOutputMode == RecordMode.FULL)
				System.out.print(results);

			// Return results, the caller is responsible for processing further
			return results.toString();
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
	public ArrayList<String> execute(ArrayList<String> cmds) throws RProcessorException
	{
		ArrayList<String> output = new ArrayList<String>(cmds.size());

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
	public ArrayList<Double> executeDoubleArray(String cmd) throws RProcessorException, RProcessorParseException
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
	public ArrayList<String> executeStringArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseStringArray(execute(cmd));
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
				vals.add(new Double(m.group()));
			}
		}
		catch(NumberFormatException ex)
		{
			// Should almost never be hit, as we recognized it with our regex
			throw new RProcessorParseException("The R result was improperly processed internally. Please tell the developer");
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

		try
		{
			Matcher m = stringPatt.matcher(rOutput);

			while(m.find())
			{
				vals.add(m.group(1));
			}
		}
		catch(NumberFormatException ex)
		{
			// Should almost never be hit, as we recognized it with our regex
			throw new RProcessorParseException("The R result was improperly processed internally. Please tell the developer");
		}

		// We clearly weren't supposed to parse the output like this, it wasn't what we wanted
		if(vals.isEmpty())
			throw new RProcessorParseException("The R result is not a vector of strings");

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
			execute(name + "=" + val);
		else
			execute(name + "=\"" + val + '"');

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
	public RecordMode setRecorder(RecordMode mode)
	{
		RecordMode oldMode = recordMode;
		recordMode = mode;
		return oldMode;
	}

	/**
	 * Sets how much the processor should output to the console. Useful debugging operations
	 * @param mode RecordMode to place the processor in.
	 * @return The mode the RProcessor was in before the switch
	 */
	public RecordMode setDebug(RecordMode mode)
	{
		RecordMode oldMode = debugOutputMode;
		debugOutputMode = mode;
		return oldMode;
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
}