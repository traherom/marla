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

import gui.Domain.PromptType;
import java.util.ArrayList;
import problem.DataColumn;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import problem.CalcException;
import problem.DataSet;
import problem.DuplicateNameException;
import problem.Operation;

/**
 * Serves as a pass-through sort of operation, performing no actual
 * action on the data it is associated with.
 * 
 * @author Ryan Morehart
 */
public class OperationMean extends problem.Operation
{
	private Rengine re;
	private REXP exp;
	private String storedName;
	private DataColumn storedColumn;

	public OperationMean()
	{
		super("Mean");
	}

	@Override
	public ArrayList<DataColumn> computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		RProcessor proc = RProcessor.getInstance();
		ArrayList<DataColumn> cols = new ArrayList<DataColumn>();

		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			DataColumn parentCol = parent.getColumn(i);
			DataColumn dc;
			try
			{
				dc = new DataColumn(this, "mean(" + parentCol.getName() + ")");
			}
			catch(DuplicateNameException ex)
			{
				throw new CalcException("Duplicate name for computed columns. Should never happen.", ex);
			}

			String varName = proc.setVariable(parentCol);
			Double sdVal = proc.executeDouble("mean(" + varName + ")");
			dc.add(sdVal);

			cols.add(dc);
		}

		return cols;
	}

	@Override
	public ArrayList<Object[]> getRequiredInfoPrompt()
	{
		ArrayList<Object[]> req = new ArrayList<Object[]>();
		req.add(new Object[]
				{
					"Are you sure you want to add the mean?", PromptType.CHECKBOX
				});
		req.add(new Object[]
				{
					"Seriously?", PromptType.COMBO, new Object[]
					{
						"Here", "Are", "Reasons"
					}
				});
		req.add(new Object[]
				{
					"Explain why:", PromptType.TEXT
				});
		return req;
	}

	@Override
	public String toString()
	{
		// What we're starting out with
		StringBuilder sb = new StringBuilder();
		sb.append(parent.toString());

		// What we're doing for the computation
		sb.append("\nmean(");
		sb.append(Operation.sanatizeName(parent));
		sb.append(")\n");

		// And the result
		sb.append(DataSet.toString(this));

		return sb.toString();
	}
}
