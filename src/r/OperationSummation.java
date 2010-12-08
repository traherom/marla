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
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import problem.CalcException;

/**
 * Using R, it returns the sum of values in the DataColumn.
 * 
 * @author Ryan Morehart
 * @author Andrew Sterling
 */
public class OperationSummation extends problem.Operation
{

	private Rengine re;
	private REXP exp;
	private String storedName;
	private DataColumn storedColumn;

	public OperationSummation()
	{
		super("Sum");
		re = new Rengine(new String[]{"--no-save"}, false, new RInterface());
	}

	@Override
	public DataColumn calcColumn(int index) throws CalcException
	{
		storedColumn = parent.getColumn(index);

		DataColumn out = new DataColumn("Sum");

		Double[] temp = new Double[storedColumn.size()];
		storedColumn.toArray(temp);

		double[] storedData = new double[storedColumn.size()];

		//casts array to double
		for(int i = 0; i < storedColumn.size(); i++)
		{
			storedData[i] = temp[i].doubleValue();
		}


		//does operation
		storedName = "sum";
		re.assign(storedName, storedData);
		exp = re.eval("sum(" + storedName + ")");

		double resultData = exp.asDouble();

		out.add((Double) resultData);
		out.setName("Sum");

		re.end();
		return out;
	}

	@Override
	public String toString()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
