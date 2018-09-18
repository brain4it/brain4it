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

import org.brain4it.lang.Utils;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Stack;
import org.brain4it.lang.BList;
import org.brain4it.lang.BReference;
import org.brain4it.lang.Structure;
import static org.brain4it.io.IOConstants.CLOSE_LIST_TOKEN;
import static org.brain4it.io.IOConstants.OPEN_LIST_TOKEN;
import static org.brain4it.io.IOConstants.NAME_OPERATOR_TOKEN;

/**
 *
 * @author realor
 */
public class Printer
{
  private final Writer writer;
  private final Stack<Cursor> cursorStack;
  private final HashMap<BList, Anchor> dataRegistry;
  private final HashMap<Structure, Anchor> structureRegistry;
  private int anchorCount = 0;
  
  public Printer(Writer writer)
  {
    this.writer = writer;
    cursorStack = new Stack<Cursor>();
    dataRegistry = new HashMap<BList, Anchor>();
    structureRegistry = new HashMap<Structure, Anchor>();
  }

  public static String toString(Object object)
  {
    try
    {
      Writer caWriter = new CharArrayWriter();
      Printer objectWriter = new Printer(caWriter);
      objectWriter.print(object);
      return caWriter.toString();
    }
    catch (IOException ex)
    {
      return ex.toString();
    }
  }

  public void print(Object object) throws IOException
  {
    createAnchorMap(object);

    cursorStack.clear();

    if (object instanceof BList)
    {
      BList list = (BList)object;
      writeListStart(list, getTag(list));
      Cursor cursor = new Cursor(list);
      cursorStack.push(cursor);
      
      while (!cursorStack.isEmpty())
      {
        cursor = cursorStack.pop();
        while (cursor.hasNext())
        {
          int index = cursor.getIndex();
          String name = cursor.getName();
          Object element = cursor.getElement();
          cursor.next();
          
          if (index > 0)
          {
            writer.write(' ');
          }

          if (name != null)
          {
            writeName(name);
          }
          if (element instanceof BList)
          {
            list = (BList)element;
            try
            {
              Tag tag = getTag(list);
              if (tag instanceof LinkTag)
              {
                writer.write(tag.toString());
              }
              else
              {
                cursorStack.push(cursor);
                writeListStart(list, tag);
                cursor = new Cursor(list);
              }
            }
            catch (ConcurrentModificationException ex)
            {
              // ignore list
            }
          }
          else
          {
            writeNonList(element);
          }
        }
        writeListEnd();
      }
    }
    else
    {
      writeNonList(object);
    }
  }

  private void writeName(String name) throws IOException
  {
    writer.write('"');
    writer.write(Utils.escapeString(name));
    writer.write('"');
    writer.write(' ');
    writer.write(NAME_OPERATOR_TOKEN);
    writer.write(' ');    
  }
  
  private void writeListStart(BList list, Tag tag) throws IOException
  {
    writer.write(OPEN_LIST_TOKEN);
    if (tag != null) 
    {
      writer.write(tag.toString());
      if (list.size() > 0)
      {
        writer.write(' ');
      }
    }
  }

  private void writeListEnd() throws IOException
  {
    writer.write(CLOSE_LIST_TOKEN);    
  }
  
  private void writeNonList(Object object) throws IOException
  {
    if (object instanceof BReference)
    {
      BReference reference = (BReference)object;
      writer.write(reference.getName());
    }
    else if (object instanceof String)
    {
      writer.write('"');
      writer.write(Utils.escapeString(object.toString()));
      writer.write('"');
    }
    else if (object instanceof Number)
    {
      writer.write(Utils.toString((Number)object));
    }
    else
    {
      writer.write(String.valueOf(object));
    }
  }
  
  private void createAnchorMap(Object baseObject) throws IOException
  {
    BList list;
    Cursor cursor;
    dataRegistry.clear();
    structureRegistry.clear();
    cursorStack.clear();

    if (baseObject instanceof BList)
    {
      list = (BList)baseObject;
      registerList(list);
      cursor = new Cursor(list);
      cursorStack.push(cursor);

      while (!cursorStack.isEmpty())
      {
        cursor = cursorStack.pop();
        while (cursor.hasNext())
        {
          Object element = cursor.getElement();
          cursor.next();
          if (element instanceof BList)
          {
            list = (BList)element;
            if (registerList(list))
            {
              cursorStack.push(cursor);
              cursor = new Cursor(list);
            }
          }
        }
      }
    }
  }

  private boolean registerList(BList list)
  {
    Anchor dataAnchor = dataRegistry.get(list);
    if (dataAnchor == null)
    {
      dataAnchor = new Anchor();
      dataRegistry.put(list, dataAnchor);
      Structure structure = list.getStructure();
      if (structure != null)
      {
        Anchor structureAnchor = structureRegistry.get(structure);
        if (structureAnchor == null)
        {
          structureRegistry.put(structure, dataAnchor);
        }
        else
        {
          structureAnchor.referenceCount++;
        }
      }
      return true;
    }
    else
    {
      dataAnchor.referenceCount++;
      return false;
    }
  }

  private Tag getTag(BList list) throws ConcurrentModificationException
  {
    Anchor dataAnchor = dataRegistry.get(list);
    if (dataAnchor == null) 
      throw new ConcurrentModificationException();
    
    if (dataAnchor.declared)
      return new LinkTag(dataAnchor.getListId());

    if (dataAnchor.visited)
      throw new ConcurrentModificationException();

    dataAnchor.visited = true;
    
    Structure structure = list.getStructure();
    Anchor structureAnchor =  structure == null ?
      null : structureRegistry.get(structure);
    
    Object dataListId = dataAnchor.referenceCount > 1 ?
      dataAnchor.getListId() : null;

    Object structureListId = structureAnchor != null &&
      structureAnchor.referenceCount > 1 ?
      structureAnchor.getListId() : null;

    if (dataListId == null && structureListId == null) return null;
    
    dataAnchor.declared = true;

    return new DeclarationTag(dataListId, structureListId);
  }

  class Anchor
  {
    int listId;
    int referenceCount;
    boolean declared = false;
    boolean visited = false;

    Anchor()
    {
      referenceCount = 1;
    }

    public int getListId()
    {
      if (listId == 0)
      {
        listId = ++anchorCount;
      }
      return listId;
    }
    
    @Override
    public String toString()
    {
      return listId + "/" + referenceCount;
    }
  }

  class Cursor
  {
    BList list;
    int index = 0;

    Cursor(BList list)
    {
      this.list = list;
      index = 0;
    }

    boolean hasNext()
    {
      return index < list.size();
    }

    void next()
    {
      index++;
    }

    String getName()
    {
      return list.getName(index);
    }

    Object getElement()
    {
      return list.get(index);
    }
    
    int getIndex()
    {
      return index;
    }
  }
}
