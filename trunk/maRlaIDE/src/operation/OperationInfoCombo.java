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

import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Morehart
 */
public class OperationInfoCombo implements OperationInformation
{
	private String prompt = null;
	private String name = null;
	List<String> options = null;

	public OperationInfoCombo(String name, String prompt, List<String> options)
	{
		this.name = name;
		this.prompt = prompt;
		this.options = options;
	}

	@Override
	public String getPrompt()
	{
		return prompt;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the possible options for this combo
	 * @return List of options
	 */
	public List<String> getOptions()
	{
		return Collections.unmodifiableList(options);
	}
}
