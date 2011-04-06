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
		UndoRedo<String> testStack = new UndoRedo<String>(10);
		
		testStack.addUndoStep("test1");
		testStack.addUndoStep("test2");
		testStack.addUndoStep("test3");
		testStack.addUndoStep("test4");
		
		String s1 = testStack.undo("test5");
		assertEquals("test4", s1);
		
		String s2 = testStack.undo(s1);
		assertEquals("test3", s2);
		
		String s3 = testStack.redo(s2);
		assertEquals(s1, s3);
		
		String s4 = testStack.redo(s3);
		assertEquals("test5", s4);
		
		String s5 = testStack.redo("test6");
		assertEquals(s5, null);
	}
	
	@Test
	public void testAddUndoStep()
	{
	}

	@Test
	public void testClearHistory()
	{
	}

	@Test
	public void testUndo()
	{
	}

	@Test
	public void testRedo()
	{
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
