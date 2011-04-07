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

import java.awt.Color;
import java.util.ArrayDeque;
import marla.ide.operation.Operation;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import org.jdom.Element;

/**
 * Stores descriptions for subproblems and points to the head(s)
 * of the chains of operations which.
 *
 * @author Ryan Morehart
 */
public class SubProblem implements ProblemPart, Comparable<SubProblem>
{
	/**
	 * Denotes if we are in the middle of a load from XML
	 */
	private boolean isLoading = false;
	/**
	 * Description for this sub part in the problem
	 */
	private String partDesc;
	/**
	 * "Conclusion" of the subproblem. IE, the final conclusion for the analysis
	 * marked in the solution.
	 */
	private String conclusion = "";
	/**
	 * Identifier for this part of the problem. For example, "part A,"
	 * B, C, etc
	 */
	private String name;
	/**
	 * List of DataSources that comprise the "solution" to this SubProblem
	 */
	private final List<DataSource> solutionSteps = new ArrayList<DataSource>();
	/**
	 * Problem we belong to
	 */
	private final ProblemPart parent;
	/**
	 * Saves a color that represents the subproblem. Used by the GUI for
	 * rendering
	 */
	private Color highlightColor = Color.BLACK;

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
		this.name = id;

		// Random color
		Random r = new Random();
		highlightColor = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
	}
	
	/**
	 * Copy constructor, copies all attributes of original and attaches 
	 * to steps in given parent
	 * @param parent Problem to look for data to reattach to
	 * @param org Original subproblems to copy
	 */
	public SubProblem(Problem parent, SubProblem org)
	{
		isLoading = true;
		
		// Easy stuff
		this.parent = parent;
		conclusion = org.conclusion;
		name = org.name;
		highlightColor = org.highlightColor;
		partDesc = org.partDesc;
		
		// Attach to steps
 		List<DataSource> allData = parent.getAllData();
		for(DataSource searchDS : org.solutionSteps)
		{
			// Get the ID of the original's step
			Integer searchID = searchDS.getUniqueID();

			// Find it in our new parent
			for(DataSource ds : allData)
			{
				if(searchID.equals(ds.getUniqueID()))
				{
					addStep(ds);
					break;
				}
			}
		}
		
		isLoading = false;
	}

	/**
	 * Sets the "highlight" color for the SubProblems and returns the previously
	 * set color
	 * @param newColor New color to set for problem
	 * @return Previously set color (black by default)
	 */
	public Color setColor(Color newColor)
	{
		changeBeginning("subproblem " + name + " color");
		
		Color oldColor = highlightColor;
		highlightColor = newColor;

		markUnsaved();

		return oldColor;
	}

	/**
	 * Returns the currently set highlight color 
	 * @return Current highlight color, black if not changed otherwise
	 */
	public Color getColor()
	{
		return highlightColor;
	}

	@Override
	public String getStatement()
	{
		return partDesc;
	}

	@Override
	public void setStatement(String newStatement)
	{
		changeBeginning("subproblem " + name + " statement");
		
		partDesc = newStatement;
		
		markUnsaved();
	}

	@Override
	public String getConclusion()
	{
		return conclusion;
	}

	@Override
	public String setConclusion(String newConclusion)
	{
		changeBeginning("subproblem " + name + " conclusion");
		
		String oldConc = conclusion;
		conclusion = newConclusion;

		markUnsaved();

		return oldConc;
	}

	@Override
	public void markUnsaved()
	{
		if(parent != null)
			parent.markUnsaved();
	}

	@Override
	public void changeBeginning(String changeMsg)
	{
		if(parent != null)
			parent.changeBeginning(changeMsg);
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
	 * Adds the given DataSource as part of the solution to this SubProblem
	 * @param ds DataSource to add
	 */
	public final void addStep(DataSource ds)
	{
		// Don't add again
		if(solutionSteps.contains(ds))
			return;
		
		int placement = 0;
		
		// Try to place it in line with anyone it is connected to
		for(int i = 0; i < solutionSteps.size(); i++)
		{
			DataSource solDS = solutionSteps.get(i);
			
			if(solDS == ds.getParentData())
			{
				// Place after solDS
				placement = i + 1;
				break;
			}
			else if(solDS.getParentData() == ds)
			{
				// Place before solDS
				placement = i;
				break;
			}
		}

		// And add
		solutionSteps.add(placement, ds);
		ds.addSubProblem(this);
		markUnsaved();
	}


	/**
	 * Adds the given step and all children of the DataSource from the solution to
	 * this SubProblem
	 * @param ds Step to start add at
	 */
	public void addAllSubSteps(DataSource ds)
	{
		List<Operation> children = ds.getAllChildOperations();

		addStep(ds);
		for(Operation op : children)
			addStep(op);
	}

	/**
	 * Removes the DataSource at the given index from the solution
	 * @param i Index of the step to remove
	 * @return Removed DataSource
	 */
	public DataSource removeStep(int i)
	{
		DataSource old = solutionSteps.remove(i);
		markUnsaved();
		old.removeSubProblem(this);
		return old;
	}

	/**
	 * Removes the given DataSource from the solution
	 * @param ds DataSource to remove
	 * @return Removed DataSource, null if the data was not in solution
	 */
	public DataSource removeStep(DataSource ds)
	{
		int loc = solutionSteps.indexOf(ds);
		if(loc != -1)
			return removeStep(loc);
		else
			return null;
	}

	/**
	 * Removes the given step and all children of the step from the solution to
	 * this SubProblem
	 * @param ds Step to start removal at
	 */
	public void removeAllSubSteps(DataSource ds)
	{
		List<Operation> children = ds.getAllChildOperations();

		removeStep(ds);
		for(Operation op : children)
			removeStep(op);
	}

	/**
	 * Returns the solution step at the given index
	 * @param i Index of the solution step to retrieve
	 * @return DataSource at the given index
	 */
	public DataSource getStep(int i)
	{
		return solutionSteps.get(i);
	}

	/**
	 * Locates the given DataSource in the solution and returns the index
	 * @param ds DataSource to locate
	 * @return Returns the index of the DataSet or -1 if it is not in the solution
	 */
	public int getStepIndex(DataSource ds)
	{
		return solutionSteps.indexOf(ds);
	}

	/**
	 * Gets the number of solution steps in this SubProblem
	 * @return Number of solution steps. 0 if there are noneg
	 */
	public int getStepCount()
	{
		return solutionSteps.size();
	}

	/**
	 * Determines if the current subproblem has a solution denoted or not
	 * @return true if a complete solution is marked, false otherwise
	 */
	public boolean hasSolution()
	{
		return !solutionSteps.isEmpty();
	}

	/**
	 * Gets the chain of operations that are the solution to this subproblem.
	 * DataSets are never part of this--even if they are marked as the start
	 * of a solution--because export of R code doesn't want to work with those.
	 * @return Chain of operations, from start to finish, that solve this subproblem
	 */
	public List<Operation> getSolutionChain()
	{
		List<Operation> solOps = new ArrayList<Operation>(solutionSteps.size());
		for(DataSource ds : solutionSteps)
		{
			if(ds instanceof Operation)
				solOps.add((Operation)ds);
		}

		return solOps;
	}

	/**
	 * Gets a list of all steps in this solution that have no parent that is
	 * also in the solution. IE, the starts of sequences of operations
	 * @return All starting data for this SubProblem
	 */
	public List<DataSource> getStartSteps()
	{
		List<DataSource> starts = new ArrayList<DataSource>(solutionSteps.size());

		for(int i = 0; i < solutionSteps.size(); i++)
		{
			DataSource currStep = solutionSteps.get(i);

			// If the step before isn't the current step's parent, then
			// include the current step as the start of a chain
			if(i == 0 || currStep.getParentData() != solutionSteps.get(i - 1))
				starts.add(currStep);
		}

		return starts;
	}

	/**
	 * Gets a list of all steps in this solution that have no child in the
	 * solution. IE, the ends of sequences of operations
	 * @return All ending data for this SubProblem
	 */
	public List<DataSource> getEndSteps()
	{
		List<DataSource> ends = new ArrayList<DataSource>(solutionSteps.size());
		int endIndex = solutionSteps.size() - 1;

		for(int i = 0; i < solutionSteps.size(); i++)
		{
			DataSource currStep = solutionSteps.get(i);

			// If the step after this isn't a child of the current step, then
			// include the current step as the end of a chain
			if(i == endIndex || currStep != solutionSteps.get(i + 1).getParentData())
				ends.add(currStep);
		}

		return ends;
	}

	/**
	 * Gets the chain of operations that run from the leaf operations of
	 * chainTop to the top, in order from first execution to last. IE, if the
	 * R for this were to be executed, the List returned here could be executed
	 * in order. DataSets are never part of this.
	 * @param chainTop DataSource to start chain from
	 * @return Chain of operations, from start to finish
	 */
	public static List<Operation> getOperationChain(DataSource chainTop)
	{
		Deque<Operation> chainRev = new ArrayDeque<Operation>();

		List<Operation> leaves = chainTop.getAllLeafOperations();
		for(Operation leaf : leaves)
		{
			// Run up chain until we find top
			DataSource currDS = leaf;
			while(currDS != chainTop)
			{
				chainRev.push((Operation) currDS);
				currDS = currDS.getParentData();
			}
		}

		// Now reverse the queue
		List<Operation> chain = new ArrayList<Operation>(chainRev.size());
		while(!chainRev.isEmpty())
			chain.add(chainRev.pop());
		
		return chain;
	}

	/**
	 * Returns the subproblem identifier, often the one-letter designation
	 * from the book. IE, "a"
	 * @return Subproblem id
	 */
	public String getSubproblemID()
	{
		return name;
	}

	/**
	 * Sets the subproblem identifier 
	 * @param newID Part ID (IE, "A")
	 */
	public void setSubproblemID(String newID)
	{
		name = newID;
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
		if(!this.name.equals(otherP.name))
			return false;

		if(!this.solutionSteps.equals(otherP.solutionSteps))
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 29 * hash + (this.partDesc != null ? this.partDesc.hashCode() : 0);
		hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 29 * hash + (this.solutionSteps != null ? this.solutionSteps.hashCode() : 0);
		return hash;
	}

	@Override
	public Element toXml()
	{
		Element subEl = new Element("part");
		subEl.setAttribute("id", name);

		subEl.setAttribute("color", String.valueOf(highlightColor.getRGB()));

		// Store pointers to all the DataSources that are part of us
		for(DataSource ds : solutionSteps)
		{
			Element stepEl = new Element("step");
			stepEl.setAttribute("id", ds.getUniqueID().toString());
			subEl.addContent(stepEl);
		}
		
		subEl.addContent(new Element("statement").addContent(partDesc));
		subEl.addContent(new Element("conclusion").addContent(conclusion));

		return subEl;
	}

	/**
	 * Creates a new SubProblem based on the data in the given JDOM element
	 * @param subEl JDOM with information for this SubProblem
	 * @param prob Parent Problem that this subproblem belongs to
	 * @return New SubProblem based on given data
	 */
	public static SubProblem fromXml(Element subEl, Problem prob)
	{
		SubProblem newSub = new SubProblem(prob,
										   subEl.getAttributeValue("id"),
										   subEl.getChildText("statement"));
		newSub.isLoading = true;
		newSub.setConclusion(subEl.getChildText("conclusion"));

		Color c = new Color(Integer.parseInt(subEl.getAttributeValue("color")));
		newSub.setColor(c);

		// Hook up to steps
		List<DataSource> allData = prob.getAllData();
		for(Object stepObj : subEl.getChildren("step"))
		{
			Element stepEl = (Element)stepObj;
			String idStr = stepEl.getAttributeValue("id");
			Integer searchID = Integer.valueOf(idStr);

			for(DataSource ds : allData)
			{
				if(searchID.equals(ds.getUniqueID()))
				{
					newSub.addStep(ds);
					break;
				}
			}
		}
		
		newSub.isLoading = false;
		return newSub;
	}

	@Override
	public boolean isLoading()
	{
		if(isLoading)
			return true;
		else if(parent != null)
			return parent.isLoading();
		else
			return false;
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
	public DataSet getData(String name)
	{
		return parent.getData(name);
	}

	@Override
	public DataSet getData(int index)
	{
		return parent.getData(index);
	}

	@Override
	public int getDataIndex(String name)
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
		return solutionSteps.contains(ds);
	}

	/**
	 * Sorts SubProblems solely on their part ID, according to ASCII ordering
	 * @param other Other SubProblem to compare against
	 * @return -1 if less, 0 if equal, 1 if greater
	 */
	@Override
	public int compareTo(SubProblem other)
	{
		return name.compareTo(other.name);
	}
}
