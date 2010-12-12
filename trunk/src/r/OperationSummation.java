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
import problem.DuplicateNameException;

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
	}

	@Override
	public void computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			DataColumn parentCol = parent.getColumn(i);
			DataColumn dc;
			try
			{
				dc = new DataColumn(this, "sum(" + parentCol.getName() + ")");
			}
			catch(DuplicateNameException ex)
			{
				throw new CalcException("Duplicate name for computed columns. Should never happen.", ex);
			}

			String varName = proc.setVariable(parentCol);
			Double sdVal = proc.executeDouble("sum(" + varName + ")");
			dc.add(sdVal);

			columns.add(dc);
		}
	}
}
