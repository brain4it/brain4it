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

package org.brain4it.io;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;

/**
 *
 * @author realor
 */
public class JSONPrinter
{
  private static final Object OPEN_BRACKETS = new Object();
  private static final Object CLOSE_BRACKETS = new Object();
  private static final Object OPEN_BRACES = new Object();
  private static final Object CLOSE_BRACES = new Object();
  private static final Object COMMA = new Object();
  private static final Object COLON = new Object();

  private final Writer writer;
  private final Stack stack;

  public JSONPrinter(Writer writer)
  {
    this.writer = writer;
    stack = new Stack();
  }

  public static String toString(Object object)
  {
    try
    {
      Writer caWriter = new CharArrayWriter();
      JSONPrinter jsonWriter = new JSONPrinter(caWriter);
      jsonWriter.print(object);
      return caWriter.toString();
    }
    catch (IOException ex)
    {
      return ex.toString();
    }
  }

  public void print(Object object) throws IOException
  {
    stack.clear();
    writeObject(object);
  }

  public void writeObject(Object object) throws IOException
  {
    stack.push(object);

    while (!stack.isEmpty())
    {
      object = stack.pop();
      if (object instanceof BList)
      {
        BList list = (BList)object;
        if (list.size() == 0 || !isAllNamed(list))
        {
          stack.push(CLOSE_BRACKETS);
          for (int i = list.size() - 1; i >= 0; i--)
          {
            stack.push(list.get(i));
            if (i > 0) stack.push(COMMA);
          }
          stack.push(OPEN_BRACKETS);
        }
        else
        {
          stack.push(CLOSE_BRACES);
          for (int i = list.size() - 1; i >= 0; i--)
          {
            String name = list.getName(i);
            Object value = list.get(i);
            stack.push(value);
            stack.push(COLON);
            stack.push(name);            
            if (i > 0) stack.push(COMMA);
          }
          stack.push(OPEN_BRACES);            
        }
      }
      else if (object == OPEN_BRACES)
      {
        writer.write("{");
      }
      else if (object == CLOSE_BRACES)
      {
        writer.write("}");
      }
      else if (object == OPEN_BRACKETS)
      {
        writer.write("[");
      }
      else if (object == CLOSE_BRACKETS)
      {
        writer.write("]");
      }
      else if (object == COMMA)
      {
        writer.write(",");
      }
      else if (object == COLON)
      {
        writer.write(":");
      }
      else if (object instanceof String)
      {
        writer.write('"');
        writer.write(Utils.escapeString((String)object));
        writer.write('"');
      }
      else if (object instanceof Number)
      {
        writer.write(Utils.toString((Number)object));
      }
      else if (object instanceof Boolean)
      {
        writer.write(object.toString());
      }
      else
      {
        writer.write("null");
      }
    }
  }
  
  protected boolean isAllNamed(BList list)
  {
    boolean allNamed = true;
    int i = 0;
    while (allNamed && i < list.size())
    {
      allNamed = list.getName(i) != null;
      i++;
    }
    return allNamed;
  }  
  
  public static void main(String[] args) throws Exception
  {
    Object data = Parser.fromString("(numbers => (\"+\" 2 3 (a => 9)) a => 8898.45)");
    System.out.println("json: " + JSONPrinter.toString(data));
  }
}
