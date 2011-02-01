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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import problem.DataSource;
import problem.Operation;
import problem.SubProblem;
import r.RProcessor;
import r.RProcessorException;

/**
 * @author Ryan Morehart
 */
public class LatexExporter
{
	/**
	 * Unless specified otherwise in the constructor, this path will be used
	 * as the template for new exporter instances
	 */
	private static String defaultTemplate = "export_template.xml";
	/**
	 * Path to the PDF LaTeX binary, used for PDF exports
	 */
	private static String pdflatexPath = "pdflatex";
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

		if(templatePath != null)
			setTemplate(templatePath);
		else
			setTemplate(defaultTemplate);
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

		try
		{
			// Load the XML
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
	 * Sets the default LaTeX template to use for new exporters. May still be
	 * changed for individual instances
	 * @param newTemplatePath new default template to use
	 */
	public static void setDefaultTemplate(String newTemplatePath)
	{
		defaultTemplate = newTemplatePath;
		System.out.println("Using LaTeX export template at '" + defaultTemplate + "'");
	}

	/**
	 * Sets the path to the pdflatex binary file
	 * @param newPdfLatexPath New path to executable
	 */
	public static void setPdflatexPath(String newPdfLatexPath)
	{
		pdflatexPath = newPdfLatexPath;
		System.out.println("Using pdflatex binary at '" + pdflatexPath + "'");
	}

	/**
	 * Configures the defaults for exporters based on the given XML configuration
	 * @param configEl XML configuration element with settings as attributes
	 */
	public static void setConfig(Element configEl)
	{
		// Extract information from configuration XML and set appropriately
		setDefaultTemplate(configEl.getAttributeValue("template"));
		setPdflatexPath(configEl.getAttributeValue("pdflatex"));
	}

	/**
	 * Creates an XML element that could be passed back to setConfig to configure
	 * the LatexExporter defaults the same as currently
	 * @param configEl XML configuration element upon which to add information
	 * @return XML element with configuration data set
	 */
	public static Element getConfig(Element configEl)
	{
		configEl.setAttribute("template", defaultTemplate);
		configEl.setAttribute("pdflatex", pdflatexPath);
		return configEl;
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
		return refreshExport();
	}

	/**
	 * Moves the temporary export files to the actual location
	 * @param fromPath File to move
	 * @param newExt The extension of the file to move to. The export dir and base
	 *			name will be used for the rest of the stuff
	 * @returns New path of the moved file
	 */
	private String moveFile(String fromPath, String newExt) throws LatexException
	{
		// Ensure everything is set
		if(exportDir == null)
			throw new LatexException("Export directory has not been set yet");
		if(baseName == null)
			throw new LatexException("Export base name has not been set yet");

		// Figure out file paths
		File fromFile = new File(fromPath);
		File toFile = new File(exportDir + "/" + baseName + "." + newExt);

		try
		{
			// Move file
			FileUtils.deleteQuietly(toFile);
			FileUtils.moveFile(fromFile, toFile);
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to move the temporary file to '" + toFile + "'");
		}

		return toFile.getPath();
	}

	/**
	 * Completely exports the problem as files
	 * @return Path to the main LaTeX file that has been exported
	 */
	public String cleanExport() throws LatexException, MarlaException
	{
		// Export to the temporary file and them move
		String tempFile = cleanTempExport();
		return moveFile(tempFile, "rnw");
	}

	/**
	 * Completely exports the problem as temporary files
	 * @return Path to the main temporary LaTeX file that has been exported
	 */
	private String cleanTempExport() throws LatexException, MarlaException
	{
		try
		{
			// Write to a temporary file
			File tempFile = File.createTempFile("marla", ".rnw");
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			// Process, making sure it's reset properly
			currentSub = null;
			processSectionClean(templateXML, writer);

			// Close it all out
			writer.close();
			return tempFile.getAbsolutePath();
		}
		catch(IOException ex)
		{
			throw new LatexException("File error occured during exporting", ex);
		}
	}

	private void processSectionClean(Element el, Writer out) throws LatexException, MarlaException
	{
		// And start looking through the template
		// Text nodes get placed in verbatim, XML elements get processing
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
						try
						{
							if(prob.getPersonName() != null)
								out.write(prob.getPersonName());
						}
						catch(IOException ex)
						{
							throw new LatexException("Unable to write person name during export", ex);
						}
						break;

					case CLASS:
						processClassClean(partEl, out);
						break;

					case CHAPTER:
						try
						{
							if(prob.getChapter() != null)
								out.write(prob.getChapter());
						}
						catch(IOException ex)
						{
							throw new LatexException("Unable to write problem chapter during export", ex);
						}
						break;

					case SECTION:
						try
						{
							if(prob.getSection() != null)
								out.write(prob.getSection());
						}
						catch(IOException ex)
						{
							throw new LatexException("Unable to write problem section during export", ex);
						}
						break;

					case PROBNUM:
						try
						{
							if(prob.getProblemNumber() != null)
								out.write(prob.getProblemNumber());
						}
						catch(IOException ex)
						{
							throw new LatexException("Unable to write problem number during export", ex);
						}
						break;

					default:
						throw new LatexException("'" + cmd + "' is not a supported element in template XML yet");
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
					throw new LatexException("Unable to write verbatim LaTeX to the output file during export", ex);
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

	private void processClassClean(Element el, Writer out) throws LatexException, MarlaException
	{
		try
		{
			String courseNameType = el.getAttributeValue("type", "short");

			if(courseNameType.equals("short"))
			{
				if(prob.getShortCourse() != null)
					out.write(prob.getShortCourse());
			}
			else if(courseNameType.equals("long"))
			{
				if(prob.getLongCourse() != null)
					out.write(prob.getLongCourse());
			}
			else
			{
				throw new LatexException("Course name type '" + courseNameType + "' is not supported");
			}
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write course name to template", ex);
		}
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
			// Get all operations, we'll skip ones that aren't leaves
			List<Operation> allOps = new ArrayList<Operation>();
			for(int i = 0; i < prob.getDataCount(); i++)
			{
				allOps.addAll(prob.getData(i).getAllChildOperations());
			}

			for(Operation op : allOps)
			{
				// Make sure it's a leaf
				if(op.getOperationCount() != 0)
					continue;

				sweaveBlock.append("\n<<label=");
				sweaveBlock.append(op.getName());
				sweaveBlock.append(">>=\n");

				sweaveBlock.append(op.getRCommands(true));

				sweaveBlock.append("@\n\n");
			}
		}
		else if(currentSub.getSolutionEnd() != null)
		{
			// Block beginning
			sweaveBlock.append("\n<<label=");
			sweaveBlock.append(currentSub.getSubproblemID());
			sweaveBlock.append(">>=\n");

			// Pull out each R block for the operations inside the solution
			List<DataSource> solOps = currentSub.getSolutionChain();
			for(int i = 0; i < solOps.size(); i++)
			{
				sweaveBlock.append(solOps.get(i).getRCommands(false));
			}

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
	public String generatePDF() throws LatexException, RProcessorException, MarlaException
	{
		// Create the rnw
		String rnwPath = cleanTempExport();

		// Sweave it
		RProcessor proc = RProcessor.getInstance();
		try
		{
			String sweaveOutput = proc.execute("Sweave('" + rnwPath.replaceAll("\\\\", "/") + "')");
		}
		catch(RProcessorException ex)
		{
			throw new LatexException("Unable to sweave file '" + rnwPath + "', likely an error in the template '" + templatePath + "'", ex);
		}

		// Figure out the file names
		String baseFileName = new File(rnwPath).getName().replace(".rnw", "");
		String texPath = baseFileName + ".tex";
		String pdfPath = baseFileName + ".pdf";

		try
		{
			// Run it through pdflatex
			String pdfOutput = proc.execute("system('" + pdflatexPath + " -halt-on-error " + texPath + "', intern=T)", true);

			// Ensure we actually succeeded. R on Windows returns an error (hence becomes
			// an exception) and on Linux it says it cannot find R
			if(!pdfOutput.startsWith(" [1] \"This is pdfTeX"))
			{
				// Unable to find pdflatex to run
				throw new LatexException("Unable to find pdflatex at '" + pdflatexPath + "', cannot do PDF export");
			}
			else if(pdfOutput.contains(".sty' not found"))
			{
				// Sweave not tied into LaTeX properly
				throw new LatexException("Sweave does not appear to be registered correctly with LaTeX");
			}
			
			// Check the output file name reported by pdflatex
			List<String> pdfOutputLines = proc.parseStringArray(pdfOutput);
			String pdfFileLine = pdfOutputLines.get(pdfOutputLines.size() - 2);
			if(!pdfFileLine.matches("^Output written on " + pdfPath + ".*"))
			{
				throw new LatexException("pdflatex reported a different output file ('" + pdfFileLine + "') than we expected ('" + pdfPath + "')");
			}

			// Move the final PDF file
			return moveFile(pdfPath, "pdf");
		}
		finally
		{
			// Remove temp files
			File dir = new File(".");
			String[] files = dir.list(new PrefixFileFilter(baseFileName));
			for(int i = 0; i < files.length; i++)
			{
				FileUtils.deleteQuietly(new File(files[i]));
			}
		}
	}
}
