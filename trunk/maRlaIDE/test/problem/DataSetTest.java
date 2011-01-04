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

import org.junit.*;
import static org.junit.Assert.*;
import org.jdom.Element;
import r.RProcessor;

/**
 *
 * @author Ryan Morehart
 */
public class DataSetTest
{
	public static DataSet createDataSet(int columns, int rows)
	{
		DataSet ds = new DataSet("DataSet Test");

		for(int dcNum = 1; dcNum <= columns; dcNum++)
		{
			DataColumn dc = ds.addColumn("Column " + dcNum);

			for(int dataNum = 0; dataNum < rows; dataNum++)
			{
				dc.add(dataNum);
			}
		}

		return ds;
	}

	@Test
	public void testEquals()
	{
		// Equal
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(5, 50);
		assertEquals(testDS1, testDS2);
	}

	@Test
	public void testNotEqualsColCount()
	{
		// Different number of columns
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(4, 50);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsNumItemsInColumn()
	{
		// Different number of items in the columns
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(5, 49);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsValuesInColumn()
	{
		// Different value in one of the columns
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(5, 50);
		testDS2.getColumn(0).set(4, 10000);
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsColumnNames()
	{
		// Different names for one of the columns
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(5, 50);
		testDS2.getColumn(0).setName("SOMETHING DIFFERENT");
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testNotEqualsNames()
	{
		// Different name for the dataset
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = createDataSet(5, 50);
		testDS2.setName("SOMETHING DIFFERENT");
		assertFalse(testDS1.equals(testDS2));
	}

	@Test
	public void testImportAndExportFile() throws Exception
	{
		// Export to file
		DataSet testDS1 = createDataSet(5, 50);
		testDS1.getColumn(0).setName("Column.1");
		testDS1.getColumn(1).setName("Column.2");
		testDS1.getColumn(2).setName("Column.3");
		testDS1.getColumn(3).setName("Column.4");
		testDS1.getColumn(4).setName("Column.5");
		testDS1.exportFile("test.csv");

		// Import and make name match (otherwise it's based on the file name)
		DataSet importedDS = DataSet.importFile("test.csv");
		assertEquals("test.csv", importedDS.getName());
		importedDS.setName(testDS1.getName());
		
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
		DataSet testDS1 = createDataSet(5, 50);

		// Save it
		String frameVar = testDS1.toRFrame();

		// Test a couple values to be sure they're right
		RProcessor proc = RProcessor.getInstance();
		assertEquals(new Double(0), proc.executeDouble(frameVar + "$Column.1[1]"));
		assertEquals(new Double(5), proc.executeDouble(frameVar + "$Column.2[6]"));
		assertEquals(new Double(40), proc.executeDouble(frameVar + "$Column.3[41]"));
	}

	@Test(expected=DataNotFound.class)
	public void testGetColumn() throws Exception
	{
		DataSet testDS1 = createDataSet(5, 50);

		// Try getting from both ends of the DataSet and in the middle
		assertEquals(testDS1.getColumn(0), testDS1.getColumn("Column 1"));
		assertEquals(testDS1.getColumn(2), testDS1.getColumn("Column 3"));
		assertEquals(testDS1.getColumn(4), testDS1.getColumn("Column 5"));

		// Try to get a non-existant column
		testDS1.getColumn("THIS COLUMN DOESN'T EXIST");
	}

	@Test
	public void testGetColumnCount()
	{
		DataSet testDS1 = createDataSet(5, 4);
		assertEquals(5, testDS1.getColumnCount());
	}

	@Test
	public void testGetColumnLengthEqual()
	{
		// Equally sized columns
		DataSet testDS1 = createDataSet(5, 50);
		assertEquals(50, testDS1.getColumnLength());
	}

	@Test
	public void testGetColumnLengthVaried()
	{
		// Some shorter
		DataSet testDS1 = createDataSet(5, 50);
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

	/**
	 * Can we save a dataset to XML, then read it back in and
	 */
	@Test
	public void testToAndFromXML()
	{
		DataSet testDS1 = createDataSet(5, 50);

		Element el = testDS1.toXml();
		DataSet testDS2 = DataSet.fromXml(el);

		assertEquals(testDS1, testDS2);
	}

	@Test
	public void testCopy()
	{
		DataSet testDS1 = createDataSet(5, 50);
		DataSet testDS2 = new DataSet(testDS1, null);
		assertEquals(testDS1, testDS2);
	}
}
