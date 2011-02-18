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

import operation.Operation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.jdom.Element;

/**
 * Stores descriptions for subproblems and points to the head(s)
 * of the chains of operations which.
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
	private DataSource startSolutionStep;
	/**
	 * End operation for the chain of ops which solves this
	 * subproblem. Actual solution steps are found by tracing
	 * _up_ from this operation to the first.
	 */
	private DataSource endSolutionStep;
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
		id = sp.id;
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
		if(parent != null)
			parent.markChanged();
	}

	@Override
	public boolean isChanged()
	{
		if(parent != null)
			return parent.isChanged();
		else
			return true;
	}

	/**
	 * Mark the operation/dataset that begins the solution to this
	 * part of the problem. Operations between here and the operation
	 * marked as the end are considered part of the solution.
	 * @param op Beginning of solution chain
	 */
	public void setSolutionStart(DataSource op)
	{
		startSolutionStep = op;
		markChanged();
	}

	/**
	 * Returns the DataSet/Operation currently marked as the "start" of
	 * our solution to this part of the problem.
	 * @return DataSet pointed to as the start or null if none is set
	 */
	public DataSource getSolutionStart()
	{
		return startSolutionStep;
	}

	/**
	 * Mark the operation/dataset that ends the solution to this
	 * part of the problem. Operations between here and the operation
	 * marked as the beginning are considered part of the solution.
	 * @param op End of solution chain
	 */
	public void setSolutionEnd(DataSource op)
	{
		endSolutionStep = op;
		markChanged();
	}

	/**
	 * Returns the DataSet/Operation currently marked as the "end" of
	 * our solution to this part of the problem.
	 * @return DataSet pointed to as the end or null if none is set
	 */
	public DataSource getSolutionEnd()
	{
		return endSolutionStep;
	}

	/**
	 * Determines if the current subproblem has a solution denoted or not
	 * @return true if a complete solution is marked, false otherwise
	 */
	public boolean hasSolution()
	{
		if(endSolutionStep != null && startSolutionStep != null)
			return true;
		else
			return false;
	}

	/**
	 * Gets the chain of operations that are the solution to this subproblem.
	 * DataSets are never part of this--even if they are marked as the start
	 * of a solution--because export of R code doesn't want to work with those.
	 * @return Chain of operations, from start to finish, that solve this subproblem
	 */
	public List<Operation> getSolutionChain() throws MarlaException
	{
		if(!hasSolution())
			throw new IncompleteInitializationException("An solution for this subproblem has not been set");

		// Push all the operations unto here in reverse order
		// (from the bottom of the chain to the top)
		Deque<DataSource> stack = new ArrayDeque<DataSource>();

		// If we are pointed at just a dataset as the start and end, then
		// get the R for _everything_ underneath it
		if(startSolutionStep == endSolutionStep)
		{
			// Get all operations, we'll skip ones that aren't leaves
			List<Operation> allOps = startSolutionStep.getAllChildOperations();

			for(Operation op : allOps)
			{
				// Make sure it's a leaf
				if(op.getOperationCount() != 0)
					continue;

				DataSource currOp = op;
				while(currOp != startSolutionStep)
				{
					stack.push(currOp);
					currOp = ((Operation)currOp).getParentData();
				}
			}

			// And add the start step
			stack.push(startSolutionStep);
		}
		else	
		{
			// Find all the ops in order from the bottom of our chain to the top
			DataSource currOp = endSolutionStep;
			try
			{
				while(currOp != startSolutionStep)
				{
					stack.push(currOp);
					currOp = ((Operation)currOp).getParentData();
				}

				// And add the start step
				stack.push(currOp);
			}
			catch(ClassCastException ex)
			{
				throw new ProblemException("The start and end of the subproblem solution appear to not be connected", ex);
			}
		}
		
		// Put them in the list in the opposite order we found them, so
		// the list runs from the top (start) operation to the bottom (end)
		// Only push on operations, ignore the datasets
		List<Operation> ops = new ArrayList<Operation>(stack.size());
		while(!stack.isEmpty())
		{
			DataSource next = stack.pop();
			if(next instanceof Operation)
				ops.add((Operation)next);
		}

		return ops;
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

		if(startSolutionStep != null)
		{
			if(!startSolutionStep.equals(otherP.startSolutionStep))
				return false;
		}
		else
		{
			 if(otherP.startSolutionStep != null)
				 return false;
		}

		if(endSolutionStep != null)
		{
			if(!endSolutionStep.equals(otherP.endSolutionStep))
				return false;
		}
		else
		{
			 if(otherP.endSolutionStep != null)
				 return false;
		}

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
	public Element toXml() throws MarlaException
	{
		Element subEl = new Element("part");
		subEl.setAttribute("id", id);
		if(startSolutionStep != null)
			subEl.setAttribute("start", Integer.toString(startSolutionStep.hashCode()));
		else
			subEl.setAttribute("start", "");
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
										   subEl.getChildText("statement"));

		// Now find our start and end Operation objects so we can point
		// to them again
		String startIDStr = subEl.getAttribute("start").getValue();
		String endIDStr = subEl.getAttribute("end").getValue();

		int startID = 0;
		if(!startIDStr.isEmpty())
			startID = Integer.parseInt(startIDStr);

		int endID = 0;
		if(!endIDStr.isEmpty())
			endID = Integer.parseInt(endIDStr);

		// Look for match in DataSets
		for(int i = 0; i < parent.getDataCount(); i++)
		{
			int hash = parent.getData(i).hashCode();
			if(!startIDStr.isEmpty() && newSub.getSolutionStart() == null && hash == startID)
				newSub.setSolutionStart(parent.getData(i));
			if(!endIDStr.isEmpty() && newSub.getSolutionEnd() == null && hash == endID)
				newSub.setSolutionEnd(parent.getData(i));
		}

		// Look for it in operations
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
	 * recursively to find it throughout the supplied DataSet. Used internally
	 * for reattaching loaded problems
	 * @param hash hashcode of DataSet/derivative to find
	 * @param parent DataSet that we should search through to find it
	 * @return DataSet located
	 */
	private static DataSource findDataSet(int hash, DataSource parent)
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
				DataSource d = findDataSet(hash, op);
				if(d != null)
					return d;
			}

			// Sigh. Next one
		}

		return null; // Not found
	}

	@Override
	public DataSet addData(DataSet data)
	{
		return parent.addData(data);
	}

	@Override
	public DataSet removeData(DataSet data)
	{
		return parent.removeData(data);
	}

	@Override
	public DataSet removeData(int index)
	{
		return parent.removeData(index);
	}

	@Override
	public DataSet getData(String name) throws DataNotFoundException
	{
		return parent.getData(name);
	}

	@Override
	public DataSet getData(int index)
	{
		return parent.getData(index);
	}

	@Override
	public int getDataIndex(String name) throws DataNotFoundException
	{
		return parent.getDataIndex(name);
	}

	@Override
	public int getDataCount()
	{
		return parent.getDataCount();
	}

	/**
	 * Checks if the given DataSource is somewhere in the chain of data
	 * that is marked as the solution to this problem. Both start and end
	 * must be set on the solution or false is returned.
	 * @param ds DataSource to locate
	 * @return true if the DataSource is in the the solution, false otherwise
	 */
	public boolean isDataSourceInSolution(DataSource ds)
	{
		if(!hasSolution())
			return false;

		if(startSolutionStep == endSolutionStep)
		{
			// Everything underneath the start is part of the solution
			if(ds instanceof DataSet && ds != startSolutionStep)
				return false;
			else if(ds == startSolutionStep) // We are the start
				return true;
			else if(startSolutionStep.getAllChildOperations().contains((Operation)ds)) // We're below the start
				return true;
			else
				return false;
		}
		else
		{
			DataSource curr = endSolutionStep;
			while(curr != startSolutionStep)
			{
				if(curr == ds)
					return true;

				// This cast should never fail because if it's actually a DataSet then the while
				// loop will have hit the beginning
				curr = ((Operation)curr).getParentData();
			}

			// And make sure it wasn't the starting step
			if(curr == ds)
				return true;
			else
				return false;
		}
	}
}
