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
 * Simple interface.
 *
 * @author Ryan Morehart
 */
public interface ProblemPart
{
	/**
	 * Gets the description for this part of the problem
	 * @return String description of the question for this sub problem
	 */
	public String getStatement();

	/**
	 * Problem statement for this problem.
	 *
	 * @param newStatement String description of problem. See
	 *		Problem(String) for more information.
	 */
	public void setStatement(String newStatement);

	/**
	 * Marks this problem as needing saving. Used by datasets and such
	 * to note when they've been altered.
	 */
	public void markChanged();

	/**
	 * Returns the starting DataSet and operations running down
	 * to the end of the solution to this part of the problem. The "starting"
	 * DataSet this returns may not be data the problem was actually
	 * initialized with. Instead, it's the data as it was at the "start"
	 * operation. For example, if we start our solution at a "divide by 10"
	 * operation, then the DataSet that is returned here will be the result
	 * of that.
	 * @return Chain of operations to solve this part of the problem
	 * @throws IncompleteInitialization Thrown if an end point has not been
	 *			indicated. If a start isn't marked it will go to the beginning
	 *			of the chain.
	 */
	public DataSet getSolutionSteps() throws IncompleteInitialization;

	/**
	 * Returns the getColumn() results for all of the columns in
	 * the first DataSet in a part
	 * @return Solved DataSet
	 * @throws IncompleteInitialization Some aspect of the problem was not initialized
	 * @throws CalcException Unable to perform R work to computer answer
	 */
	public DataSet getAnswer(int index) throws IncompleteInitialization, CalcException;

	/**
	 * Trues true if the problem has unsaved changes
	 * @return true if the problem has changes that are not yet saved
	 */
	public boolean isChanged();

	/**
	 * Adds an existing dataset to the problem.
	 * @param data Dataset to add.
	 * @return Reference to newly added dataset.
	 */
	public DataSet addData(DataSet data);

	/**
	 * Remove a given dataset from this problem
	 * @param data DataSet object to remove
	 */
	public DataSet removeData(DataSet data);

	/**
	 * Remove the DataSet at the given index from the problem
	 * @param index Index of DataSet to remove
	 * @return DataSet being removed from the problem
	 */
	public DataSet removeData(int index);

	/**
	 * Returns the dataset with the given name.
	 * @param name Dataset name
	 * @return Dataset with matching name
	 * @throws DataNotFound Unable to find the DataSet requested
	 */
	public DataSet getData(String name) throws DataNotFound;

	/**
	 * Returns the DataSet at the given index
	 * @param index Index of DataSet to retrieve
	 * @return DataSet at given index
	 */
	public DataSet getData(int index);

	/**
	 * Returns the index of the DataSet with the given name
	 * @param name Dataset name
	 * @return Dataset with matching name
	 * @throws DataNotFound Unable to find the DataSet requested
	 */
	public int getDataIndex(String name) throws DataNotFound;

	/**
	 * Returns the number of DataSets this Problem contains
	 * @return Number of DataSets in this Problem
	 */
	public int getDataCount();

	/**
	 * Returns this problem part as a JDOM Element
	 * @return JDOM element containing all information needed to rebuild
	 *		this exact problem
	 */
	public Element toXml();
}
