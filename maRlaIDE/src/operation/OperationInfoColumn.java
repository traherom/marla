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
package operation;

import java.util.List;
import problem.DataColumn;
import problem.DataColumn.DataMode;
import problem.DataSource;
import problem.MarlaException;

/**
 * @author Ryan Morehart
 */
public class OperationInfoColumn extends OperationInfoCombo
{
	private DataMode columnType = null;

	/**
	 * Constructs a new column select prompt with all the columns in the given
	 * DataSource as options
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param ds DataSource from which to pull column information
	 */
	public OperationInfoColumn(Operation op, String name, String prompt) throws MarlaException
	{
		this(op, name, prompt, null);
	}

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param ds DataSource from which to pull column information
	 * @param columnType Type of column to include in the list
	 */
	public OperationInfoColumn(Operation op, String name, String prompt, DataMode columnType) throws MarlaException
	{
		super(op, name, prompt, PromptType.COLUMN);
		this.columnType = columnType;
	}

	/**
	 * Retrieves the appropriate columns from the parent data
	 */
	private void setOptions()
	{
		// Clear anything existing out
		List<String> opts = getModifiableOptions();
		opts.clear();

		try
		{
			// Get columns in the given mode (if possible)
			DataSource ds = getOperation().getParentData();
			if(ds == null)
				return;

			for(int i = 0; i < ds.getColumnCount(); i++)
			{
				DataColumn dc = ds.getColumn(i);
				if(columnType == null || dc.getMode() == columnType)
					opts.add(dc.getName());
			}
		}
		catch(MarlaException ex)
		{
			// Can't do anything right now
			opts.clear();
		}
	}

	@Override
	public String setAnswer(Object newAnswer) throws OperationInfoRequiredException
	{
		setOptions();
		return super.setAnswer(newAnswer);
	}

	@Override
	public String getAnswer()
	{
		// Recheck that the column selected is still in the parent data
		setOptions();
		String current = super.getAnswer();
		if(super.getOptions().contains(current))
		{
			return current;
		}
		else
		{
			// Not found, clear answer
			clearAnswer();
			return null;
		}
	}

	@Override
	public List<String> getOptions()
	{
		// Grab options if possible
		setOptions();
		return super.getOptions();
	}
}
