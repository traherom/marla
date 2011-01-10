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
package problem;

/**
 * Used to indicate when a method doesn't have the necessary information
 * to complete the request, typically because prior setting methods need
 * to be called first.
 * 
 * @author Ryan Morehart
 */
public class IncompleteInitializationException extends MarlaException
{
	public IncompleteInitializationException(String msg)
	{
		super(msg);
	}

	public IncompleteInitializationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
