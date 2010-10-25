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
package problem;

import java.io.FileNotFoundException;
import org.jdom.JDOMException;
import r.OperationNOP;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Problem
 *
 * @author Ryan Morehart
 */
public class ProblemTest
{

	private RandomValues random = new RandomValues();
	private String tempFileName = "testing_temp_file.marlatemp";

	/**
	 * Test of getStatement and setStatement method, of class Problem.
	 */
	@Test
	public void testStatement()
	{
		// Quick test to make sure when you set it in the constructor it pops
		// out the same
		String name = random.nextString();
		Problem instance = new Problem(name);
		assertEquals(instance.getStatement(), name);

		for(int i = 0; i < 50; i++)
		{
			name = random.nextString();
			instance.setStatement(name);
			assertEquals(instance.getStatement(), name);
		}
	}

	/**
	 * Test of addData() accepting a pre-created DataSet with
	 * no pre-set parent
	 */
	@Test
	public void testAddDataNoParent_DataSet()
	{
		Problem instance = new Problem();

		// Data where the parent wasn't set explicitly beforehand
		DataSet testData1 = new DataSet("Test1");
		DataSet returnedData1 = instance.addData(testData1);
		assertEquals(testData1, returnedData1);

		DataSet retrievedData1;
		try
		{
			retrievedData1 = instance.getData("Test1");
			assertEquals(testData1, retrievedData1);
		}
		catch(DataNotFound ex)
		{
			fail("Unable to retreive added data");
		}
	}

	/**
	 * Test of addData() accepting a pre-created DataSet with the parent set
	 */
	@Test
	public void testAddDataWithParent_DataSet()
	{
		Problem instance = new Problem();

		// Data where the parent wasn't set explicitly beforehand
		DataSet testData1 = new DataSet(instance, "Test1");
		DataSet returnedData1 = instance.addData(testData1);
		assertEquals(testData1, returnedData1);

		DataSet retrievedData1;
		try
		{
			retrievedData1 = instance.getData("Test1");
			assertEquals(testData1, retrievedData1);
		}
		catch(DataNotFound ex)
		{
			fail("Unable to retreive added data");
		}
	}

	/**
	 * Test of getData method, of class Problem.
	 */
	@Test
	public void testGetData()
	{
		Problem instance = new Problem();

		// Insert a whole bunch of datasets
		ArrayList<String> names = new ArrayList<String>();
		HashMap<String, DataSet> inserted = new HashMap<String, DataSet>();
		for(int i = 0; i < 50; i++)
		{
			String newName = random.nextString();
			names.add(newName);

			DataSet ds = new DataSet(newName);
			instance.addData(ds);

			// Save somewhere else so we can see if they come out the same
			inserted.put(newName, ds);
		}

		// Retrieve data back out... twice. Ha.
		for(int j = 0; j < 2; j++)
		{
			for(int i = 0; i < names.size(); i++)
			{
				String lookingFor = names.get(i);

				try
				{
					DataSet ds = instance.getData(lookingFor);
					assertEquals(ds, inserted.get(lookingFor));
				}
				catch(DataNotFound ex)
				{
					fail("Unable to locate the inserted data: '" + lookingFor + "'");
				}
			}
		}
	}

	/**
	 * Test of setFileName and getFileName methods, of class Problem.
	 *
	 * Tries many times with random names
	 */
	@Test
	public void testFileName()
	{
		Problem instance = new Problem();
		for(int i = 0; i < 50; i++)
		{
			String name = random.nextString();
			instance.setFileName(name);
			assertEquals(instance.getFileName(), name);
		}
	}

	/**
	 * Test of isSaved and isChanged method, of class Problem.
	 */
	@Test
	public void testIsSaved()
	{
		Problem instance = new Problem();

		// Newly created problem, should be unsaved
		assertFalse(instance.isSaved());
		assertTrue(instance.isChanged());

		// Save it to a temporary file
		instance.setFileName(tempFileName);

		try
		{
			instance.save();
		}
		catch(FileException ex)
		{
			fail("Save failed with the error: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			fail("IO Exception: " + ex.getMessage());
		}

		// Should be saved nicely now
		assertTrue(instance.isSaved());
		assertFalse(instance.isChanged());

		// Now alter it, should be marked as changed
		instance.addData(new DataSet("Test"));
		assertFalse(instance.isSaved());
		assertTrue(instance.isChanged());

		// Save it, then change the file path. Should be marked as change
		try
		{
			instance.save();
			assertTrue(instance.isSaved());
			assertFalse(instance.isChanged());
		}
		catch(FileException ex)
		{
			fail("Save failed with the error: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			fail("IO Exception: " + ex.getMessage());
		}

		instance.setFileName("temp");
		instance.setFileName(tempFileName);
		assertFalse(instance.isSaved());
		assertTrue(instance.isChanged());
	}

	/**
	 * Test of markChanged method, of class Problem.
	 */
	@Test
	public void testMarkChanged()
	{
		Problem instance = new Problem();
		assertFalse(instance.isSaved());

		// Save it to a temporary file so it marks as saved
		instance.setFileName(tempFileName);

		try
		{
			instance.save();
			assertTrue(instance.isSaved());
		}
		catch(FileException ex)
		{
			fail("Save failed with the error: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			fail("IO Exception: " + ex.getMessage());
		}

		// Mark as changed and make sure it shows
		instance.markChanged();
		assertFalse(instance.isSaved());
	}

	/**
	 * Test of save and load methods, of class Problem.
	 */
	@Test
	public void testSave()
	{
		// Make a sort of complex Problem
		Problem instance = makeProblem();

		// Try saving without setting a path. This _should_ fail
		try
		{
			instance.save();
			fail("Somehow save() succeeded without a path. Hm.");
		}
		catch(FileException ex)
		{
		}
		catch(IOException ex)
		{
			fail("IO Exception: " + ex.getMessage());
		}

		// Save to disk, then read it back in. It should match up with
		// the problem we just made
		try
		{
			instance.setFileName(tempFileName);
			instance.save();
		}
		catch(FileException ex)
		{
			fail("File save failed with error: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			fail("Save IO Exception: " + ex.getMessage());
		}

		try
		{
			Problem readIn = Problem.load(tempFileName);
			assert(readIn.equals(instance));
		}
		catch(JDOMException ex)
		{
			fail("Save failed with the error: " + ex.getMessage());
		}
		catch(FileNotFoundException ex)
		{
			fail("Unable to locate file to load");
		}
		catch(IOException ex)
		{
			fail("Load IO Exception: " + ex.getMessage());
		}
	}

	/**
	 * Test of copy constructor of Problem
	 */
	@Test
	public void testCopy()
	{
		Problem instance = makeProblem();
		Problem copied = new Problem(instance);
		assert(instance.equals(copied));
	}

	/**
	 * Creates a populated problem that's relatively random for testing
	 * @return Newly created Problem
	 */
	private Problem makeProblem()
	{
		// Make a sort of complex Problem
		Problem instance = new Problem();

		instance.setStatement(random.nextString());

		for(int dsNum = 0; dsNum < 5; dsNum++)
		{
			DataSet ds = instance.addData(new DataSet(random.nextString()));

			for(int dcNum = 0; dcNum < 5; dcNum++)
			{
				DataColumn dc = ds.addColumn(random.nextString());

				for(int dataNum = 0; dataNum < 50; dataNum++)
				{
					dc.add(random.nextDouble());
				}
			}

			for(int opNum = 0; opNum < 5; opNum++)
			{
				Operation op = new OperationNOP();
				ds.addOperation(op);
			}
		}

		return instance;
	}
}
