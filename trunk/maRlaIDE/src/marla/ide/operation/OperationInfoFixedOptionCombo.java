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

import java.util.Collections;
import java.util.List;
import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * Combo selection that is a fixed set of options (vs dynamically found
 * from the parent column or such).
 * @author Ryan Morehart
 */
public class OperationInfoFixedOptionCombo extends OperationInfoCombo
{
	/**
	 * Options that are allowed for this combo information
	 */
	private final List<String> options;
	/**
	 * Selected answer
	 */
	private String answer = null;

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param options Options to display to the user, will be presented in the
	 *	order given, not sorted in any way.
	 */
	public OperationInfoFixedOptionCombo(Operation op, String name, String prompt, List<String> options)
	{
		super(op, name, prompt, PromptType.COMBO);
		this.options = options;

		// If there's only one possibility, then just set that as our answer
		if(options.size() == 1)
			answer = options.get(0);
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

		// Ensure it's within our options
		if(!options.contains((String)newAnswer))
			throw new OperationInfoRequiredException("'" + answer + "' not valid option for combo", getOperation());

		answer = (String)newAnswer;

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
		if(options.size() == 1)
		{
			try
			{
				setAnswer(options.get(0));
				return true;
			}
			catch(OperationInfoRequiredException ex)
			{
				throw new InternalMarlaException("Fixed option combo attempted to invalidly auto-answer question");
			}
		}
		else
			return false;
	}

	/**
	 * Returns the possible options for this combo
	 * @return List of options
	 */
	@Override
	public List<String> getOptions()
	{
		return Collections.unmodifiableList(options);
	}

	/**
	 * Returns an editable list of the options in this prompt. Intended
	 * for derivate class usage.
	 * @return Modifiable list of options
	 */
	protected final List<String> getModifiableOptions()
	{
		return options;
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
}
