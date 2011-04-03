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

import java.util.List;

/**
 * Information with multiple options
 * @author Ryan Morehart
 */
public abstract class OperationInfoCombo extends OperationInformation
{
	/**
	 * Constructs a new combo select prompt with the given options.
	 * @param op Operation that this question applies to
	 * @param name Unique reference name for this prompt
	 * @param prompt User-visible prompt
	 * @param type Lower-level type of combo box this is
	 */
	protected OperationInfoCombo(Operation op, String name, String prompt, PromptType type)
	{
		super(op, name, prompt, type);
	}

	/**
	 * Returns the possible options for this combo. List is unmodifiable
	 * @return List of options
	 */
	public abstract List<String> getOptions();
}
