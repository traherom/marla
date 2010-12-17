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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author Ryan Morehart
 */
public class InputStreamCombine extends PipedReader
{
	/**
	 * Active threads so we can tell them all to die if we're told to close
	 */
	private final ArrayList<StreamThread> threads = new ArrayList<StreamThread>();
	/**
	 * Pipe that writes to this stream
	 */
	private PipedWriter pipe = null;
	/**
	 * Buffered version of the writer
	 */
	private BufferedWriter pipeBuff = null;

	/**
	 * Creates a new stream combiner
	 * @throws IOException Thrown if unable to wrap components with appropriate stream layers
	 */
	public InputStreamCombine() throws IOException
	{
		pipe = new PipedWriter(this);
		pipeBuff = new BufferedWriter(pipe);
	}

	/**
	 * Adds another stream to combine with others.
	 * @param is New input stream to add
	 * @throws IOException A error working with the stream occurred
	 */
	public void addStream(InputStream is) throws IOException
	{
		StreamThread gobbler = new StreamThread(pipeBuff, is);
		gobbler.start();
		threads.add(gobbler);
	}

	/**
	 * Closes all threads and streams associated with this stream combiner
	 * @throws IOException Thrown if an error occurs during closing
	 */
	@Override
	public void close() throws IOException
	{
		for(StreamThread t : threads)
		{
			if(t.isAlive())
			{
				t.finish();
				threads.remove(t);
			}
		}

		// Close out pipes
		pipeBuff = null;
		pipe = null;

		super.close();
	}

	/**
	 * Ensures all threads and streams associated with this stream combiner
	 * are good and dead
	 */
	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}

	/**
	 * Used to continuously read in from a stream and pump into into the pipe
	 * it receives.
	 */
	private class StreamThread extends Thread
	{
		private InputStream in = null;
		private Writer out = null;
		private boolean shouldRun = true;

		/**
		 * Creates a new StreamThread timed to the given streams.
		 * @param outputPipe The Writer (probably a PipedWriter) that we should
		 *			output to.
		 * @param inputStream The input we'll be watching. Anything new on it gets
		 *			pushed to pipe
		 */
		StreamThread(Writer outputPipe, InputStream inputStream)
		{
			in = inputStream;
			out = outputPipe;
		}

		/**
		 * Tries to force run() to terminate
		 */
		public void finish()
		{
			try
			{
				shouldRun = false;
				in.close();
				out = null;
			}
			catch(IOException ex)
			{
				// Ignore, it'll die
			}
		}

		/**
		 * Watches the input stream and puts any new bytes on it straight into
		 * the output pipe.
		 */
		@Override
		public void run()
		{
			try
			{
				while(shouldRun)
				{
					out.write(in.read());
					out.flush();
				}
			}
			catch(IOException ex)
			{
				// Only throw if we weren't instructed to die
				if(shouldRun)
					throw new RuntimeException("Input stream died", ex);
			}
			catch(NullPointerException ex)
			{
				// Somebody closed our streams on us, which
				// means R was killed. Carry on, nothing to see here
			}
		}
	}
}
