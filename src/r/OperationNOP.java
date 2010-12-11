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
import java.util.logging.Level;
import java.util.logging.Logger;
import problem.CalcException;
import problem.DataColumn;
import problem.DataSet;
import problem.DuplicateNameException;
import problem.Operation;

/**
 * Serves as a pass-through sort of operation, performing no actual
 * action on the data it is associated with.
 * 
 * @author Ryan Morehart
 */
public class OperationNOP extends Operation
{
	public OperationNOP()
	{
		super("NoOperation");
	}

	@Override
	public ArrayList<DataColumn> computeColumns() throws RProcessorParseException, RProcessorException, CalcException
	{
		ArrayList<DataColumn> cols = new ArrayList<DataColumn>();
		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			cols.add(new DataColumn(parent.getColumn(i), this));
		}
		return cols;
	}

	@Override
	public String toString()
	{
		// What we're starting out with
		StringBuilder sb = new StringBuilder();
		sb.append(parent.toString());

		// And the result
		sb.append(DataSet.toString(this));

		return sb.toString();
	}
}
