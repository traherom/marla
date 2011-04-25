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
package marla.ide.operation;

import marla.ide.resource.Configuration;
import org.jdom.Element;
import org.junit.*;
import marla.ide.problem.DataSet;
import marla.ide.problem.DataSetTest;
import marla.ide.problem.MarlaException;
import marla.ide.r.RProcessor;
import static org.junit.Assert.*;

/**
 * Test a very basic operation, exercising the abstract Operation more than anything
 * @author Ryan Morehart
 */
public class OperationTest
{
	private String opName = null;
	private DataSet ds1 = null;
	private Operation op1 = null;

	public OperationTest() throws OperationException
	{
		this.opName = "NOP";
		System.out.println("Testing operation '" + opName + "'");
	}

	@BeforeClass
	public static void configure() throws MarlaException
	{
		Configuration.load();
	}

	@Before
	public void setUp() throws Exception
	{
		// Ensure everything shows
		RProcessor.setDebugMode(RProcessor.RecordMode.FULL);

		// Create fake dataset to work with
		ds1 = DataSetTest.createDataSet(4, 10, 0);
		op1 = Operation.createOperation(opName);
		op1.setRemark("FAKE REMARK");
		ds1.addOperation(op1);
	}

	@Test
	public void testEquals() throws Exception
	{
		Operation op2 = Operation.createOperation(opName);
		op2.setRemark(op1.getRemark());
		assertTrue(op1.equals(op2));
	}

	@Test
	public void testEqualsDifferentOps() throws Exception
	{
		Operation op2 = Operation.createOperation(Operation.getAvailableOperationsList().get(0));
		ds1.addOperation(op2);

		assertFalse(op1.equals(op2));
	}

	@Test
	public void testEqualsDifferentParentData() throws Exception
	{
		DataSet ds2 = DataSetTest.createDataSet(3, 5, 0);
		assertFalse(ds1.equals(ds2));
		Operation op2 = Operation.createOperation(opName);
		op2.setRemark(op1.getRemark());
		ds2.addOperation(op2);

		// TODO determine if this should be equal or not. I could see the argument both ways
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
	public void testCheckCache() throws Exception
	{
		// Cache should be dirty and nothing computed
		assertTrue(op1.isDirty());

		// Tell it to check
		if(op1.isInfoUnanswered())
			op1.fakeFillRequiredInfo();
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
		if(op1.isInfoUnanswered())
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

			op1.fakeFillRequiredInfo();

			// Now it should compute fine
			op1.checkCache();
		}
		else
		{
			// No info was required, it had better not error out on us
			op1.checkCache();
		}
	}

	@Test
	public void testPlot() throws Exception
	{
		// Fill info if needed
		if(op1.isInfoUnanswered())
			op1.fakeFillRequiredInfo();
			
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
			op1.fakeFillRequiredInfo();

			el = op1.toXml();
			op2 = Operation.fromXml(el);

			assertEquals(op1, op2);
		}
	}

	@Test
	public void testToRString() throws Exception
	{
		// Fill info if needed
		if(op1.isInfoUnanswered())
			op1.fakeFillRequiredInfo();
		
		String opStr = op1.getRCommands(true);
		assertFalse(opStr.isEmpty());
	}

	@Test
	public void testParentAssignment() throws Exception
	{
		DataSet ds2 = DataSetTest.createDataSet(3, 5, 0);
		Operation op2 = Operation.createOperation(opName);

		assertEquals(null, op2.getParentData());
		assertEquals(0, ds2.getOperationCount());

		ds2.addOperation(op2);

		assertEquals(ds2, op2.getParentData());
		assertEquals(1, ds2.getOperationCount());
		assertEquals(op2, ds2.getOperation(0));
	}
}
