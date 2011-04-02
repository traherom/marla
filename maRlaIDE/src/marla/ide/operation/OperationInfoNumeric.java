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
package marla.ide.operation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * @author Ryan Morehart
 */
public class OperationInfoNumeric extends OperationInformation
{
	/**
	 * Minimum value for the answer
	 */
	private final double min;
	/**
	 * Maximum value for the answer
	 */
	private final double max;
	/**
	 * Current answer to the question. Null if there is none
	 */
	private Double answer = null;

	/**
	 * Constructs a new numeric input with the given options.
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 */
	public OperationInfoNumeric(Operation op, String name, String prompt)
	{
		super(op, name, prompt, PromptType.NUMERIC);
		this.min = Double.MIN_VALUE;
		this.max = Double.MAX_VALUE;
	}

	/**
	 * Constructs a new numeric input with the given options.
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param min Minimum (inclusive) value which the user may enter for this value
	 * @param max Maximum (inclusive) value which the user may enter for this value
	 */
	public OperationInfoNumeric(Operation op, String name, String prompt, double min, double max)
	{
		super(op, name, prompt, PromptType.NUMERIC);
		this.min = min;
		this.max = max;

		// Max had better be greater or equal to the min
		if(max < min)
			throw new OperationException("Minimum for query is greater than the maximum");
	}

	@Override
	public Double getAnswer()
	{
		return answer;
	}

	@Override
	public Double setAnswer(Object newAnswer)
	{
		Double oldAnswer = answer;

		if(newAnswer == null)
			throw new InternalMarlaException("Info may only be cleared by calling clearAnswer()");

		// Ensure it matches requirements
		Double a;
		if(newAnswer instanceof Double)
			a = (Double)newAnswer;
		else
		{
			try
			{
				a = Double.valueOf(newAnswer.toString());
			}
			catch(NumberFormatException ex)
			{
				throw new OperationInfoRequiredException("'" + newAnswer + "' not a number", getOperation());
			}
		}

		if(a < min)
			throw new OperationInfoRequiredException("Set answer is below the minimum of " + min, getOperation());
		if(a > max)
			throw new OperationInfoRequiredException("Set answer is above the maximum of " + max, getOperation());

		// Assign
		answer = a;
		
		getOperation().checkDisplayName();
		getOperation().markDirty();
		getOperation().markUnsaved();

		return oldAnswer;
	}

	@Override
	public void clearAnswer()
	{
		answer = null;
		getOperation().checkDisplayName();
		getOperation().markDirty();
		getOperation().markUnsaved();
	}

	@Override
	public boolean autoAnswer()
	{
		if(min == max)
		{
			try
			{
				setAnswer(min);
				return true;
			}
			catch(OperationInfoRequiredException ex)
			{
				throw new InternalMarlaException("Numeric information was attempted to be autofilled but failed to set properly");
			}
		}
		else
			return false;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	@Override
	protected void toXmlAnswer(Element saveEl)
	{
		if(answer != null)
			saveEl.setAttribute("answer", answer.toString());
	}

	@Override
	protected void fromXmlAnswer(Element answerEl)
	{
		try
		{
			String answerStr = answerEl.getAttributeValue("answer");
			if(answerStr != null)
				setAnswer(Double.valueOf(answerStr));
		}
		catch(OperationInfoRequiredException ex)
		{
			// Make them re-answer
			clearAnswer();
		}
	}
}
