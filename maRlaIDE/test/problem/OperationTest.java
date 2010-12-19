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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author traherom
 */
public class OperationTest
{

	private RandomValues random = new RandomValues();

	@Test
	public void testClone()
	{
		//Operation op = new OperationNOP();
		//Operation cloned = op.clone();
		//assertEquals(op, cloned);

		//op = new Summary();
		//cloned = op.clone();
		//assertEquals(op, cloned);
	}

	@Test
	public void testSanatizeName()
	{
		try
		{
			String dirtyName = "Hi, how are you?";
			String cleanName = Operation.sanatizeName(dirtyName);
			assertEquals("Hihowareyou", cleanName);

			dirtyName = "whatisup";
			cleanName = Operation.sanatizeName(dirtyName);
			assertEquals("whatisup", cleanName);

			dirtyName = "12Hippos_Are+Funny16_22_";
			cleanName = Operation.sanatizeName(dirtyName);
			assertEquals("Hippos_AreFunny__", cleanName);
		}
		catch(ExceptionInInitializerError e)
		{
			fail(e.getException().toString());
		}
	}
}
