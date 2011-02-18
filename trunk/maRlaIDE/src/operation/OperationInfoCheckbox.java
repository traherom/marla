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
public class OperationInfoCheckbox extends OperationInformation
{
	private Boolean answer = null;

	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param options Options to display to the user, will be presented in the
	 *	order given, not sorted in any way.
	 */
	public OperationInfoCheckbox(Operation op, String name, String prompt)
	{
		super(op, name, prompt, PromptType.CHECKBOX);
	}

	@Override
	public Boolean getAnswer()
	{
		return answer;
	}

	@Override
	public Boolean setAnswer(Object newAnswer)
	{
		Boolean oldAnswer = answer;
		
		answer = (Boolean)newAnswer;

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

	@Override
	protected void toXmlAnswer(Element saveEl)
	{
		if(answer != null)
			saveEl.setAttribute("answer", answer.toString());
	}

	@Override
	protected void fromXmlAnswer(Element answerEl)
	{
		String answerStr = answerEl.getAttributeValue("answer");
		if(answerStr != null)
			setAnswer(Boolean.valueOf(answerStr));
	}
}
