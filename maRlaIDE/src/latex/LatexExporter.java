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

import java.io.BufferedWriter;
import problem.MarlaException;
import problem.Problem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import problem.SubProblem;

/**
 * @author Ryan Morehart
 */
public class LatexExporter
{
	/**
	 * Problem this exporter is working with
	 */
	private final Problem prob;
	/**
	 * Folder the exported files will actually be dropped
	 */
	private String exportDir = null;
	/**
	 * File name of the main file, minus the extension. Any additional files are
	 * based off of this. This should not include a path component, that is set
	 * in exportLocation
	 */
	private String baseName = null;
	/**
	 * Path to XML template to use for latex export
	 */
	private String templatePath = null;
	/**
	 * XML template to use for latex export
	 */
	private Element templateXML = null;
	/**
	 * Used during processing to mark the subproblem we are working on
	 */
	private SubProblem currentSub = null;

	/**
	 * Types of substitutions and commands we support from XML
	 */
	private enum CommandType
	{
		LOOP, STATEMENT, SOLUTION, DATASET, NAME, CLASS, CHAPTER, SECTION, PROBNUM
	};

	/**
	 * Types of loops we support
	 */
	private enum LoopType
	{
		SUBPROBLEM
	};

	/**
	 * Creates a new Latex exporter for the given problem
	 * @param problem Problem to export to
	 */
	public LatexExporter(Problem problem) throws LatexException
	{
		this(problem, null);
	}

	/**
	 * Creates a new Latex exporter for the given problem, using the given template
	 * @param problem Problem to export to
	 * @param templatePath Path to the template to use for exporting.
	 */
	public LatexExporter(Problem problem, String templatePath) throws LatexException
	{
		if(problem == null)
			throw new LatexException("Problem to export may not be null");

		prob = problem;
		setTemplate(templatePath);
	}

	/**
	 * Sets the LaTeX template this exporter should use
	 * @param newTemplatePath Path to the template file
	 * @return Previously set template path
	 */
	public final String setTemplate(String newTemplatePath) throws LatexException
	{
		String oldPath = templatePath;
		templatePath = newTemplatePath;

		// Load the XML
		try
		{
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(templatePath);
			templateXML = doc.getRootElement();
		}
		catch(JDOMException ex)
		{
			throw new LatexException("LaTeX template could not be parsed", ex);
		}
		catch(IOException ex)
		{
			throw new LatexException("LaTeX template XML file could not be read", ex);
		}

		return oldPath;
	}

	/**
	 * Sets directory for the export. This should not include specifics on the actual
	 * file name, that is set by setExportBaseName
	 * @param fileName File name for the main file
	 */
	public String setExportDirectory(String newExportDir)
	{
		String oldDir = exportDir;
		exportDir = newExportDir;
		return oldDir;
	}

	/**
	 * Changes the base name for the exporter. The base name is the primary
	 * part of the file name, minus the extension. The actual directory that
	 * the export will occur in is set by setExportDirectory
	 * @param newBaseName File name for the main file
	 */
	public String setExportBaseName(String newBaseName)
	{
		String oldBase = baseName;
		baseName = newBaseName;
		return oldBase;
	}

	/**
	 * Exports the problem using either cleanExport() if an existing export at this location
	 * doesn't already exist or refreshExport() if there is one
	 */
	public String export() throws LatexException, MarlaException
	{
		File exportFile = new File(exportDir + "/" + baseName + ".rnw");
		if(exportFile.isFile())
		{
			return refreshExport();
		}
		else
		{
			return cleanExport();
		}
	}

	/**
	 * Completely exports the problem, overwriting any existing files.
	 * @return Path to the main LaTeX file that has been exported
	 */
	public String cleanExport() throws LatexException, MarlaException
	{
		// Ensure everything is set
		if(exportDir == null)
			throw new LatexException("Export directory has not been set yet");
		if(baseName == null)
			throw new LatexException("Export base name has not been set yet");

		// Create directory if needed
		File exportFile = new File(exportDir);
		if(!exportFile.isDirectory() && !exportFile.mkdirs())
			throw new LatexException("Unable to create directory '" + exportDir + "' to export to");

		try
		{
			// Write out the template until we find a marker to work with
			BufferedWriter writer = new BufferedWriter(new FileWriter(exportDir + "/" + baseName + ".rnw"));

			processSectionClean(templateXML, writer);

			// Close the files
			writer.close();
		}
		catch(IOException ex)
		{
			throw new LatexException("Error occured during exporting", ex);
		}

		return exportFile.getAbsolutePath();
	}

	private void processSectionClean(Element el, Writer out) throws LatexException, MarlaException
	{
		// And start looking through the template
		// Text nodes get placed in verbatim,
		for(Object partObj : el.getContent())
		{
			// We only deal with elements (stuff we need to replace/handle)
			// and text, which we stick in verbatim. Ignore everything else, such as comments
			if(partObj instanceof Element)
			{
				// Process
				Element partEl = (Element) partObj;
				CommandType cmd = CommandType.valueOf(partEl.getName().toUpperCase());
				switch(cmd)
				{
					case LOOP:
						processLoopClean(partEl, out);
						break;

					case STATEMENT:
						processStatementClean(partEl, out);
						break;

					case SOLUTION:
						processSolutionClean(partEl, out);
						break;

					case NAME:
					case CLASS:
					case CHAPTER:
					case SECTION:
					case PROBNUM:
						break;

					default:
						throw new LatexException("'" + cmd + "' is not a supported element in template XML");
				}
			}
			else if(partObj instanceof Text)
			{
				try
				{
					// Verbatim dump it to the export
					Text partText = (Text) partObj;
					String text = partText.getText();
					out.write(text, 0, text.length());
				}
				catch(IOException ex)
				{
					throw new LatexException("Unable to write to the output file during export");
				}
			}
		}
	}

	private void processLoopClean(Element el, Writer out) throws LatexException, MarlaException
	{
		// Double loops not allowed.
		if(currentSub != null)
			throw new LatexException("Nested loops are not permitted within LaTeX export template");

		for(int i = 0; i < prob.getSubProblemCount(); i++)
		{
			currentSub = prob.getSubProblem(i);
			processSectionClean(el, out);
		}

		// All done with loop
		currentSub = null;
	}

	private void processStatementClean(Element el, Writer out) throws LatexException
	{
		// Are we in a loop?
		String statement = null;
		if(currentSub == null)
		{
			// Get statement from the main problem
			statement = prob.getStatement();
		}
		else
		{
			// Get statement from the subproblem we are on
			statement = currentSub.getStatement();
		}

		try
		{
			// Write it out
			out.write(statement, 0, statement.length());
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write statement to export file");
		}
	}

	public void processSolutionClean(Element el, Writer out) throws LatexException, MarlaException
	{
		StringBuilder sweaveBlock = new StringBuilder();

		// R code itself. First, use the main problem for the solution unless
		// we are in a loop with a current subproblem. The subproblem must
		// have a solution denoted though.
		if(currentSub == null)
		{
			sweaveBlock.append("<<label=main>>\n");

			// Pull in all the R from every leaf operation in the system
			

			// End block
			sweaveBlock.append("@\n");
		}
		else if(currentSub.getSolutionEnd() != null)
		{
			// Block beginning
			sweaveBlock.append("<<label=");
			sweaveBlock.append(currentSub.getSubproblemID());
			sweaveBlock.append(">>=\n");

			// Make sure there's a denoted solution
			sweaveBlock.append(currentSub.getSolutionEnd().getRCommands(currentSub.getSolutionStart()));

			// End block
			sweaveBlock.append("@\n");
		}
		else
		{
			// No solution yet
			sweaveBlock.append("No solution yet\n");
		}

		try
		{
			// Write it out
			String sweave = sweaveBlock.toString();
			out.write(sweave, 0, sweave.length());
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write statement to export file");
		}
	}

	/**
	 * Replaces existing R portions in an exported problem and leaves everything else
	 * untouched. If problem (or subproblem) statements have changed, nothing is done with
	 * them. It is the responsibility of the user to change those after the initial export.
	 */
	public String refreshExport() throws LatexException, MarlaException
	{
		// Ensure everything is set
		if(exportDir == null)
			throw new LatexException("Export directory has not been set yet");
		if(baseName == null)
			throw new LatexException("Export base name has not been set yet");

		// It must already exist or we just go ahead and do a clean export
		File exportFile = new File(exportDir + "/" + baseName + ".rnw");
		if(!exportFile.isFile())
			return cleanExport();

		// We must be able to read and write it
		if(!exportFile.canRead())
			throw new LatexException("Unable to read '" + exportFile.getName() + "' to refresh the export");
		if(!exportFile.canWrite())
			throw new LatexException("Unable to write '" + exportFile.getName() + "' to refresh the export");

		// TODO refresh, look for R sections, replace as needed
		// TODO tack on new subproblems?

		throw new RuntimeException("refreshExport() not implemented yet");
	}

	/**
	 * Does a clean export into a temporary directory, then runs the result through pdflatex.
	 * The generated PDF file is then copied to the export location and the path to that file
	 * is returned.
	 * @return Path to the newly created PDF
	 */
	public String generatePDF()
	{
		// Write it all to a temporary folder and write the PDF to the export location
		String oldExportLoc = exportDir;
		String oldBaseName = baseName;

		File pdfFile = new File(exportDir + "/" + baseName + ".rnw");

		// Restore old settings
		exportDir = oldExportLoc;
		baseName = oldBaseName;

		return pdfFile.getAbsolutePath();
	}
}
