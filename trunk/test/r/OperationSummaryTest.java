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

import org.junit.Test;
import static org.junit.Assert.*;
import problem.CalcException;
import problem.DataColumn;
import problem.DataNotFound;
import problem.DataSet;
import problem.Operation;

/**
 *
 * @author Ryan Morehart
 */
public class OperationSummaryTest
{

	@Test
	public void testRIntegration() throws CalcException
	{
		try
		{
			DataSet simple = new DataSet("simple");
			DataColumn col = simple.addColumn("basic");
			col.add(5.0);
			col.add(10.0);
			col.add(15.0);

			Operation op = simple.addOperation(new OperationSummary());
			Double result = op.getColumn("mean").get(0);

			assertEquals(result.doubleValue(), 10.0);
		}
		catch(DataNotFound ex)
		{
			fail("Unable to find the right average in summary");
		}
	}
}
