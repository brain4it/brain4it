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
package org.brain4it.manager.widgets;

import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;

/**
 *
 * @author realor
 */
public class WidgetProperty
{
  public static final String BOOLEAN = "boolean";
  public static final String NUMBER = "number";
  public static final String STRING = "string";
  public static final String LIST = "list";
  public static final String OBJECT = "object";

  protected final String name;
  protected final String type;
  protected final boolean function;
  protected final Object defaultValue;

  public WidgetProperty(String name, String type, boolean function,
     Object defaultValue)
  {
    this.name = name;
    this.type = type;
    this.function = function;
    this.defaultValue = defaultValue;
  }

  public String getName()
  {
    return name;
  }

  public String getType()
  {
    return type;
  }

  public boolean isFunction()
  {
    return function;
  }

  public Object getDefaultValue()
  {
    return defaultValue;
  }

  public BSoftReference getFunction(BList properties)
    throws Exception
  {
    Object value = properties.get(name);
    if (value == null)
    {
      return null;
    }
    else if (value instanceof BSoftReference)
    {
      return (BSoftReference)value;
    }
    throw new Exception(
      "Property " + name + " must be an exterior function reference!");
  }

  public Object getValue(BList properties)
  {
    if (properties.has(name))
    {
      return properties.get(name);
    }
    return defaultValue;
  }

  public boolean getBoolean(BList properties)
  {
    Object value = getValue(properties);
    if (value == null)
    {
      return false;
    }
    else if (value instanceof Boolean)
    {
      return ((Boolean)value);
    }
    else
    {
      return Boolean.valueOf(value.toString());
    }
  }

  public int getInteger(BList properties) throws Exception
  {
    Object value = getValue(properties);
    if (value == null)
    {
      return 0;
    }
    else if (value instanceof Number)
    {
      return ((Number)value).intValue();
    }
    throw new Exception("Property " + name + " must be a number!");
  }

  public long getLong(BList properties) throws Exception
  {
    Object value = getValue(properties);
    if (value == null)
    {
      return 0;
    }
    else if (value instanceof Number)
    {
      return ((Number)value).longValue();
    }
    throw new Exception("Property " + name + " must be a number!");
  }

  public double getDouble(BList properties) throws Exception
  {
    Object value = getValue(properties);
    if (value == null)
    {
      return 0.0;
    }
    else if (value instanceof Number)
    {
      return ((Number)value).doubleValue();
    }
    throw new Exception("Property " + name + " must be a number!");
  }

  public String getString(BList properties)
  {
    Object value = getValue(properties);
    if (value == null) return null;
    return String.valueOf(value);
  }

  public int getColor(BList properties)
  {
    String color = getString(properties);
    if (color != null && color.startsWith("#") && color.length() == 7)
    {
      try
      {
        return Integer.parseInt(color.substring(1), 16);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return -1;
  }

  public void setDefaultValue(BList properties)
  {
    if (!properties.has(name))
    {
      properties.put(name, defaultValue);
    }
  }
}
