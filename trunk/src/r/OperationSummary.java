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

import java.util.ArrayList;
import problem.DataColumn;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import problem.CalcException;
import problem.DataSet;
import problem.DuplicateNameException;
import problem.Operation;

/**
 *
 * @author Andrew Sterling
 */
public class OperationSummary extends problem.Operation
{
	private Rengine re;
	private REXP exp;
	private String storedName;
	private DataColumn storedColumn;

	public OperationSummary()
	{
		super("Summary");
		re = new Rengine(new String[]
				{
					"--no-save"
				}, false, new RInterface());
	}

	@Override
	public ArrayList<DataColumn> computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		RProcessor proc = RProcessor.getInstance();
		ArrayList<DataColumn> cols = new ArrayList<DataColumn>();

		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			try
			{
				DataColumn parentCol = parent.getColumn(i);

				String colVarName = proc.setVariable(parentCol);
				String sumVarName = proc.executeSave("summary(" + colVarName + ")");
				ArrayList<String> names = proc.executeStringArray("attr(" + sumVarName + ", 'names')");
				ArrayList<Double> values = proc.executeDoubleArray(sumVarName);
				for(int j = 0; j < names.size(); j++)
				{
					DataColumn dc = new DataColumn(this, parentCol.getName() + " " + names.get(j));
					dc.add(values.get(j));
					cols.add(dc);
				}
			}
			catch(DuplicateNameException ex)
			{
				throw new CalcException("Duplicate name for computed columns. Should never happen.", ex);
			}
		}

		return cols;
	}

	@Override
	public String toString()
	{
		// What we're starting out with
		StringBuilder sb = new StringBuilder();
		sb.append(parent.toString());

		// What we're doing for the computation
		sb.append("\nsummary(");
		sb.append(Operation.sanatizeName(parent));
		sb.append(")\n");

		// And the result
		sb.append(DataSet.toString(this));

		return sb.toString();
	}
}
