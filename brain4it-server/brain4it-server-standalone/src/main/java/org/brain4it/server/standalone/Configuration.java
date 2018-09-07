/*
 * Brain4it
 *
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/
 *   and
 *   https://www.gnu.org/licenses/lgpl.txt
 */
package org.brain4it.server.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author realor
 */
public class Configuration extends Properties
{
  private static final String PROPERTY_PREFIX = "-";
  private static final String PROPERTY_VALUE_SEPARATOR = "=";

  /**
   * Loads configuration properties from an array of arguments
   *
   * @param args an array of arguments where each argument may be a
   * -&lt;name&gt;=&lt;property&gt; pair or a Java properties file path.
   * @throws FileNotFoundException
   * @throws IOException
   */

  public void load(String[] args) throws FileNotFoundException, IOException
  {
    for (String arg : args)
    {
      if (arg.startsWith(PROPERTY_PREFIX))
      {
        int index = arg.indexOf(PROPERTY_VALUE_SEPARATOR);
        String property;
        String value;
        if (index != -1)
        {
          property = arg.substring(PROPERTY_PREFIX.length(), index);
          value = arg.substring(index + PROPERTY_PREFIX.length());
        }
        else
        {
          property = arg.substring(PROPERTY_PREFIX.length());
          value = "true";
        }
        put(property, value);
      }
      else // expect Properties file path
      {
        File file = new File(arg);
        FileInputStream fis = new FileInputStream(file);
        try
        {
          load(fis);
        }
        finally
        {
          fis.close();
        }
      }
    }
  }

  public boolean getBooleanProperty(String name)
  {
    return Boolean.valueOf(getProperty(name, "false"));
  }
  
  public int getIntegerProperty(String name)
  {
    String value = getProperty(name);

    if (value == null)
      throw new RuntimeException("Undefined property " + name);

    try
    {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
      throw new NumberFormatException(name + ": " + value);
    }
  }
}
