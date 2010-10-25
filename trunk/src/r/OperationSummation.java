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

import problem.DataColumn;
import problem.Operation;

/**
 * DOES NOT ACTUALLY USE R! Just returns the summation of the values in
 * a column.
 * @author Ryan Morehart
 */
public class OperationSummation extends problem.Operation
{

	public OperationSummation()
	{
		super("Sum");
	}

	@Override
	public DataColumn calcColumn(int index)
	{
		DataColumn col = parent.getColumn(index);

		Double total = new Double(0);
		for(Double d : col)
			total += d;

		DataColumn sum = new DataColumn("sum" + index);
		sum.add(total);
		return sum;
	}
}
