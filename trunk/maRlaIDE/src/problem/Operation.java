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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JLabel;
import org.jdom.Element;
import r.OperationXML;
import r.OperationXMLException;
import r.RProcessor;
import r.RProcessorException;
import r.RProcessorParseException;

/**
 * Operation to perform on a parent object that implements
 * the DataAccess interface. The root parent will be a DataSet,
 * which then gets refined down by a chain of Operations.
 *
 * @author Ryan Morehart
 */
public abstract class Operation extends JLabel implements DataSource, Changeable
{
	/**
	 * Operation name.
	 */
	protected String name;
	/**
	 * Actual values from computation
	 */
	protected final DataSet data;
	/**
	 * Commands to perform on this dataset
	 */
	protected final ArrayList<Operation> solutionOps = new ArrayList<Operation>();
	/**
	 * Parent data that this operation works on
	 */
	protected DataSource parent;
	/**
	 * Saves the R operations used the last time refreshCache() was called. This
	 * string can then be dumped out by toRString() to give an idea of how to perform
	 * the calculations
	 */
	protected String operationRecord = null;
	/**
	 * Pointer to the current RProcessor instance
	 */
	protected RProcessor proc = null;
	/**
	 * True if the operation needs to recompute its values before returning results
	 */
	protected boolean isCacheDirty = true;
	/**
	 * True if the operation is recalculating its results, used to allow it to
	 * work with its own columns and not cause infinite recursion. Not certain
	 * about the proper handling of threads here. TODO: ensure thread safety.
	 */
	protected boolean inRecompute = false;
	/**
	 * List of Java Operation derivative classes that may be created by
	 * the GUI front end.
	 */
	protected static HashMap<String, String> javaOps = initJavaOperationList();

	/**
	 * Initializes the list of available Java-based (hard coded) operations.
	 * This list should contain key value pairs with the key being a friendly, user
	 * readable name and the value being the class string, as would be passed to Class.forName().
	 * For example, a mean operation in the the r package would be "Mean" => "r.OperationMean"
	 * @return New HashMap to save into javaOps
	 */
	private static HashMap<String, String> initJavaOperationList()
	{
		HashMap<String, String> ops = new HashMap<String, String>();
		return ops;
	}

	/**
	 * Returns a list of all the operations available, both from XML and Java. This is a
	 * hard coded list for now and eases adding new operations to the GUI (no need to edit the
	 * other package in a few places).
	 * @return ArrayList of the names of operations available. Each name will be unique. If an XML
	 *		operation with the same name as a Java operation exists then the XML version will
	 *		be used. Otherwise an OperationException is thrown.
	 * @throws OperationException Thrown when multiple operations with the same name are detected
	 */
	public static ArrayList<String> getAvailableOperations() throws OperationException
	{
		ArrayList<String> ops = new ArrayList<String>();

		// Java
		ops.addAll(javaOps.keySet());

		// XML operations
		for(String xmlOpName : OperationXML.getAvailableOperations())
		{
			if(!ops.contains(xmlOpName))
				ops.add(xmlOpName);
		}

		// And sort 'em
		Collections.sort(ops);

		return ops;
	}

	/**
	 * Creates a new Operation via the given name. Operations are first searched for in
	 * the currently loaded XML operations list, then in the Java-based list.
	 * @param opName Name of operation to search for, usually taken from getAvailableOperations().
	 * @return Newly created operation of the given type
	 * @throws OperationException Thrown if an operation matching the name cannot be found and/or
	 *		instantiated.
	 */
	public static Operation createOperation(String opName) throws OperationException, RProcessorException
	{
		// Locate the operation
		Operation op = null;
		try
		{
			// Try first in XML, then the hardcoded list
			op = OperationXML.createOperation(opName);
		}
		catch(OperationXMLException ex)
		{
			try
			{
				// Try in the list of Java classes
				Class opClass = Class.forName(Operation.javaOps.get(opName));
				op = (Operation) opClass.newInstance();
			}
			catch(IllegalAccessException ex2)
			{
				throw new OperationException("Illegal access during creation of operation '" + opName + "'", ex2);
			}
			catch(InstantiationException ex2)
			{
				throw new OperationException("Unable to create instance of operation '" + opName + "'", ex2);
			}
			catch(ClassNotFoundException ex2)
			{
				throw new OperationException("Unable to locate operation '" + opName + "' for loading", ex2);
			}
		}

		return op;
	}

	/**
	 * Sets the text name for the JLabel
	 * @param newName Text for JLabel
	 * @throws RProcessorException Thrown when the R processor could not be loaded
	 */
	protected Operation(String newName) throws RProcessorException
	{
		super(newName);
		setName(newName);
		data = new DataSet(this, "internal");
		proc = RProcessor.getInstance();
	}

	/**
	 * Sets the name of the operation, only used internally
	 * @param newName New name for the operation
	 */
	@Override
	public final void setName(String newName)
	{
		// Set the label
		super.setText(newName);

		// And save the op name
		name = newName;
	}

	/**
	 * Creates the appropriate derivative Operation from the given JDOM
	 * XML. Class must be specified as an attribute ("type") of the
	 * Element supplied.
	 * @param opEl JDOM Element with the information to construct Operation
	 * @return Constructed and initialized operation
	 * @throws OperationException Unable to create operation. Inner exception has more information
	 */
	public final static Operation fromXml(Element opEl) throws OperationException
	{
		String opName = opEl.getAttributeValue("type");

		try
		{
			// Create the correct type of Operation
			Class opClass = Class.forName(opName);
			Operation newOp = (Operation) opClass.newInstance();

			int x = Integer.parseInt(opEl.getAttributeValue("x"));
			int y = Integer.parseInt(opEl.getAttributeValue("y"));
			int height = Integer.parseInt(opEl.getAttributeValue("height"));
			int width = Integer.parseInt(opEl.getAttributeValue("width"));
			newOp.setBounds(x, y, width, height);

			// Allow it to do its custom thing
			newOp.fromXmlExtra(opEl);
			return newOp;
		}
		catch(IllegalAccessException ex)
		{
			throw new OperationException("Illegal access during creation of operation '" + opName + "'", ex);
		}
		catch(InstantiationException ex)
		{
			throw new OperationException("Unable to create instance of operation '" + opName + "'", ex);
		}
		catch(ClassNotFoundException ex)
		{
			throw new OperationException("Unable to locate operation '" + opName + "' for loading", ex);
		}
	}

	/**
	 * May be overridden by derivative classes in order to reload extra
	 * information saved for their type of Operation
	 * @param opEl JDOM Element with all data for Operation
	 */
	protected void fromXmlExtra(Element opEl) throws OperationException
	{
	}

	/**
	 * Assigns this Operation to a new parent. Should only be called by
	 * the new parent DataSet, as that needs to actually insert the
	 * operation into its array.
	 * @param newParent Parent DataSet/Operation we're a part of
	 */
	protected final void setParentData(DataSource newParent)
	{
		// If we're already a part of this parent, ignore request
		if(parent == newParent)
			return;

		// Tell our old parent we're removing ourselves
		if(parent != null)
		{
			DataSource oldParent = parent;
			parent = null;
			oldParent.removeOperation(this);
		}

		// Assign ourselves to the new guy
		parent = newParent;
		if(parent != null)
			parent.addOperation(this);

		markUnsaved();
		markChanged();
	}

	/**
	 * Returns the parent this Operation derives from
	 * @return Next higher set of data or null if there is none.
	 */
	public final DataSource getParentData()
	{
		return parent;
	}

	@Override
	public final boolean isUniqueColumnName(String name) throws MarlaException
	{
		checkCache();
		return data.isUniqueColumnName(name);
	}

	@Override
	public final int getColumnIndex(String colName) throws MarlaException
	{
		checkCache();
		return data.getColumnIndex(colName);
	}

	@Override
	public final DataColumn getColumn(String colName) throws MarlaException
	{
		return data.getColumn(colName);
	}

	@Override
	public final DataColumn getColumn(int index) throws MarlaException
	{
		checkCache();
		return data.getColumn(index);
	}

	@Override
	public final int getColumnLength() throws MarlaException
	{
		checkCache();
		return data.getColumnLength();
	}

	@Override
	public final String[] getColumnNames() throws MarlaException
	{
		checkCache();
		return data.getColumnNames();
	}

	@Override
	public final int getOperationCount()
	{
		return solutionOps.size();
	}

	/**
	 * Duplicates an operation. Derivative classes should override this
	 * if additional information needs to be copied.
	 * @return Duplicated Operation
	 */
	@Override
	public Operation clone()
	{
		try
		{
			// Create an operation with the same type
			Operation newOp = Operation.createOperation(name);

			for(Operation op : solutionOps)
			{
				newOp.addOperation(op.clone());
			}

			return newOp;
		}
		catch(MarlaException ex)
		{
			throw new InternalMarlaException("Unable to clone Operation. See internal exception.", ex);
		}
	}

	/**
	 * Refreshes the cache if needed
	 * @throws CalcException Unable to recompute the values for this operation.
	 */
	public final void checkCache() throws MarlaException
	{
		if(isCacheDirty && !inRecompute)
			refreshCache();
	}

	/**
	 * Recalculates cached columns and informs children to
	 * refresh themselves as well.
	 * @throws CalcException Unable to recompute the values for this Operation
	 * @throws OperationInfoRequiredException  Thrown when the operation needs more information to
	 *		complete its calculations. The GUI should catch this and display a dialog for the user
	 *		based on getRequiredInfo().
	 */
	public final synchronized void refreshCache() throws MarlaException
	{
		if(parent == null)
			throw new CalcException("No parent for operation to get data from");

		try
		{
			// Compute new columns and save the way we do so (R commands) for use by toString()
			proc.setRecorder(RProcessor.RecordMode.CMDS_ONLY);
			data.clearColumns();
			inRecompute = true;
			computeColumns();
			operationRecord = proc.fetchInteraction();
			proc.setRecorder(RProcessor.RecordMode.DISABLED);

			// We're clean!
			isCacheDirty = false;
		}
		catch(RProcessorParseException ex)
		{
			throw new CalcException("An error occured while refreshing the calculation cache", ex);
		}
		catch(RProcessorException ex)
		{
			throw new CalcException("An error occured while refreshing the calculation cache", ex);
		}
		finally
		{
			// Well we're certainly not recomputing any more
			inRecompute = false;
		}
	}

	/**
	 * Overridden by child operations to actually perform the task. When the
	 * column/other data is requested the deriving class should place the
	 * result of the appropriate operation on the dataset above in the data
	 * DataSet.
	 *
	 * Caching is performed by Operation. Concrete Operation derivatives
	 * should not implement their own caching unless a specific need
	 * arises.
	 * @throws CalcException Thrown as a result of other functions performing calculations
	 * @throws RProcessorParseException Thrown if the R processor could not parse the R output
	 *		as it was instructed to. Likely a programming error.
	 * @throws RProcessorException Error working with the R process itself (permissions or closed
	 *		pipes, for example).
	 * @throws OperationInfoRequiredException Thrown when the operation needs more information to
	 *		complete its calculations. The GUI should catch this and display a dialog for the user
	 *		based on getRequiredInfo().
	 */
	protected abstract void computeColumns() throws MarlaException;

	/**
	 * Returns true if the Operation has questions/prompts for the user.
	 * getRequiredInfoPrompt() returns the actual ArrayList of data needed
	 * @return true if additional information is required
	 */
	public boolean isInfoRequired() throws MarlaException
	{
		return !getRequiredInfoPrompt().isEmpty();
	}

	/**
	 * Deriving classes should override this to prompt the user for the
	 * information they need.
	 * @return Object[] ArrayList of questions to ask the user. The Object[] array
	 *			is two dimensional, the first element is a verbatim string to
	 *			ask the user and the second is a constant on the question type.
	 *			If the question type requires for information (for example, a
	 *			combo selection box), then the third element in Object[] will
	 *			be whatever is needed.
	 */
	public ArrayList<Object[]> getRequiredInfoPrompt() throws MarlaException
	{
		return new ArrayList<Object[]>();
	}

	/**
	 * After the user is prompted for additional values, their selections
	 * are returned as an ArrayList where the index corresponds to the question
	 * originally asked by getInforRequiredPrompt(). If a derived class needs
	 * to handle them it should override this.
	 * @param values ArrayList of Objects that answer the questions
	 * @throws OperationException Info was attempted to be set when not requested
	 */
	public void setRequiredInfo(ArrayList<Object> values) throws MarlaException
	{
		if(!isInfoRequired())
			throw new OperationException("This operation does not require info, should not be set");
	}

	/**
	 * Returns true if this operation has graphical output. The path to the graphic
	 * file can be obtained via getPlot().
	 * @return true if there is available graphical output, false otherwise
	 * @throws CalcException Unable to perform calculations
	 */
	public boolean hasPlot() throws MarlaException
	{
		return getPlot() != null;
	}

	/**
	 * Returns the path to the graphical plot this operation generated.
	 * @return Path to plot, null if there is none associated with this operation.
	 * @throws CalcException Unable to perform calculations
	 */
	public String getPlot() throws MarlaException
	{
		checkCache();
		return null;
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

		// TODO determine the best way to tell equality
		// The problem is that a user should be able to add multiple
		// ops of the same type to the parent dataset. Sometimes it would be ok
		// to restrict this, but I'd rather not. So for now, we're just saying
		// object equality is good enough.
		//return false;

		// Actually an operation?
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
		dataEl.setAttribute("type", this.getClass().getName());

		Rectangle rect = getBounds();
		dataEl.setAttribute("x", Integer.toString((int) rect.getX()));
		dataEl.setAttribute("y", Integer.toString((int) rect.getY()));
		dataEl.setAttribute("height", Integer.toString((int) rect.getHeight()));
		dataEl.setAttribute("width", Integer.toString((int) rect.getWidth()));

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
	 * Marks the Operation as having had something change about it and it
	 * needing to recompute its values.
	 */
	@Override
	public final void markChanged()
	{
		// Mark as dirty but don't actually recompute yet
		isCacheDirty = true;

		// Tell all children they need to recompute
		for(Operation op : solutionOps)
		{
			op.markChanged();
		}
	}

	/**
	 * Outputs this Operation as the string of R commands it took to produce it
	 * @return R commands
	 */
	@Override
	public final String toRString() throws MarlaException
	{
		checkCache();

		StringBuilder sb = new StringBuilder();
		if(parent != null)
			sb.append(parent.toString());
		sb.append(operationRecord);
		return operationRecord;
	}

	/**
	 * Returns the calculated result of this operation.
	 * @return String of the R commands used to do computations
	 */
	@Override
	public final String toString()
	{
		try
		{
			checkCache();

			// Just display the results as a normal DataSet
			return data.toString();
		}
		catch(MarlaException ex)
		{
			throw new InternalMarlaException("Unable to do toString() because the values could not be computed.", ex);
		}
	}

	@Override
	public final void exportFile(String filePath) throws IOException, MarlaException
	{
		checkCache();
		data.exportFile(filePath);
	}

	@Override
	public final String toRFrame() throws MarlaException
	{
		checkCache();
		return data.toRFrame();
	}

	/**
	 * Informs the parent source that a change has occurred that should be passed up
	 * the chain to the Problem.
	 */
	@Override
	public final void markUnsaved()
	{
		if(parent != null)
			parent.markUnsaved();
	}

	@Override
	public final String getName()
	{
		return name;
	}

	@Override
	public final int getColumnCount() throws MarlaException
	{
		checkCache();
		return data.getColumnCount();
	}

	@Override
	public final Operation addOperation(Operation op)
	{
		// Tell the operation to set us as the parent
		op.setParentData(this);

		if(!solutionOps.contains(op))
		{
			// They weren't already assigned to us, so stick them on our list
			solutionOps.add(op);
			markUnsaved();
		}

		return op;
	}

	@Override
	public final Operation addOperationToEnd(Operation op) throws CalcException
	{
		if(solutionOps.isEmpty())
		{
			return addOperation(op);
		}
		else
		{
			return solutionOps.get(0).addOperationToEnd(op);
		}
	}

	@Override
	public final Operation removeOperation(Operation op)
	{
		// Tell operation to we're not its parent any more
		op.setParentData(null);

		// Remove them from our list if still needed
		if(solutionOps.remove(op))
		{
			markUnsaved();
		}

		return op;
	}

	@Override
	public final Operation removeOperation(int index)
	{
		return removeOperation(solutionOps.get(index));
	}

	@Override
	public final Operation getOperation(int index)
	{
		return solutionOps.get(index);
	}
}
