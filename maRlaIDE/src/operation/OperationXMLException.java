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

import operation.OperationException;

/**
 * Thrown when an error occurs within XML operations, often due to improper XML
 * specification.
 * @author Ryan Morehart
 */
public class OperationXMLException extends OperationException
{
	private String opName = null;

	public OperationXMLException(String msg)
	{
		super(msg);
	}

	public OperationXMLException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public void addName(String newName)
	{
		opName = newName;
	}

	@Override
	public String getMessage()
	{
		if(opName == null)
			return super.getMessage();
		else
			return opName + ": " + super.getMessage();
	}

	@Override
	public String toString()
	{
		if(opName == null)
			return super.toString();
		else
			return opName + ": " + super.toString();
	}
}
