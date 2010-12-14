/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problem;

import java.io.FileNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;
import r.RProcessorException;
import r.RProcessorParseException;

/**
 *
 * @author Ryan Morehart
 */
public class DataSetTest
{
	RandomValues random = new RandomValues();

	/**
	 * Test of toArray method, of class DataSet.
	 */
	@Test
	public void testToArray() throws CalcException
	{
		try
		{
			DataSet instance = new DataSet("test");
			Double[][] comp = new Double[10][];
			for(int i = 0; i < comp.length; i++)
			{
				DataColumn col = instance.addColumn("col" + i);
				Double[] compCol = new Double[50];
				for(int j = 0; j < compCol.length; j++)
				{
					Double t = random.nextDouble();
					col.add(t);
					compCol[j] = t;
				}

				comp[i] = compCol;
			}

			Double[][] result = instance.toArray();
			assertEquals(comp, result);
		}
		catch(CalcException ex)
		{
			fail("CalcException occured: " + ex.toString());
		}
	}

	/**
	 * Test of equals method, of class DataSet.
	 */
	@Test
	public void testEquals()
	{
		// Internal data should be the same
	}

	@Test
	public void testToString() throws CalcException
	{
		DataSet ds = createSet();
		System.out.println(ds);
	}

	@Test
	public void testImportFile() throws CalcException, RProcessorException, RProcessorParseException
	{
		try
		{
			DataSet ds = DataSet.importFile("test.csv");
			System.out.println(ds);
		}
		catch(CalcException ex)
		{
			fail("CalcException: " + ex.toString());
		}
		catch(FileNotFoundException ex)
		{
			fail("Make the test.csv file");
		}
	}

	/**
	 * Creates a random DataSet
	 * @return
	 */
	public DataSet createSet() throws CalcException
	{
		try
		{
			DataSet ds = new DataSet(random.nextString());

			for(int dcNum = 0; dcNum < 5; dcNum++)
			{
				DataColumn dc = ds.addColumn(random.nextString(10));

				for(int dataNum = 0; dataNum < 50; dataNum++)
				{
					dc.add(random.nextDouble());
				}
			}

			return ds;
		}
		catch(CalcException ex)
		{
			fail("Unable to create test DataSet");
			return null;
		}
	}
}
