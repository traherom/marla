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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import problem.MarlaException;
import problem.Problem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import r.RProcessor.RecordMode;
import r.RProcessorException;
import resource.ConfigurationException;

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
			throw new ConfigurationException("LaTeX template '" + templatePath + "' XML file could not be read", ex);
		}

		return oldPath;
	}

	/**
	 * Sets the default LaTeX template to use for new exporters. May still be
	 * changed for individual instances
	 * @param newTemplatePath new default template to use
	 */
	public static void setDefaultTemplate(String newTemplatePath) throws ConfigurationException
	{
		defaultTemplate = newTemplatePath;

		// Ensure it at least exists
		if(!(new File(defaultTemplate)).exists())
			throw new ConfigurationException("LaTeX template '" + defaultTemplate + "' XML file does not exist");

		System.out.println("Using LaTeX export template at '" + defaultTemplate + "'");
	}

	/**
	 * Sets the path to the pdflatex binary file
	 * @param newPdfLatexPath New path to executable
	 */
	public static void setPdflatexPath(String newPdfLatexPath) throws ConfigurationException
	{
		pdflatexPath = newPdfLatexPath;

		// Ensure we could run this if wished
		//if(!(new File(pdflatexPath)).canExecute())
		//	throw new ConfigurationException("pdflatex could not be located at '" + defaultTemplate + "'");

		System.out.println("Using pdflatex binary at '" + pdflatexPath + "'");
	}

	/**
	 * Configures the defaults for exporters based on the given XML configuration
	 * @param configEl XML configuration element with settings as attributes
	 */
	public static void setConfig(Element configEl) throws ConfigurationException
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
	 * Exports the problem using either cleanExport() if an existing export at this location
	 * doesn't already exist or refreshExport() if there is one
	 * @param rnwPath Path for new Sweave file
	 */
	public String export(String rnwPath) throws LatexException, MarlaException
	{
		return refreshExport(rnwPath);
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
			throw new LatexException("Unable to move the temporary file to '" + toFile + "'");
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			// Process, making sure it's reset properly
			currentSub = null;
			processSequenceClean(templateXML, writer);

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
				else if(name.equals("if"))
					processIfClean(partEl, out);
				else if(name.equals("name"))
				{
					try
					{
						if(prob.getPersonName() != null)
							out.write(prob.getPersonName());
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
							out.write(prob.getChapter());
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
							out.write(prob.getSection());
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
							out.write(prob.getProblemNumber());
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
					out.write(prob.getShortCourse());
			}
			else if(courseNameType.equals("long"))
			{
				if(prob.getLongCourse() != null)
					out.write(prob.getLongCourse());
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

		// Stick in a newline. Sweave blocks must begin at the beginning of the line,
		// but we don't want to have to require that in the template
		sweaveBlock.append('\n');

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

				// Is the end result a plot?
				if(op.hasPlot())
					sweaveBlock.append("\n<<fig=T>>=\n");
				else
					sweaveBlock.append("\n<<>>=\n");

				sweaveBlock.append(op.getRCommands(true));

				sweaveBlock.append("@\n\n");
			}
		}
		else if(currentSub.getSolutionEnd() != null)
		{
			// Block beginning
			sweaveBlock.append("\n<<label=");
			sweaveBlock.append(currentSub.getSubproblemID());
			
			// Is the end result a plot?
			DataSource end = currentSub.getSolutionEnd();
			if(end instanceof Operation && ((Operation)end).hasPlot())
				sweaveBlock.append(", fig=T");

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

	public void processIfClean(Element ifEl, Writer out) throws LatexException, MarlaException
	{
		// Figure out what type of if we are and determine the truthiness of the statement
		boolean ifExprResult = false;
		String hasSub = ifEl.getAttributeValue("has_subproblems");

		if(hasSub != null)
		{
			boolean hasSubRequired = Boolean.parseBoolean(hasSub);
			boolean subExist = prob.getSubProblemCount() > 0;
			ifExprResult = (subExist == hasSubRequired);
		}
		else
			throw new LatexException("Type of if not recognized");

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
	 * Replaces existing R portions in an exported problem and leaves everything else
	 * untouched. If problem (or subproblem) statements have changed, nothing is done with
	 * them. It is the responsibility of the user to change those after the initial export.
	 * @param nnwPath Path for the new Sweave file
	 */
	public String refreshExport(String rnwPath) throws LatexException, MarlaException
	{
		// It must already exist or we just go ahead and do a clean export
		File exportFile = new File(rnwPath);
		if(!exportFile.isFile())
			return cleanExport(rnwPath);

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
	 * @param pdfPath Path for the newly created PDF file
	 * @return Path to the newly created PDF
	 */
	public String generatePDF(String pdfPath) throws LatexException, RProcessorException, MarlaException
	{
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

			try
			{
				// Create pdflatex instance
				ProcessBuilder procBuild = new ProcessBuilder(pdflatexPath, texPath);
				procBuild.redirectErrorStream(true);
				texProc = procBuild.start();
				texOutStream = new BufferedReader(new InputStreamReader(texProc.getInputStream()));
				texProc.getOutputStream().close();
			}
			catch(IOException ex)
			{
				throw new ConfigurationException("Unable to run '" + pdflatexPath + "'", ex);
			}

			try
			{
				// Read the output and save the important parts
				boolean output = (proc.getDebugMode() == RecordMode.FULL
						|| proc.getDebugMode() == RecordMode.OUTPUT_ONLY);

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
				throw new LatexException("Error occurred in reading output from pdflatex", ex);
			}
			catch(InterruptedException ex)
			{
				throw new LatexException("Interrupted while waiting for pdflatex to exit", ex);
			}

			// Ensure we actually succeeded. Check for the Sweave file error
			if(pdfOutput.contains(".sty' not found"))
			{
				// Sweave not tied into LaTeX properly
				throw new ConfigurationException("Sweave does not appear to be registered correctly with LaTeX");
			}

			// Get the output file name reported by pdflatex
			Pattern outfilePatt = Pattern.compile("^Output written on (.*\\.pdf)", Pattern.MULTILINE);
			Matcher outfileMatch = outfilePatt.matcher(pdfOutput);
			if(!outfileMatch.find())
				throw new LatexException("pdflatex failed to write PDF file");

			String tempPdfPath = outfileMatch.group(1);

			// Move the final PDF file
			return moveFile(tempPdfPath, pdfPath);
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
