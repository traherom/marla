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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;

/**
 * Operation to perform on a parent object that implements
 * the DataAccess interface. The root parent will be a DataSet,
 * which then gets refined down by a chain of Operations.
 *
 * @author Ryan Morehart
 */
public abstract class Operation extends DataSet
{

	/**
	 * Used to create "R acceptable" variable names
	 */
	private static Pattern rNamesPatt = Pattern.compile("[^a-zA-Z_]");
	/**
	 * Parent data that this operation works on
	 */
	protected DataSet parent;

	/**
	 * Sets the text name for the JLabel
	 * @param name Text for JLabel
	 */
	public Operation(String name)
	{
		super(name);
	}

	/**
	 * Creates the appropriate derivative Operation from the given JDOM
	 * XML. Class must be specified as an attribute ("type") of the
	 * Element supplied.
	 * @param opEl JDOM Element with the information to construct Operation
	 * @return Constructed and initialized operation
	 */
	public static Operation fromXml(Element opEl)
	{
		try
		{
			// Create the correct type of Operation
			Class opClass = Class.forName(opEl.getAttributeValue("type"));
			Operation newOp = (Operation) opClass.newInstance();

			int x = Integer.parseInt(opEl.getAttributeValue("x"));
			int y = Integer.parseInt(opEl.getAttributeValue("y"));
			int height = Integer.parseInt(opEl.getAttributeValue("height"));
			int width = Integer.parseInt(opEl.getAttributeValue("width"));
			newOp.setBounds(x, y, height, width);

			// Allow it to do its custom thing
			newOp.fromXmlExtra(opEl);
			return newOp;
		}
		catch(IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(InstantiationException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(ClassNotFoundException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * May be overridden by derivative classes in order to reload extra
	 * information saved for their type of Operation
	 * @param opEl JDOM Element with all data for Operation
	 */
	private void fromXmlExtra(Element opEl)
	{
	}

	/**
	 * Assigns this Operation to a new parent. Should only be called by
	 * the new parent DataSet, as that needs to actually insert the
	 * operation into its array. The package private access is intentional.
	 * 
	 * @param newParent Parent DataSet/Operation we're a part of
	 */
	void setParentData(DataSet newParent) throws CalcException
	{
		parent = newParent;
		refreshCache();
	}

	/**
	 * Returns the parent this Operation derives from
	 * @return Next higher set of data or null if there is none.
	 */
	@Override
	public DataSet getParentData()
	{
		return parent;
	}

	/**
	 * Duplicates an operation
	 * @return Duplicated Operation
	 */
	@Override
	public Operation clone()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(InstantiationException ex)
		{
			throw new RuntimeException(ex);
		}
		catch(IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Recalculates cached columns and informs children to
	 * refresh themselves as well.
	 */
	public void refreshCache() throws CalcException
	{
		try
		{
			columns.clear();
			for(int i = 0; i < parent.getColumnCount(); i++)
			{
				columns.add(calcColumn(i));
			}

			for(Operation op : solutionOps)
			{
				op.refreshCache();
			}
		}
		catch(Exception ex)
		{
			throw new CalcException("An error occured while refreshing the calculation cache", ex);
		}
	}

	/**
	 * Overridden by child operations to actually perform the task. When the
	 * column/other data is requested the deriving class should return
	 * the result of the appropriate operation on the dataset above. If more
	 * than one column is needed in the operation that's fine, just return
	 * only the requested column.
	 *
	 * Will cache results from calcColumn() and recalculate as needed
	 * @param index Column to retrieve
	 */
	@Override
	public DataColumn getColumn(int index)
	{
		return columns.get(index);
	}

	@Override
	public int getColumnIndex(String colName) throws DataNotFound
	{
		return parent.getColumnIndex(colName);
	}

	/**
	 * Overridden by child operations to actually perform the task. When the
	 * column/other data is requested the deriving class should return
	 * the result of the appropriate operation on the dataset above. If more
	 * than one column is needed in the operation that's fine, just return
	 * only the requested column.
	 *
	 * Caching is performed by Operation. Concrete Operation derivatives
	 * should not implement their own caching unless a specific need
	 * arises.
	 * @param index Column to calculate
	 */
	public abstract DataColumn calcColumn(int index) throws CalcException;

	@Override
	public DataSet getAllColumns() throws CalcException
	{
		DataSet newDS = new DataSet(parent.getName() + " solved");
		for(int i = 0; i < parent.getColumnCount(); i++)
		{
			newDS.addColumn(getColumn(i));
		}
		return newDS;
	}

	/**
	 * Returns true if the Operation has questions/prompts for the user.
	 * getRequiredInfoPrompt() returns the actual ArrayList of data needed
	 * @return true if additional information is required
	 */
	public boolean isInfoRequired()
	{
		return !getRequiredInfoPrompt().isEmpty();
	}

	/**
	 * Deriving classes should override this to prompt the user for the
	 * information they need. Alex and I need to work out how this actually
	 * works. We'll need to display a window for it, but we don't want to
	 * do that from here. Do we call back to a class by him?
	 * @return Object[] ArrayList of questions to ask the user. The Object[] array
	 *			is two dimensional, the first element is a verbatim string to
	 *			ask the user and the second is a constant on the question type.
	 *			If the question type requires for information (for example, a
	 *			combo selection box), then the third element in Object[] will
	 *			be whatever is needed.
	 */
	public ArrayList<Object[]> getRequiredInfoPrompt()
	{
		return new ArrayList<Object[]>();
	}

	/**
	 * After the user is prompted for additional values, their selections
	 * are returned as an ArrayList where the index corresponds to the question
	 * originally asked by getInforRequiredPrompt(). This function ignores
	 * the values by default. If a derived class needs to handle them then
	 * it should override this.
	 * @param values ArrayList of Objects that answer the questions
	 */
	public void setRequiredInfo(ArrayList<Object> values)
	{
		
	}

	/**
	 * An Operation is equal all the operations tied to it are the same
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
		if(!(other instanceof Operation))
			return false;

		Operation otherOp = (Operation) other;
		if(!solutionOps.equals(otherOp.solutionOps))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 31 * hash + (this.solutionOps != null ? this.solutionOps.hashCode() : 0);
		return hash;
	}

	/**
	 * Produces the XML Element for this operation. Derivative classes
	 * should override toXmlExtra(Element) if they want to save additional
	 * information.
	 * XML elements to the returned Element.
	 * @return XML Element with all settings for this Operation
	 */
	@Override
	public final Element toXml()
	{
		Element dataEl = new Element("operation");
		dataEl.setAttribute("name", name);
		dataEl.setAttribute("type", this.getClass().getName());

		Rectangle rect = getBounds();
		dataEl.setAttribute("x", Integer.toString((int)rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int)rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int)rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int)rect.getWidth()));

		// Add Ops
		Element opEls = new Element("operations");
		dataEl.addContent(opEls);
		for(Operation op : solutionOps)
		{
			opEls.addContent(op.toXml());
		}

		// Extra info?
		Element extraEl = toXmlExtra();
		if(extraEl != null)
			dataEl.addContent(extraEl);

		return dataEl;
	}

	/**
	 * May be overridden by derivative classes to save additional information
	 * need by their operation type
	 * @return null if no extra information, JDOM Element otherwise
	 */
	protected Element toXmlExtra()
	{
		return null;
	}

	/**
	 * Takes the given name and returns a version of it that is
	 * usable in R (obeys all the naming rules for variables basically)
	 * @param str Name that needs to be cleaned
	 * @return Valid R variable name
	 */
	public static String sanatizeName(String dirtyName)
	{
		Matcher m = Operation.rNamesPatt.matcher(dirtyName);
		return m.replaceAll("");
	}

	/**
	 * Takes the given DataSet (or Operation, obviously) and returns a unique
	 * name for it.
	 * @param str Name that needs to be cleaned
	 * @return Valid R variable name
	 */
	public static String sanatizeName(DataSet ds)
	{
		String s = ds.getName() + Integer.toString(ds.hashCode());
		Matcher m = Operation.rNamesPatt.matcher(s);
		return m.replaceAll("");
	}

	/**
	 * Derivative Operations _must_ override this so that they display the
	 * appropriate operations they are performing on the data
	 * @return
	 */
	@Override
	public abstract String toString();
}
