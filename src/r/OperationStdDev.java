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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import problem.DataColumn;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import problem.CalcException;
import problem.DataSet;
import problem.Operation;

/**
 * Serves as a pass-through sort of operation, performing no actual
 * action on the data it is associated with.
 * 
 * @author Ryan Morehart
 * @author Andrew Sterling
 */
public class OperationStdDev extends problem.Operation
{
	private Rengine re;
	private REXP exp;
	private String storedName;
	private DataColumn storedColumn;

	public OperationStdDev()
	{
		super("StdDev");
		re = new Rengine(new String[]
				{
					"--no-save"
				}, false, new RInterface());
	}

	@Override
	public DataColumn calcColumn(int index) throws CalcException
	{
		try
		{
			DataColumn inCol = parent.getColumn(index);
			DataColumn outCol = new DataColumn("SD");
			RProcessor proc = RProcessor.getInstance();
			String varName = proc.setVariable(inCol);
			Double sdVal = proc.executeDouble("sd(" + varName + ")");
			outCol.add(sdVal);
			return outCol;
		}
		catch(RProcessorParseException ex)
		{
			throw new CalcException();
		}
		catch(IOException ex)
		{
			throw new CalcException("Unable to work with R");
		}
		catch(RProcessorException ex)
		{
			throw new CalcException();
		}
	}

	@Override
	public String toString()
	{
		// What we're starting out with
		StringBuilder sb = new StringBuilder();
		sb.append(parent.toString());

		// What we're doing for the computation
		sb.append("\nsd(");
		sb.append(Operation.sanatizeName(parent));
		sb.append(")\n");

		// And the result
		sb.append(DataSet.toString(this));

		return sb.toString();
	}
}
