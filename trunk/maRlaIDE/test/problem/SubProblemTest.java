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
import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Ryan Morehart
 */
public class SubProblemTest
{
	/**
	 * Main problem/subproblems to test with
	 */
	private Problem testProb1 = null;
	private SubProblem testSub1 = null;
	private SubProblem testSub2 = null;
	/**
	 * Empty test problem that subproblems can be added to
	 */
	private Problem testProb2 = null;

	public static SubProblem createSubProblem()
	{
		SubProblem sub = null;

		return sub;
	}

	@BeforeClass
	public static void initOperations() throws Exception
	{
		r.OperationXML.loadXML("ops.xml");
	}
	
	@Before
	public void beforeTest() throws Exception
	{
		// Create a problem and some subproblems to work with
		testProb1 = ProblemTest.createProblem(3, 3, 5);
		testSub1 = testProb1.addSubProblem("a", "Test subproblem A.");
		testSub2 = testProb1.addSubProblem("b", "Test subproblem B.");

		// And a stupid one to do other stuff to
		testProb2 = ProblemTest.createProblem(3, 3, 5);
	}

	@Test
	public void testToAndFromXMLNoSolution()
	{
		Element el = testSub1.toXml();
		SubProblem newSub = SubProblem.fromXml(el, testProb2);
		assertEquals(testSub1, newSub);
	}

	@Test
	public void testToAndFromXMLWithSolution()
	{
		// TBD, hook up the start and ends in some places
		Element el = testSub1.toXml();
		SubProblem newSub = SubProblem.fromXml(el, testProb2);
		assertEquals(testSub1, newSub);
	}
}
