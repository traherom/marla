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
package marla.ide.latex;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import marla.ide.problem.MarlaException;
import marla.ide.problem.Problem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import marla.ide.operation.Operation;
import marla.ide.operation.OperationInformation;
import marla.ide.problem.DataColumn;
import marla.ide.problem.DataSource;
import marla.ide.problem.SubProblem;
import marla.ide.r.RProcessor;
import marla.ide.r.RProcessor.RecordMode;
import marla.ide.r.RProcessorException;
import marla.ide.resource.Configuration.ConfigType;
import marla.ide.resource.ConfigurationException;

/**
 * @author Ryan Morehart
 */
public class LatexExporter
{
	/**
	 * Unless specified otherwise in the constructor, this path will be used
	 * as the template for new exporter instances
	 */
	private static String defaultTemplate = null;
	/**
	 * Path to the PDF LaTeX binary, used for PDF exports
	 */
	private static String pdfTexPath = null;
	/**
	 * Problem this exporter is working with
	 */
	private final Problem prob;
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
	 * Pattern to find a single slash in latex code. Here just it's precompiled
	 * An export will use this and the other replacement patterns a lot
	 */
	private static List<Object[]> latexReplacements = fillEscapeMap();
	
	private static List<Object[]> fillEscapeMap()
	{
		List<Object[]> l = new ArrayList<Object[]>();
		l.add(new Object[]{Pattern.compile("\\\\"), "\\\\backslash"});
		l.add(new Object[]{Pattern.compile("\\$"), "\\\\$"});
		l.add(new Object[]{Pattern.compile("%"), "\\\\%"});
		l.add(new Object[]{Pattern.compile("&"), "\\\\&"});
		l.add(new Object[]{Pattern.compile("\\{"), "\\\\{"});
		l.add(new Object[]{Pattern.compile("}"), "\\\\}"});
		l.add(new Object[]{Pattern.compile("#"), "\\\\#"});
		l.add(new Object[]{Pattern.compile("_"), "\\\\_"});
		l.add(new Object[]{Pattern.compile("-"), "\\\\--"});
		return l;
	}

	/**
	 * Creates a new Latex exporter for the given problem
	 * @param problem Problem to export to
	 */
	public LatexExporter(Problem problem) throws LatexException, ConfigurationException
	{
		this(problem, null);
	}

	/**
	 * Creates a new Latex exporter for the given problem, using the given template
	 * @param problem Problem to export to
	 * @param templatePath Path to the template to use for exporting.
	 */
	public LatexExporter(Problem problem, String templatePath) throws LatexException, ConfigurationException
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
	public final String setTemplate(String newTemplatePath) throws LatexException, ConfigurationException
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
			throw new ConfigurationException("LaTeX template '" + templatePath + "' XML file could not be read", ConfigType.TexTemplate, ex);
		}

		return oldPath;
	}

	/**
	 * Sets the default LaTeX template to use for new exporters. May still be
	 * changed for individual instances
	 * @param newTemplatePath new default template to use
	 * @return Previously set default template
	 */
	public static String setDefaultTemplate(String newTemplatePath) throws ConfigurationException
	{
		String oldTemplate = defaultTemplate;

		// Ensure it at least exists
		if(!(new File(newTemplatePath)).exists())
			throw new ConfigurationException("LaTeX template '" + newTemplatePath + "' XML file does not exist", ConfigType.TexTemplate);

		defaultTemplate = newTemplatePath;
		return oldTemplate;
	}

	/**
	 * Returns the current default LaTeX export template path
	 * @return path to export template
	 */
	public static String getDefaultTemplate()
	{
		return defaultTemplate;
	}

	/**
	 * Sets the path to the pdflatex binary file
	 * @param newPdfTexPath New path to executable
	 * @return Previously set path to pdflatex
	 */
	public static String setPdfTexPath(String newPdfTexPath) throws ConfigurationException
	{
		String oldPath = pdfTexPath;

		// Ensure we could run this if wished
		if(!newPdfTexPath.equals(oldPath))
		{
			boolean validInstall = false;
			
			try
			{
				// Get current files in dir. Any new files will be removed
				File currentDir = new File(".");
				List<File> originalFiles = Arrays.asList(currentDir.listFiles());

				// Test to see if pdflatex works by running a test file through it
				ProcessBuilder procBuild = new ProcessBuilder(newPdfTexPath);
				Process texProc = procBuild.start();
				BufferedReader texOutStream = new BufferedReader(new InputStreamReader(texProc.getInputStream()));

				// Write out the test tex
				String testTex = "\\documentclass{article}\\begin{document}Test\\end{document}";
				BufferedOutputStream out = new BufferedOutputStream(texProc.getOutputStream());
				out.write(testTex.getBytes(), 0, testTex.length());
				out.close();

				// Look for where it says that pdf output was produced
				validInstall = false;
				String line = texOutStream.readLine();
				while(line != null)
				{
					if(line.startsWith("Output written on"))
						validInstall = true;

					line = texOutStream.readLine();
				}

				// Good game boys, gg
				texProc.waitFor();
				texOutStream.close();

				// Clean up by removing any new files
				List<File> newFiles = new ArrayList<File>();
				newFiles.addAll(Arrays.asList(currentDir.listFiles()));
				newFiles.removeAll(originalFiles);
				for(File f : newFiles)
					FileUtils.deleteQuietly(f);
			}
			catch(IOException ex)
			{
				validInstall = false;
			}
			catch(InterruptedException ex)
			{
				validInstall = false;
			}

			if(!validInstall)
				throw new ConfigurationException("pdfTeX could not be located at '" + defaultTemplate + "'", ConfigType.PdfTex);
		}

		pdfTexPath = newPdfTexPath;
		return oldPath;
	}

	/**
	 * Returns the current pdfTeX path
	 * @return path to pdfTeX executable
	 */
	public static String getPdfTexPath()
	{
		return pdfTexPath;
	}

	/**
	 * Exports the problem using either cleanExport() if an existing export at this location
	 * doesn't already exist or refreshExport() if there is one
	 * @param rnwPath Path for new Sweave file
	 */
	public String export(String rnwPath) throws LatexException, MarlaException
	{
		return cleanExport(rnwPath);
	}

	/**
	 * Moves the temporary export files to the actual location
	 * @param fromPath File to move
	 * @param toPath Path to move the file to
	 * @returns New path of the moved file
	 */
	private String moveFile(String fromPath, String toPath) throws LatexException
	{
		// Figure out file paths
		File fromFile = new File(fromPath);
		File toFile = new File(toPath);

		try
		{
			// Move file
			FileUtils.deleteQuietly(toFile);
			FileUtils.moveFile(fromFile, toFile);

			return toFile.getPath();
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to move the temporary file '" + fromFile + "' to '" + toFile + "'");
		}
	}

	/**
	 * Completely exports the problem as files
	 * @param rnwPath Path at which to save the newly produced Sweave file
	 * @return Path to the main LaTeX file that has been exported
	 */
	public String cleanExport(String rnwPath) throws LatexException, MarlaException
	{
		// Export to the temporary file and them move
		String tempFile = cleanTempExport();
		return moveFile(tempFile, rnwPath);
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
			tempFile.deleteOnExit();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			// Process, making sure it's reset properly
			currentSub = null;
			processSequenceClean(templateXML.getChild("main"), writer);

			// Close it all out
			writer.close();
			return tempFile.getAbsolutePath();
		}
		catch(IOException ex)
		{
			throw new LatexException("File error occured during exporting", ex);
		}
	}

	private void processSequenceClean(Element el, Writer out) throws LatexException, MarlaException
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
				String name = partEl.getName();

				if(name.equals("loop"))
					processLoopClean(partEl, out);
				else if(name.equals("statement"))
					processStatementClean(partEl, out);
				else if(name.equals("solution"))
					processSolutionClean(partEl, out);
				else if(name.equals("conclusion"))
					processConclusionClean(partEl, out);
				else if(name.equals("data"))
					processDataClean(partEl, out);
				else if(name.equals("if"))
					processIfClean(partEl, out);
				else if(name.equals("name"))
				{
					try
					{
						if(prob.getPersonName() != null)
							out.write(escapeLatex(prob.getPersonName()));
					}
					catch(IOException ex)
					{
						throw new LatexException("Unable to write person name during export", ex);
					}
				}
				else if(name.equals("class"))
					processClassClean(partEl, out);
				else if(name.equals("chapter"))
				{
					try
					{
						if(prob.getChapter() != null)
							out.write(escapeLatex(prob.getChapter()));
					}
					catch(IOException ex)
					{
						throw new LatexException("Unable to write problem chapter during export", ex);
					}
				}
				else if(name.equals("section"))
				{
					try
					{
						if(prob.getSection() != null)
							out.write(escapeLatex(prob.getSection()));
					}
					catch(IOException ex)
					{
						throw new LatexException("Unable to write problem section during export", ex);
					}
				}
				else if(name.equals("probnum"))
				{
					try
					{
						if(prob.getProblemNumber() != null)
							out.write(escapeLatex(prob.getProblemNumber()));
					}
					catch(IOException ex)
					{
						throw new LatexException("Unable to write problem number during export", ex);
					}
				}
				else
					throw new LatexException("'" + name + "' is not a supported element in template XML yet");
			}
			else if(partObj instanceof Text)
			{
				try
				{
					// Remove whitespace at the front of lines and then dump out
					Text partText = (Text) partObj;
					String[] lines = partText.getText().split("\n");
					for(String line : lines)
					{
						String trimmed = line.trim() + "\n";
						out.write(trimmed, 0, trimmed.length());
					}
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
			processSequenceClean(el, out);
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
					out.write(escapeLatex(prob.getShortCourse()));
			}
			else if(courseNameType.equals("long"))
			{
				if(prob.getLongCourse() != null)
					out.write(escapeLatex(prob.getLongCourse()));
			}
			else
				throw new LatexException("Course name type '" + courseNameType + "' is not supported");
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
			statement = escapeLatex(prob.getStatement());
		}
		else
		{
			// Get statement from the subproblem we are on
			statement = escapeLatex(currentSub.getStatement());
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

	private void processSolutionClean(Element el, Writer out) throws LatexException, MarlaException
	{
		// Are we supposed to show the raw R code?
		boolean withR = Boolean.parseBoolean(el.getAttributeValue("rcode", "false"));

		// Stick in a newline. Sweave blocks must begin at the beginning of the line,
		// but we don't want to have to require that in the template
		StringBuilder solutionBlock = new StringBuilder();
		solutionBlock.append('\n');

		// Find all the operations that are the "solution" right now.
		List<Operation> solOps = null;
		if(currentSub == null)
		{
			// For each dataset, get its leaf operations and get the chains
			// running back up to the dataset
			solOps = new ArrayList<Operation>();
			for(int i = 0; i < prob.getDataCount(); i++)
			{
				DataSource top = prob.getData(i);
				solOps.addAll(SubProblem.getOperationChain(top));
			}
		}
		else if(currentSub.hasSolution())
		{
			// Get all operations that are port of the current subproblem
			solOps = currentSub.getSolutionChain();
		}

		if(solOps != null && !solOps.isEmpty())
		{
			// Walk through each operation in this solution
			for(Operation op : solOps)
			{
				if(op.hasRemark())
				{
					solutionBlock.append("\n\n\\par ");
					solutionBlock.append(escapeLatex(op.getRemark()));
				}

				solutionBlock.append("\n\\par ");
				solutionBlock.append(summarizeOperation(op, Integer.parseInt(el.getAttributeValue("maxlen", "7")), withR));
			}
		}
		else
			solutionBlock.append("No solution yet\n");

		try
		{
			// Write it out
			String sweave = solutionBlock.toString();
			out.write(sweave, 0, sweave.length());
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write solution to export file");
		}
	}

	/**
	 * Takes the given operation and summarizes it, including it's name,
	 * input parameters, the "main R" given by the operation, and the columns
	 * it added to the data.
	 * @param op Operation to summarize
	 * @param abbrvLen Maximum number of new elements to display for columns. Must
	 *		be 3 or greater.
	 * @param includeR Should the R code be included in the summary?
	 * @return Latex string that gives information about the operation
	 */
	private String summarizeOperation(Operation op, int abbrvLen, boolean includeR) throws LatexException, MarlaException
	{
		if(abbrvLen < 3)
			throw new LatexException("Abbreviation length for summarizing operations must be 3 or greater (" + abbrvLen + ") given");

		StringBuilder sb = new StringBuilder();

		// Because data might be very long, limit the maximum number
		// of elements shown by showing the first three and last three
		int dispColLen = op.getNewColumnLength();
		int newColLen = dispColLen;
		if(newColLen > abbrvLen)
			dispColLen = abbrvLen;

		// Start table
		sb.append("\\begin{tabular}{ l || l ");
		for(int i = 1; i < dispColLen; i++)
			sb.append(" l");
		sb.append("}\n");

		// Operation name
		sb.append("\\multicolumn{1}{l||}{{\\bf ");
		sb.append(escapeLatex(op.getName()));
		sb.append("}} & ");

		// Parameters
		List<OperationInformation> params = op.getRequiredInfoPrompt();
		if(!params.isEmpty())
		{
			sb.append("\\multicolumn{");
			if(dispColLen > 0)
				sb.append(dispColLen);
			else
				sb.append('1');
			sb.append("}{l}{ \\begin{tabular}{");

			for(int i = 0; i < params.size(); i++)
				sb.append("l ");
			sb.append("} ");

			for(int i = 0; i < params.size(); i++)
			{
				OperationInformation param = params.get(i);

				sb.append("{\\it ");
				sb.append(escapeLatex(param.getPrompt()));
				sb.append(":} ");
				sb.append(escapeLatex(param.getAnswer()));

				if(i != params.size() - 1)
					sb.append(" & ");
			}

			sb.append("\\end{tabular} }\\\\\n");
		}
		else
			sb.append("{\\it No Parameters}\\\\\n");

		// New data
		sb.append("\\hline\n");
		List<DataColumn> newCols = op.getNewColumns();
		for(DataColumn dc : newCols)
		{
			sb.append(escapeLatex(dc.getName()));
			sb.append(" & ");

			// Show everything or abbreviate?
			if(dispColLen >= dc.size())
			{
				for(int i = 0; i < dc.size(); i++)
				{
					sb.append(escapeLatex(dc.get(i)));

					if(i != dc.size() - 1)
						sb.append(" & ");
				}
			}
			else
			{
				int halfSize = dispColLen / 2;

				// Beginning elements
				for(int i = 0; i < halfSize; i++)
				{
					sb.append(escapeLatex(dc.get(i)));
					sb.append(" & ");
				}

				// Ellipse
				sb.append(" $\\cdots$ & ");

				// Ending elements
				for(int i = dc.size() - halfSize; i < dc.size(); i++)
				{
					sb.append(escapeLatex(dc.get(i)));

					if(i != dc.size() - 1)
						sb.append(" & ");
				}
			}

			sb.append("\\\\\n");
		}

		// Plot?
		if(op.hasPlot())
		{
			sb.append("\\multicolumn{");
			if(dispColLen > 0)
				sb.append(dispColLen + 1);
			else
				sb.append('2');
			sb.append("}{|c|}{\n");

			sb.append("<<echo=F,fig=T>>=\n");
			sb.append(op.getRCommands());
			sb.append("@\n");

			sb.append("}\\\\\n\\hline\n");
		}

		/* TBD figure out a good way to include R if desired
		if(includeR)
		{
			sb.append("\\multicolumn{");
			if(dispColLen > 0)
				sb.append(dispColLen + 1);
			else
				sb.append('2');
			sb.append("}{|p{5cm}|}{\n");

			sb.append("<<>>=\n");
			sb.append(op.getRCommands(false));
			sb.append("@\n");
		
			sb.append("}\\\\\n\\hline\n");
		}
		 */

		// End table
		sb.append("\\end{tabular}\n");

		return sb.toString();
	}

	private void processConclusionClean(Element el, Writer out) throws LatexException, MarlaException
	{
		StringBuilder sb = new StringBuilder();
		if(currentSub == null)
			sb.append(escapeLatex(prob.getConclusion()));
		else
			sb.append(escapeLatex(currentSub.getConclusion()));

		try
		{
			// Write it out
			String sweave = sb.toString();
			out.write(sweave, 0, sweave.length());
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write solution to export file");
		}
	}

	private void processDataClean(Element el, Writer out) throws LatexException, MarlaException
	{
		// Are we showing start or end data?
		String type = el.getAttributeValue("type", "start");
		boolean isStartDS = true;
		if(type.equals("start"))
			isStartDS = true;
		else if(type.equals("end"))
			isStartDS = false;
		else
			throw new LatexException("Unrecognized type of data '" + type + "' to display in LaTeX template");

		// Grab all the DataSources we need to show
		List<DataSource> dsToShow = null;
		if(currentSub == null)
		{
			if(isStartDS)
			{
				// All DataSets
				dsToShow = new ArrayList<DataSource>();
				for(int i = 0; i < prob.getDataCount(); i++)
					dsToShow.add(prob.getData(i));
			}
			else
			{
				// All leaf operations
				dsToShow = new ArrayList<DataSource>();
				for(int i = 0; i < prob.getDataCount(); i++)
					dsToShow.addAll(prob.getData(i).getAllLeafOperations());
			}
		}
		else
		{
			if(isStartDS)
				dsToShow = currentSub.getStartSteps();
			else
				dsToShow = currentSub.getEndSteps();
		}

		// Limit the width of the DataSources in order to wrap nicely
		// around the page
		int maxColCount = Integer.parseInt(el.getAttributeValue("maxcols", "6"));
		int colCount = 0;
		boolean insideCenter = false;

		// Create latex array for each DataSource
		StringBuilder sb = new StringBuilder();
		for(DataSource ds : dsToShow)
		{
			colCount += ds.getColumnCount();
			if(colCount > maxColCount)
			{
				colCount = ds.getColumnCount();
				
				if(insideCenter)
					sb.append("\\end{center}\n");

				insideCenter = false;
			}
			
			if(!insideCenter)
			{
				insideCenter = true;
				sb.append("\\begin{center}\n");
			}

			if(isStartDS)
				sb.append(dataToLatex("Starting Data : " + ds.getName(), ds.getColumnLength(), ds.getColumns()));
			else
				sb.append(dataToLatex("Final Data : " + ds.getName(), ds.getColumnLength(), ds.getColumns()));
		}

		if(insideCenter)
			sb.append("\\end{center}\n");

		try
		{
			// Write it out
			String data = sb.toString();
			out.write(data, 0, data.length());
		}
		catch(IOException ex)
		{
			throw new LatexException("Unable to write data to export file");
		}
	}

	private String dataToLatex(DataSource ds) throws MarlaException
	{
		return dataToLatex(ds.getName(), ds.getColumnLength(), ds.getColumns());
	}

	private String dataToLatex(String dsName, int maxLen, List<DataColumn> columns)
	{
		if(columns.isEmpty())
			return "";

		StringBuilder sb = new StringBuilder();

		// Start tabular
		sb.append("\\begin{tabular}{r || ");
		for(int i = 0; i < columns.size(); i++)
			sb.append("r | ");
		sb.append("}\n");

		// DataSource name
		if(dsName != null)
		{
			sb.append("\\multicolumn{");
			sb.append(columns.size() + 1);
			sb.append("}{c}{\\bf ");
			sb.append(escapeLatex(dsName));
			sb.append("} \\\\\n \\cline{2-");
			sb.append(columns.size() + 1);
			sb.append("}\n");
		}
		
		// Column names
		sb.append("   & ");
		for(int i = 0; i < columns.size(); i++)
		{
			sb.append(escapeLatex(columns.get(i).getName()));

			// Don't end the row with the cell separator
			if(i + 1 < columns.size())
				sb.append("& ");
		}
		sb.append("\\\\ \\hline\n");

		// Data
		for(int i = 0; i < maxLen; i++)
		{
			// Index in DataColumn
			sb.append(i + 1);
			sb.append(" & ");

			for(int j = 0; j < columns.size(); j++)
			{
				// Ensure this column extends this far
				DataColumn dc = columns.get(j);
				if(dc.size() > i)
					sb.append(escapeLatex(dc.get(i)));

				// Don't end the row with the cell separator
				if(j + 1 < columns.size())
					sb.append(" & ");
			}

			sb.append("\\\\\n");
		}

		sb.append("\\end{tabular}\n");

		return sb.toString();
	}

	private void processIfClean(Element ifEl, Writer out) throws LatexException, MarlaException
	{
		// Process if questions and determine the truthiness of the statement
		// Short circuit, so if we ever go false just stop thinking about it
		boolean ifExprResult = true;

		String hasSub = ifEl.getAttributeValue("has_subproblems");
		if(hasSub != null)
		{
			boolean hasSubRequired = Boolean.parseBoolean(hasSub);
			boolean subExist = prob.getSubProblemCount() > 0;
			ifExprResult = (subExist == hasSubRequired);
		}

		String hasConc = ifEl.getAttributeValue("has_conclusion");
		if(ifExprResult && hasConc != null)
		{
			boolean hasConcRequired = Boolean.parseBoolean(hasConc);
			boolean concExist = false;
			if(currentSub == null)
				concExist = (prob.getConclusion() != null);
			else
				concExist = (currentSub.getConclusion() != null);
			ifExprResult = (concExist == hasConcRequired);
		}

		// Run then then/else blocks as appropriate
		if(ifExprResult)
		{
			// Is there a then?
			Element thenEl = ifEl.getChild("then");
			if(thenEl != null)
				processSequenceClean(thenEl, out);
		}
		else
		{
			// Is there an else?
			Element elseEl = ifEl.getChild("else");
			if(elseEl != null)
				processSequenceClean(elseEl, out);
		}
	}

	/**
	 * Takes the given object, converts it to a string, then escapes all LaTeX
	 * inside it
	 * @param dirty String/stringable object which needs latex escaped
	 * @return String with latex escaped properly
	 */
	private String escapeLatex(Object dirty)
	{
		return escapeLatex(dirty.toString());
	}

	/**
	 * Takes the given string and escapes all LaTeX inside it
	 * @param dirty String which needs latex escaped
	 * @return String with latex escaped properly
	 */
	private String escapeLatex(String dirty)
	{
		String clean = dirty;

		// Order is important here. If we replace slashes later, then
		// the others won't properly escape
		for(Object[] patt : latexReplacements)
		{
			Matcher m = ((Pattern)patt[0]).matcher(clean);
			clean = m.replaceAll((String)patt[1]);
		}

		return clean;
	}

	/**
	 * Does a clean export into a temporary directory, then runs the result through pdflatex.
	 * The generated PDF file is then copied to the export location and the path to that file
	 * is returned.
	 * @param pdfPath Path for the newly created PDF file
	 * @return Path to the newly created PDF
	 */
	public String generatePDF(String pdfPath) throws LatexException, RProcessorException, MarlaException
	{
		// Get current files in dir. Any new files will be removed
		File currentDir = new File(".");
		List<File> originalFiles = new ArrayList<File>();
		originalFiles.addAll(Arrays.asList(currentDir.listFiles()));

		// Create the rnw
		String rnwPath = cleanTempExport();
		String baseFileName = new File(rnwPath).getName().replace(".rnw", "");

		try
		{
			// Sweave it
			String sweaveOutput = null;
			RProcessor proc = RProcessor.getInstance();
			try
			{
				sweaveOutput = proc.execute("Sweave('" + rnwPath.replaceAll("\\\\", "/") + "')");
			}
			catch(RProcessorException ex)
			{
				throw new LatexException("Unable to sweave file '" + rnwPath + "', likely an error in the template '" + templatePath + "'", ex);
			}

			// Pull out tex file generated by Sweave
			Pattern texPatt = Pattern.compile("^You can now run LaTeX on '(.*\\.tex)'", Pattern.MULTILINE);
			Matcher texMatcher = texPatt.matcher(sweaveOutput);
			if(!texMatcher.find())
				throw new LatexException("Unable to Sweave rnw file, likely a template error");

			String texPath = texMatcher.group(1);

			// Run through pdflatex, save results here
			Process texProc = null;
			BufferedReader texOutStream = null;
			int exitVal = 0;
			String pdfOutput = null;

			// Create pdflatex instance
			System.out.println("Running '" + texPath + "' through '" + pdfTexPath + "'");
			ProcessBuilder procBuild = new ProcessBuilder(pdfTexPath, texPath);
			procBuild.directory(new File(System.getProperty("java.io.tmpdir")));
			procBuild.redirectErrorStream(true);
			
			try
			{
				texProc = procBuild.start();
				texOutStream = new BufferedReader(new InputStreamReader(texProc.getInputStream()));
				texProc.getOutputStream().close();
			}
			catch(IOException ex)
			{
				throw new ConfigurationException("Unable to run '" + pdfTexPath + "'", ConfigType.PdfTex, ex);
			}

			try
			{
				// Read the output and save the important parts
				boolean output = (RProcessor.getDebugMode() == RecordMode.FULL
						|| RProcessor.getDebugMode() == RecordMode.OUTPUT_ONLY);

				StringBuilder sb = new StringBuilder();
				String line = texOutStream.readLine();
				while(line != null)
				{
					if(output)
						System.out.println(line);
					
					sb.append(line);
					sb.append('\n');
					line = texOutStream.readLine();
				}

				pdfOutput = sb.toString();

				// Close process
				texProc.waitFor();
				texOutStream.close();
				exitVal = texProc.exitValue();
			}
			catch(IOException ex)
			{
				throw new LatexException("Error occurred in reading output from pdfTeX", ex);
			}
			catch(InterruptedException ex)
			{
				throw new LatexException("Interrupted while waiting for pdfTeX to exit", ex);
			}

			// Ensure we actually succeeded. Check for the Sweave file error
			if(pdfOutput.contains(".sty' not found"))
			{
				// Sweave not tied into LaTeX properly
				throw new LatexException("Sweave does not appear to be registered correctly with LaTeX");
			}

			// Get the output file name reported by pdflatex
			Pattern outfilePatt = Pattern.compile("^Output written on (.*\\.pdf)", Pattern.MULTILINE);
			Matcher outfileMatch = outfilePatt.matcher(pdfOutput);
			if(!outfileMatch.find())
				throw new LatexException("pdfTeX failed to write PDF file");

			String tempPdfPath = procBuild.directory() + "/" + outfileMatch.group(1);

			// Move the final PDF file and ensure we don't autoremove it (if
			// the user were to request it built in our working dir)
			String finalPath = moveFile(tempPdfPath, pdfPath);
			originalFiles.add(new File(finalPath));
			return finalPath;
		}
		finally
		{
			// Clean up by removing any new files
			List<File> newFiles = new ArrayList<File>();
			newFiles.addAll(Arrays.asList(currentDir.listFiles()));
			newFiles.removeAll(originalFiles);
			for(File f : newFiles)
			{
				if(!FileUtils.deleteQuietly(f))
					f.deleteOnExit();
			}
		}
	}
}
