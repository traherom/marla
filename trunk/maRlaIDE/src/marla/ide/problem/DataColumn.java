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
package marla.ide.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Simple named list of data that belongs to a DataSource. List
 * implementation isn't complete, as iterators don't actually work yet.
 * 
 * @author Ryan Morehart
 */
public class DataColumn implements List<Object>
{
	/**
	 * The DataSource we belong to.
	 */
	private DataSet parent = null;
	/**
	 * Name of the column
	 */
	private String name = new String();
	/**
	 * Data in column. Could be strings or doubles
	 */
	private ArrayList<Object> values = new ArrayList<Object>();
	/**
	 * Mode this column is in
	 */
	private DataMode mode = DataMode.NUMERIC;
	/**
	 * Valid storage modes for a column to be in. Every value in it will
	 * be interpreted this way.
	 */
	public enum DataMode {NUMERIC, STRING};

	/**
	 * Creates a new DataColumn with the given name that does not
	 * belong to a certain DataSource
	 * @param name Human-friendly name for column
	 */
	public DataColumn(DataSet parent, String name)
	{
		this.parent = parent;
		this.name = name;
	}

	/**
	 * Creates a deep copy of the current data column pointing to the same parent.
	 * @param col Column to copy values from
	 * @param parent DataSource the copy should belong to
	 */
	public DataColumn(DataColumn col, DataSet parent)
	{
		this.parent = parent;
		name = col.name;
		for(Object v : col.values)
		{
			values.add(v);
		}
	}

	/**
	 * Returns the name this data column goes by.
	 * @return Current name of column
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Changes the name this data column goes by.
	 * @param newName New name for column.
	 */
	public final void setName(String newName)
	{
		if(parent != null && !parent.isUniqueColumnName(newName))
		{
			throw new DuplicateNameException("Data column with name '"
					+ newName + "' already exists in data source '" + parent.getName() + "'");
		}

		name = newName;
		markChanged();
	}

	/**
	 * Returns the DataSource that this column belongs to
	 * @return Parent DataSource
	 */
	public DataSource getParentData()
	{
		return parent;
	}

	/**
	 * Determines if this DataColumn contains numerical values (returned as Doubles
	 * by other functions)
	 * @return true if the data should be interpreted as Doubles
	 */
	public boolean isNumeric()
	{
		return mode == DataMode.NUMERIC;
	}

	/**
	 * Determines if this DataColumn contains strings (R "factors")
	 * @return true if the data should be interpreted as strings
	 */
	public boolean isString()
	{
		return mode == DataMode.STRING;
	}

	/**
	 * Returns the interpretation mode of the DataColumn. Every value returned
	 * from the column will be cost to an appropriate object for that type
	 * (numerical is for doubles, for example).
	 * @return Mode from DataMode
	 */
	public DataMode getMode()
	{
		return mode;
	}

	/**
	 * Changes the interpretation mode of the DataColumn and returns the old mode
	 * @param newMode New DataMode to put the DataColumn into
	 * @return DataMode the DataColumn used to be operating in.
	 */
	public DataMode setMode(DataMode newMode)
	{
		DataMode oldMode = mode;
		mode = newMode;

		// Only changed if they actually switched modes
		if(oldMode != newMode)
			markChanged();

		return oldMode;
	}

	/**
	 * Changes the interpretation mode of the DataColumn to the mode which
	 * matches the data. For example, if all elements can be interpreted as
	 * numbers, it will be put into NUMERIC mode.
	 * @return DataMode the DataColumn is now operating under
	 */
	public DataMode autodetectMode()
	{
		// Try to interpret everything as a number. If this fails then
		// we switch to STRING
		DataMode oldMode = mode;
		mode = DataMode.NUMERIC;

		// Try to cast everything as a number
		try
		{
			for(Object o : values)
				castToMode(o);
		}
		catch(NumberFormatException ex)
		{
			mode = DataMode.STRING;
		}

		// Only changed if we actually switched modes
		if(oldMode != mode)
			markChanged();

		return mode;
	}

	/**
	 * Adds an element to the end of the column
	 * @param val New element value to be added
	 * @return True if the value was successfully placed in column
	 */
	@Override
	public boolean add(Object val)
	{
		markChanged();
		return values.add(val);
	}

	/**
	 * Returns the number of elements in the data column
	 * @return Count of number of elements in column.
	 */
	@Override
	public int size()
	{
		return values.size();
	}

	/**
	 * Indicates if the data column has no elements
	 * @return True if the data column is empty.
	 */
	@Override
	public boolean isEmpty()
	{
		return values.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return values.contains(o);
	}

	@Override
	public Iterator<Object> iterator()
	{
		return new DataColumnIterator(this);
	}

	@Override
	public Object[] toArray()
	{
		castAllToMode();
		return values.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		castAllToMode();
		return values.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		if(values.remove(o))
		{
			markChanged();
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return values.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c)
	{
		if(values.addAll(c))
		{
			markChanged();
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c)
	{
		if(values.addAll(index, c))
		{
			markChanged();
			return true;
		}
		else
			return false;

	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		if(values.removeAll(c))
		{
			markChanged();
			return true;
		}
		else
			return false;
	}

	/**
	 * Keeps only those elements in the given collection
	 * @param c Elements to keep in column
	 * @return true if the column has been changed.
	 */
	@Override
	public boolean retainAll(Collection<?> c)
	{
		if(values.retainAll(c))
		{
			markChanged();
			return true;
		}
		else
			return false;
	}

	/**
	 * Empties column of all data values. Only marks DataSource as changed if
	 * it wasn't already empty.
	 */
	@Override
	public void clear()
	{
		if(!values.isEmpty())
		{
			markChanged();
			values.clear();
		}
	}

	/**
	 * Returns the value at the given index in the column.
	 * @param index Location in column
	 * @return Current value at requested location.
	 */
	@Override
	public Object get(int index)
	{
		return castToMode(values.get(index));
	}

	/**
	 * Converts the given object to the correct type for the mode the DataColumn
	 * is in.
	 * @param val Object (Double or String preferably) to save to the column
	 * @return 
	 */
	private Object castToMode(Object val)
	{
		if(mode == DataMode.NUMERIC)
			return Double.valueOf(val.toString());
		else
			return val.toString();
	}

	/**
	 * Forces all objects to be cast to the correct mode now,
	 * rather than lazily when the values are retrieved
	 */
	private void castAllToMode()
	{
		// Convert all values to the correct mode now,
		// rather than waiting for the lazy cast
		for(int i = 0; i < values.size(); i++)
			values.set(i, castToMode(values.get(i)));
	}

	/**
	 * Changes a value in the list. Marks DataSource as changed if the value
	 * differs from the original. Changes mode to string if needed
	 * @param index Index in the column to change
	 * @param element New value for column
	 * @return Old value at given location
	 */
	@Override
	public Object set(int index, Object element)
	{
		Object old = values.get(index);

		try
		{
			values.set(index, castToMode(element));
		}
		catch(NumberFormatException ex)
		{
			// Change modes and try again
			mode = DataMode.STRING;
			values.set(index, castToMode(element));
		}

		// Only mark unsaved if it actually set a new value
		if(!old.equals(element))
			markChanged();

		return old;
	}

	/**
	 * Inserts an element at the given index. Elements are shifted down (higher
	 * index) to make room.
	 * @param index Location to insert element at.
	 * @param element New value to insert.
	 */
	@Override
	public void add(int index, Object element)
	{
		markChanged();
		values.add(index, element);
	}

	/**
	 * Remove an element at the given index. Elements are shifted up (lower
	 * index) to compensate.
	 * @param index Index to delete.
	 * @return Old value in the index location.
	 */
	@Override
	public Object remove(int index)
	{
		markChanged();
		return values.remove(index);
	}

	/**
	 * Locates the first instance of a value in the column.
	 * @param o Value to find
	 * @return Index of first instance of value in column
	 */
	@Override
	public int indexOf(Object o)
	{
		return values.indexOf(o);
	}

	/**
	 * Locates the last instance of a value in the column.
	 * @param o Value to find
	 * @return Index of last instance of value in column
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return values.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator()
	{
		return new DataColumnIterator(this);
	}

	@Override
	public ListIterator<Object> listIterator(int index)
	{
		return new DataColumnIterator(this, index);
	}

	@Override
	public DataColumn subList(int fromIndex, int toIndex)
	{
		throw new UnsupportedOperationException("DataColumn slicing is not supported.");
	}

	/**
	 * Outputs the data column as an R vector of elements
	 * @return R vector with the data in this column
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < values.size(); i++)
		{
			if(mode == DataMode.STRING)
				sb.append('"');
			sb.append(castToMode(values.get(i)));
			if(mode == DataMode.STRING)
				sb.append('"');

			sb.append(", ");
		}
		if(values.size() > 0)
			sb.replace(sb.length() - 2, sb.length(), "");

		return sb.toString();
	}

	/**
	 * A DataColumn is equal if all solution ops, columns, and name are the same
	 * @param other Object to compare against
	 * @return True if the the given object is the same as this one
	 */
	@Override
	public boolean equals(Object other)
	{
		// Ourselves?
		if(other == this)
			return true;

		// Actually a problem?
		if(!(other instanceof DataColumn))
			return false;

		DataColumn otherCol = (DataColumn) other;
		if(!mode.equals(otherCol.mode))
			return false;
		if(!name.equals(otherCol.name))
			return false;

		// Convert all values to the correct mode, to ensure we compare fairly
		castAllToMode();
		otherCol.castAllToMode();
		if(!values.equals(otherCol.values))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		castAllToMode();

		int hash = 5;
		hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 11 * hash + (this.values != null ? this.values.hashCode() : 0);
		hash = 11 * hash + (this.mode != null ? this.mode.hashCode() : 0);
		return hash;
	}

	/**
	 * Marks this DataColumn as having changes than haven't been saved
	 * and tells parent about it, so that they can take appropriate action
	 */
	public void markChanged()
	{
		if(parent != null)
		{
			parent.markUnsaved();
			parent.markDirty ();
		}
	}
}
