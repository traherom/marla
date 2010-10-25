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

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterator for working with the values in a DataColumn
 * @author Ryan Morehart
 */
public class DataColumnIterator implements ListIterator<Double>
{
	/**
	 * DataColumn this iterator uses
	 */
	private DataColumn col;
	/**
	 * Index we are currently at. Points to the element that would be returned
	 * by next()
	 */
	private int current;
	/**
	 * Whether it would be legal to set, add, or remove. Must have just done a
	 * next() or previous(). See ListIterator documentation
	 */
	private boolean mayChange = false;

	/**
	 * Creates an iterator pointed to the first element of in a data column
	 * @param column DataColumn this iterator will operate over
	 */
	public DataColumnIterator(DataColumn column)
	{
		this(column, 0);
	}

	/**
	 * Creates an iterator pointed to the specified element in the
	 * data column.
	 * @param column DataColumn this iterator will operate over
	 * @param location Index in data column to start iterating at
	 */
	public DataColumnIterator(DataColumn column, int location)
	{
		// Location must actually be in the column
		// Note we allow the location be at size(), IE there would be no next()
		// when we first start
		if(location < 0 || column.size() < location)
		{
			throw new NoSuchElementException("Stating index for iterator illegal"
					+ " (given " + location + ", must be 0 to " + column.size() + ")");
		}

		col = column;
		current = location;
	}

	@Override
	public boolean hasNext()
	{
		// Are we not sitting at the end?
		return (current != col.size());
	}

	@Override
	public Double next()
	{
		if(!hasNext())
			throw new NoSuchElementException("Already at end of DataColumn");

		mayChange = true;

		Double val = col.get(current);
		current++;
		return val;
	}

	@Override
	public boolean hasPrevious()
	{
		// Are we not at beginning?
		return current != 0;
	}

	@Override
	public Double previous()
	{
		if(!hasPrevious())
			throw new NoSuchElementException("Already at beginning of DataColumn");

		mayChange = true;

		current--;
		return col.get(current);
	}

	@Override
	public int nextIndex()
	{
		// Should return list size if at end of list. next()
		// prevent current from moving beyond col.size(), so
		// this works out
		return current;
	}

	@Override
	public int previousIndex()
	{
		// Should return list size if at end of list. previous()
		// prevent current from moving beyond 0, so
		// this works out
		return current - 1;
	}

	@Override
	public void remove()
	{
		if(!mayChange)
			throw new IllegalStateException("Must call next() or previous() first");

		mayChange = false;
		col.remove(current);
	}

	@Override
	public void set(Double e)
	{
		if(!mayChange)
			throw new IllegalStateException("Must call next() or previous() first");

		col.set(current, e);
	}

	@Override
	public void add(Double e)
	{
		if(!mayChange)
			throw new IllegalStateException("Must call next() or previous() first");

		mayChange = false;
		col.add(current, e);
	}
}
