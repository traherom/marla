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
import marla.ide.problem.*;
import java.util.List;
import java.util.ArrayList;
import org.junit.runners.Parameterized.Parameters;
import java.util.Collection;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.*;
import marla.ide.r.RProcessor;
import static org.junit.Assert.*;

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
		Configuration.load();

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

	public ImplementedOperationTest(String opName)
	{
		this.opName = opName;
	}

	@BeforeClass
	public static void setUpR() throws Exception
	{
		// Ensure everything shows
		RProcessor.setDebugMode(RProcessor.RecordMode.FULL);
		RProcessor.getInstance();
	}

	@AfterClass
	public static void tearDownR()
	{
		RProcessor.getInstance().close();
	}

	@Test
	public void testWorking() throws Exception
	{
		System.out.println("Testing operation " + opName);
		Operation op = Operation.createOperation(opName);
		assertTrue(op.runTest());
	}
}
