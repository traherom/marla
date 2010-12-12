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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ryan Morehart
 */
public class RProcessor
{
	/**
	 * Sent to R to force an output we can recognize as finishing the command set
	 */
	private final String SENTINEL_STRING_CMD = "print('---MARLA R OUTPUT END. DONT USE THIS STRING---')\n";
	/**
	 * Return from R for the command given in SENTINEL_STRING_CMD, we watch for this result
	 */
	private final String SENTINEL_STRING_RETURN = "[1] \"---MARLA R OUTPUT END. DONT USE THIS STRING---\"";
	/**
	 * Enumeration denoting the record mode the R processor can use
	 */
	public enum RecordMode {DISABLED, CMDS_ONLY, OUTPUT_ONLY, FULL};
	/**
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern doublePatt = Pattern.compile("(?<!\\[)-?[0-9]+(\\.[0-9]+)?(?!\\])");
	/**
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern stringPatt = Pattern.compile("(?<=\")[^\"]*?\\w[^\"]*?(?=\")");
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
	 * Denotes the mode the RProcessor is
	 */
	private RecordMode recordMode = RecordMode.DISABLED;
	/**
	 * Record of output returned from R
	 */
	private StringBuilder interactionRecord = new StringBuilder();
	/**
	 * Stores the next value to use for the "unique" name generator
	 */
	private long uniqueValCounter = 0;

	/**
	 * Creates a new R instance that can be fed commands
	 * @param rPath R executable to run
	 */
	private RProcessor(String rPath) throws RProcessorException
	{
		try
		{
			rProc = Runtime.getRuntime().exec(new String[]
					{
						rPath, "--slave", "--no-readline"
					});
			procOut = new BufferedReader(new InputStreamReader(rProc.getInputStream()));
			procIn = (BufferedOutputStream) rProc.getOutputStream();
		}
		catch(IOException ex)
		{
			throw new RProcessorException("Unable to initialize R processor", ex);
		}
	}

	/**
	 * Creates a new instance of R which can be fed commands. Assumes R is accessible on the path
	 * @return Instance of RProcessor that can be used for calculations
	 * @throws RProcessorException Thrown in the R process cannot be located and/or launched
	 */
	public static RProcessor getInstance() throws RProcessorException
	{
		try
		{
			return getInstance("R");
		}
		catch(RProcessorException ex)
		{
			// Try to find it another way?
			throw ex;
		}
	}

	/**
	 * Creates a new instance of R at the given path which can be fed commands
	 * @param rPath Path to the R executable
	 * @return Instance of RProcessor that can be used for calculations
	 * @throws RProcessorException Unable to load R processor
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
	 * Ensures that R is killed cleanly if at all possible
	 */
	public void destroy()
	{
		try
		{
			execute("q()");
			procIn.close();
			procOut.close();
			rProc.waitFor();
		}
		catch(Exception ex)
		{
			// Don't care, kill it forcibly
			rProc.destroy();
		}
	}

	public void close()
	{
		try
		{
			// Tell R we're closing, then close everything out
			execute("q()");
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
			procIn = null;
			procOut = null;
			rProc = null;
		}
	}

	/**
	 * Passes the given string onto R just as if you typed it at the command line. Only a single
	 * command may be executed by this command. If the user wants to run multiple commands as a
	 * group, use execute(ArrayList<String>). The command will be automatically terminated with
	 * a newline if it does not have one.
	 * @param cmd R command to execute
	 * @return String output from R. Use one of the parse functions to processor further
	 * @throws RProcessorException Thrown if called with more than one command
	 */
	public String execute(String cmd) throws RProcessorException
	{
		try
		{
			StringBuilder sentinelCmd = new StringBuilder(cmd);

			// Only allow a single command to be run by this method
			// Use this opportunity to append a newline if needed
			int newLineLoc = cmd.indexOf('\n');
			if(newLineLoc == -1)
				sentinelCmd.append('\n');
			else if(newLineLoc != cmd.length() - 1)
				throw new RProcessorException("Only a single command may be run at a time with execute(String)");

			// Record if needed
			if(recordMode == RecordMode.CMDS_ONLY || recordMode == RecordMode.FULL)
				interactionRecord.append(sentinelCmd);

			// Send command with a sentinel at the end so we know when the output is done
			sentinelCmd.append(this.SENTINEL_STRING_CMD);
			byte[] cmdArray = sentinelCmd.toString().getBytes();
			procIn.write(cmdArray, 0, cmdArray.length);
			procIn.flush();

			// Get results back
			StringBuilder results = new StringBuilder();
			String line = procOut.readLine();
			while(line != null && !line.equals(this.SENTINEL_STRING_RETURN))
			{
				results.append(line);
				results.append('\n');
				line = procOut.readLine();
			}

			// Record interaction if needed
			if(recordMode == RecordMode.OUTPUT_ONLY || recordMode == RecordMode.FULL)
				interactionRecord.append(results);

			// Return results, the caller is responsible for processing further
			return results.toString();
		}
		catch(IOException ex)
		{
			// Unable to read/write to pipes. Try to create new R instance and try again, then die
			throw new RProcessorException("Unable to read or write to the R instance", ex);
		}
	}

	/**
	 * Calls execute(String) for each of the commands given in the cmds array. Commands will
	 * be automatically terminated with a newline if they does not have one.
	 * @param cmds List of R commands to execute
	 * @return ArrayList of Strings, where each entry is the output from one of the commands given.
	 * @throws RProcessorException Thrown if one of the commands in cmds contains more than
	 *		one command or we encounter an error executing command itself
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
	 * Convenience function that executes the given command and parses the result as a double
	 * @param cmd R command to execute
	 * @return Double value of the R call
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a single double
	 *		value. This includes if the output has multiple doubles
	 */
	public Double executeDouble(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseDouble(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a double
	 * @param cmd R command to execute
	 * @return ArrayList of doubles that the R command returned
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a vector of
	 *		numerical values.
	 */
	public ArrayList<Double> executeDoubleArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseDoubleArray(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a string
	 * @param cmd R command to execute
	 * @return String value of the R call
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a single string
	 *		value. This includes if the output has multiple string
	 */
	public String executeString(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseString(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a string
	 * @param cmd R command to execute
	 * @return ArrayList of strings that the R command returned
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a vector of
	 *		strings
	 */
	public ArrayList<String> executeStringArray(String cmd) throws RProcessorException, RProcessorParseException
	{
		return parseStringArray(execute(cmd));
	}

	/**
	 * Runs the given command and saves it into a new, unique variable. The variable name used
	 * is returned as a string.
	 * @param cmd R command to execute
	 * @return R variable name that contains the results of the executed command
	 * @throws RProcessorException Thrown if called with more than one command
	 */
	public String executeSave(String cmd) throws RProcessorException
	{
		String varName = getUniqueName();
		execute(varName + " = " + cmd);
		return varName;
	}

	/**
	 * Takes the given R output and attempts to parse it as a single double val
	 * @param rOutput R output, as returned by execute(String)
	 * @return Double value contained in the output
	 * @throws RProcessorParseException Thrown if the given output does not contain a single double
	 *		value. This includes if the output has multiple doubles
	 */
	public Double parseDouble(String rOutput) throws RProcessorParseException
	{
		ArrayList<Double> arr = parseDoubleArray(rOutput);

		if(arr.size() != 1)
			throw new RProcessorParseException("The R result was not a single double value");

		return arr.get(0);
	}

	/**
	 * Takes the given R output and attempts to parse it as an array of doubles
	 * @param rOutput R output, as returned by execute(String)
	 * @return ArrayList of Doubles from the output
	 * @throws RProcessorParseException Thrown if the output does not contain double values
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
	 * Takes the given R output and attempts to parse it as a single string value
	 * @param rOutput R output, as returned by execute(String)
	 * @return String value contained in the output
	 * @throws RProcessorParseException Thrown if the given output does not contain a single string
	 *		value. This includes if the output has multiple strings
	 */
	public String parseString(String rOutput) throws RProcessorParseException
	{
		ArrayList<String> arr = parseStringArray(rOutput);

		if(arr.size() != 1)
			throw new RProcessorParseException("The R result was not a single string value");

		return arr.get(0);
	}

	/**
	 * Takes the given R output and attempts to parse it as an array of strings
	 * @param rOutput R output, as returned by execute(String)
	 * @return ArrayList of Strings from the output
	 * @throws RProcessorParseException Thrown if the output does not contain string values
	 */
	public ArrayList<String> parseStringArray(String rOutput) throws RProcessorParseException
	{
		ArrayList<String> vals = new ArrayList<String>();

		try
		{
			Matcher m = stringPatt.matcher(rOutput);

			while(m.find())
			{
				vals.add(m.group());
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
	 * @throws RProcessorException Thrown if an internal error occur
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
	 * @throws RProcessorException Thrown if an internal error occur
	 */
	public String setVariable(Double val) throws RProcessorException
	{
		return setVariable(getUniqueName(), val);
	}

	/**
	 * Sets a new unique variable with a vector of the values given
	 * @param vals Array of values to store in the variable
	 * @return Name of the variable used
	 * @throws RProcessorException Thrown if an internal error occur
	 */
	public String setVariable(List<Double> vals) throws RProcessorException
	{
		return setVariable(getUniqueName(), vals);
	}

	/**
	 * Sets the given variable with a vector of the values given
	 * @param name R-conforming variable name
	 * @param vals Array of values to store in the variable
	 * @return Name of the variable used
	 * @throws RProcessorException Thrown if an internal error occur
	 */
	public String setVariable(String name, List<Double> vals) throws RProcessorException
	{
		// Builds an R command to set the given variable name with the values in the array
		StringBuilder cmd = new StringBuilder();
		cmd.append(name);
		cmd.append(" = c(");

		for(Double val : vals)
		{
			cmd.append(val);
			cmd.append(", ");
		}
		cmd.replace(cmd.length() - 2, cmd.length() - 1, "");
		cmd.append(")\n");

		// Run R command
		execute(cmd.toString());

		return name;
	}

	/**
	 * Sets the recording mode for the processor
	 * @param mode RecordMode to place the processor in. 
	 */
	public void setRecorder(RecordMode mode)
	{
		recordMode = mode;
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

		return "marlaUniqueVar" + uniqueValCounter;
	}

	public static void main(String[] args) throws Exception
	{
		RProcessor test = RProcessor.getInstance();

		test.setRecorder(RecordMode.CMDS_ONLY);

		String output = test.execute("mean(c(-1, -2, -3))");
		System.out.println(output);
		System.out.println("Parses to: " + test.parseDouble(output));

		output = test.execute("1:10");
		System.out.println(output);
		System.out.println("Parses to: " + test.parseDoubleArray(output));

		output = test.execute("\"test\"");
		System.out.println(output);
		System.out.println("Parses to: " + test.parseString(output));

		output = test.executeSave("summary(1:200)");
		System.out.println(output);
		String outputNames = test.execute("attr(" + output + ", \"names\")");
		System.out.println(outputNames);
		System.out.println("Parses to: " + test.parseStringArray(outputNames));
		String outputValues = test.execute(output);
		System.out.println(outputValues);
		System.out.println("Parses to: " + test.parseDoubleArray(outputValues));

		System.out.println("\n\n------\nInteractions");
		System.out.println(test.fetchInteraction());

		test.close();
	}
}
