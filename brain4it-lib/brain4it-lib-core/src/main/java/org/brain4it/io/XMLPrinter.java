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
import static org.brain4it.io.XMLParser.CHILDREN;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;

/**
 *
 * @author realor
 */
public class XMLPrinter
{
  private final Writer writer;
  private String preambule;
  private boolean separatorNeeded = false;

  public XMLPrinter(Writer writer)
  {
    this.writer = writer;
  }

  public String getPreambule()
  {
    return preambule;
  }

  public void setPreambule(String preambule)
  {
    this.preambule = preambule;
  }

  public static String toString(Object object)
  {
    try
    {
      Writer caWriter = new CharArrayWriter();
      XMLPrinter xmlWriter = new XMLPrinter(caWriter);
      xmlWriter.print(object);
      return caWriter.toString();
    }
    catch (IOException ex)
    {
      return ex.toString();
    }
  }

  public void print(Object object) throws IOException
  {
    if (preambule != null)
    {
      writer.write(preambule);
    }
    writeObject(object);
  }

  private void writeObject(Object object) throws IOException
  {
    if (object instanceof BList)
    {
      // object represent a tag
      BList tagList = (BList)object;
      if (tagList.size() > 0)
      {
        String tagName = (String)tagList.get(0);
        writer.write("<");
        writer.write(tagName);
        for (int i = 1; i < tagList.size(); i++)
        {
          String attributeName = tagList.getName(i);
          if (attributeName != null &&
              !CHILDREN.equals(attributeName) &&
              isValidAttributeName(attributeName))
          {
            String attributeValue = Utils.toString(tagList.get(i));
            writer.write(" ");
            writer.write(attributeName);
            writer.write("=\"");
            writer.write(Utils.escapeString(attributeValue));
            writer.write("\"");
          }
        }
        writer.write(">");
        separatorNeeded = false;
        if (tagList.has(CHILDREN))
        {
          Object value = tagList.get(CHILDREN);
          if (value instanceof BList)
          {
            BList children = (BList)value;
            for (int i = 0; i < children.size(); i++)
            {
              writeObject(children.get(i));
            }
          }
          else
          {
            writeObject(value);
          }
        }
        writer.write("</");
        writer.write(tagName);
        writer.write(">");
        separatorNeeded = false;
      }
    }
    else // literal
    {
      if (separatorNeeded) writer.write(" "); // separator
      writer.write(Utils.toString(object));
      separatorNeeded = true;
    }
  }

  private boolean isValidAttributeName(String attributeName)
  {
    return attributeName.length() > 0 &&
      Character.isLetter(attributeName.charAt(0));
  }
}
