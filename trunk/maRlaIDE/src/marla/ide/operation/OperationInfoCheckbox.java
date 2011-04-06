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

import org.jdom.Element;
import marla.ide.problem.InternalMarlaException;

/**
 * Binary selection information 
 * @author Ryan Morehart
 */
public class OperationInfoCheckbox extends OperationInformation
{
	/**
	 * Answer to the question. Null if there is none
	 */
	private Boolean answer = null;

	/**
	 * Constructs a new checkbox prompt.
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 */
	public OperationInfoCheckbox(Operation op, String name, String prompt)
	{
		super(op, name, prompt, PromptType.CHECKBOX);
	}

	/**
	 * Copy constructor
	 * @param parent Operation this information belongs to. Does not actually
	 *		place the information in that operation!
	 * @param org Information to copy
	 */
	protected OperationInfoCheckbox(Operation parent, OperationInfoCheckbox org)
	{
		super(parent, org);
		answer = org.answer;
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

		if(newAnswer == null)
			throw new InternalMarlaException("Info may only be cleared by calling clearAnswer()");
		
		changeBeginning();
		
		if(newAnswer instanceof Boolean)
			answer = (Boolean)newAnswer;
		else
			answer = Boolean.valueOf(newAnswer.toString());

		getOperation().checkDisplayName();
		getOperation().markDirty();
		markUnsaved();

		return oldAnswer;
	}

	@Override
	public void clearAnswer()
	{
		changeBeginning();
		
		answer = null;
		
		getOperation().checkDisplayName();
		getOperation().markDirty();
		markUnsaved();
	}

	@Override
	public boolean autoAnswer()
	{
		return false;
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
			setAnswer(answerStr);
	}

	@Override
	OperationInformation clone(Operation parent)
	{
		return new OperationInfoCheckbox(parent, this);
	}
}
