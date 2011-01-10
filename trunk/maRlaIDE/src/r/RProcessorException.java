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
package r;

import problem.MarlaException;

/**
 * Thrown when an error occurs within the RProcessor, often in working with R itself
 * or passing improper commands or parameters.
 * @author Ryan Morehart
 */
public class RProcessorException extends MarlaException
{
	public RProcessorException(String msg)
	{
		super(msg);
	}

	public RProcessorException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
