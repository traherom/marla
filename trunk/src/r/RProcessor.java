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
	 * Creates a new instance of R which can be fed commands. Assumes R is accessible on the path
	 * @throws IOException Thrown if R cannot be run
	 */
	public RProcessor() throws IOException
	{
		this("R");
	}

	/**
	 * Creates a new R instance that can be fed commands
	 * @param rPath R executable to run
	 * @throws IOException Thrown if R cannot be run
	 */
	public RProcessor(String rPath) throws IOException
	{
		rProc = Runtime.getRuntime().exec(new String[]
				{
					rPath, "--slave", "--no-readline"
				});
		procOut = new BufferedReader(new InputStreamReader(rProc.getInputStream()));
		procIn = (BufferedOutputStream) rProc.getOutputStream();
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
	 * @return ArrayList of Doubles if the results of the command is a simple numerical vector.
	 *		Null otherwise.
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if called with more than one command
	 */
	public ArrayList<Double> execute(String cmd) throws IOException, RProcessorException
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
			System.out.println("READ: " + line);
			line = procOut.readLine();
		}

		// Process down to array if possible
		System.out.println(results.toString());
		return null;
	}

	/**
	 * Passes the given string onto R just as if you typed it at the command line. Only a single
	 * command may be executed by this command. If the user wants to run multiple commands as a
	 * group, use execute(ArrayList<String>). The command will be automatically terminated with
	 * a newline if it does not have one.
	 * @param cmds List of R commands to execute
	 * @return ArrayList of Doubles if the results of the final command is a simple numerical vector.
	 *		Null otherwise.
	 * @throws IOException Thrown if an error reading or writing to the pipes attached to R
	 * @throws RProcessorException Thrown if one of the commands in cmds contains more than
	 *		one command
	 */
	public ArrayList<Double> execute(ArrayList<String> cmds) throws IOException, RProcessorException
	{
		ArrayList<Double> output = null;
		for(String cmd : cmds)
		{
			output = execute(cmd);
		}
		return output;
	}

	public static void main(String[] args) throws Exception
	{
		RProcessor test = new RProcessor();

		System.out.println(test.execute("mean(c(5, 5, 6))"));
		System.out.println(test.execute("sd(c(5, 5, 6))"));
		System.out.println(test.execute("t.test(c(5, 5, 6))"));

		ArrayList<String> batch = new ArrayList<String>();
		batch.add("mean(c(5, 5, 6))");
		batch.add("sd(c(5, 5, 6))");
		batch.add("t.test(c(5, 5, 6))");
		System.out.println(test.execute(batch));
	}
}
