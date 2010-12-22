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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.jdom.Element;

/**
 * Simple named list of data that belongs to a DataSet. List
 * implementation isn't complete, as iterators don't actually work yet.
 * 
 * @author Ryan Morehart
 */
public class DataColumn implements List<Double>
{
	/**
	 * The dataset we belong to.
	 */
	private DataSet parent = null;
	/**
	 * Name of the column
	 */
	private String name = new String();
	/**
	 * Data in column. Assumed to be doubles since stats works with those a lot.
	 */
	private ArrayList<Double> values = new ArrayList<Double>();

	/**
	 * Creates a new DataColumn with the given name that does not
	 * belong to a certain DataSet
	 * @param name Human-friendly name for column
	 * @throws DuplicateNameException There is already a column in this DataSet with the same name
	 * @throws CalcException Unable to compute values for the new column
	 */
	public DataColumn(String name) throws DuplicateNameException
	{
		this(null, name);
	}

	/**
	 * Creates a new data column for a given dataset with the given name.
	 * @param parent DataSet we belong to
	 * @param name Human-friendly name for this column
	 * @throws DuplicateNameException There is already a column in this DataSet with the same name
	 * @throws CalcException UNable to compute values for the new column
	 */
	public DataColumn(DataSet parent, String name) throws DuplicateNameException
	{
		this.parent = parent;
		setName(name);
	}

	/**
	 * Creates a deep copy of the current data column pointing to the same parent.
	 * @param col Column to copy values from
	 * @param parent DataSet the copy should belong to
	 */
	public DataColumn(DataColumn col, DataSet parent)
	{
		this.parent = parent;
		name = col.name;
		for(Double v : col.values)
		{
			values.add(new Double(v));
		}
	}

	/**
	 * Private function help create a copy of just a part of the column.
	 * Points to the same parent but may not truly belong to it.
	 * @param col Column to copy values from
	 * @param fromIndex Beginning index to start copying at
	 * @param toIndex Last index to include in new column
	 */
	private DataColumn(DataColumn col, int fromIndex, int toIndex)
	{
		throw new UnsupportedOperationException("Not yet implemented");
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
	 * @param name New name for column.
	 * @throws DuplicateNameException Another DataColumn with the given name already exists
	 */
	public final void setName(String name) throws DuplicateNameException
	{
		if(parent != null)
		{
			// Make sure no other columns have this name
			for(int i = 0; i < parent.getColumnCount(); i++)
			{
				if(name.equalsIgnoreCase(parent.getColumn(i).getName()))
				{
					throw new DuplicateNameException("Data column with name '"
							+ name + "' already exists in dataset '" + parent.getName() + "'");
				}
			}
		}
		
		markChanged();
		this.name = name;
	}

	/**
	 * Returns the dataset that this column belongs to
	 * @return Parent DataSet
	 */
	public DataSet getParentDataSet()
	{
		return parent;
	}

	/**
	 * Adds an element to the end of the column
	 * @param val New element value to be added
	 * @return True if the value was successfully placed in column
	 */
	@Override
	public boolean add(Double val)
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
		return values.contains((Double) o);
	}

	@Override
	public Iterator<Double> iterator()
	{
		return new DataColumnIterator(this);
	}

	@Override
	public Object[] toArray()
	{
		return values.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return values.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		if(values.remove((Double) o))
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
	public boolean addAll(Collection<? extends Double> c)
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
	public boolean addAll(int index, Collection<? extends Double> c)
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
	 * Empties column of all data values. Only marks dataset as changed if
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
	public Double get(int index)
	{
		return values.get(index);
	}

	/**
	 * Changes a value in the list. Marks dataset as changed if the value
	 * differs from the original.
	 * @param index Index in the column to change
	 * @param element New value for column
	 * @return Old value at given location
	 */
	@Override
	public Double set(int index, Double element)
	{
		// Only mark unsaved if it actually set a new value
		Double old = values.set(index, element);
		if(old != element && parent != null)
		{
			markChanged();
		}

		return old;
	}

	/**
	 * Inserts an element at the given index. Elements are shifted down (higher
	 * index) to make room.
	 * @param index Location to insert element at.
	 * @param element New value to insert.
	 */
	@Override
	public void add(int index, Double element)
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
	public Double remove(int index)
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
	public ListIterator<Double> listIterator()
	{
		return new DataColumnIterator(this);
	}

	@Override
	public ListIterator<Double> listIterator(int index)
	{
		return new DataColumnIterator(this, index);
	}

	@Override
	public DataColumn subList(int fromIndex, int toIndex)
	{
		throw new UnsupportedOperationException("Not supported yet.");
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
			sb.append(values.get(i));
			sb.append(", ");
		}
		if(values.size() > 0)
			sb.replace(sb.length() - 2, sb.length(), "");

		return sb.toString();
	}

	/**
	 * Generates a JDOM Element with the data from this column
	 * @return JDOM Element containing all data needed to restore this column
	 */
	public Element toXml()
	{
		Element colEl = new Element("column");
		colEl.setAttribute("name", name);

		for(Double d : values)
		{
			colEl.addContent(new Element("value").addContent(d.toString()));
		}
		return colEl;
	}

	/**
	 * Builds a DataColumn from the given JDOM Element
	 * @param colEl JDOM Element with data for column
	 * @return New DataColumn
	 * @throws DuplicateNameException The save file must be wrong, there is already a column with
	 *			this name
	 */
	public static DataColumn fromXml(Element colEl) throws DuplicateNameException
	{
		DataColumn newCol = new DataColumn(colEl.getAttributeValue("name"));
		for(Object el : colEl.getChildren("value"))
		{
			newCol.add(Double.parseDouble(((Element) el).getText()));
		}
		return newCol;
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
		if(!name.equals(otherCol.name))
			return false;
		if(!values.equals(otherCol.values))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 11 * hash + (this.values != null ? this.values.hashCode() : 0);
		return hash;
	}

	/**
	 * Assigns this column to a new DataSet. Removes self from current parent
	 * if needed, but assumes that it has already been assigned to the new
	 * parent. Package scope is intentional.
	 * @param newParent New DataSet we belong to
	 */
	void setParent(DataSet newParent)
	{
		if(parent != null)
		{
			parent.removeColumn(this);
			markChanged();
		}
		parent = newParent;
	}

	/**
	 * Marks this DataColumn as having changes than haven't been saved
	 * and tells parent about it, so that they can take appropriate action
	 */
	public void markChanged()
	{
		if(parent != null)
			parent.markChanged();
	}
}
