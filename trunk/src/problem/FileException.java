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
 * Thrown for any error that occurs in the process of saving to disk
 * @author Ryan Morehart
 */
public class FileException extends Exception
{

	/**
	 * Creates a new instance of <code>FileSaveException</code> without detail message.
	 */
	public FileException()
	{
	}

	/**
	 * Constructs an instance of <code>FileSaveException</code> with the specified detail message.
	 * @param msg the detail message.
	 */
	public FileException(String msg)
	{
		super(msg);
	}
}
