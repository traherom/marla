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
package latex;

import problem.MarlaException;
import resource.Configuration;
import java.io.File;
import problem.ProblemTest;
import problem.Problem;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Ryan Morehart
 */
public class LatexExporterTest
{
	private Problem testProb = null;
	private LatexExporter testExp = null;

	@BeforeClass
	public static void configure() throws MarlaException
	{
		Configuration.load();
	}

	@Before
	public void setUp() throws Exception
	{
		testProb = ProblemTest.createProblem(2, 2, 3, 10);
		testExp = new LatexExporter(testProb);
	}

	@Test
	public void testCleanExport() throws Exception
	{
		File tempFile = File.createTempFile("marla", "rnw");
		testExp.cleanExport(tempFile.getPath());
		System.out.println("Look at " + tempFile);
	}

	@Test
	public void testPDFExport() throws Exception
	{
		File tempFile = File.createTempFile("marla", ".pdf");
		testExp.generatePDF(tempFile.getPath());
		System.out.println("Look at " + tempFile);
	}
}
