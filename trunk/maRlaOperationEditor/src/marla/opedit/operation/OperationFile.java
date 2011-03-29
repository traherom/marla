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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class OperationFile
{
	/**
	 * Location to save operation XML to
	 */
	private final String xmlPath;
	/**
	 * Operations in this file
	 */
	private final List<OperationXMLEditable> ops = new ArrayList<OperationXMLEditable>();

	/**
	 * Creates a new operation file pointed at the given location.
	 * @param savePath Path to XML file
	 */
	public static OperationFile createNew(String savePath)
	{
		try
		{
			// Build new file
			String newOpFileContents = "<?xml version='1.0' ?><operations></operations>";
			BufferedWriter bw = new BufferedWriter(new FileWriter(savePath));
			bw.write(newOpFileContents, 0, newOpFileContents.length());
			bw.close();
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Unable to write new file to '" + savePath + "'", ex);
		}

		// And return operation file pointed to new file
		return new OperationFile(savePath);
	}

	/**
	 * Opens an operation file pointed at the given location.
	 * @param savePath Path to XML file
	 */
	public OperationFile(String savePath)
	{
		xmlPath = savePath;

		if(savePath != null)
			loadOperations();
	}

	/**
	 * (Re)loads the current XML file operations
	 */
	private void loadOperations()
	{
		try
		{
			// Make sure we know where we're looking
			if(xmlPath == null)
				throw new OperationEditorException("XML file for operations has not been specified");

			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(xmlPath);
			Element operationXML = doc.getRootElement();

			// Pull each separate operation out
			ops.clear();
			for(Object opObj : operationXML.getChildren("operation"))
			{
				Element opEl = (Element)opObj;

				OperationXMLEditable op = new OperationXMLEditable();
				ops.add(op);

				try
				{
					op.setConfiguration(opEl);
				}
				catch(OperationEditorException ex)
				{
					// Ignore
				}
			}
		}
		catch(JDOMException ex)
		{
			throw new OperationEditorException("Operation XML file '" +xmlPath + "' is invalid XML. Edit it manually before loading.", ex);
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Unable to read the operation XML file '" + xmlPath + "'", ex);
		}
	}

	/**
	 * Gets the names in the current operations in this file
	 * @return List of the op names in the current file
	 */
	public List<String> getOperationNames()
	{
		List<String> names = new ArrayList<String>(ops.size());
		for(OperationXMLEditable op : ops)
			names.add(op.getName());
		return names;
	}

	/**
	 * Adds a new operation to this file. Name is automatically created
	 * @return Newly added operation
	 */
	public OperationXMLEditable addOperation() throws OperationEditorException
	{
		return addOperation(getUniqueName());
	}
	
	/**
	 * Adds a new operation to this file with the given name
	 * @param operationName Name of the new operation
	 * @return Newly added operation
	 */
	public OperationXMLEditable addOperation(String operationName) throws OperationEditorException
	{
		// Ensure it's a unique name
		if(getOperationNames().contains(operationName))
			throw new OperationEditorException("Duplicate operation name '" + operationName + "' not allowed");

		OperationXMLEditable newOp = new OperationXMLEditable();
		ops.add(newOp);
		
		// Create XML for basic operation
		Element newOpEl = new Element("operation");
		newOpEl.setAttribute("name", operationName);
		newOpEl.addContent(new Element("displayname"));
		newOpEl.addContent(new Element("computation"));

		try
		{
			newOp.setConfiguration(newOpEl);
		}
		catch(OperationEditorException ex)
		{
			// This shouldn't happen, means our template is messed up
			Domain.logger.add(ex);
		}

		return newOp;
	}

	/**
	 * Removes the given operation via name
	 * @param name Name of the operation to remove
	 * @return Removed operation
	 */
	public OperationXMLEditable removeOperation(String name)
	{
		return removeOperation(getOperation(name));
	}

	/**
	 * Removes the given operation
	 * @param op Operation to remove from file
	 * @return Removed operation
	 */
	public OperationXMLEditable removeOperation(OperationXMLEditable op)
	{
		ops.remove(op);
		return op;
	}

	/**
	 * Returns the operation with the given name
	 * @param name Operation name to search for
	 * @return Operation to find, null if it doesn't exist
	 */
	public OperationXMLEditable getOperation(String name)
	{
		for(OperationXMLEditable op : ops)
		{
			if(op.getName().equals(name))
				return op;
		}

		return null;
	}

	/**
	 * Returns a unique name for a new operation
	 * @return Name for operation
	 */
	public String getUniqueName()
	{
		List<String> names = getOperationNames();
		String newName = "New Operation ";
		int i = 1;
		while(names.contains(newName + i))
			i++;

		return newName + i;
	}

	/**
	 * Saves the file to disk
	 */
	public void save() throws OperationEditorException
	{
		Element rootEl = new Element("operations");
		for(OperationXMLEditable op : ops)
			rootEl.addContent((Element)op.getConfiguration().clone());

		try
		{
			// Output to file
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(xmlPath));
			BufferedWriter outputStream = new BufferedWriter(os);

			Document doc = new Document(rootEl);
			Format formatter = Format.getPrettyFormat();
			formatter.setEncoding(os.getEncoding());
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Problem occured writing to file during save", ex);
		}
	}

	/**
	 * Returns the path of the XML file.
	 *
	 * @return The path of the XML file.
	 */
	public String toString()
	{
		return xmlPath;
	}
}
