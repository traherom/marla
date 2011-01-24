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

import problem.Operation.PromptType;
import java.util.List;
import org.jdom.Element;
import java.util.ArrayList;
import r.OperationXML;
import org.junit.runners.Parameterized.Parameters;
import java.util.Collection;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test each available operation in the same battery of hair-raising trials
 * @author Ryan Morehart
 */
@RunWith(Parameterized.class)
public class OperationTest
{
	private String opName = null;
	private DataSet ds1 = null;
	private Operation op1 = null;
	
	@Parameters
    public static Collection<Object[]> operationsAvailable() throws Exception
	{
		// Get the available operations
		OperationXML.loadXML("ops.xml");
		List<String> available = Operation.getAvailableOperations();

		// Massage into the right format and remove "NOP" (we don't want to test it here)
		Collection<Object[]> objectArray = new ArrayList<Object[]>();
		for(String op : available)
		{
			if(!op.equals("NOP"))
				objectArray.add(new Object[]{op});
		}

		return objectArray;
	}

	private void fillRequiredInfo(Operation op) throws MarlaException
	{
		List<Object[]> info = op.getRequiredInfoPrompt();

		// Fill with some BS. Not every operation is nicely handled with this approach
		// if it actually uses the data we're probably not going to have much fun
		List<Object> answers = new ArrayList<Object>();
		for(Object[] question : info)
		{
			PromptType questionType = (PromptType)question[0];
			switch(questionType)
			{
				case CHECKBOX:
					answers.add(true);
					break;

				case NUMBER:
					answers.add(50.0);
					break;

				case STRING:
					answers.add("test string");
					break;

				case COMBO:
				case COLUMN:
					// Choose one of the values they offered us
					Object[] options = (Object[])question[3];
					answers.add(options[0]);
					break;

				default:
					fail("Unrecognized question type '" + questionType + "'");
			}
		}

		op.setRequiredInfo(answers);
	}

	public OperationTest(String opName) throws OperationException
	{
		System.out.println("Testing operation '" + opName + "'");
		this.opName = opName;
	}

	@Before
	public void setUp() throws Exception
	{
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

		// Unable to do test if operation has a plot, as we can't
		// add children to it
		if(op2.hasPlot())
			return;

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
			fillRequiredInfo(op1);
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

			fillRequiredInfo(op1);

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
			fillRequiredInfo(op1);
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
			fillRequiredInfo(op1);
			
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
			fillRequiredInfo(op1);

			el = op1.toXml();
			op2 = Operation.fromXml(el);

			assertEquals(op1, op2);
		}
	}

	@Test
	public void testToRString() throws Exception
	{
		// Fill info if needed
		if(op1.isInfoRequired())
			fillRequiredInfo(op1);
		
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
