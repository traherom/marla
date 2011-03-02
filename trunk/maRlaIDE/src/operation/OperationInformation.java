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
import problem.MarlaException;

/**
 * @author Ryan Morehart
 */
public abstract class OperationInformation
{
	private final String prompt;
	private final String name;
	private final Operation op;
	private final PromptType type;

	/**
	 * Prompts that can be requested, used by get and setPromptTypes()
	 */
	public enum PromptType
	{
		COLUMN, COMBO, STRING, NUMERIC, CHECKBOX, FIXED
	};

	/**
	 * Creates a new piece of operation information with the basic data set
	 * @param op
	 * @param name
	 * @param prompt
	 */
	protected OperationInformation(Operation op, String name, String prompt, PromptType type)
	{
		this.op = op;
		this.name = name;
		this.prompt = prompt;
		this.type = type;
	}

	/**
	 * Returns the operation associated with this prompt
	 * @return Operation that created this OperationInformation
	 */
	public final Operation getOperation()
	{
		return op;
	}

	/**
	 * Gets the user-visible prompt for this operation
	 * @return Prompt to show the user
	 */
	public final String getPrompt()
	{
		return prompt;
	}

	/**
	 * Gets the internal name for this information
	 * @return Name of the information
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Returns the currently set answer to the question
	 * @return Current answer, null if there is none.
	 */
	public abstract Object getAnswer();

	/**
	 * Sets the answer to the requested information and returns the previously
	 * set answer
	 * @param newAnswer New answer to use for this question
	 * @return Previously set answer, null if there was none
	 */
	public abstract Object setAnswer(Object newAnswer) throws OperationInfoRequiredException;

	/**
	 * Clears any currently set answer to this question
	 */
	public abstract void clearAnswer();

	/**
	 * Attempts to automatically answer the question. For example, if there
	 * is only one option, that option is selected. If an answer is set that
	 * is invalid and a valid one can be determined, the valid answer will
	 * be used.
	 * @return true if automatically answered, false otherwise
	 */
	public abstract boolean autoAnswer();

	/**
	 * Notes if the question has a valid answer or not.
	 * @return true if it is answered and the answer is still valid, false otherwise
	 */
	public final boolean isAnswered()
	{
		return (getAnswer() != null);
	}

	/**
	 * Returns the type of question being asked, rather than having to instanceof
	 * every OperationInformation object
	 * @return Type of information requested
	 */
	public final PromptType getType()
	{
		return type;
	}

	/**
	 * Returns this OperationInformation object as an XML element
	 * @return New XML holding all relevant information 
	 */
	public final Element toXml() throws MarlaException
	{
		Element questionEl = new Element("question");

		// Save name so we can restore the answer to the right operation next time
		questionEl.setAttribute("name", name);

		// And ask the derivative class to save the answer
		toXmlAnswer(questionEl);

		return questionEl;
	}

	/**
	 * Saves the question answer (if there is one) to the given element
	 */
	protected abstract void toXmlAnswer(Element saveEl);

	/**
	 * Runs through the questions from XML and passes them on the the loaded questions
	 * in the given Operation
	 * @param questionEls XML to read questions from
	 * @param Operation Operation to fill in answers for
	 */
	public static void fromXml(Element questionEl, Operation op)
	{
		op.getQuestion(questionEl.getAttributeValue("name")).fromXmlAnswer(questionEl);
	}

	/*
	 * Restores the answer from the given XML element
	 */
	protected abstract void fromXmlAnswer(Element answerEl);

	@Override
	public boolean equals(Object other)
	{
		// Ourselves?
		if(other == this)
			return true;

		// Actually an operation?
		if(!(other instanceof OperationInformation))
			return false;

		OperationInformation otherInfo = (OperationInformation) other;

		// Anything obviously different?
		if(type != otherInfo.type)
			return false;
		if(!name.equals(otherInfo.name))
			return false;
		if(!prompt.equals(otherInfo.prompt))
			return false;

		// Is the answer the same?
		Object ourAnswer = getAnswer();
		Object theirAnswer = otherInfo.getAnswer();
		if(ourAnswer == null && theirAnswer != null)
			return false;
		else if(ourAnswer != null && !ourAnswer.equals(theirAnswer))
			return false;
		
		// Alrighty then
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 37 * hash + (this.prompt != null ? this.prompt.hashCode() : 0);
		hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);

		Object answer = getAnswer();
		hash = 37 * hash + (answer != null ? answer.hashCode() : 0);

		return hash;
	}
}
