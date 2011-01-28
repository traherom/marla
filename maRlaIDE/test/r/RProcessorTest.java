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

import java.util.List;
import java.io.File;
import problem.DataColumn.DataMode;
import problem.DataColumn;
import problem.DataColumnTest;
import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Ryan Morehart
 */
public class RProcessorTest
{
	private static RProcessor proc = null;

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		proc = RProcessor.restartInstance();
	}

	@Before
	public void setUp() throws Exception
	{
		// Restart processor if needed
		if(!proc.isRunning())
			proc = RProcessor.restartInstance();
	}

	@Test(expected=RProcessorDeadException.class)
	public void testCloseExecute() throws Exception
	{
		assertTrue(proc.isRunning());
		proc.close();
		assertFalse(proc.isRunning());
		proc.execute("'a'");
	}

	@Test
	public void testCloseDouble()
	{
		assertTrue(proc.isRunning());
		proc.close();
		assertFalse(proc.isRunning());
		proc.close();
		assertFalse(proc.isRunning());
	}

	@Test
	public void testExecute() throws Exception
	{
		String result = proc.execute("print('test')");
		assertEquals("[1] \"test\"\n", result);
	}

	@Test
	public void testExecuteSave() throws Exception
	{
	}

	@Test
	public void testParseDouble() throws Exception
	{
		Double d = proc.parseDouble("[1] 7.889\n");
		assertEquals(new Double(7.889), d);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseDoubleInvalidExtraValues() throws Exception
	{
		proc.parseDouble("[1]  7.889 12.900\n");
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseDoubleInvalidString() throws Exception
	{
		proc.parseDouble("[1]  \"haha\"\n");
	}

	@Test
	public void testParseDoubleArray() throws Exception
	{
		ArrayList<Double> expectedVals = new ArrayList<Double>();
		expectedVals.add(7.889);
		expectedVals.add(12.9);
		expectedVals.add(89.902);
		expectedVals.add(2.492);
		expectedVals.add(90.742);

		ArrayList<Double> vals = proc.parseDoubleArray("[1]  7.889 12.900 89.902\n[2]  2.492 90.742\n");
		assertEquals(expectedVals, vals);
	}

	@Test
	public void testParseDoubleArraySingleValue() throws Exception
	{
		ArrayList<Double> expectedVals = new ArrayList<Double>();
		expectedVals.add(7.889);

		ArrayList<Double> vals = proc.parseDoubleArray("[1]  7.889\n");
		assertEquals(expectedVals, vals);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseDoubleArrayInvalidString() throws Exception
	{
		proc.parseDoubleArray("[1]  \"haha\"\n");
	}

	@Test
	public void testParseBoolean() throws Exception
	{
		Boolean d = proc.parseBoolean("[1] TRUE\n");
		assertEquals(true, d);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseBooleanInvalidExtraValues() throws Exception
	{
		proc.parseBoolean("[1]  TRUE FALSE\n");
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseBooleanInvalidString() throws Exception
	{
		proc.parseBoolean("[1]  \"aoeu\"\n");
	}

	@Test
	public void testParseBooleanArray() throws Exception
	{
		List<Boolean> expectedVals = new ArrayList<Boolean>();
		expectedVals.add(false);
		expectedVals.add(true);
		expectedVals.add(true);
		expectedVals.add(false);
		expectedVals.add(false);

		List<Boolean> vals = proc.parseBooleanArray("[1]  FALSE TRUE\n[2]  TRUE FALSE FALSE\n");
		assertEquals(expectedVals, vals);
	}

	@Test
	public void testParseBooleanArraySingleValue() throws Exception
	{
		List<Boolean> expectedVals = new ArrayList<Boolean>();
		expectedVals.add(false);

		List<Boolean> vals = proc.parseBooleanArray("[1]  FALSE\n");
		assertEquals(expectedVals, vals);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseBooleanArrayInvalidString() throws Exception
	{
		proc.parseBooleanArray("[1]  \"haha\"\n");
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseBooleanArrayInvalidString2() throws Exception
	{
		proc.parseBooleanArray("[1]  \"FALSE\"\n");
	}

	@Test
	public void testParseString() throws Exception
	{
		String str = proc.parseString("[1] \"test\"\n");
		assertEquals("test", str);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseStringInvalidExtraValues() throws Exception
	{
		proc.parseString("[1]  \"test 1\" \"test 2\"\n");
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseStringInvalidDouble() throws Exception
	{
		proc.parseString("[1]  7.890\n");
	}

	@Test
	public void testParseStringArray() throws Exception
	{
		ArrayList<String> expectedVals = new ArrayList<String>();
		expectedVals.add("test 1");
		expectedVals.add("test the second");
		expectedVals.add("aoeu. ,.u");
		expectedVals.add("Hello");
		expectedVals.add("crhcr");

		ArrayList<String> vals = proc.parseStringArray("[1]  \"test 1\" \"test the second\" \"aoeu. ,.u\"\n[2]  \"Hello\" \"crhcr\"\n");
		assertEquals(expectedVals, vals);
	}

	@Test
	public void testParseStringArraySingleValue() throws Exception
	{
		ArrayList<String> expectedVals = new ArrayList<String>();
		expectedVals.add("test");

		ArrayList<String> vals = proc.parseStringArray("[1]  \"test\"\n");
		assertEquals(expectedVals, vals);
	}

	@Test(expected=RProcessorParseException.class)
	public void testParseStringArrayInvalidDouble() throws Exception
	{
		proc.parseStringArray("[1]  7.902\n");
	}

	@Test
	public void testSetVariableSingleDouble() throws Exception
	{
		Double val = new Double(67.9);
		String var = proc.setVariable(val);
		Double ret = proc.executeDouble(var);
		assertEquals(val, ret);
	}

	@Test
	public void testSetVariableSingleString() throws Exception
	{
		String val = "test string";
		String var = proc.setVariable(val);
		String ret = proc.executeString(var);
		assertEquals(val, ret);
	}

	@Test
	public void testSetVariableListDouble() throws Exception
	{
		DataColumn col = DataColumnTest.createDataColumn(50);
		assertEquals(DataMode.NUMERICAL, col.getMode());
		String var = proc.setVariable(col);

		// Check
		DataColumn ret = DataColumnTest.createDataColumn(0);
		assertEquals(DataMode.NUMERICAL, ret.getMode());
		ret.addAll(proc.executeDoubleArray(var));
		assertEquals(col, ret);
	}

	@Test
	public void testSetVariableListString() throws Exception
	{
		DataColumn col = DataColumnTest.createDataColumn(50);
		col.setMode(DataMode.STRING);
		assertEquals(DataMode.STRING, col.getMode());
		String var = proc.setVariable(col);

		// Check
		DataColumn ret = DataColumnTest.createDataColumn(0);
		ret.setMode(DataMode.STRING);
		assertEquals(DataMode.STRING, ret.getMode());
		ret.addAll(proc.executeStringArray(var));
		assertEquals(col, ret);
	}

	@Test
	public void testGraphicOutput() throws Exception
	{
		String fileName = proc.startGraphicOutput();

		// Doing the plot shouldn't result in output
		String cmdResult = proc.execute("plot(5:10)");
		assertTrue(cmdResult.isEmpty());

		// File name reported by start and stop should be the same
		String secondFileName = proc.stopGraphicOutput();
		assertEquals(fileName, secondFileName);

		// And the file exists, right?
		File png = new File(fileName);
		assertTrue(png.exists());
	}

	@Test
	public void testFetchInteractionFull() throws Exception
	{
		// Clear it out
		proc.fetchInteraction();
		
		proc.setRecorderMode(RProcessor.RecordMode.FULL);
		proc.execute("print('test')");
		String interaction = proc.fetchInteraction();

		// Count number of lines in interaction, should have 2
		assertEquals(2, interaction.replaceAll("[^\\n]","").length());
	}

	@Test
	public void testFetchInteractionCmds() throws Exception
	{
		// Clear it out
		proc.fetchInteraction();

		proc.setRecorderMode(RProcessor.RecordMode.CMDS_ONLY);
		proc.execute("print('test')");
		String interaction = proc.fetchInteraction();

		// Count number of lines in interaction, should have 1
		assertEquals(1, interaction.replaceAll("[^\\n]","").length());
	}

	@Test
	public void testFetchInteractionOutput() throws Exception
	{
		// Clear it out
		proc.fetchInteraction();

		proc.setRecorderMode(RProcessor.RecordMode.OUTPUT_ONLY);
		proc.execute("print('test')");
		String interaction = proc.fetchInteraction();

		// Count number of lines in interaction, should have 1
		assertEquals(1, interaction.replaceAll("[^\\n]","").length());
	}

	@Test
	public void testFetchInteractionEmpty() throws Exception
	{
		// Clear it out
		proc.fetchInteraction();

		proc.setRecorderMode(RProcessor.RecordMode.DISABLED);
		proc.execute("print('test')");
		String interaction = proc.fetchInteraction();

		// Count number of lines in interaction, should have 2
		assertTrue(interaction.isEmpty());
	}

	@Test
	public void testFetchInteractionDoubleFetch() throws Exception
	{
		// Clear it out
		proc.fetchInteraction();

		proc.setRecorderMode(RProcessor.RecordMode.FULL);
		proc.execute("print('test')");

		// First time should have stuff
		String interaction = proc.fetchInteraction();
		assertFalse(interaction.isEmpty());

		// Second shouldn't
		interaction = proc.fetchInteraction();
		assertTrue(interaction.isEmpty());
	}

	@Test
	public void testGetUniqueName()
	{
		// Successive calls should never give the same thing
		ArrayList<String> vars = new ArrayList<String>();
		for(int i = 0; i < 100; i++)
		{
			String var = proc.getUniqueName();
			if(vars.contains(var))
				fail("Duplicate name returned by getUniqueName(). '" + var + "' repeated.");
			vars.add(var);
		}
	}
}
