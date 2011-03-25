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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import marla.ide.operation.OperationXML;
import marla.ide.operation.OperationXMLException;
import marla.ide.problem.MarlaException;
import marla.ide.resource.ConfigurationException;
import org.jdom.Element;

/**
 * @author Ryan Morehart
 */
public class OperationXMLEditable extends OperationXML
{
	/**
	 * XML we have, whether it's parsable as an operation or not
	 */
	private Element opEl = null;
	/**
	 * Not null if an exception has been detected and the XML contains errors
	 */
	private OperationEditorException lastError = null;

	@Override
	public void setConfiguration(Element newConfig) throws OperationEditorException
	{
		try
		{
			opEl = newConfig;
			super.setConfiguration(newConfig);
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
	 * Override things we don't want to have available. Hackish, I know
	 */
	public static String setPrimaryXMLPath(String newPath) throws ConfigurationException
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static String getPrimaryXMLPath()
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static List<String> setUserXMLPaths(List<String> newPaths) throws ConfigurationException
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
	public static void loadXML() throws OperationXMLException, ConfigurationException
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static OperationXML createOperation(String opName) throws MarlaException
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static Map<String, List<String>> getAvailableOperationsCategorized() throws OperationXMLException, ConfigurationException
	{
		throw new UnsupportedOperationException("Blocked");
	}
	public static List<String> getAvailableOperations() throws OperationXMLException, ConfigurationException
	{
		throw new UnsupportedOperationException("Blocked");
	}
}
