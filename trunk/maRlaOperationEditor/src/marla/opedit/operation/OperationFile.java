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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public final class OperationFile
{
	/**
	 * Location to save operation XML to
	 */
	private String xmlPath;
	/**
	 * Operations in this file
	 */
	private final List<OperationXMLEditable> ops = new ArrayList<OperationXMLEditable>();
	/**
	 * Denotes if this file has unsaved changes
	 */
	private boolean isChanged = false;

	/**
	 * Creates a new operation file pointed at the given location.
	 * @param savePath Path to XML file
	 */
	public static OperationFile createNew(String savePath)
	{
		try
		{
			// Output to file
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(savePath));
			BufferedWriter outputStream = new BufferedWriter(os);

			Document doc = new Document(new Element("operations"));
			Format formatter = Format.getPrettyFormat();
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Unable to write new file to '" + savePath + "'", ex);
		}

		// And return operation file pointed to new file
		OperationFile newFile = new OperationFile(savePath);
		newFile.markUnsaved();
		return newFile;
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
	 * Copy constructor
	 * @param org File to copy
	 */
	public OperationFile(OperationFile org)
	{
		xmlPath = org.xmlPath;
		isChanged = org.isChanged;
		
		for(OperationXMLEditable op : org.ops)
			addOperation(op.clone());
	}

	/**
	 * Sets the path to save file to
	 * @param newPath New path to save to
	 * @return Previously set path
	 */
	public String setFilePath(String newPath)
	{
		String old = xmlPath;
		xmlPath = newPath;
		return old;
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

			// Load file
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(xmlPath);
			Element operationXML = doc.getRootElement();

			// Ensure it's actually an operations file
			if(!operationXML.getName().equals("operations"))
				throw new OperationEditorException("'" + xmlPath + "' does not appear to be an operations file");

			// Pull each separate operation out
			ops.clear();
			for(Object opObj : operationXML.getChildren("operation"))
			{
				Element opEl = (Element)opObj;

				OperationXMLEditable op = new OperationXMLEditable(this);
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

			markSaved();
		}
		catch(JDOMException ex)
		{
			throw new OperationEditorException("Operation XML file '" + xmlPath + "' is invalid XML. Edit it manually before loading.", ex);
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
		
		Collections.sort(names);
		
		return names;
	}

	/**
	 * Adds a new operation to this file. Name is automatically created
	 * @return Newly added operation
	 */
	public OperationXMLEditable addOperation()
	{
		return addOperation(getUniqueName());
	}
	
	/**
	 * Adds a new operation to this file with the given name
	 * @param operationName Name of the new operation
	 * @return Newly added operation
	 */
	public OperationXMLEditable addOperation(String operationName)
	{
		// Ensure it's a unique name
		if(getOperationNames().contains(operationName))
			throw new OperationEditorException("Duplicate operation name '" + operationName + "' not allowed");

		OperationXMLEditable newOp = new OperationXMLEditable(this);
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

		markUnsaved();
		return newOp;
	}
	
	/**
	 * Adds an exsting operation to this file
	 * @param op Operation to add
	 * @return Newly added operation
	 */
	public OperationXMLEditable addOperation(OperationXMLEditable op)
	{
		// Ensure it's a unique name
		if(getOperationNames().contains(op.getName()))
			throw new OperationEditorException("Duplicate operation name '" + op.getName() + "' not allowed");

		op.setParentFile(this);
		ops.add(op);
		
		markUnsaved();
		
		return op;
	}

	/**
	 * Replaces the operation with the given name with the new operation
	 * @param oldName Name of operation to replace 
	 * @param newOp Operation to replace the named operation with
	 * @return Operation that was removed
	 */
	public OperationXMLEditable replaceOperation(String oldName, OperationXMLEditable newOp)
	{
		int index = getOperationIndex(oldName);
		OperationXMLEditable oldOp = ops.get(index);
		ops.set(index, newOp);
		return oldOp;
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
		markUnsaved();
		return op;
	}

	/**
	 * Returns the operation with the given name
	 * @param name Operation name to search for
	 * @return Operation to find, null if it doesn't exist
	 */
	public OperationXMLEditable getOperation(String name)
	{
		int index = getOperationIndex(name);
		if(index != -1)
			return ops.get(index);
		else	
			return null;
	}
	
	/**
	 * Returns the operation with the given name
	 * @param name Operation name to search for
	 * @return index of the operation with the given name
	 */
	public int getOperationIndex(String name)
	{
		for(int i = 0; i < ops.size(); i++)
		{
			if(ops.get(i).getName().equals(name))
				return i;
		}

		return -1;
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
	public void save()
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
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);

			markSaved();
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Problem occured writing to file during save", ex);
		}
	}

	/**
	 * Denotes that something within the operation file needs saving
	 */
	public void markUnsaved()
	{
		isChanged = true;
		Domain.markUnsaved();
	}

	/**
	 * Denotes that the operation file has no unsaved changes
	 */
	public void markSaved()
	{
		isChanged = false;
		Domain.markSaved();
	}

	/**
	 * Denotes that something within the operation file needs saving
	 */
	public void changeBeginning()
	{
		Domain.changeBeginning();
	}
	
	/**
	 * Indicates if this operation file has unsaved changes
	 * @return true if there are unsaved changes, false otherwise
	 */
	public boolean isChanged()
	{
		return isChanged;
	}

	/**
	 * Returns the path of the XML file.
	 *
	 * @return The path of the XML file.
	 */
	@Override
	public String toString()
	{
		return xmlPath;
	}
	
	@Override
	public OperationFile clone()
	{
		return new OperationFile(this);
	}
}
