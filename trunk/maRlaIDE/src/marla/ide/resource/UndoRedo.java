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
package marla.ide.resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores undo/redo steps and offers a quick interface for looping through them
 * @author Ryan Morehart
 */
public class UndoRedo<T>
{
	/**
	 * Undo/redo steps that have been pushed unto our list
	 */
	private final List<T> states = new ArrayList<T>();
	/**
	 * Current point in the state list. This is one past the most recently 
	 * added step. If an undo were performed, it would get the step before
	 * this one. A redo returns the step we're actually on
	 */
	private int currentState = 0;
	/**
	 * Maximum undo/redo steps to hold. If list exceeds this, older steps are 
	 * removed when new ones are added
	 */
	private final int maxStates;
	
	/**
	 * Creates a new undo/redo stack with the given limit
	 * @param maxSteps Maximum steps to hold, applies to both forward and back. If
	 *		less than 1, no history is ever removed
	 */
	public UndoRedo(int maxSteps)
	{
		if(maxSteps > 0)
			maxStates = maxSteps;
		else
			maxStates = 0;
	}
	
	/**
	 * Creates a new undo/redo stack with no state limit
	 */
	public UndoRedo()
	{
		this(0);
	}
	
	/**
	 * Adds a history step. If an immediate undo would return the newly
	 * added step.
	 * @param step New step to add to stack
	 */
	public void addStep(T step)
	{
		states.add(maxStates, step);
		currentState++;
		
		// Blow away any redo steps we had
		while(hasRedo())
			states.remove(currentState);
		
		// Do we have too much history?
		while(states.size() > maxStates)
		{
			states.remove(0);
			
			// Keep pointer in sync with list
			currentState--;
		}
	}
	
	/**
	 * Undoes one step, returning the state at that point
	 * @return State at the previous point
	 */
	public T undo()
	{
		if(!hasUndo())
			return null;
		
		currentState--;
		return states.get(currentState);
	}
	
	/**
	 * Redoes one step, returning the state at that point
	 * @return State at the next history point
	 */
	public T redo()
	{
		if(!hasRedo())
			return null;
		
		T step = states.get(currentState);
		currentState++;
		return step;
	}

	/**
	 * Checks if there are redoes available in the current stack.
	 * @return true if there are newer steps, false otherwise
	 */
	public boolean hasRedo()
	{
		return currentState < states.size();
	}

	/**
	 * Checks if there are undoes available in the current stack.
	 * @return true if there are older steps, false otherwise
	 */
	public boolean hasUndo()
	{
		return 0 < currentState;
	}
}
