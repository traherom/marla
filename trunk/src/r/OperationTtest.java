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
import problem.CalcException;
import problem.DuplicateNameException;

/**
 *
 * @author Andrew Sterling
 */
public class OperationTtest extends problem.Operation
{
	private String storedName;
	private DataColumn storedColumn;

	public OperationTtest()
	{
		super("Ttest");
	}

	@Override
	protected void computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			DataColumn parentCol = parent.getColumn(i);
			DataColumn dcT;
			DataColumn dcDF;
			DataColumn dcP;
			DataColumn dcMean;
			DataColumn dcCI;
			DataColumn dcAlpha;
			try
			{
				dcT = new DataColumn(this, "t(" + parentCol.getName() + ")");
				dcDF = new DataColumn(this, "df(" + parentCol.getName() + ")");
				dcP = new DataColumn(this, "P Value(" + parentCol.getName() + ")");
				dcMean = new DataColumn(this, "mean(" + parentCol.getName() + ")");
				dcCI = new DataColumn(this, "CI(" + parentCol.getName() + ")");
				dcAlpha = new DataColumn(this, "alpha(" + parentCol.getName() + ")");
			}
			catch(DuplicateNameException ex)
			{
				throw new CalcException("Duplicate name for computed columns. Should never happen.", ex);
			}

			String varName = proc.setVariable(parentCol);
			String resultVarName = proc.executeSave("t.test(" + varName + ")");
			dcT.add(proc.executeDouble(resultVarName + "$statistic"));
			dcDF.add(proc.executeDouble(resultVarName + "$parameter"));
			dcP.add(proc.executeDouble(resultVarName + "$p.value"));
			dcMean.add(proc.executeDouble(resultVarName + "$estimate"));
			dcCI.add(proc.executeDouble(resultVarName + "$conf.int[1]"));
			dcAlpha.add(proc.executeDouble("attr(" + resultVarName + "$conf.int, 'conf.level')"));

			columns.add(dcT);
			columns.add(dcDF);
			columns.add(dcP);
			columns.add(dcMean);
			columns.add(dcCI);
			columns.add(dcAlpha);
		}
	}
}
