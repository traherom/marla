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
package marla.opedit.operation;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import marla.ide.operation.OperationXML;
import marla.ide.problem.MarlaException;
import marla.opedit.gui.Domain;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author Ryan Morehart
 */
public class OperationXMLEditable extends OperationXML
{
	/**
	 * STOP WHINING JAVA!
	 */
	private static final long serialVersionUID = -432343533907498L;
	/**
	 * Parent file that we belong to
	 */
	private OperationFile parent = null;
	/**
	 * XML we have, whether it's parsable as an operation or not
	 */
	private Element opEl = null;
	/**
	 * Inner XML we have
	 */
	private String innerXmlStr = null;
	/**
	 * Not null if an exception has been detected and the XML contains errors
	 */
	private OperationEditorException lastError = null;

	/**
	 * Constructs a new editable xml operation which belows to the given parent
	 * @param parent OperationFile we belong to
	 */
	public OperationXMLEditable(OperationFile parent)
	{
		this.parent = parent;
	}

	/**
	 * Copy constructor
	 * @param org Original operation to copy
	 */
	protected OperationXMLEditable(OperationXMLEditable org)
	{
		super(org);
		
		lastError = org.lastError;
		innerXmlStr = org.innerXmlStr;
		opEl = (Element)org.opEl.clone();
	}
	
	@Override
	public void markUnsaved()
	{
		// Ignore
	}

	@Override
	public void setConfiguration(Element newConfig) 
	{
		try
		{
			opEl = newConfig;
			
			super.setConfiguration((Element)newConfig.clone());

			// Pull out string version of the inner xml if needed
			if(innerXmlStr == null)
			{
				Format formatter = Format.getPrettyFormat();
				XMLOutputter xml = new XMLOutputter(formatter);
				innerXmlStr = xml.outputString(opEl.getContent());
			}

			if(parent != null)
				parent.markUnsaved();

			lastError = null;
		}
		catch(MarlaException ex)
		{
			lastError = new OperationEditorException(ex.getMessage(), ex);
			throw lastError;
		}
	}

	/**
	 * Returns the currently set configuration for this operation (whether
	 * valid or not)
	 * @return XML containing details of operation
	 */
	@Override
	public Element getConfiguration()
	{
		// TODO name could update without making it here
		return opEl;
	}

	/**
	 * Returns the currently known last working configuration
	 * @return XML containing details of operation
	 */
	public Element getLastWorkingConfiguration()
	{
		return super.getConfiguration();
	}

	/**
	 * Returns true the operation contains errors of any kind
	 * @return true if error, false otherwise
	 */
	public boolean hasError()
	{
		return lastError != null;
	}

	/**
	 * Returns the most recently detected error in the XML
	 * @return Exception containing details of error
	 */
	public OperationEditorException getError()
	{
		return lastError;
	}

	/**
	 * Gets the category this operation is currently in
	 * @return Category operation is in. Blank if there is none set
	 */
	public String getCategory()
	{
		return opEl.getAttributeValue("category", "");
	}

	/**
	 * Sets the category for this operation
	 * @param newCat New category to place this operation in. Setting it to null
	 *		or blank removes the category
	 * @return Previously set category
	 */
	public String setCategory(String newCat) 
	{
		String old = opEl.getAttributeValue("category");

		if(parent != null)
			parent.changeBeginning();
			
		if(newCat != null && !newCat.isEmpty())
			opEl.setAttribute("category", newCat);
		else
			opEl.removeAttribute("category");

		setConfiguration(opEl);

		return old;
	}

	/**
	 * Sets the name for this operation
	 * @param newName New name for this operation
	 * @return Previously set name
	 */
	public String setEditableName(String newName) 
	{
		String old = opEl.getAttributeValue("name");

		if(parent != null)
			parent.changeBeginning();
			
		if(newName != null && !newName.isEmpty())
			opEl.setAttribute("name", newName);
		else
			throw new OperationEditorException("Operation name must be specified");

		setConfiguration(opEl);

		return old;
	}

	/**
	 * Changes whether this operation produces a plot or not
	 * @param newVal true if it should produce a plot, false otherwise
	 * @return previously set value for plot producing
	 */
	public boolean setHasPlot(boolean newVal)
	{
		boolean old = hasPlot();

		if(parent != null)
			parent.changeBeginning();
		
		if(newVal)
			opEl.setAttribute("plot", "true");
		else
			opEl.removeAttribute("plot");

		setConfiguration(opEl);

		return old;
	}

	/**
	 * String version of the inner XML powering this operation
	 * @return Current XML
	 */
	public String getInnerXML() 
	{
		return innerXmlStr;
	}

	/**
	 * Sets the parent for this operation. Does not actually add it to the file,
	 * only changes the parent pointer!
	 * @param newParent OperationFile to use as parent
	 */
	public void setParentFile(OperationFile newParent)
	{
		parent = newParent;
	}
	
	/**
	 * Changes the XML that the operation contains internally.
	 * @param newXMLStr New XML to parse and use for operation guts
	 * @return Previously set XML
	 */
	public String setInnerXML(String newXMLStr) 
	{
		String oldXML = innerXmlStr;
		innerXmlStr = newXMLStr;

		// Stick parsed version into opEl
		Element el = strToXml("<wrapper>" + innerXmlStr + "</wrapper>");
		opEl.removeContent();
		opEl.addContent(el.cloneContent());

		// And use these settings
		setConfiguration(opEl);

		return oldXML;
	}

	@Override
	public OperationXMLEditable clone()
	{
		return new OperationXMLEditable(this);
	}

	private Element strToXml(String xmlStr)
	{
		try
		{
			StringReader sr = new StringReader(xmlStr);
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(sr);
			return doc.getRootElement();
		}
		catch(JDOMException ex)
		{
			throw new OperationEditorException("XML is invalid: " + ex.getMessage(), ex);
		}
		catch(IOException ex)
		{
			Domain.logger.add(ex);
			throw new OperationEditorException("IO error setting, shouldn't happen: " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Override things we don't want to have available. Hackish, I know
	 */
	public static String setPrimaryXMLPath(String newPath)
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static String getPrimaryXMLPath()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static List<String> setUserXMLPaths(List<String> newPaths)
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static List<String> getUserXMLPaths()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static void clearXMLOps()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static void loadXML()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static OperationXML createOperation(String opName) 
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static Map<String, List<String>> getAvailableOperationsCategorized()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static List<String> getAvailableOperations()
	{
		throw new UnsupportedOperationException("Blocked");
	}
}
