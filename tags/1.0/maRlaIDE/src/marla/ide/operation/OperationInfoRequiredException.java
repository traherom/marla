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

package marla.ide.operation;

import marla.ide.problem.MarlaException;

/**
 * Thrown when an operation needs information to be set before it can perform
 * any calculations. getOperation() in this exception returns the operation
 * that needs information.
 * @author Ryan Morehart
 */
public class OperationInfoRequiredException extends MarlaException
{
	/**
	 * STOP WHINING JAVA!
	 */
	private static final long serialVersionUID = -4323435339074124L;
	/**
	 * Operation that is requesting more information
	 */
	private final Operation op;
	
	public OperationInfoRequiredException(String msg, Operation op)
	{
		super(msg);
		this.op = op;
	}

	public OperationInfoRequiredException(String msg, Throwable cause, Operation op)
	{
		super(msg, cause);
		this.op = op;
	}

	/**
	 * Operation that this information applies
	 * @return Operation we reference
	 */
	public final Operation getOperation()
	{
		return op;
	}
}
