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

package org.brain4it.lang;

import java.io.IOException;
import java.io.StringReader;
import org.brain4it.io.IOConstants;
import org.brain4it.io.Token;
import org.brain4it.io.Tokenizer;

/**
 * A soft reference.
 * 
 * A soft reference is a reference that points to data or a user defined
 * function.
 * 
 * The value they reference can be modified, so they are like variables.
 * 
 * There are two types of soft references:
 * <ul>
 * <li>Single references ({@link org.brain4it.lang.BSingleReference}) that 
 * reference a value inside the local or global scope lists.</li>
 * <li>Path references ({@link org.brain4it.lang.BPathReference}) that contains 
 * a list of names that represent a path to access a value inside a structure 
 * of nested lists.
 * </li>
 * </ul>
 * 
 * All soft references evaluate to the value they reference.
 * 
 * @author realor
 */
public abstract class BSoftReference extends BReference
{
  protected BSoftReference(String name)
  {
    super(name);
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof BSoftReference)
    {
      return name.equals(((BSoftReference)other).getName());
    }
    return false;
  }

  public abstract BList getPath();

  public abstract Object getReferencedData(Context context);

  public abstract void setReferencedData(Context context, Object data);

  public abstract boolean deleteReferencedData(Context context);

  public abstract boolean existsReferencedData(Context context);

  public static final BSoftReference getInstance(String value)
  {
    BList path = stringToPath(value, false);
    if (path == null)
    {
      return new BSingleReference(value);
    }
    else
    {
      return new BPathReference(path);
    }
  }

  public static final BList stringToPath(String value, boolean nonNull)
  {
    BList path = null;
    Tokenizer tokenizer = new Tokenizer(new StringReader(value));
    try
    {
      Token token = new Token();
      tokenizer.readToken(token);
      if (!token.isType(Token.REFERENCE))
        throw new BException("InvalidReference", value);

      if (token.getObject() instanceof BList)
      {
        // Tokenizer do not fully validate paths so this validation is required
        path = (BList)token.getObject();
        Object first = path.get(0);
        if (!(first instanceof String))
        {
          throw new BException("InvalidReference",
            "First element is not a string: " + path.get(0));
        }
      }
      tokenizer.readToken(token);
      if (!token.isType(Token.EOF))
        throw new BException("InvalidReference", value);
    }
    catch (IOException ex)
    {
      throw new BException("InvalidReference", value);
    }
    if (path == null && nonNull)
    {
      path = new BList();
      path.add(value);
    }
    return path;
  }

  public static final String pathToString(BList path)
  {
    if (path.size() == 0)
      throw new BException("InvalidReference", "No path");

    Object first = path.get(0);
    if (!(first instanceof String))
      throw new BException("InvalidReference", 
        "First element is not a string: " + path.get(0));
    
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < path.size(); i++)
    {
      Object value = path.get(i);
      if (value instanceof String)
      {
        String name = (String)value;
        if (needEscape(name))
        {
          buffer.append(IOConstants.PATH_REFERENCE_SEPARATOR);
          buffer.append('"').append(Utils.escapeString(name)).append('"');
        }
        else
        {
          if (i > 0 || path.size() == 1) 
            buffer.append(IOConstants.PATH_REFERENCE_SEPARATOR);
          buffer.append(name);
        }
      }
      else if (value instanceof Number)
      {
        int index = ((Number)value).intValue();
        if (index < 0)
          throw new BException("InvalidReference", "Invalid index: " + index);

        buffer.append(IOConstants.PATH_REFERENCE_SEPARATOR);
        buffer.append(index);
      }
      else throw new BException("InvalidReference", "Invalid index: " + value);
    }
    return buffer.toString();
  }

  public static boolean needEscape(String name)
  {
    boolean needEscape = name.length() == 0 ||
      Character.isDigit(name.charAt(0));
    int j = 0;
    while (!needEscape && j < name.length())
    {
      char ch = name.charAt(j);
      needEscape =
        ch == IOConstants.OPEN_LIST_TOKEN.charAt(0) ||
        ch == IOConstants.CLOSE_LIST_TOKEN.charAt(0) ||
        ch == IOConstants.PATH_REFERENCE_SEPARATOR ||
        ch == ' ' ||
        ch == '"';
      j++;
    }
    return needEscape;
  }
}
