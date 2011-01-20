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

/**
 * @author Ryan Morehart
 */
public class DataColumnTest
{
	public static DataColumn createDataColumn(int items)
	{
		DataColumn dc = null;
		try
		{
			DataSet ds = new DataSet("temp");
			dc = ds.addColumn("Column");
			assertEquals(1, ds.getColumnCount());
		}
		catch(DuplicateNameException ex)
		{
			// Sholudn't happen
			throw new InternalMarlaException("Duplicate name in createDataColumn(). Shouldn't happen", ex);
		}

		for(int i = 0; i < items; i++)
		{
			dc.add(i);
		}

		// Make sure we built correctly
		assertEquals(items, dc.size());

		return dc;
	}

	@Test
	public void testEquals()
	{
		DataColumn testDC1 = createDataColumn(35);
		DataColumn testDC2 = createDataColumn(35);
		assertEquals(testDC1, testDC2);
	}

	@Test
	public void testNotEqualsCount()
	{
		DataColumn testDC1 = createDataColumn(35);
		DataColumn testDC2 = createDataColumn(34);
		assertFalse(testDC1.equals(testDC2));
	}

	@Test
	public void testNotEqualsValues()
	{
		DataColumn testDC1 = createDataColumn(35);
		DataColumn testDC2 = createDataColumn(35);
		testDC2.set(5, 1000);
		assertFalse(testDC1.equals(testDC2));
	}

	@Test
	public void testNotEqualsNames() throws Exception
	{
		DataColumn testDC1 = createDataColumn(35);
		DataColumn testDC2 = createDataColumn(35);
		testDC2.setName("SOMETHING DIFFERENT");
		assertFalse(testDC1.equals(testDC2));
	}

	@Test
	public void testNotEqualsModes()
	{
		DataColumn testDC1 = createDataColumn(35);
		DataColumn testDC2 = createDataColumn(35);
		testDC2.setMode(DataColumn.DataMode.STRING);
		assertFalse(testDC1.equals(testDC2));
	}

	@Test
	public void testChangeMode()
	{
		DataColumn testDC1 = createDataColumn(5);

		// To string
		testDC1.setMode(DataColumn.DataMode.STRING);
		assertTrue(testDC1.isString());
		assertEquals("0.0", testDC1.get(0));
		assertEquals("3.0", testDC1.get(3));
		assertEquals("4.0", testDC1.get(4));

		// To doubles
		testDC1.setMode(DataColumn.DataMode.NUMERICAL);
		assertTrue(testDC1.isNumerical());
		assertEquals(0D, testDC1.get(0));
		assertEquals(3D, testDC1.get(3));
		assertEquals(4D, testDC1.get(4));

		// And back to string
		testDC1.setMode(DataColumn.DataMode.STRING);
		assertTrue(testDC1.isString());
		assertEquals("0.0", testDC1.get(0));
		assertEquals("3.0", testDC1.get(3));
		assertEquals("4.0", testDC1.get(4));
	}

	@Test
	public void testSize()
	{
		DataColumn testDC1 = createDataColumn(50);
		assertEquals(50, testDC1.size());
	}

	@Test
	public void testIsEmpty()
	{
		DataColumn testDC1 = createDataColumn(0);
		assertTrue(testDC1.isEmpty());
	}

	@Test
	public void testIsNotEmpty()
	{
		DataColumn testDC1 = createDataColumn(5);
		assertFalse(testDC1.isEmpty());
	}

	@Test
	public void testRemove()
	{
		DataColumn testDC1 = createDataColumn(50);
		assertEquals(testDC1.get(10), 10D);

		testDC1.remove(10);
		assertEquals(testDC1.size(), 49);
		assertEquals(testDC1.get(9), 9D);
		assertEquals(testDC1.get(10), 11D);
	}

	@Test
	public void testAdd()
	{
		DataColumn testDC1 = createDataColumn(50);
		assertEquals(10D, testDC1.get(10));

		testDC1.add(10, 1000);
		assertEquals(51, testDC1.size());
		assertEquals(9D, testDC1.get(9));
		assertEquals(1000D, testDC1.get(10));
		assertEquals(10D, testDC1.get(11));
	}


	@Test
	public void testClear()
	{
		DataColumn testDC1 = createDataColumn(50);
		assertEquals(50, testDC1.size());
		testDC1.clear();
		assertEquals(0, testDC1.size());
	}

	@Test
	public void testCopy()
	{
		DataColumn testDC1 = createDataColumn(50);
		DataColumn testDC2 = new DataColumn(testDC1, null);
		assertEquals(testDC1, testDC2);
	}
}
