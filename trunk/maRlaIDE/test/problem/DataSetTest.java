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

import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import org.jdom.Element;
import r.RProcessor;
import resource.Configuration;

/**
 *
 * @author Ryan Morehart
 */
public class DataSetTest
{
	public static DataSet createDataSet(int columns, int rows, int opCount) throws Exception
	{
		DataSet ds = new DataSet("DataSet Test");

		for(int dcNum = 1; dcNum <= columns; dcNum++)
		{
			DataColumn dc = ds.addColumn("Column " + dcNum);

			for(int dataNum = 0; dataNum < rows; dataNum++)
			{
				dc.add(dataNum);
			}

			// Make sure we built correctly
			assertEquals(rows, dc.size());
		}

		// Make last one a string?
		if(columns != 1)
		{
			//ds.getColumn(0).setMode(DataColumn.DataMode.STRING);
		}

		// Add operations, just for giggles
		for(int i = 0; i < opCount; i++)
		{
			ds.addOperation(Operation.createOperation("NOP"));
		}
		
		// Make sure we built correctly
		assertEquals(columns, ds.getColumnCount());

		return ds;
	}

	@BeforeClass
	public static void configure() throws MarlaException
	{
		Configuration.load();
	}

	@Test
	public void testEquals() throws Exception
	{
		// Equal
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 50, 1);
		testDS1.equals(testDS2);
		assertEquals(testDS1, testDS2);
	}

	@Test
	public void testNotEqualsColCount() throws Exception
	{
		// Different number of columns
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(4, 50, 1);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsOpCount() throws Exception
	{
		// Different number of columns
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 50, 2);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsNumItemsInColumn() throws Exception
	{
		// Different number of items in the columns
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 49, 1);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsValuesInColumn() throws Exception
	{
		// Different value in one of the columns
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 50, 1);
		testDS2.getColumn(0).set(4, 10000);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsColumnNames() throws Exception
	{
		// Different names for one of the columns
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 50, 1);
		testDS2.getColumn(0).setName("SOMETHING DIFFERENT");
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsNames() throws Exception
	{
		// Different name for the dataset
		DataSet testDS1 = createDataSet(5, 50, 1);
		DataSet testDS2 = createDataSet(5, 50, 1);
		testDS2.setDataName("SOMETHING DIFFERENT");
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testImportAndExportFile() throws Exception
	{
		// Export to file. Change column names to match the way it will
		// be imported. We also don't export operations, so dump those.
		DataSet testDS1 = createDataSet(5, 50, 0);
		testDS1.exportFile("test.csv");

		// Import and make name match (otherwise it's based on the file name,
		// which clearly wouldn't be right)
		DataSet importedDS = DataSet.importFile("test.csv");
		assertEquals("test.csv", importedDS.getName());
		importedDS.setDataName(testDS1.getName());
		
		assertEquals(testDS1, importedDS);
	}

	@Test
	public void testFromRFrame() throws Exception
	{
		RProcessor proc = RProcessor.getInstance();
		proc.execute("col1 = c(1, 2, 3)");
		proc.execute("col2 = c(4, 5, 6)");
		proc.execute("col3 = c(7, 8, 9)");
		proc.execute("f = data.frame(col1, col2, col3)");

		DataSet ds = DataSet.fromRFrame("f");

		assertEquals("col1", ds.getColumn(0).getName());
		assertEquals("col2", ds.getColumn(1).getName());
		assertEquals("col3", ds.getColumn(2).getName());

		assertEquals(1D, ds.getColumn(0).get(0));
		assertEquals(2D, ds.getColumn(0).get(1));
		assertEquals(3D, ds.getColumn(0).get(2));
		assertEquals(4D, ds.getColumn(1).get(0));
		assertEquals(5D, ds.getColumn(1).get(1));
		assertEquals(6D, ds.getColumn(1).get(2));
		assertEquals(7D, ds.getColumn(2).get(0));
		assertEquals(8D, ds.getColumn(2).get(1));
		assertEquals(9D, ds.getColumn(2).get(2));
	}

	@Test
	public void testToRFrame() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 50, 1);

		// Save it
		String frameVar = testDS1.toRFrame();

		// Test a couple values to be sure they're right
		RProcessor proc = RProcessor.getInstance();
		assertEquals(new Double(0), proc.executeDouble(frameVar + "$Column.1[1]"));
		assertEquals(new Double(5), proc.executeDouble(frameVar + "$Column.2[6]"));
		assertEquals(new Double(40), proc.executeDouble(frameVar + "$Column.3[41]"));
	}

	@Test(expected=DataNotFoundException.class)
	public void testGetColumn() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 50, 0);

		// Try getting from both ends of the DataSet and in the middle
		assertEquals(testDS1.getColumn(0), testDS1.getColumn("Column 1"));
		assertEquals(testDS1.getColumn(2), testDS1.getColumn("Column 3"));
		assertEquals(testDS1.getColumn(4), testDS1.getColumn("Column 5"));

		// Try to get a non-existant column
		testDS1.getColumn("THIS COLUMN DOESN'T EXIST");
	}

	@Test
	public void testGetColumnCount() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 4, 0);
		assertEquals(5, testDS1.getColumnCount());
	}

	@Test
	public void testGetColumnLengthEqual() throws Exception
	{
		// Equally sized columns
		DataSet testDS1 = createDataSet(5, 50, 0);
		assertEquals(50, testDS1.getColumnLength());
	}

	@Test
	public void testGetColumnLengthVaried() throws Exception
	{
		// Some shorter
		DataSet testDS1 = createDataSet(5, 50, 0);
		testDS1.getColumn(0).remove(0);
		testDS1.getColumn(4).remove(0);
		assertEquals(50, testDS1.getColumnLength());

		// And the same ones longer
		testDS1.getColumn(0).add(1000);
		testDS1.getColumn(0).add(2000);
		testDS1.getColumn(4).add(1000);
		testDS1.getColumn(4).add(2000);
		assertEquals(51, testDS1.getColumnLength());
	}

	@Test
	public void testToAndFromXML() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 50, 1);

		Element el = testDS1.toXml();
		DataSet testDS2 = DataSet.fromXml(el);

		assertEquals(testDS1, testDS2);
	}

	@Test
	public void testCopy() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 50, 2);
		DataSet testDS2 = new DataSet(testDS1, new Problem());
		assertEquals(testDS1, testDS2);
	}

	@Test
	public void testUniqueName() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 20, 0);
		assertTrue(testDS1.isUniqueColumnName("New Column"));
		assertFalse(testDS1.isUniqueColumnName(testDS1.getColumn(0).getName()));
	}

	@Test
	public void testAddColumnByName() throws Exception
	{
		DataSet testDS1 = createDataSet(1, 20, 0);

		assertTrue(testDS1.isUniqueColumnName("New Column"));
		DataColumn addedCol = testDS1.addColumn("New Column");
		assertFalse(testDS1.isUniqueColumnName("New Column"));

		assertEquals(addedCol, testDS1.getColumn("New Column"));
		assertEquals(testDS1, addedCol.getParentData());
	}

	@Test(expected=DuplicateNameException.class)
	public void testAddColumnDuplicateName() throws Exception
	{
		DataSet testDS1 = createDataSet(3, 20, 0);

		String existingName = testDS1.getColumn(1).getName();
		assertFalse(testDS1.isUniqueColumnName(existingName));
		
		testDS1.addColumn(existingName);
	}
	@Test
	public void testAddOperation() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 20, 0);
		Operation newOp = Operation.createOperation("NOP");

		assertEquals(0, testDS1.getOperationCount());
		Operation addedOp = testDS1.addOperation(newOp);
		assertEquals(1, testDS1.getOperationCount());
		
		assertEquals(newOp, addedOp);
		assertEquals(newOp, testDS1.getOperation(0));
		assertEquals(testDS1, newOp.getParentData());
	}

	@Test
	public void testAddOperationViaOperationSetParent() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 20, 0);
		Operation newOp = Operation.createOperation("NOP");

		assertEquals(0, testDS1.getOperationCount());
		newOp.setParentData(testDS1);
		assertEquals(1, testDS1.getOperationCount());
		assertEquals(1, testDS1.getOperationCount());

		assertEquals(newOp, testDS1.getOperation(0));
		assertEquals(testDS1, newOp.getParentData());
	}

	@Test
	public void testAddOperationExistingParentByObject() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 20, 0);
		DataSet testDS2 = createDataSet(2, 20, 0);

		// Add to one dataset and make sure it's right
		Operation newOp = Operation.createOperation("NOP");
		assertEquals(0, testDS1.getOperationCount());
		Operation addedOp = testDS1.addOperation(newOp);
		assertEquals(1, testDS1.getOperationCount());
		assertEquals(newOp, addedOp);
		assertEquals(newOp, testDS1.getOperation(0));
		assertEquals(testDS1, addedOp.getParentData());

		// Move it
		assertEquals(0, testDS2.getOperationCount());
		addedOp = testDS2.addOperation(newOp);
		assertEquals(1, testDS2.getOperationCount());
		assertEquals(newOp, addedOp);
		assertEquals(newOp, testDS2.getOperation(0));
		assertEquals(testDS2, newOp.getParentData());
	}

	@Test
	public void testAddOperationWithExistingParentViaSetParent() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 20, 0);
		DataSet testDS2 = createDataSet(2, 20, 0);

		// Add to one dataset and make sure it's right
		Operation newOp = Operation.createOperation("NOP");
		assertEquals(0, testDS1.getOperationCount());
		newOp.setParentData(testDS1);
		assertEquals(1, testDS1.getOperationCount());
		assertEquals(newOp, testDS1.getOperation(0));
		assertEquals(testDS1, newOp.getParentData());

		// Move it
		assertEquals(0, testDS2.getOperationCount());
		newOp.setParentData(testDS2);
		assertEquals(1, testDS2.getOperationCount());
		assertEquals(newOp, testDS2.getOperation(0));
		assertEquals(testDS2, newOp.getParentData());
	}

	@Test
	public void testGetAllChildOperations() throws Exception
	{
		DataSet testDS1 = createDataSet(2, 10, 4);
		
		// Getting all right now should give us just the 4
		assertEquals(4, testDS1.getOperationCount());
		assertEquals(4, testDS1.getAllChildOperations().size());

		// Now add 1 to it and double check
		Operation newOp = testDS1.addOperation(Operation.createOperation("NOP"));
		assertEquals(5, testDS1.getOperationCount());
		List<Operation> insideOps = testDS1.getAllChildOperations();
		assertEquals(5, insideOps.size());
		assertTrue(insideOps.contains(newOp));

		// Now tack one onto one of our inside operations. It should come out
		// when we call the allChildOps
		Operation secondOp = newOp.addOperation(Operation.createOperation("NOP"));
		assertEquals(5, testDS1.getOperationCount());
		insideOps = testDS1.getAllChildOperations();
		assertEquals(6, insideOps.size());
		assertTrue(insideOps.contains(newOp));
		assertTrue(insideOps.contains(secondOp));
	}
}
