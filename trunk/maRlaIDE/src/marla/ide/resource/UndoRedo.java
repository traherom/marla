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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stores undo/redo steps and offers a quick interface for looping through them
 * @author Ryan Morehart
 */
public class UndoRedo<T>
{
	/**
	 * Position of the step (type T) in the Object[] array stored in
	 * undo/redo stacks
	 */
	private static final int STEP_LOC = 0;
	/**
	 * Position of the message in the Object[] array stored in
	 * undo/redo stacks
	 */
	private static final int MSG_LOC = 1;
	/**
	 * Undo steps that have been pushed unto our list. The object array contains:
	 *   STEP_LOC - actual undo step, of type T
	 *   MSG_LOC - message given to describe this step, as different from the next
	 *			step. IE, "problem conclusion". If none, null
	 */
	private final Deque<Object[]> undoStack = new ArrayDeque<Object[]>();
	/**
	 * Redo steps that have been pushed unto our list. See undoStack
	 * for info on the object array
	 */
	private final Deque<Object[]> redoStack = new ArrayDeque<Object[]>();
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
	public void addUndoStep(T step)
	{
		addUndoStep(step, null);
	}

	/**
	 * Adds a history step. If an immediate undo would return the newly
	 * added step.
	 * @param step New step to add to stack
	 * @param msg Message on what this change represents, usually what's different
	 *		about it compared to the "current" T now
	 */
	public void addUndoStep(T step, String msg)
	{
		undoStack.addLast(new Object[]{step, msg});

		// Blow away any redo steps we had
		redoStack.clear();

		// Do we have too much history?
		if(maxStates > 0)
		{
			while(undoStack.size() > maxStates)
				undoStack.pollFirst();
		}
	}
	
	/**
	 * Removed all existing steps
	 */
	public void clearHistory()
	{
		undoStack.clear();
		redoStack.clear();
	}

	/**
	 * Undoes one step, returning the state at that point
	 * @param redoStep Step that will be inserted as the next redo. Typically the
	 *		current state of whatever is being undone
	 * @return State at the previous point
	 */
	public T undo(T redoStep)
	{
		if(!hasUndo())
			return null;

		// Pull out the step we're undoing to
		Object[] stepData = undoStack.pollLast();
		String msg = (String)stepData[MSG_LOC];
		@SuppressWarnings("unchecked")
		T step = (T)stepData[STEP_LOC];

		// Add to redo stack and ensure we don't violate set limits
		// Use the same message as the corresponding undo step
		redoStack.addLast(new Object[]{redoStep, msg});
		if(maxStates > 0)
		{
			while(redoStack.size() > maxStates)
				redoStack.pollFirst();
		}
		
		return step;
	}

	/**
	 * Redoes one step, returning the state at that point
	 * @param undoStep Step that will be inserted as the next undo. Tyically the
	 *		current state of whatever is being unredone
	 * @return State at the next history point
	 */
	public T redo(T undoStep)
	{
		if(!hasRedo())
			return null;

		// Pull out the step we're redoing to
		Object[] stepData = redoStack.pollLast();
		String msg = (String)stepData[MSG_LOC];
		@SuppressWarnings("unchecked")
		T step = (T)stepData[STEP_LOC];

		// Add to undo stack and ensure we don't violate set limits
		undoStack.addLast(new Object[]{undoStep, msg});
		if(maxStates > 0)
		{
			while(undoStack.size() > maxStates)
				undoStack.pollFirst();
		}

		return step;
	}

	/**
	 * Fetches the message associated with the next undo step. Must be called
	 * before the actual undo occurs!
	 * @return String to show to the user, typically. null if there is none
	 */
	public String undoMessage()
	{
		String msg = (String)undoStack.peekLast()[MSG_LOC];
		if(msg != null)
			return "Reverted " + msg;
		else
			return null;
	}

	/**
	 * Fetches the message associated with the next redo step.
	 * @return String to show to the user, typically. null if there is none
	 */
	public String redoMessage()
	{
		String msg = (String)redoStack.peekLast()[MSG_LOC];
		if(msg != null)
			return "Restored " + msg;
		else
			return null;
	}

	/**
	 * Checks if there are redoes available in the current stack.
	 * @return true if there are newer steps, false otherwise
	 */
	public boolean hasRedo()
	{
		return !redoStack.isEmpty();
	}

	/**
	 * Checks if there are undoes available in the current stack.
	 * @return true if there are older steps, false otherwise
	 */
	public boolean hasUndo()
	{
		return !undoStack.isEmpty();
	}
}
