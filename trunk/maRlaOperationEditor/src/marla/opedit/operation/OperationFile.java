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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author Ryan Morehart
 */
public class OperationFile
{
	/**
	 * Location to save operation XML to
	 */
	private String xmlPath = null;
	/**
	 * Storage location for parsed operation XML file
	 */
	private static Element operationXML = null;

	/**
	 * 
	 * @param savePath
	 * @throws OperationEditorException
	 */
	public OperationFile(String savePath) throws OperationEditorException
	{
		xmlPath = savePath;

		if(savePath != null)
			loadOperations();
	}

	/**
	 * (Re)loads the current XML file operations
	 */
	private void loadOperations() throws OperationEditorException
	{
		try
		{
			// Make sure we know where we're looking
			if(xmlPath == null)
				throw new OperationEditorException("XML file for operations has not been specified");

			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(xmlPath);

			// Just save it or bring it into the combined doc?
			operationXML = doc.getRootElement();
		}
		catch(JDOMException ex)
		{
			throw new OperationEditorException("Operation XML file '" +xmlPath + "' contains XML error(s)", ex);
		}
		catch(IOException ex)
		{
			throw new OperationEditorException("Unable to read the operation XML file '" + xmlPath + "'", ex);
		}
	}
}
