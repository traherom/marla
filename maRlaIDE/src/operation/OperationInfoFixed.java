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
import problem.InternalMarlaException;

/**
 * @author Ryan Morehart
 */
public class OperationInfoFixed extends OperationInformation
{
	/**
	 * Answer to the question. Null if there is none
	 */
	private final Object answer;

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param options Options to display to the user, will be presented in the
	 *	order given, not sorted in any way.
	 * @param newAnswer Answer to assign to this question.
	 */
	public OperationInfoFixed(Operation op, String name, String prompt, Object newAnswer)
	{
		super(op, name, prompt, PromptType.FIXED);

		Object finalAnswer = null;

		// If it's not a string, just save it. Assume they know best
		if(!(newAnswer instanceof String))
			finalAnswer = newAnswer;

		// Can we parse it as something?
		if(finalAnswer == null)
		{
			try
			{
				Double d = Double.valueOf((String)newAnswer);
				finalAnswer = d;
			}
			catch(NumberFormatException ex) { }
		}

		// Can we parse it as something?
		if(finalAnswer == null)
		{
			try
			{
				Boolean b = Boolean.valueOf(((String)newAnswer).toLowerCase());
				finalAnswer = b;
			}
			catch(NumberFormatException ex) { }
		}

		// Oh well, save it as a string
		if(finalAnswer == null)
			finalAnswer = newAnswer;

		// Actually init
		answer = finalAnswer;
	}

	@Override
	public Object getAnswer()
	{
		return answer;
	}

	@Override
	public Boolean setAnswer(Object newAnswer)
	{
		throw new InternalMarlaException("Fixed information may not be changed once set");
	}

	@Override
	public void clearAnswer()
	{
		throw new InternalMarlaException("Fixed information may not be changed");
	}

	@Override
	public boolean autoAnswer()
	{
		return true;
	}

	@Override
	protected void toXmlAnswer(Element saveEl)
	{
		// Never save
	}

	@Override
	protected void fromXmlAnswer(Element answerEl)
	{
		// Never load it
	}
}
