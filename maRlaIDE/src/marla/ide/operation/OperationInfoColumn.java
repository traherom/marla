/*
 * The maRla Project - Graphical problem solver for statistical calculations.
 * Copyright © 2011 Cedarville University
 * http://marla.googlecode.com
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

package marla.ide.operation;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataColumn.DataMode;
import marla.ide.problem.DataSource;
import marla.ide.problem.InternalMarlaException;
import marla.ide.problem.MarlaException;

/**
 * Gathers a column name from the user, presenting only columns that match the
 * operation-specified parameters
 * @author Ryan Morehart
 */
public class OperationInfoColumn extends OperationInfoCombo
{
	/**
	 * Answer for this question
	 */
	private String answer = null;
	/**
	 * Type of DataColumn we're limited to. If null, no limitation
	 */
	private DataMode columnType = null;

	/**
	 * Constructs a new column select prompt with all the columns in the given
	 * DataSource as options
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 */
	public OperationInfoColumn(Operation op, String name, String prompt) 
	{
		this(op, name, prompt, null);
	}

	/**
	 * Constructs a new column select prompt with the columns of the given type
	 * in the DataSource as options
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param columnType Type of column to include in the list
	 */
	public OperationInfoColumn(Operation op, String name, String prompt, DataMode columnType)
	{
		super(op, name, prompt, PromptType.COLUMN);
		this.columnType = columnType;
	}

	/**
	 * Copy constructor
	 * @param parent Operation this information belongs to. Does not actually
	 *		place the information in that operation!
	 * @param org Information to copy
	 */
	protected OperationInfoColumn(Operation parent, OperationInfoColumn org)
	{
		super(parent, org);
		answer = org.answer;
		columnType = org.columnType;
	}
	
	@Override
	public String setAnswer(Object newAnswer)
	{
		String oldAnswer = answer;

		if(newAnswer == null)
			throw new InternalMarlaException("Info may only be cleared by calling clearAnswer()");
		
		changeBeginning("question " + getName() + " answer");
		
		if(!getOperation().isLoading())
		{
			DataColumn dc = null;

			try
			{
				// Ensure it's a valid column in the parent
				dc = getOperation().getParentData().getColumn(newAnswer.toString());
			}
			catch(MarlaException ex)
			{
				// Couldn't find it, don't save this answer
				throw new OperationInfoRequiredException("Column '" + answer + "' does not exist in parent", getOperation());
			}

			// Check the type
			if(columnType != null && dc.getMode() != columnType)
				throw new OperationInfoRequiredException("Column '" + answer + "' not correct type", getOperation());
		}

		answer = (String) newAnswer;

		getOperation().checkDisplayName();
		getOperation().markDirty();
		markUnsaved();

		return oldAnswer;
	}

	@Override
	public String getAnswer()
	{
		// If we're still loading, just return without fault-checking
		if(getOperation().isLoading())
			return answer;

		DataSource parent = getOperation().getParentData();

		// No parent? Don't clear here, if they move it to a new
		// DataSet with the same column name then we can just start
		// using that right away
		if(parent == null)
			return null;

		try
		{
			// Answered or not?
			if(answer != null)
			{
				// Recheck that the column selected is still in the parent data
				DataColumn dc = parent.getColumn(answer.toString());

				// Check the type
				if(columnType != null && dc.getMode() != columnType)
				{
					clearAnswer();
				}
			}
			else
			{
				// Attempt to automatically answer
				autoAnswer();
			}
		}
		catch(MarlaException ex)
		{
			clearAnswer();
		}
		
		return answer;
	}

	@Override
	public boolean autoAnswer()
	{
		List<String> options = getOptions();
		if(options.size() == 1)
		{
			try
			{
				setAnswer(options.get(0));
				return true;
			}
			catch(OperationInfoRequiredException ex)
			{
				throw new InternalMarlaException("Column combo attempted to invalidly auto-answer question");
			}
		}
		else
			return false;
	}

	@Override
	public List<String> getOptions()
	{
		List<String> opts = new ArrayList<String>();

		try
		{
			// Get columns in the given mode (if possible)
			DataSource ds = getOperation().getParentData();
			if(ds == null)
				return opts;

			for(int i = 0; i < ds.getColumnCount(); i++)
			{
				DataColumn dc = ds.getColumn(i);
				if(columnType == null || dc.getMode() == columnType)
					opts.add(dc.getName());
			}

			return opts;
		}
		catch(MarlaException ex)
		{
			// Can't do anything right now
			return opts;
		}
	}

	@Override
	public void clearAnswer()
	{
		changeBeginning("clearing question " + getName() + " answer");
		
		answer = null;

		getOperation().checkDisplayName();
		getOperation().markDirty();
		markUnsaved();
	}

	@Override
	protected void toXmlAnswer(Element saveEl)
	{
		if(answer != null)
			saveEl.setAttribute("answer", answer);
	}

	@Override
	protected void fromXmlAnswer(Element answerEl)
	{
		answer = answerEl.getAttributeValue("answer");
	}

	@Override
	OperationInformation clone(Operation parent)
	{
		return new OperationInfoColumn(parent, this);
	}
}
