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

import resource.Configuration;
import problem.*;
import java.util.List;
import org.jdom.Element;
import java.util.ArrayList;
import org.junit.runners.Parameterized.Parameters;
import java.util.Collection;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.*;
import static org.junit.Assert.*;
import resource.ConfigurationException;

/**
 * Test each available operation in the same battery of hair-raising trials
 * @author Ryan Morehart
 */
@RunWith(Parameterized.class)
public class ImplementedOperationTest
{
	private String opName = null;
	private DataSet ds1 = null;
	private Operation op1 = null;
	
	@Parameters
    public static Collection<Object[]> operationsAvailable() throws Exception
	{
		// Get the available operations
		List<String> available = Operation.getAvailableOperationsList();

		// Massage into the right format and output so we know the index references
		System.out.println("Testing operation array: (" + available.size() + " operations)");
		Collection<Object[]> objectArray = new ArrayList<Object[]>(available.size());
		for(int i = 0; i < available.size(); i++)
		{
			System.out.println("  [" + i + "]: " + available.get(i));
			objectArray.add(new Object[]{available.get(i)});
		}

		return objectArray;
	}

	@BeforeClass
	public static void configureR() throws ConfigurationException
	{
		RProcessor.setRLocation(Configuration.findR());
	}

	public ImplementedOperationTest(String opName) throws OperationException
	{
		System.out.println("Testing operation '" + opName + "'");
		this.opName = opName;
	}

	@BeforeClass
	public static void printOpArray() throws Exception
	{
		ImplementedOperationTest.operationsAvailable();
	}

	@Before
	public void setUp() throws Exception
	{
		// Ensure everything shows
		RProcessor.getInstance().setDebugMode(RProcessor.RecordMode.FULL);

		// Create fake dataset to work with
		ds1 = DataSetTest.createDataSet(4, 10, 0);
		op1 = Operation.createOperation(opName);
		ds1.addOperation(op1);
	}

	@Test
	public void testEquals() throws Exception
	{
		Operation op2 = Operation.createOperation(opName);
		assertTrue(op1.equals(op2));
	}

	@Test
	public void testEqualsDifferentOps() throws Exception
	{
		Operation op2 = Operation.createOperation("NOP");
		ds1.addOperation(op2);

		assertFalse(op1.equals(op2));
	}

	@Test
	public void testEqualsDifferentParentData() throws Exception
	{
		DataSet ds2 = DataSetTest.createDataSet(3, 5, 0);
		assertFalse(ds1.equals(ds2));
		Operation op2 = Operation.createOperation(opName);
		ds2.addOperation(op2);

		// TODO determine if this should be equal or not. I could see the arguement both ways
		assertEquals(op1, op2);
	}

	@Test
	public void testEqualsDifferentChildren() throws Exception
	{
		Operation op2 = Operation.createOperation(opName);
		ds1.addOperation(op2);

		Operation op3 = Operation.createOperation(opName);
		op2.addOperation(op3);

		assertFalse(op1.equals(op2));
	}

	@Test
	public void testClone() throws Exception
	{
		Operation op2 = op1.clone();

		assertEquals(op1, op2);
	}

	@Test
	public void testCheckCache() throws Exception
	{
		// Cache should be dirty and nothing computed
		assertTrue(op1.isDirty());

		// Tell it to check
		if(op1.isInfoRequired())
			OperationTester.fillRequiredInfo(op1);
		op1.checkCache();

		assertFalse(op1.isDirty());
		if(!op1.hasPlot())
		{
			// Should be full now
			assertFalse(op1.getColumnCount() == 0);
		}
		else
		{
			// Should have a plot
			assertFalse(op1.getPlot().isEmpty());
		}
	}

	@Test
	public void testInfo() throws Exception
	{
		if(op1.isInfoRequired())
		{
			try
			{
				op1.checkCache();
				fail("An exception should have been thrown, required info not set yet");
			}
			catch(OperationInfoRequiredException ex)
			{
				// Good
			}

			OperationTester.fillRequiredInfo(op1);

			// Now it should compute fine
			op1.checkCache();
		}
		else
		{
			// No info was required, it had better not error out on us
			op1.checkCache();
		}
	}

	@Test(expected=OperationException.class)
	public void testSetInfoWhenNoneRequired() throws Exception
	{
		if(!op1.isInfoRequired())
		{
			OperationTester.fillRequiredInfo(op1);
		}
		else
		{
			throw new OperationException("Info is expected, this test passes");
		}
	}

	@Test
	public void testPlot() throws Exception
	{
		// Fill info if needed
		if(op1.isInfoRequired())
			OperationTester.fillRequiredInfo(op1);
			
		if(op1.hasPlot())
		{
			assertTrue(!op1.getPlot().isEmpty());
		}
		else
		{
			// It doesn't report a plot, shouldn't return one
			op1.checkCache();
			assertEquals(null, op1.getPlot());
		}
	}

	@Test
	public void testToAndFromXML() throws Exception
	{
		Element el = op1.toXml();
		Operation op2 = Operation.fromXml(el);
		assertEquals(op1, op2);

		if(op1.isInfoRequired())
		{
			// Do again with the info assigned
			OperationTester.fillRequiredInfo(op1);

			el = op1.toXml();
			op2 = Operation.fromXml(el);

			assertEquals(op1, op2);
		}
	}
}
