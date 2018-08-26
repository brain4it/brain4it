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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map;
import java.util.Collections;
import org.brain4it.lang.Function;
import org.brain4it.lang.BList;
import org.brain4it.lang.BReference;
import org.brain4it.lang.BHardReference;
import org.brain4it.lang.BPathReference;
import org.brain4it.lang.BSingleReference;
import org.brain4it.lang.Structure;

public class Parser
{
  private final Tokenizer tokenizer;
  private final Token token;
  private final Stack<BList> stack;
  private final HashMap<String, BList> listsById;
  private final Map<String, Function> functions;
  private final HashMap<BList, Structure> structuredLists;
  private String name;

  public Parser(Reader reader)
  {
    this(reader, Collections.EMPTY_MAP);
  }

  public Parser(Reader reader, Map<String, Function> functions)
  {
    tokenizer = new Tokenizer(reader);
    token = new Token();
    stack = new Stack<BList>();
    listsById = new HashMap<String, BList>();
    structuredLists = new HashMap<BList, Structure>();
    this.functions = functions;
  }

  public Object parse() throws IOException, ParseException
  {
    Object result = null;
    tokenizer.readToken(token);
    name = null;
    boolean end = false;

    do
    {
      if (token.isType(Token.INVALID))
      {
        throw new ParseException("Invalid token: " + token.getText(), 
          token.getStartPosition());
      }
      else if (token.isType(Token.OPEN_LIST))
      {
        BList list = new BList();
        if (stack.isEmpty())
        {
          result = list;
        }
        else
        {
          addToCurrentList(list);
        }
        stack.push(list);
      }
      else if (token.isType(Token.CLOSE_LIST))
      {
        if (stack.size() > 0)
        {
          stack.pop();
          if (stack.isEmpty())
          {
            end = true;
          }
        }
        else throw new ParseException("Unmatched parentheses",
          token.getStartPosition());
      }
      else if (token.isType(Token.NAME_OPERATOR))
      {
        if (stack.isEmpty())
          throw new ParseException("Missing name", token.getStartPosition());

        BList currentList = stack.peek();
        if (currentList.size() == 0)
          throw new ParseException("Missing name", token.getStartPosition());

        Object nameObject = currentList.remove(currentList.size() - 1);
        name = nameObject.toString();
      }
      else if (token.isType(Token.TAG))
      {
        if (stack.isEmpty())
        {
          throw new ParseException("Invalid tag", token.getStartPosition());
        }
        else
        {
          Tag tag = (Tag)token.getObject();
          BList currentList = stack.peek();
          if (tag instanceof DeclarationTag)
          {
            String dataListId = tag.getDataListId();
            if (dataListId != null)
            {
              listsById.put(dataListId, currentList);
            }            
            String structureListId = ((DeclarationTag)tag).getStructureListId();
            if (structureListId != null)
            {
              BList list = listsById.get(structureListId);
              if (list == null)
              {
                throw new ParseException("Invalid declaration tag",
                  token.getStartPosition());
              }
              structuredLists.put(currentList, list.getStructure());
            }
          }
          else // LinkTag
          {
            String dataListId = tag.getDataListId();
            BList list = listsById.get(dataListId);
            if (list == null)
              throw new ParseException("Invalid link tag",
                token.getStartPosition());
            addToCurrentList(list);
          }
        }
      }
      else if (token.isType(Token.REFERENCE))
      {
        String text = token.getText();
        BReference reference;
        Function function = (Function)functions.get(text);
        if (function == null)
        {
          if (token.object == null)
          {
            reference = new BSingleReference(text);
          }
          else
          {
            // token.object is the BList that contains the reference path
            reference = new BPathReference((BList)token.object);
          }
        }
        else
        {
          reference = new BHardReference(text, function);
        }
        if (stack.isEmpty())
        {
          result = reference;
          end = true;
        }
        else
        {
          addToCurrentList(reference);
        }
      }
      else // Literals
      {
        Object object = token.getObject();
        if (stack.isEmpty())
        {
          result = object;
          end = true;
        }
        else // add to current list
        {
          addToCurrentList(object);
        }
      }
      tokenizer.readToken(token);
    }
    while (!token.isType(Token.EOF) && !end);

    if (!(end && token.isType(Token.EOF)))
      throw new ParseException("Unmatched parenthesis",
        token.getStartPosition());

    for (Map.Entry<BList, Structure> entry : structuredLists.entrySet())
    {
      BList list = entry.getKey();
      Structure structure = entry.getValue();
      list.setStructure(structure);
    }
    return result;
  }
  
  private void addToCurrentList(Object object)
  {
    BList currentList = stack.peek();
    if (name == null || structuredLists.containsKey(currentList))
    {
      currentList.add(object);
    }
    else
    {
      currentList.put(name, object);
    }
    name = null;
  }
  
  public static Object fromString(String codeString)
    throws ParseException
  {
    return fromString(codeString, Collections.EMPTY_MAP);
  }

  public static Object fromString(String codeString,
    Map<String, Function> functions) throws ParseException
  {
    CharArrayReader caReader = new CharArrayReader(codeString.toCharArray());
    Parser objectReader = new Parser(caReader, functions);
    try
    {
      return objectReader.parse();
    }
    catch (IOException ex)
    {
      // never happens
      return null;
    }
  }
}
