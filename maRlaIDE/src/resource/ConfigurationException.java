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

import latex.LatexExporter;
import operation.OperationXML;
import problem.InternalMarlaException;
import problem.MarlaException;
import r.RProcessor;

/**
 * Thrown when a configuration error occurs. The user should be able to correct
 * it, mostly likely
 * @author Ryan Morehart
 */
public class ConfigurationException extends MarlaException
{
	public enum ConfigType {PdfTex, R, OpsXML, TexTemplate};
	private ConfigType type = null;

	public ConfigurationException(String msg, ConfigType type)
	{
		super(msg);
		this.type = type;
	}

	public ConfigurationException(String msg, ConfigType type, Throwable cause)
	{
		super(msg, cause);
		this.type = type;
	}

	public String getName()
	{
		switch(type)
		{
			case PdfTex:
				return "pdfTeX path";

			case OpsXML:
				return "Operation XML path";

			case R:
				return "R path";

			case TexTemplate:
				return "LaTeX export template path";

			default:
				throw new InternalMarlaException("Unhandled configuration exception type in name");
		}
	}

	public String getExtension()
	{
		switch(type)
		{
			case PdfTex:
			case R:
				return "exe";

			case OpsXML:
			case TexTemplate:
				return "xml";

			default:
				throw new InternalMarlaException("Unhandled configuration exception type in extension");
		}
	}

	public void setPath(String newPath) throws ConfigurationException, MarlaException
	{
		try
		{
			switch(type)
			{
				case PdfTex:
					LatexExporter.setPdfTexPath(newPath);
					break;

				case OpsXML:
					OperationXML.loadXML(newPath);
					break;

				case R:
					RProcessor.setRLocation(newPath);
					break;

				case TexTemplate:
					LatexExporter.setDefaultTemplate(newPath);
					break;

				default:
					throw new InternalMarlaException("Unhandled configuration exception type in name");
			}
		}
		catch(ConfigurationException ex)
		{
			Configuration.errors.push (ex);
		}
	}
}
