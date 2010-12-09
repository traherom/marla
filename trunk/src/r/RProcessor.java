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
	 * Pattern used to recognize doubles in R output, mainly for use with vectors
	 */
	private final Pattern doublePatt = Pattern.compile("(?<!\\[)-?[0-9]+(\\.[0-9]+)?(?!\\])");
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
	 * Single instance of RProcessor that we allow
	 */
	private static RProcessor singleRProcessor = null;

	/**
	 * Creates a new R instance that can be fed commands
	 * @param rPath R executable to run
	 * @throws IOException Thrown if R cannot be run
	 */
	private RProcessor(String rPath) throws IOException
	{
		rProc = Runtime.getRuntime().exec(new String[]
				{
					rPath, "--slave", "--no-readline"
				});
		procOut = new BufferedReader(new InputStreamReader(rProc.getInputStream()));
		procIn = (BufferedOutputStream) rProc.getOutputStream();
	}

	/**
	 * Creates a new instance of R which can be fed commands. Assumes R is accessible on the path
	 * @return Instance of RProcessor that can be used for calculations
	 * @throws IOException Thrown if R cannot be run
	 */
	public static RProcessor getInstance() throws IOException
	{
		return getInstance("R");
	}

	/**
	 * Creates a new instance of R at the given path which can be fed commands
	 * @param rPath Path to the R executable
	 * @return Instance of RProcessor that can be used for calculations
	 * @throws IOException Thrown if R cannot be run
	 */
	public static RProcessor getInstance(String rPath) throws IOException
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

	/**
	 * Passes the given string onto R just as if you typed it at the command line. Only a single
	 * command may be executed by this command. If the user wants to run multiple commands as a
	 * group, use execute(ArrayList<String>). The command will be automatically terminated with
	 * a newline if it does not have one.
	 * @param cmd R command to execute
	 * @return String output from R. Use one of the parse functions to processor further
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if called with more than one command
	 */
	public String execute(String cmd) throws IOException, RProcessorException
	{
		StringBuilder sentinelCmd = new StringBuilder(cmd);

		// Only allow a single command to be run by this method
		// Use this opportunity to append a newline if needed
		int newLineLoc = cmd.indexOf('\n');
		if(newLineLoc == -1)
			sentinelCmd.append('\n');
		else if(newLineLoc != cmd.length() - 1)
			throw new RProcessorException("Only a single command may be run at a time with execute(String)");

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

		// Process down to array if possible
		System.out.println(results.toString());
		return results.toString();
	}

	/**
	 * Calls execute(String) for each of the commands given in the cmds array. Commands will
	 * be automatically terminated with a newline if they does not have one.
	 * @param cmds List of R commands to execute
	 * @return ArrayList of Strings, where each entry is the output from one of the commands given.
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if one of the commands in cmds contains more than
	 *		one command
	 */
	public ArrayList<String> execute(ArrayList<String> cmds) throws IOException, RProcessorException
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
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a single double
	 *		value. This includes if the output has multiple doubles
	 */
	public Double executeDouble(String cmd) throws IOException, RProcessorException, RProcessorParseException
	{
		return parseDouble(execute(cmd));
	}

	/**
	 * Convenience function that executes the given command and parses the result as a double
	 * @param cmd R command to execute
	 * @return ArrayList of doubles that the R command returned
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if called with more than one command
	 * @throws RProcessorParseException Thrown if the given output does not contain a vector of
	 *		numerical values.
	 */
	public ArrayList<Double> executeDoubleArray(String cmd) throws IOException, RProcessorException, RProcessorParseException
	{
		return parseDoubleArray(execute(cmd));
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

	public static void main(String[] args) throws Exception
	{
		RProcessor test = RProcessor.getInstance();

		String output = test.execute("mean(c(-1, -2, -3))");
		System.out.println(output);
		System.out.println("Parses to: " + test.parseDouble(output));

		output = test.execute("1:70");
		System.out.println(output);
		System.out.println("Parses to: " + test.parseDoubleArray(output));
		/*
		System.out.println(test.execute("sd(c(5, 5, 6))"));
		System.out.println(test.execute("t.test(c(5, 5, 6))"));
		System.out.println(test.execute("plot(c(5, 5, 6))"));

		ArrayList<String> batch = new ArrayList<String>();
		batch.add("mean(c(5, 5, 6))");
		batch.add("sd(c(5, 5, 6))");
		batch.add("t.test(c(5, 5, 6))");
		System.out.println(test.execute(batch));*/
	}
}