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

import org.jdom.Element;

/**
 * Stores descriptions for subproblems and points to the head(s)
 * of the chains of operations which
 *
 * @author Ryan Morehart
 */
public class SubProblem implements ProblemPart
{

	/**
	 * Description for this sub part in the problem
	 */
	private String partDesc;
	/**
	 * Identifier for this part of the problem. For example, "part A,"
	 * B, C, etc
	 */
	private String id;
	/**
	 * Beginning operation for the chain of ops which solves this
	 * subproblem.  Actual solution steps are found by tracing
	 * _up_ from last operation to this one.
	 */
	private DataSet startSolutionStep;
	/**
	 * End operation for the chain of ops which solves this
	 * subproblem. Actual solution steps are found by tracing
	 * _up_ from this operation to the first.
	 */
	private DataSet endSolutionStep;
	/**
	 * Problem we belong to
	 */
	private final ProblemPart parent;

	/**
	 * Initializes the subproblem with a description of the question it asks
	 * @param parent Parent problem that we are a part of
	 * @param id "id" of the subproblem. IE, "A"
	 * @param desc Description of question
	 */
	public SubProblem(Problem parent, String id, String desc)
	{
		this.parent = parent;
		partDesc = desc;
		this.id = id;
		startSolutionStep = null;
		endSolutionStep = null;
	}

	/**
	 * Copy constructor, creates a deep copy of the subproblem. CURRENTLY
	 * NOT PERFECT! TBD: The start and end solution steps would need to be
	 * updated to match the copied versions of themselves in our parent
	 * problem
	 * @param sp SubProblem to copy
	 * @param parent Problem to set as the parent of the copy
	 */
	public SubProblem(SubProblem sp, Problem parent)
	{
		partDesc = sp.partDesc;
		startSolutionStep = sp.startSolutionStep;
		endSolutionStep = sp.endSolutionStep;
		this.parent = parent;
	}

	@Override
	public String getStatement()
	{
		return partDesc;
	}

	@Override
	public void setStatement(String newStatement)
	{
		partDesc = newStatement;
	}

	@Override
	public void markChanged()
	{
		parent.markChanged();
	}

	@Override
	public boolean isChanged()
	{
		return parent.isChanged();
	}

	/**
	 * Mark the operation/dataset that begins the solution to this
	 * part of the problem. Operations between here and the operation
	 * marked as the end are considered part of the solution.
	 * @param op Beginning of solution chain
	 */
	public void setSolutionStart(DataSet op)
	{
		startSolutionStep = op;
		markChanged();
	}

	/**
	 * Returns the DataSet/Operation currently marked as the "start" of
	 * our solution to this part of the problem.
	 * @return DataSet pointed to as the start or null if none is set
	 */
	public DataSet getSolutionStart()
	{
		return startSolutionStep;
	}

	/**
	 * Mark the operation/dataset that ends the solution to this
	 * part of the problem. Operations between here and the operation
	 * marked as the beginning are considered part of the solution.
	 * @param op End of solution chain
	 */
	public void setSolutionEnd(DataSet op)
	{
		endSolutionStep = op;
		markChanged();
	}

	/**
	 * Returns the DataSet/Operation currently marked as the "end" of
	 * our solution to this part of the problem.
	 * @return DataSet pointed to as the end or null if none is set
	 */
	public DataSet getSolutionEnd()
	{
		return endSolutionStep;
	}

	@Override
	public DataSet getSolutionSteps() throws IncompleteInitialization
	{
		if(endSolutionStep == null)
			throw new IncompleteInitialization();

		return new DataSet("Solution");
	}

	/**
	 * Returns the subproblem identifier, often the one-letter designation
	 * from the book. IE, "a"
	 * @return Subproblem id
	 */
	public String getSubproblemID()
	{
		return id;
	}
	
	/**
	 * Sets the subproblem identifier 
	 * @param newID Part ID (IE, "A")
	 */
	public void setSubproblemID(String newID)
	{
		id = newID;
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
		if(!(other instanceof SubProblem))
			return false;

		SubProblem otherP = (SubProblem) other;
		if(!this.partDesc.equals(otherP.partDesc))
			return false;
		if(!this.id.equals(otherP.id))
			return false;
		if(!endSolutionStep.equals(otherP.endSolutionStep))
			return false;
		if(!startSolutionStep.equals(otherP.startSolutionStep))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 29 * hash + (this.partDesc != null ? this.partDesc.hashCode() : 0);
		hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
		hash = 29 * hash + (this.startSolutionStep != null ? this.startSolutionStep.hashCode() : 0);
		hash = 29 * hash + (this.endSolutionStep != null ? this.endSolutionStep.hashCode() : 0);
		return hash;
	}

	@Override
	public DataSet getAnswer() throws IncompleteInitialization, CalcException
	{
		if(endSolutionStep == null)
			throw new IncompleteInitialization();

		return endSolutionStep.getAllColumns();
	}

	@Override
	public Element toXml()
	{
		Element subEl = new Element("part");
		subEl.setAttribute("id", id);
		if(startSolutionStep != null)
			subEl.setAttribute("start",	 Integer.toString(startSolutionStep.hashCode()));
		else
			subEl.setAttribute("start",	 "");
		if(endSolutionStep != null)
			subEl.setAttribute("end", Integer.toString(endSolutionStep.hashCode()));
		else
			subEl.setAttribute("end", "");
		subEl.addContent(new Element("statement").addContent(partDesc));

		return subEl;
	}

	/**
	 * Creates a new SubProblem based on the data in the given JDOM element
	 * @param subEl JDOM with information for this SubProblem
	 * @param parent Parent Problem that this subproblem belongs to
	 * @return New SubProblem based on given data
	 */
	public static SubProblem fromXml(Element subEl, Problem parent)
	{
		SubProblem newSub = new SubProblem(parent,
				subEl.getAttributeValue("id"),
				subEl.getText());

		// Now find our start and end Operation objects so we can point
		// to them again
		String startIDStr = subEl.getAttribute("start").toString();
		String endIDStr = subEl.getAttribute("end").toString();

		int startID = 0;
		if(!startIDStr.isEmpty())
			startID = Integer.parseInt(startIDStr);

		int endID = 0;
		if(!endIDStr.isEmpty())
			endID = Integer.parseInt(endIDStr);

		for(int i = 0; i < parent.getDataCount(); i++)
		{
			if(!startIDStr.isEmpty() && newSub.getSolutionStart() == null)
				newSub.setSolutionStart(findDataSet(startID, parent.getData(i)));
			if(!endIDStr.isEmpty() && newSub.getSolutionEnd() == null)
				newSub.setSolutionEnd(findDataSet(endID, parent.getData(i)));
		}

		return newSub;
	}

	/**
	 * Find the DataSet/Operation with the given hashcode() value. Works
	 * recursively to find it throughout the supplied DataSet
	 * @param hash hashcode of DataSet/derivative to find
	 * @param parent DataSet that we should search through to find it
	 * @return DataSet located
	 */
	private static DataSet findDataSet(int hash, DataSet parent)
	{
		for(int i = 0; i < parent.getOperationCount(); i++)
		{
			Operation op = parent.getOperation(i);
			if(op.hashCode() == hash)
			{
				// Found it
				return op;
			}
			else
			{
				// Can we find it deeper? If not just try the next one I guess
				DataSet d = findDataSet(hash, op);
				if(d != null)
					return d;
			}

			// Sigh. Next one
		}

		return null; // Not found
	}
}
