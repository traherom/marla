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

import org.jdom.Element;

/**
 * Simple interface.
 *
 * @author Ryan Morehart
 */
public interface ProblemPart extends Changeable, Loadable
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
	 * Gets the current conclusion of this problem part. (See setConclusion)
	 * @return Current conclusion. Null if there was none
	 */
	public String getConclusion();

	/**
	 * Sets a new conclusion for this problem part. This is intended as
	 * to contain the ending thought of the associated analysis and operations.
	 * @param newConclusion New conclusion for ProblemPart
	 * @return Current conclusion. Null if there was none
	 */
	public String setConclusion(String newConclusion);

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
	 * @return DataSet that was removed
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
	 */
	public DataSet getData(String name) throws DataNotFoundException;

	/**
	 * Returns the DataSet at the given index
	 * @param index Index of DataSet to retrieve
	 * @return DataSet at given index
	 */
	public DataSet getData(int index);

	/**
	 * Returns the index of the DataSet with the given name. An exception is
	 * thrown if a DataSet with the given name cannot be found.
	 * @param name Dataset name
	 * @return Dataset with matching name
	 */
	public int getDataIndex(String name) throws DataNotFoundException;

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
	public Element toXml() throws MarlaException;
}
