/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package marla.ide.resource;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author traherom
 */
public class UndoRedoTest
{
	@Test
	public void fullTest()
	{
		UndoRedo<String> testStack = new UndoRedo<String>(5);
		assertFalse(testStack.hasUndo());
		assertFalse(testStack.hasRedo());
		
		int curr = 0;
		String[] toPush = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};

		// Push on 3
		for( ; curr < 3; curr++)
			testStack.addUndoStep(toPush[curr]);

		assertTrue(testStack.hasUndo());
		assertFalse(testStack.hasRedo());

		// Undo 2
		for( ; curr > 1; curr--)
		{
			String removed = testStack.undo(toPush[curr]);
			assertEquals(toPush[curr-1], removed);
		}

		assertTrue(testStack.hasUndo());
		assertTrue(testStack.hasRedo());

		// Undo remainder
		for( ; curr > 0; curr--)
		{
			String removed = testStack.undo(toPush[curr]);
			assertEquals(toPush[curr-1], removed);
		}

		assertFalse(testStack.hasUndo());
		assertTrue(testStack.hasRedo());

		// Redo 2
		for( ; curr < 2; curr++)
		{
			String removed = testStack.redo(toPush[curr]);
			assertEquals(toPush[curr+1], removed);
		}

		assertTrue(testStack.hasUndo());
		assertTrue(testStack.hasRedo());

		// Redo remainder
		for( ; curr < 3; curr++)
		{
			String removed = testStack.redo(toPush[curr]);
			assertEquals(toPush[curr+1], removed);
		}

		assertTrue(testStack.hasUndo());
		assertFalse(testStack.hasRedo());

		// Push a ton on
		for( ; curr < toPush.length - 1; curr++)
			testStack.addUndoStep(toPush[curr]);
		
		assertTrue(testStack.hasUndo());
		assertFalse(testStack.hasRedo());

		// Undo everything
		while(testStack.hasUndo())
		{
			String removed = testStack.undo(toPush[curr]);
			assertEquals(toPush[curr-1], removed);
			curr--;
		}

		assertEquals("Max size violated", 5, toPush.length - 1 - curr);
		assertFalse(testStack.hasUndo());
		assertTrue(testStack.hasRedo());
	}

	@Test
	public void testHasRedo()
	{
		UndoRedo<String> testStack = new UndoRedo<String>(10);
		assertFalse(testStack.hasRedo());
		testStack.addUndoStep("bah");
		assertFalse(testStack.hasRedo());
		testStack.undo("bah2");
		assertTrue(testStack.hasRedo());
		testStack.redo("bah3");
		assertFalse(testStack.hasRedo());
	}

	@Test
	public void testHasUndo()
	{
		UndoRedo<String> testStack = new UndoRedo<String>(10);
		assertFalse(testStack.hasUndo());
		testStack.addUndoStep("bah");
		assertTrue(testStack.hasUndo());
	}
}
