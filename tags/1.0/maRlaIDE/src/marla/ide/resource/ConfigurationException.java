/*
 * The maRla Project - Graphical problem solver for statistical calculations.
 * Copyright © 2011 Cedarville University
 * http://marla.googlecode.com
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

package marla.ide.resource;

import marla.ide.problem.MarlaException;
import marla.ide.resource.Configuration.ConfigType;

/**
 * Thrown when a configuration error occurs. The user should be able to correct
 * it, mostly likely
 * @author Ryan Morehart
 */
public class ConfigurationException extends MarlaException
{
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

	/**
	 * Returns a "friendly" string for the configuration setting
	 * @return String intended for display to user
	 */
	public String getName()
	{
		return Configuration.getName(type);
	}

	/**
	 * Sets the configuration option to the given value 
	 * @param newVal New value for the configuration option
	 */
	public void setOption(String newVal)
	{
		Configuration.getInstance().set(type, newVal);
	}
}
