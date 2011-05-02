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

package marla.ide.r;

/**
 * Thrown when an action is attempted on the current RProcessor instance but
 * the internal R process is dead/closed.
 * @author Ryan Morehart
 */
public class RProcessorDeadException extends RProcessorException
{
	public RProcessorDeadException(String msg)
	{
		super(msg);
	}

	public RProcessorDeadException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
