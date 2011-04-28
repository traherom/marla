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

import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * Single-answer, non-changeable prompt
 * @author Ryan Morehart
 */
public class OperationInfoFixed extends OperationInformation
{
	/**
	 * Answer to the question. Null if there is none
	 */
	private final Object answer;

	/**
	 * Constructs a new fixed "prompt" with the given value. Fixed information
	 * is intended to let the user know parameters that are being used.
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
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

	/**
	 * Copy constructor
	 * @param parent Operation this information belongs to. Does not actually
	 *		place the information in that operation!
	 * @param org Information to copy
	 */
	protected OperationInfoFixed(Operation parent, OperationInfoFixed org)
	{
		super(parent, org);
		answer = org.answer;
	}
	
	@Override
	public Object getAnswer()
	{
		return answer;
	}

	@Override
	public Boolean setAnswer(Object newAnswer)
	{
		// Ignore if they set to the same thing
		if(!answer.equals(newAnswer))
			throw new InternalMarlaException("Fixed information may not be changed once set");
		return Boolean.TRUE;
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

	@Override
	OperationInformation clone(Operation parent)
	{
		return new OperationInfoFixed(parent, getName(), getPrompt(), answer);
	}
}
