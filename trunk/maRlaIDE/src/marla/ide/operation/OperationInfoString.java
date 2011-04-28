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

import java.util.regex.Pattern;
import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * String prompt for the user. Freely entered, although may be check by a 
 * regular expression
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
	 * Constructs a new free-input string prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 */
	public OperationInfoString(Operation op, String name, String prompt)
	{
		this(op, name, prompt, ".*");
	}

	/**
	 * Constructs a new checked string input prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param mustMatch Regular expression that the user-entered value must match
	 */
	public OperationInfoString(Operation op, String name, String prompt, String mustMatch)
	{
		super(op, name, prompt, PromptType.STRING);
		this.mustMatchPatt = Pattern.compile(mustMatch);
	}

	/**
	 * Copy constructor
	 * @param parent Operation this information belongs to. Does not actually
	 *		place the information in that operation!
	 * @param org Information to copy
	 */
	protected OperationInfoString(Operation parent, OperationInfoString org)
	{
		super(parent, org);
		answer = org.answer;
		mustMatchPatt = org.mustMatchPatt;
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

		changeBeginning("question " + getName() + " answer");
		
		// Ensure it matches the pattern
		String a = newAnswer.toString();
		if(!mustMatchPatt.matcher(a).matches())
			throw new OperationInfoRequiredException("'" + a + "' invalid answer for '" + getName() + "'", getOperation());

		// Save
		answer = newAnswer.toString();
		
		getOperation().checkDisplayName();
		getOperation().markDirty();
		markUnsaved();

		return oldAnswer;
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

	@Override
	OperationInformation clone(Operation parent)
	{
		return new OperationInfoString(parent, this);
	}
}
