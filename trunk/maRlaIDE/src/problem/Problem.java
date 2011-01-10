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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Contains an entire stats problem, including the question itself,
 * associated data sets, and the operations (R commands) a user
 * chooses to solve a part of a problem with. The interface for the
 * data allows the GUI to extract the information.
 *
 * @author Ryan Morehart
 */
public class Problem implements ProblemPart
{
	/**
	 * Problem statement.
	 */
	private String statement = null;
	/**
	 * All datasets associated with this problem.
	 */
	private ArrayList<DataSet> datasets = new ArrayList<DataSet>();
	/**
	 * All subproblems associated with this problem.
	 */
	private ArrayList<SubProblem> subProblems = new ArrayList<SubProblem>();
	/**
	 * File this problem will be saved to.
	 */
	private String fileName = null;
	/**
	 * Whether this problem has been saved with save() since
	 * it was last updated in any way.
	 */
	private boolean isSaved = false;

	/**
	 * Creates a new problem with the problem statement unspecified.
	 */
	public Problem()
	{
		this("");
	}

	/**
	 * Creates a new problem with the given problem statement.
	 *
	 * @param statement Description for the problem, as it would appear in a
	 *				book. LaTeX may be included for formatting formulas but may
	 *				be stripped when not displayed in final PDF output.
	 */
	public Problem(String statement)
	{
		this.statement = statement;
	}

	/**
	 * Creates a duplicate of the supplied problem. Any changes to the new
	 * problem do _not_ reflect on the original.
	 * @param prob Problem to copy
	 */
	public Problem(Problem prob)
	{
		isSaved = false;
		statement = prob.statement;
		for(DataSet ds : prob.datasets)
		{
			datasets.add(new DataSet(ds, this));
		}
		for(SubProblem sp : prob.subProblems)
		{
			subProblems.add(new SubProblem(sp, this));
		}
	}

	@Override
	public String getStatement()
	{
		return statement;
	}

	@Override
	public void setStatement(String newStatement)
	{
		this.statement = newStatement;
	}

	@Override
	public DataSet addData(DataSet data)
	{
		// Don't add the same data again. Do an actual object
		// comparison, not .equals(). Hence why we don't use .contains.
		for(DataSet ds : datasets)
		{
			if(ds == data)
				return data;
		}

		// Remove from the old problem if needed
		ProblemPart oldParent = data.getParentProblem();
		if(oldParent != null)
			oldParent.removeData(data);

		markChanged();
		isSaved = false;
		data.setParentProblem(this);
		datasets.add(data);
		return data;
	}

	@Override
	public DataSet removeData(DataSet data)
	{
		return removeData(datasets.indexOf(data));
	}

	@Override
	public DataSet removeData(int index)
	{
		DataSet d = datasets.get(index);
		markChanged();
		d.setParentProblem(null);
		datasets.remove(index);
		return d;
	}

	@Override
	public DataSet getData(String name) throws DataNotFoundException
	{
		return getData(getDataIndex(name));
	}

	@Override
	public DataSet getData(int index)
	{
		return datasets.get(index);
	}

	@Override
	public int getDataIndex(String name) throws DataNotFoundException
	{
		for(int i = 0; i < datasets.size(); i++)
		{
			if(datasets.get(i).getName().equals(name))
				return i;
		}

		// Dataset not found, name was bad
		throw new DataNotFoundException("Failed to find dataset with name '" + name + "'");
	}

	@Override
	public int getDataCount()
	{
		return datasets.size();
	}

	/**
	 * Adds a new SubProblem to the problem.
	 * @param id Subproblem identifier. For example, "a," "part B," etc
	 * @param description Problem statement for new SubProblem
	 * @return Reference to newly added SubProblem.
	 */
	public SubProblem addSubProblem(String id, String description)
	{
		SubProblem sub = new SubProblem(this, id, description);
		return addSubProblem(sub);
	}

	/**
	 * Adds an existing SubProblem to this problem
	 * @param sub SubProblem to be added
	 * @return The newly added SubProblem
	 */
	private SubProblem addSubProblem(SubProblem sub)
	{
		markChanged();
		subProblems.add(sub);
		return sub;
	}

	/**
	 * Remove a given sub problem from this problem
	 * @param sub SubProblem object to remove
	 */
	public void removeSubProblem(SubProblem sub)
	{
		markChanged();
		subProblems.remove(sub);
	}

	/**
	 * Returns the SubProblem at the given index
	 * @param index Index of the SubProblem to return
	 * @return SubProblem at the given index
	 */
	public SubProblem getSubProblem(int index)
	{
		return subProblems.get(index);
	}

	/**
	 * Returns the number of SubProblems that are a part of this problem
	 * @return Number of parts in this Problem
	 */
	public int getSubProblemCount()
	{
		return subProblems.size();
	}

	/**
	 * Set/change the path this problem saves to.
	 * @param file New location to save problem to.
	 */
	public void setFileName(String file)
	{
		isSaved = false;
		fileName = file;
	}

	/**
	 * Gets the file this problem is/will be saved to.
	 *
	 * @return Path this file will save to when @see #save() save() is called.
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Indicates whether the problem is saved to disk. If this is true then
	 * a call to load(String) would return a Problem
	 * in the same state as the current one.
	 *
	 * @return true if there are no unsaved changes to the problem.
	 */
	public boolean isSaved()
	{
		return isSaved;
	}

	/**
	 * Indicates whether there has been a change since the problem was last
	 * saved with save().
	 *
	 * @return true if there are changes that need to be written out.
	 */
	@Override
	public boolean isChanged()
	{
		return !isSaved;
	}

	@Override
	public void markChanged()
	{
		isSaved = false;
	}

	/**
	 * Forces all DataSets to recompute their values the next time they are accessed
	 */
	public void markDirty()
	{
		// Tell all children to recompute themselves when they need to
		for(DataSet ds : datasets)
		{
			ds.markChanged();
		}
	}

	/**
	 * Attempts to save problem to file path given.
	 * @param fileName Where to attempt to save the problem.
	 * @throws FileSaveException Thrown if a file save fails in any way
	 */
	private void save(String fileName) throws FileException, IOException
	{
		if(fileName == null)
			throw new FileException("File name may not be null");

		// Build
		Document doc = new Document(this.toXml());

		// Output to file
		OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(fileName));
		BufferedWriter outputStream = new BufferedWriter(os);

		Format formatter = Format.getPrettyFormat();
		formatter.setEncoding(os.getEncoding());
		XMLOutputter xml = new XMLOutputter(formatter);
		xml.output(doc, outputStream);
	}

	/**
	 * Attempts to save problem to file specified by fileName
	 * @throws FileException Thrown if a file save fails in any way
	 * @throws IOException Unable to save file for some reason.
	 */
	public void save() throws FileException, IOException
	{
		save(fileName);
		isSaved = true;
	}

	/**
	 * Loads a problem from the file path given. Returned Problem object
	 * should essentially duplicate the state the problem was in when it
	 * was saved.
	 * @param fileName Path to save file
	 * @return Restored Problem object
	 * @throws FileNotFoundException The file requested to be loaded could not be found
	 * @throws IOException Unable to access and/or read the file to load
	 * @throws JDOMException The save file is likely corrupt, we were unable to parse it
	 * @throws CalcException Unable to compute values after the tree has been built
	 */
	public static Problem load(String fileName) throws FileNotFoundException, IOException, JDOMException, MarlaException
	{
		// Load file into JDOM
		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(fileName);

		// Make problem
		Problem newProb = Problem.fromXml(doc.getRootElement());
		newProb.markChanged();
		newProb.setFileName(fileName);
		newProb.isSaved = true;

		return newProb;
	}

	/**
	 * A Problem is equal if all DataSets and the problem statements
	 * match
	 * @param other Object to compare against
	 * @return True if the the given object is the same as this one
	 */
	@Override
	public boolean equals(Object other)
	{
		// Ourself?
		if(other == this)
			return true;

		// Actually a problem?
		if(!(other instanceof Problem))
			return false;

		Problem otherP = (Problem) other;
		if(!statement.equals(otherP.statement))
			return false;
		if(!datasets.equals(otherP.datasets))
			return false;
		if(!subProblems.equals(otherP.subProblems))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 29 * hash + (this.statement != null ? this.statement.hashCode() : 0);
		hash = 29 * hash + (this.datasets != null ? this.datasets.hashCode() : 0);
		hash = 29 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
		return hash;
	}

	@Override
	@Deprecated
	public DataSource getAnswer(int index) throws IncompleteInitializationException
	{
		if(datasets.isEmpty())
			throw new IncompleteInitializationException("This problem has no datasets yet, unable to solve");

		// Find the bottom operation. Assume the first one for any
		// places where we find multiple ops or whatever
		DataSource curr = datasets.get(index);
		while(curr.getOperationCount() != 0)
		{
			curr = curr.getOperation(0);
		}

		return curr;
	}

	@Override
	public Element toXml()
	{
		Element rootEl = new Element("problem");
		rootEl.addContent(new Element("statement").addContent(statement));

		// Add each DataSet
		for(DataSet data : datasets)
		{
			rootEl.addContent(data.toXml());
		}

		// Add each subproblem
		for(SubProblem sub : subProblems)
		{
			rootEl.addContent(sub.toXml());
		}

		return rootEl;
	}

	/**
	 * Creates a new problem based on the data in the given XML tree
	 * @param rootEl JDOM Tree to load problem from
	 * @return Newly created problem from the given XML
	 * @throws CalcException Unable to compute values
	 */
	public static Problem fromXml(Element rootEl) throws MarlaException
	{
		Problem newProb = new Problem();

		newProb.setStatement(rootEl.getChild("statement").getText());

		for(Object dataEl : rootEl.getChildren("data"))
		{
			newProb.addData(DataSet.fromXml((Element) dataEl));
		}

		for(Object partEl : rootEl.getChildren("part"))
		{
			newProb.addSubProblem(SubProblem.fromXml((Element) partEl, newProb));
		}

		// Make sure we're all dirty and recompute when needed
		newProb.markDirty();

		return newProb;
	}
}
