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

package resource;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import latex.LatexExporter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import problem.MarlaException;
import r.OperationXML;
import r.RProcessor;

/**
 * @author Ryan Morehart
 */
public class Configuration
{
	/**
	 * Loads maRla configuration from the default location, config.xml in the
	 * current directory
	 */
	public static void load() throws MarlaException
	{
		load("config.xml");
	}

	/**
	 * Loads maRla configuration from the specified config file
	 * @param configPath XML file to load data from
	 */
	public static void load(String configPath) throws MarlaException
	{
		Element configXML = null;

		try
		{
			// Load the XML
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(configPath);
			configXML = doc.getRootElement();
		}
		catch(JDOMException ex)
		{
			throw new MarlaException("Config file could not be parsed, using default configuration", ex);
		}
		catch(IOException ex)
		{
			throw new MarlaException("Config file could not be read", ex);
		}

		// Tell various components about their settings
		RProcessor.setConfig(configXML.getChild("rprocessor"));
		LatexExporter.setConfig(configXML.getChild("latex"));
		OperationXML.setConfig(configXML.getChild("xmlops"));
	}

	/**
	 * Saves current maRla configuration to the default location, config.xml
	 * in the current directory
	 */
	public static void save() throws MarlaException
	{
		save("config.xml");
	}

	/**
	 * Saves current maRla configuration to the given location
	 * @param configPath Location to save data to
	 */
	public static void save(String configPath) throws MarlaException
	{
		// Build document
		Element root = new Element("marla");
		root.addContent(RProcessor.getConfig(new Element("rprocessor")));
		root.addContent(LatexExporter.getConfig(new Element("latex")));
		root.addContent(OperationXML.getConfig(new Element("xmlops")));
		Document doc = new Document(root);

		try
		{
			// Output to file
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(configPath));
			BufferedWriter outputStream = new BufferedWriter(os);

			Format formatter = Format.getPrettyFormat();
			formatter.setEncoding(os.getEncoding());
			XMLOutputter xml = new XMLOutputter(formatter);
			xml.output(doc, outputStream);
		}
		catch(IOException ex)
		{
			throw new MarlaException("Problem occured writing to configuration file", ex);
		}
	}
}
