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
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;

/**
 * Serves as a pass-through sort of operation, performing no actual
 * action on the data it is associated with.
 * @author Ryan Morehart
 */
public class OperationMean extends problem.Operation
{

	private Rengine re;
	private REXP exp;
	private String storedName;
	private double[] storedData;
	private double[] resultData;
	private DataColumn storedColumn;


	public OperationMean()
	{
		super("Mean");
		re = new Rengine(new String[]{"--no-save"}, false, new RInterface());
	}

	@Override
	public DataColumn calcColumn(int index)
	{
		storedColumn = parent.getColumn(index);

		DataColumn out = new DataColumn("Mean");

		Double[] temp = (Double[]) storedColumn.toArray();

		//casts array to double
		for(int i = 0; i < storedColumn.size(); i++)
		{
			storedData[i] = temp[i].doubleValue();
		}


		//does operation
		storedName = storedColumn.getName();
		re.assign(storedName, storedData);
		exp = re.eval("mean(" + storedName + ")");

		//throw results from exp into the local column
		resultData = exp.asDoubleArray();

		for(int i = 0; i < resultData.length; i++)
		{
			out.add((Double) resultData[i]);
		}
		out.setName("Mean");

		return out;
	}
}
