/*
 * The maRla Project - Graphical problem solver for statistical calculations.
 * Copyright © 2011 Cedarville University
 * http://marla.googlecode.com
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

import marla.ide.gui.Domain;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import marla.ide.operation.Operation;
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
public final class Problem implements ProblemPart, Cloneable
{
	/**
	 * Domain that this Problem is working with
	 */
	private static Domain domain = null;
	/**
	 * New problems will be created with this person's name
	 * set as the default
	 */
	private static String defaultPersonName = "";
	/**
	 * New problems will be created with this as the long course name,
	 * unless otherwise changed
	 */
	private static String defaultCourseLong = "";
	/**
	 * New problems will be created with this as the short course name,
	 * unless otherwise changed
	 */
	private static String defaultCourseShort = "";
	/**
	 * Keeps track of whether this Problem is in the process of loading.
	 */
	private boolean isLoading = false;
	/**
	 * Problem statement.
	 */
	private String statement = null;
	/**
	 * "Conclusion" of the problem. IE, the final conclusion for the analysis
	 */
	private String conclusion = "";
	/**
	 * All datasets associated with this problem.
	 */
	private final List<DataSet> datasets = new ArrayList<DataSet>();
	/**
	 * Operations associated with this problem that are not actually attached to
	 * a dataset
	 */
	private final List<Operation> unusedOperations = new ArrayList<Operation>();
	/**
	 * All subproblems associated with this problem.
	 */
	private final List<SubProblem> subProblems = new ArrayList<SubProblem>();
	/**
	 * Name of the person who is working on this problem. Arbitrary, used for
	 * export and that's about it
	 */
	private String personName = "";
	/**
	 * Short name for the course this problem is for. For example, "MA2510"
	 */
	private String shortCourseName = "";
	/**
	 * Long name for the course this problem is for. For example, "Probability and Statistics"
	 */
	private String longCourseName = "";
	/**
	 * Chapter. Arbitrary
	 */
	private String probChapter = "";
	/**
	 * Section. Arbitrary
	 */
	private String probSection = "";
	/**
	 * Problem number. Arbitrary
	 */
	private String probNum = "";
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
	 * Creates a new problem with the given problem statement and the defaults set for
	 * the short and long course names and the person name.
	 *
	 * @param statement Description for the problem, as it would appear in a
	 *				book. LaTeX may be included for formatting formulas but may
	 *				be stripped when not displayed in final PDF output.
	 */
	public Problem(String statement)
	{
		this.statement = statement;
		shortCourseName = defaultCourseShort;
		longCourseName = defaultCourseLong;
		personName = defaultPersonName;
	}
	
	/**
	 * Copy constructor for problems
	 * @param org Problem to copy
	 */
	public Problem(Problem org)
	{
		isLoading = true;
		
		// Get all the easy stuff
		statement = org.statement;
		conclusion = org.conclusion;
		personName = org.personName;
		shortCourseName = org.shortCourseName;
		longCourseName = org.longCourseName;
		probChapter = org.probChapter;
		probSection = org.probSection;
		probNum = org.probNum;
		fileName = org.fileName;
		
		// Copy lists of things
		for(DataSet orgDS : org.datasets)
			addData(new DataSet(orgDS));
		
		for(Operation orgOp : org.unusedOperations)
			addUnusedOperation(orgOp.clone());
		
		for(SubProblem orgSub : org.subProblems)
			addSubProblem(new SubProblem(this, orgSub));
		
		// Restore saved setting now, as adding the stuff above probably marked us
		// unsaved no matter what
		isSaved = org.isSaved;
		
		isLoading = false;
	}
 
	/**
	 * Sets all Problems to work with a new Domain
	 * @return Previous Domain. Null if there was none
	 */
	public static Domain setDomain(Domain newDomain)
	{
		Domain oldDomain = domain;
		domain = newDomain;
		return oldDomain;
	}

	/**
	 * Returns the current Domain Problems are associated with
	 * @return Currently active Domain. Null if there is none
	 */
	public static Domain getDomain()
	{
		return domain;
	}

	@Override
	public String getConclusion()
	{
		return conclusion;
	}

	@Override
	public String setConclusion(String newConclusion)
	{
		String oldConc = conclusion;
		conclusion = newConclusion;
		return oldConc;
	}

	/**
	 * Sets the person's name that new Problems will be created with, unless otherwise specified
	 * @return Previous person name. Blank if none
	 */
	public static String setDefaultPersonName(String newName)
	{
		String oldName = defaultPersonName;
		defaultPersonName = newName;
		return oldName;
	}

	/**
	 * Gets the person name that new Problems will be created with
	 * @return Current person name. Blank if none
	 */
	public static String getDefaultPersonName()
	{
		return defaultPersonName;
	}

	/**
	 * Sets the short course name that new Problems will be created
	 * with, unless otherwise specified
	 * @return Current course name. Blank if none
	 */
	public static String setDefaultShortCourseName(String newName)
	{
		String oldName = defaultCourseShort;
		defaultCourseShort = newName;
		return oldName;
	}

	/**
	 * Gets the short course name that new Problems will be created with
	 * @return Current course name. Blank if none
	 */
	public static String getDefaultShortCourseName()
	{
		return defaultCourseShort;
	}

	/**
	 * Sets the long course name that new Problems will be created
	 * with, unless otherwise specified
	 * @return Current course name. Blank if none
	 */
	public static String setDefaultLongCourseName(String newName)
	{
		String oldName = defaultCourseLong;
		defaultCourseLong = newName;
		return oldName;
	}

	/**
	 * Gets the long course name that new Problems will be created with
	 * @return Current course name. Blank if none
	 */
	public static String getDefaultLongCourseName()
	{
		return defaultCourseLong;
	}

	@Override
	public String getStatement()
	{
		return statement;
	}

	@Override
	public void setStatement(String newStatement)
	{
		changeBeginning("problem statement");
		this.statement = newStatement;
		markUnsaved();
	}

	/**
	 * Returns the current name for the user creating this Problem
	 * @return Name of person
	 */
	public String getPersonName()
	{
		return personName;
	}

	/**
	 * Sets a new name for the person creating this Problem. (IE, the user/student)
	 * @param newName New name to refer to the student by
	 * @return Previously set name
	 */
	public String setPersonName(String newName)
	{
		changeBeginning("person name");
		String oldName = personName;
		personName = newName;
		markUnsaved();
		return oldName;
	}

	/**
	 * Gets the current short name of the course
	 * @return String of the short name
	 */
	public String getShortCourse()
	{
		return shortCourseName;
	}

	/**
	 * Sets the name of the course this problem comes from, in short form.
	 * Typically the course number or an abbreviation, such as "MA2510"
	 * @param newCourse New short name of the course
	 * @return Previously set short name of the course
	 */
	public String setShortCourse(String newCourse)
	{
		changeBeginning("short course name");
		String oldCourse = shortCourseName;
		shortCourseName = newCourse;
		markUnsaved();
		return oldCourse;
	}

	/**
	 * Current long name of the course
	 * @return String with the full name of the course
	 */
	public String getLongCourse()
	{
		return longCourseName;
	}

	/**
	 * Sets the name of the course this problem comes from, in long form.
	 * Typically the full name of the class, such as "Probability and Statistics I"
	 * @param newCourse New long name of the course
	 * @return Previously set long name of the course
	 */
	public String setLongCourse(String newCourse)
	{
		changeBeginning("long course name");
		String oldCourse = longCourseName;
		longCourseName = newCourse;
		markUnsaved();
		return oldCourse;
	}

	/**
	 * Gets the currently set chapter
	 * @return String of the current chapter
	 */
	public String getChapter()
	{
		return probChapter;
	}

	/**
	 * Sets the chapter this Problem comes from
	 * @param newChapter New chapter to assign to this problem
	 * @return Previously set chapter
	 */
	public String setChapter(String newChapter)
	{
		changeBeginning("problem chapter");
		String oldChapter = probChapter;
		probChapter = newChapter;
		markUnsaved();
		return oldChapter;
	}

	/**
	 * Returns the current section this problem comes from
	 * @return The current section
	 */
	public String getSection()
	{
		return probSection;
	}

	/**
	 * Sets the section this problem comes from. The string is arbitrary
	 * and not used internally by Problem in any way
	 * @param newSection New section to assign to this Problem
	 * @return previously assigned section
	 */
	public String setSection(String newSection)
	{
		changeBeginning("problem section");
		String oldSection = probSection;
		probSection = newSection;
		markUnsaved();
		return oldSection;
	}

	/**
	 * Current problem number in the chapter/section
	 * @return current problem number
	 */
	public String getProblemNumber()
	{
		return probSection;
	}

	/**
	 * This Problem's number in the chapter/section
	 * @param newNum New problem number
	 * @return Previously set problem number
	 */
	public String setProblemNumber(String newNum)
	{
		changeBeginning("problem number");
		String oldNum = probNum;
		probNum = newNum;
		markUnsaved();
		return oldNum;
	}

	/**
	 * Adds the given operation as an "unused," unattached operation.
	 * It is unused in the sense that it is not used in any calculation
	 * @param op Operation to add
	 * @return Newly added operation
	 */
	public final Operation addUnusedOperation(Operation op)
	{
		// Only add if it's not already on the list
		if(unusedOperations.contains(op))
			return op;

		// Ensure it actually has no parent
		if(op.getParentData() != null)
			throw new ProblemException("Operation was not detached before being marked as unused");

		// Add to our list
		if(unusedOperations.add(op))
			markUnsaved();
		
		return op;
	}

	/**
	 * Removes the given operation from our list of unused operations
	 * @param op Operation to remove from list
	 * @return Removed operation
	 */
	public final Operation removeUnusedOperation(Operation op)
	{
		if(unusedOperations.remove(op))
			markUnsaved();

		return op;
	}

	/**
	 * Number of unused operations attached to this problem
	 * @return number of unattached operations on this problem
	 */
	public final int getUnusedOperationCount()
	{
		return unusedOperations.size();
	}

	/**
	 * Returns the unused operation at the given index
	 * @param i Index of the operation to retrieve
	 * @return Operation at the given index
	 */
	public final Operation getUnusedOperation(int i)
	{
		return unusedOperations.get(i);
	}

	/**
	 * Adds an existing dataset to the problem.
	 * @param data Dataset to add.
	 * @return Reference to newly added dataset.
	 */
	public DataSet addData(DataSet data)
	{
		// Don't add the same data again. Do an actual object
		// comparison, not .equals(). Hence why we don't use .contains.
		for(DataSet ds : datasets)
		{
			if(ds == data)
				return data;
		}

		changeBeginning("adding dataset " + data.getName());
		
		// Remove from the old problem if needed
		Problem oldParent = data.getParentProblem();
		if(oldParent != null)
			oldParent.removeData(data);

		markUnsaved();
		data.setParentProblem(this);
		datasets.add(data);

		if(!isLoading && getDomain() != null)
			getDomain().rebuildTree(data);
		
		return data;
	}

	/**
	 * Remove a given dataset from this problem
	 * @param data DataSet object to remove
	 * @return DataSet that was removed
	 */
	public DataSet removeData(DataSet data)
	{
		return removeData(datasets.indexOf(data));
	}

	/**
	 * Remove the DataSet at the given index from the problem
	 * @param index Index of DataSet to remove
	 * @return DataSet being removed from the problem
	 */
	public DataSet removeData(int index)
	{
		DataSet d = datasets.get(index);

		changeBeginning("removing dataset " + d.getName());
		
		// Ensure the SubProblems that are used don't contain it any more
		for(SubProblem sub : d.getSubProblems())
			sub.removeStep(d);

		markUnsaved();
		d.setParentProblem(null);
		datasets.remove(index);
		return d;
	}

	/**
	 * Gets all leaf data attached to this problem. These may be DataSets
	 * that have no children or the ending operations on those
	 * @return All DataSources attached to the Problem that have no children
	 */
	public List<DataSource> getAllLeafData()
	{
		List<DataSource> myData = new ArrayList<DataSource>();

		// Add either each DataSet (if it has no children)
		// or the dataset's leaf operations
		for(DataSet ds : datasets)
		{
			List<Operation> leaves = ds.getAllLeafOperations();
			if(!leaves.isEmpty())
				myData.addAll(leaves);
			else
				myData.add(ds);
		}

		return myData;
	}

	/**
	 * Gets all data attached to this problem. These may be DataSets or Operations
	 * @return All DataSources attached to the Problem directly or indirectly
	 */
	public List<DataSource> getAllData()
	{
		List<DataSource> myData = new ArrayList<DataSource>();

		// Add each DataSet and their attached operations
		for(DataSet ds : datasets)
		{
			myData.add(ds);
			myData.addAll(ds.getAllChildOperations());
		}

		// And all our unsused stuff
		for(Operation op : unusedOperations)
		{
			myData.add(op);
			myData.addAll(op.getAllChildOperations());
		}

		return myData;
	}

	/**
	 * Gets all visible data attached to this problem. These may be DataSets or Operations
	 * @return All visible DataSources attached to the Problem directly or indirectly
	 */
	public List<DataSource> getVisibleData()
	{
		List<DataSource> myData = new ArrayList<DataSource>();

		// Add each DataSet and their attached operations
		for(DataSet ds : datasets)
		{
			if(!ds.isHidden())
			{
				myData.add(ds);
				myData.addAll(ds.getAllChildOperations());
			}
		}

		// And all our unused stuff is visible
		for(Operation op : unusedOperations)
		{
			myData.add(op);
			myData.addAll(op.getAllChildOperations());
		}

		return myData;
	}

	/**
	 * Gets all visible DataSets attached to this problem. These are only DataSets
	 * @return All visible DataSets attached to the Problem directly
	 */
	public List<DataSet> getVisibleDataSets()
	{
		List<DataSet> myData = new ArrayList<DataSet>();

		// Add each DataSet and their attached operations
		for(DataSet ds : datasets)
		{
			if(!ds.isHidden())
				myData.add(ds);
		}

		return myData;
	}

	/**
	 * Gets all leaf operations attached to this problem. Unlike getAllLeafData(),
	 * this does not include DataSets with no children
	 * @return All operations attached to the Problem that have no children
	 */
	public List<Operation> getAllLeafOperations()
	{
		List<Operation> myData = new ArrayList<Operation>();

		// Add either each DataSet (if it has no children)
		// or the dataset's leaf operations
		for(DataSet ds : datasets)
			myData.addAll(ds.getAllLeafOperations());

		return myData;
	}

	/**
	 * Returns the dataset with the given name.
	 * @param name Dataset name
	 * @return Dataset with matching name
	 */
	public DataSet getData(String name) 
	{
		return getData(getDataIndex(name));
	}

	/**
	 * Returns the DataSet at the given index
	 * @param index Index of DataSet to retrieve
	 * @return DataSet at given index
	 */
	public DataSet getData(int index)
	{
		return datasets.get(index);
	}

	/**
	 * Returns the index of the DataSet with the given name. An exception is
	 * thrown if a DataSet with the given name cannot be found.
	 * @param name Dataset name
	 * @return Dataset with matching name
	 */
	public int getDataIndex(String name)
	{
		for(int i = 0; i < datasets.size(); i++)
		{
			if(datasets.get(i).getName().equals(name))
				return i;
		}

		// Dataset not found, name was bad
		throw new DataNotFoundException("Failed to find dataset with name '" + name + "'");
	}

	/**
	 * Returns the number of DataSets this Problem contains
	 * @return Number of DataSets in this Problem
	 */
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
		changeBeginning("adding subproblem " + sub.getSubproblemID());
		subProblems.add(sub);
		markUnsaved();
		return sub;
	}

	/**
	 * Remove a given sub problem from this problem
	 * @param sub SubProblem object to remove
	 * @return The sub problem that was just removed
	 */
	public SubProblem removeSubProblem(SubProblem sub)
	{
		changeBeginning("removing subproblem " + sub.getSubproblemID());
		
		// Tell any datasource that pointed to here to no longer do so
		for(int i = 0; i < sub.getStepCount(); i++)
			sub.removeStep(i);

		markUnsaved();
		subProblems.remove(sub);
		return sub;
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
	 * @return Path this file will save to when save() is called.
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
	public void markUnsaved()
	{
		isSaved = false;

		// Don't bother if we're loading
		if(isLoading)
			return;
		
		Domain d = getDomain();
		if(d != null)
			d.markUnsaved();
	}

	@Override
	public void changeBeginning(String changeMsg)
	{
		// Don't bother if we're loading
		if(isLoading)
			return;
		
		Domain d = getDomain();
		if(d != null)
			d.changeBeginning(changeMsg);
	}

	/**
	 * Attempts to save problem to file path given.
	 * @param fileName Where to attempt to save the problem.
	 */
	private void save(String fileName)
	{
		if(fileName == null)
			throw new ProblemException("File name may not be null");

		// Build
		Document doc = new Document(this.toXml());

		try
		{
			// Output to file
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(fileName));
			BufferedWriter outputStream = new BufferedWriter(os);

			Format formatter = Format.getPrettyFormat();
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);
		}
		catch(IOException ex)
		{
			throw new ProblemException("Problem occured writing to file during save", ex);
		}
	}

	/**
	 * Attempts to save problem to file specified by fileName
	 */
	public void save()
	{
		save(fileName);
		isSaved = true;

		Domain d = getDomain();
		if(d != null)
			d.markSaved();
	}

	/**
	 * Loads a problem from the file path given. Returned Problem object
	 * should essentially duplicate the state the problem was in when it
	 * was saved.
	 * @param fileName Path to save file
	 * @return Restored Problem object
	 */
	public static Problem load(String fileName)
	{
		try
		{
			// Load file into JDOM
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(fileName);

			// Make problem
			Problem newProb = Problem.fromXml(doc.getRootElement());
			newProb.setFileName(fileName);
			newProb.isSaved = true;

			return newProb;
		}
		catch(JDOMException ex)
		{
			throw new ProblemException("Save file contains unparsable XML errors", ex);
		}
		catch(NullPointerException ex)
		{
			throw new ProblemException("An error occurred loading from the save file. It may be an old version.", ex);
		}
		catch(IOException ex)
		{
			throw new ProblemException("Error reading save file from disk", ex);
		}
	}

	/**
	 * Utility to determine if the current problem is in the process of loading
	 * from XML. Use internally to prevent computations from trying to happen
	 * (and usually failing).
	 * @return true if the Problem is in the process of loading, false otherwise
	 */
	@Override
	public boolean isLoading()
	{
		return isLoading;
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
		if(!conclusion.equals(otherP.conclusion))
			return false;
		if(!personName.equals(otherP.personName))
			return false;
		if(!shortCourseName.equals(otherP.shortCourseName))
			return false;
		if(!longCourseName.equals(otherP.longCourseName))
			return false;
		if(!probChapter.equals(otherP.probChapter))
			return false;
		if(!probSection.equals(otherP.probSection))
			return false;
		if(!probNum.equals(otherP.probNum))
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
		hash = 29 * hash + (this.conclusion != null ? this.conclusion.hashCode() : 0);
		hash = 29 * hash + (this.personName != null ? this.personName.hashCode() : 0);
		hash = 29 * hash + (this.longCourseName != null ? this.longCourseName.hashCode() : 0);
		hash = 29 * hash + (this.shortCourseName != null ? this.shortCourseName.hashCode() : 0);
		hash = 29 * hash + (this.probNum != null ? this.probNum.hashCode() : 0);
		hash = 29 * hash + (this.probSection != null ? this.probSection.hashCode() : 0);
		hash = 29 * hash + (this.probChapter != null ? this.probChapter.hashCode() : 0);
		hash = 29 * hash + (this.datasets != null ? this.datasets.hashCode() : 0);
		hash = 29 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
		return hash;
	}

	@Override
	public Element toXml()
	{
		Element rootEl = new Element("problem");

		rootEl.addContent(new Element("statement").addContent(statement));
		rootEl.addContent(new Element("conclusion").addContent(conclusion));

		rootEl.setAttribute("person", personName);
		rootEl.setAttribute("course_long", longCourseName);
		rootEl.setAttribute("course_short", shortCourseName);

		rootEl.setAttribute("chapter", probChapter);
		rootEl.setAttribute("section", probSection);
		rootEl.setAttribute("probnum", probNum);

		// Add each DataSet
		for(DataSet data : datasets)
		{
			rootEl.addContent(data.toXml());
		}

		// Add the unused operations problems
		Element unusedOpEl = new Element("unused");
		rootEl.addContent(unusedOpEl);
		for(Operation op : unusedOperations)
		{
			unusedOpEl.addContent(op.toXml());
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
	 */
	public static Problem fromXml(Element rootEl)
	{
		Problem newProb = new Problem();
		newProb.isLoading = true;

		newProb.setStatement(rootEl.getChildText("statement"));
		newProb.setConclusion(rootEl.getChildText("conclusion"));

		newProb.setPersonName(rootEl.getAttributeValue("person"));
		newProb.setLongCourse(rootEl.getAttributeValue("course_long"));
		newProb.setShortCourse(rootEl.getAttributeValue("course_short"));
		
		newProb.setChapter(rootEl.getAttributeValue("chapter"));
		newProb.setSection(rootEl.getAttributeValue("section"));
		newProb.setProblemNumber(rootEl.getAttributeValue("probnum"));

		// Main data
		for(Object dataEl : rootEl.getChildren("data"))
		{
			newProb.addData(DataSet.fromXml((Element) dataEl));
		}

		// Add the unattached operations, if applicable
		Element unusedOpsEl = rootEl.getChild("unused");
		if(unusedOpsEl != null)
		{
			for(Object partEl : unusedOpsEl.getChildren("operation"))
			{
				newProb.addUnusedOperation(Operation.fromXml((Element) partEl));
			}
		}

		// SubProblems
		for(Object partEl : rootEl.getChildren("part"))
		{
			newProb.addSubProblem(SubProblem.fromXml((Element) partEl, newProb));
		}

		newProb.isLoading = false;

		// After we're all done loading, rebuild the trees
		if(domain != null)
			domain.rebuildWorkspace();

		return newProb;
	}
	
	@Override
	public Problem clone()
	{
		return new Problem(this);
	}
}
