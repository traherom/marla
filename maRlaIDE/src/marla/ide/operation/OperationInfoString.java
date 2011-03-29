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

import java.util.regex.Pattern;
import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * @author Ryan Morehart
 */
public class OperationInfoString extends OperationInformation
{
	/**
	 * Pattern that our answer must match
	 */
	private final Pattern mustMatchPatt;
	/**
	 * Answer for this question, null if there is none
	 */
	private String answer = null;

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param options Options to display to the user, will be presented in the
	 *	order given, not sorted in any way.
	 */
	public OperationInfoString(Operation op, String name, String prompt)
	{
		this(op, name, prompt, ".*");
	}

	public OperationInfoString(Operation op, String name, String prompt, String mustMatch)
	{
		super(op, name, prompt, PromptType.STRING);
		this.mustMatchPatt = Pattern.compile(mustMatch);
	}

	@Override
	public String getAnswer()
	{
		return answer;
	}

	@Override
	public String setAnswer(Object newAnswer) 
	{
		String oldAnswer = answer;

		if(newAnswer == null)
			throw new InternalMarlaException("Info may only be cleared by calling clearAnswer()");

		// Ensure it matches the pattern
		String a = newAnswer.toString();
		if(!mustMatchPatt.matcher(a).matches())
			throw new OperationInfoRequiredException("'" + a + "' invalid answer for '" + getName() + "'", getOperation());

		// Save
		answer = newAnswer.toString();
		
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

	/**
	 * Returns the pattern that this string argument must meet to be accepted
	 * @return Regular expression being checked
	 */
	public String getPattern()
	{
		return mustMatchPatt.pattern();
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
		try
		{
			String answerStr = answerEl.getAttributeValue("answer");
			if(answerStr != null)
				setAnswer(answerStr);
		}
		catch(OperationInfoRequiredException ex)
		{
			// Make them re-answer
			clearAnswer();
		}
	}

	@Override
	public boolean autoAnswer()
	{
		return false;
	}
}
