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

import org.jdom.Element;

/**
 * @author Ryan Morehart
 */
public class OperationInfoNumeric extends OperationInformation
{
	private final double min;
	private final double max;
	private Double answer = null;

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param options Options to display to the user, will be presented in the
	 *	order given, not sorted in any way.
	 */
	public OperationInfoNumeric(Operation op, String name, String prompt) throws OperationException
	{
		super(op, name, prompt, PromptType.NUMERIC);
		this.min = Double.MIN_VALUE;
		this.max = Double.MAX_VALUE;
	}

	public OperationInfoNumeric(Operation op, String name, String prompt, double min, double max) throws OperationException
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
	public Double setAnswer(Object newAnswer) throws OperationInfoRequiredException
	{
		Double oldAnswer = answer;
		
		// Ensure it matches requirements
		Double a;
		if(newAnswer instanceof Double)
			a = (Double)newAnswer;
		else
			a = Double.valueOf(newAnswer.toString());

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
