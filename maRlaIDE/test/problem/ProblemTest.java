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

import org.jdom.Element;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for Problem
 *
 * @author Ryan Morehart
 */
public class ProblemTest
{
	private String tempFileName = "testing_temp_file.marlatemp";

	@BeforeClass
	public static void initOperations() throws Exception
	{
		r.OperationXML.loadXML("ops.xml");
	}

	public static Problem createProblem(int dataSetNum, int colNum, int valNum) throws Exception
	{
		Problem prob = new Problem("Test Problem");

		// Add DataSets
		for(int dsNum = 0; dsNum < dataSetNum; dsNum++)
		{
			prob.addData(DataSetTest.createDataSet(colNum, valNum, 1));
		}

		// Make sure it built correctly
		assertEquals(dataSetNum, prob.getDataCount());

		return prob;
	}

	@Test
	public void testEquals() throws Exception
	{
		Problem prob1 = createProblem(2, 3, 10);
		Problem prob2 = createProblem(2, 3, 10);
		assertEquals(prob1, prob2);
	}

	@Test
	public void testEqualsDifferentStatement() throws Exception
	{
		Problem prob1 = createProblem(2, 3, 10);
		Problem prob2 = createProblem(2, 3, 10);
		prob2.setStatement("different statement");
		assertFalse(prob1.equals(prob2));
	}

	@Test
	public void testEqualsDifferentData() throws Exception
	{
		Problem prob1 = createProblem(2, 3, 10);
		Problem prob2 = createProblem(2, 3, 10);
		prob2.addData(DataSetTest.createDataSet(3, 5, 1));
		assertFalse(prob1.equals(prob2));
	}

	@Test
	public void testEqualsDifferentSubProblem() throws Exception
	{
		Problem prob1 = createProblem(2, 3, 10);
		Problem prob2 = createProblem(2, 3, 10);
		prob2.addSubProblem("New Part", "sub problem statement");
		assertFalse(prob1.equals(prob2));
	}

	@Test
	public void testStatement()
	{
		Problem prob = new Problem("Test statement");
		assertEquals("Test statement", prob.getStatement());

		prob.setStatement("Test 2");
		assertEquals("Test 2", prob.getStatement());
	}

	@Test
	public void testAddDataNoParent() throws Exception
	{
		Problem prob = new Problem();

		DataSet testDS1 = DataSetTest.createDataSet(5, 50, 1);
		assertEquals(null, testDS1.getParentProblem());
		prob.addData(testDS1);

		// Make sure it changed the changed the parent of the dataset
		// and that we can retreive it
		assertEquals(prob, testDS1.getParentProblem());
		assertEquals(testDS1, prob.getData(0));
		assertEquals(1, prob.getDataCount());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testAddDataWithParent() throws Exception
	{
		// Stick a dataset in one problem
		Problem prob1 = new Problem();
		DataSet ds1 = DataSetTest.createDataSet(5, 50, 1);
		prob1.addData(ds1);
		assertEquals(prob1, ds1.getParentProblem());

		// And assign to a new location
		Problem prob2 = new Problem();
		prob2.addData(ds1);

		// Should be set to the new location and the old problem shouldn't
		// know about it any more
		assertEquals(prob2, ds1.getParentProblem());
		assertEquals(0, prob1.getDataCount());
		assertEquals(1, prob2.getDataCount());
		assertEquals(ds1, prob2.getData(0));
		
		prob1.getData(0); // Should throw exception
	}

	@Test(expected=DataNotFoundException.class)
	public void testGetData() throws Exception
	{
		Problem prob = new Problem();

		DataSet ds1 = DataSetTest.createDataSet(5, 50, 1);
		ds1.setDataName("test 1");
		prob.addData(ds1);

		DataSet ds2 = DataSetTest.createDataSet(5, 50, 1);
		ds1.setDataName("test 2");
		prob.addData(ds2);

		assertEquals(ds1, prob.getData(0));
		assertEquals(ds1, prob.getData("test 1"));
		assertEquals(ds2, prob.getData(1));
		assertEquals(ds2, prob.getData("test 2"));

		// Should fail
		prob.getData("test 3");
	}

	@Test
	public void testIsSavedSimple() throws Exception
	{
		// Newly created problem, should be unsaved
		Problem instance = createProblem(2, 3, 10);
		assertFalse(instance.isSaved());

		// Save it to a temporary file and check it's now marked saved
		instance.setFileName(tempFileName);
		instance.save();
		assertTrue(instance.isSaved());
	}

	@Test
	public void testIsSavedAddData() throws Exception
	{
		// Newly created problem, should be unsaved
		Problem instance = createProblem(2, 3, 10);
		assertFalse(instance.isSaved());

		// Save it to a temporary file and check it's now marked saved
		instance.setFileName(tempFileName);
		instance.save();
		assertTrue(instance.isSaved());

		// Now add a dataset, should be marked as changed
		instance.addData(new DataSet("Test"));
		assertFalse(instance.isSaved());
	}

	@Test
	public void testIsSavedChangePath() throws Exception
	{
		// Newly created problem, should be unsaved
		Problem instance = createProblem(2, 3, 10);
		assertFalse(instance.isSaved());

		// Save it to a temporary file and check it's now marked saved
		instance.setFileName(tempFileName);
		instance.save();
		assertTrue(instance.isSaved());

		// Now add a column, should be marked as changed
		instance.setFileName("different");
		assertFalse(instance.isSaved());
	}

	@Test
	public void testIsSavedChangeData() throws Exception
	{
		// Newly created problem, should be unsaved
		Problem instance = createProblem(2, 3, 10);
		assertFalse(instance.isSaved());

		// Save it to a temporary file and check it's now marked saved
		instance.setFileName(tempFileName);
		instance.save();
		assertTrue(instance.isSaved());

		// Now prentend we changed a value is a dataset we hold
		instance.getData(0).markChanged();
		assertFalse(instance.isSaved());
	}

	@Test(expected=FileException.class)
	public void testSaveAndLoadNoPath() throws Exception
	{
		// Make a sort of complex Problem
		Problem prob = createProblem(2, 3, 4);

		// Try saving without setting a path. This _should_ fail
		prob.save();
		fail("Somehow save() succeeded without a path.");
	}

	@Test
	public void testToAndFromXML() throws Exception
	{
		// Make a sort of complex Problem
		Problem prob = createProblem(2, 3, 4);

		Element el = prob.toXml();
		Problem newProb = Problem.fromXml(el);
		
		assertEquals(prob, newProb);
	}

	@Test
	public void testSaveAndLoad() throws Exception
	{
		// Make a sort of complex Problem
		Problem prob = createProblem(2, 3, 4);

		// Save to disk, then read it back in. It should match up with
		// the problem we just made
		prob.setFileName(tempFileName);
		prob.save();

		Problem readInProb = Problem.load(tempFileName);
		assertEquals(prob, readInProb);
	}

	@Test
	public void testCopy() throws Exception
	{
		Problem instance = createProblem(3, 4, 10);
		Problem copied = new Problem(instance);
		assertEquals(instance, copied);
	}
}
