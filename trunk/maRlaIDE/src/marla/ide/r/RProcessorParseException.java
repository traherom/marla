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
package marla.ide.r;

import marla.ide.problem.MarlaException;

/**
 * Thrown when an R output cannot be parsed as specified.
 * @author Ryan Morehart
 */
public class RProcessorParseException extends RProcessorException
{
	public RProcessorParseException(String msg)
	{
		super(msg);
	}

	public RProcessorParseException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}