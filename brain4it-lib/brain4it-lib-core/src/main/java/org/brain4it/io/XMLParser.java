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
import java.util.Stack;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;

/**
 *
 * @author realor
 */
public class XMLParser
{
  public static final String CHILDREN = "*";
  private static final char EOF = (char)-1;
  private final Reader reader;
  private final Stack<BList> stack;
  private final StringBuilder buffer = new StringBuilder();
  private int charBuffer = -2;
  private int charPosition = -1;
  private BList options;

  public XMLParser(Reader reader)
  {
    this(reader, null);
  }

  public XMLParser(Reader reader, BList options)
  {
    this.reader = reader;
    this.options = options;
    stack = new Stack<BList>();
  }

  public static Object fromString(String xml) throws ParseException
  {
    CharArrayReader caReader = new CharArrayReader(xml.toCharArray());
    XMLParser objectReader = new XMLParser(caReader);
    try
    {
      return objectReader.parse();
    }
    catch (IOException ex)
    {
      return null;
    }
  }

  public Object parse() throws IOException, ParseException
  {
    BList document = new BList();
    document.add("document");
    stack.push(document);
    boolean mergeSeparators = isMergeSeparators();

    char ch = readChar();
    while (ch != EOF)
    {
      if (ch == '<')
      {
        if (buffer.length() > 0) // add previous text
        {
          BList parent = stack.peek();
          BList children = getChildren(parent);
          children.add(buffer.toString());
          buffer.setLength(0);
        }
        ch = readChar();
        if (ch == '?')
        {
          readPreambule();
        }
        else if (ch == '!')
        {
          readCommentOrCDATA();
        }
        else if (ch != EOF)
        {
          unreadChar(ch);
          readTag();
        }
      }
      else if (ch == '&')
      {
        readEntity();
      }
      else if (isSeparator(ch))
      {
        if (!mergeSeparators || isSeparatorRequired(buffer))
        {
          buffer.append(ch);
        }
      }
      else
      {
        buffer.append(ch);
      }
      ch = readChar();
    }
    return document;
  }

  protected void readTag() throws IOException, ParseException
  {
    boolean endTag = false;
    char ch = readChar();
    if (ch == '/')
    {
      endTag = true;
      ch = readChar();
    }
    while (isValidNameCharacter(ch))
    {
      buffer.append(ch);
      ch = readChar();
    }
    if (!(isSeparator(ch) || ch == '/' || ch == '>'))
      throw new ParseException("Unexpected character: " + ch, charPosition);

    String tagName = buffer.toString();
    buffer.setLength(0);
    if (endTag)
    {
      BList parent = stack.pop();
      if (!parent.get(0).equals(tagName))
        throw new ParseException("Tag mismatch: " + tagName, charPosition);
      BList children = (BList)parent.get(CHILDREN);
      if (children != null && children.size() == 1)
      {
        Object child = children.get(0);
        if (child instanceof String)
        {
          parent.put(CHILDREN, child);
        }
      }
    }
    else
    {
      BList parent = stack.peek();
      BList list = new BList();
      BList children = getChildren(parent);
      children.add(list);
      stack.push(list);
      list.add(tagName);
      if (isSeparator(ch))
      {
        readAttributes(list);
        ch = readChar(); // expecting / or >
      }
      if (ch == '/')
      {
        ch = readChar(); // expecting >
        if (ch != '>')
          throw new ParseException("Unexpected char: " + ch, charPosition);
        stack.pop();
      }
    }
  }
  
  private void readPreambule() throws IOException, ParseException
  {
    readSequence("xml");
    BList document = (BList)stack.peek();
    readAttributes(document);    
    readUntilSequence("?>");
  }

  private void readCommentOrCDATA() throws IOException, ParseException
  {
    char ch = readChar();
    if (ch == '-') // Comment
    {
      readSequence("-");
      readUntilSequence("-->");
    }
    else if (ch == '[') // CDATA
    {
      readSequence("CDATA[");
      String chunk = readUntilSequence("]]>");
      if (chunk.length() > 0)
      {
        BList parent = stack.peek();
        BList children = getChildren(parent);
        children.add(chunk);
      }      
    }
    else throw new ParseException("Unexpected char: " + ch, charPosition);
  }

  private void readEntity() throws IOException, ParseException
  {
    String chunk = readUntilSequence(";");
    if (chunk.equals("amp")) buffer.append('&');
    else if (chunk.equals("quot")) buffer.append('"');
    else if (chunk.equals("apos")) buffer.append('\'');
    else if (chunk.equals("lt")) buffer.append("<");
    else if (chunk.equals("gt")) buffer.append(">");
    else if (chunk.startsWith("x"))
    {
      int code = Integer.parseInt(chunk.substring(1), 16);
      buffer.append((char)code);
    }
    else
    {
      int code = Integer.parseInt(chunk, 10);
      buffer.append((char)code);      
    }
  }
  
  private void readAttributes(BList list) throws IOException, ParseException
  {
    int state = 0;
    String name = null;
    String value;
    char ch = readChar();
    boolean end = false;
    while (ch != EOF && !end)
    {
      switch (state)
      {
        case 0: // expecting name
          if (isSeparator(ch))
          {
          }
          else if (ch == '"' || ch == '=')
          {
            throw new ParseException("Tag name expected", charPosition);
          }
          else if (ch == '?' || ch == '/' || ch == '>')
          {
            unreadChar(ch);
            end = true;
          }
          else if (isValidNameCharacter(ch))
          {
            buffer.append(ch);
            state = 1;
          }
          else throw new ParseException("Unexpected char: " + ch, charPosition);
          break;

        case 1: // have name, expecting separator or =
          if (isSeparator(ch))
          {
            name = buffer.toString();
            buffer.setLength(0);
            state = 2;
          }
          else if (ch == '=')
          {
            name = buffer.toString();
            buffer.setLength(0);
            state = 3;
          }
          else if (ch == '"')
          {
            throw new ParseException("Unexpected token:" + ch, charPosition);
          }
          else
          {
            buffer.append(ch);
          }
          break;

        case 2: // expecting =
          if (ch == '=')
          {
            state = 3;
          }
          else if (!isSeparator(ch))
          {
            throw new ParseException("Unexpected token:" + ch, charPosition);
          }
          break;

        case 3: // expecting starting " or '
          if (ch == '"')
          {
            state = 4;
          }
          else if (ch == '\'')
          {
            state = 5;
          }
          else if (!isSeparator(ch))
          {
            throw new ParseException("Unexpected token:" + ch, charPosition);
          }
          break;

        case 4: // expecting ending "
          if (ch == '"')
          {
            value = buffer.toString();
            buffer.setLength(0);
            list.put(name, value);
            state = 0;
          }
          else if (ch == '&')
          {
            readEntity();
          }
          else
          {
            buffer.append(ch);
          }
          break;

        case 5: // expecting ending '
          if (ch == '\'')
          {
            value = buffer.toString();
            buffer.setLength(0);
            list.put(name, value);
            state = 0;
          }
          else if (ch == '&')
          {
            readEntity();
          }
          else
          {
            buffer.append(ch);
          }
          break;          
      }
      ch = readChar();
    }
    unreadChar(ch);
  }

  private void readSequence(String sequence) throws IOException, ParseException
  {
    for (int i = 0; i < sequence.length(); i++)
    {
      char ch = readChar();
      if (ch != sequence.charAt(i))
        throw new ParseException("Unexpected char: " + ch, charPosition);
    }
  }
  
  private String readUntilSequence(String sequence)
    throws IOException, ParseException
  {
    StringBuilder chunk = new StringBuilder();
    char ch;
    int j = 0;
    do
    {
      ch = readChar();
      if (ch == sequence.charAt(j))
      {
        j++;
      }
      else
      {
        if (j > 0)
        {
          chunk.append(sequence.substring(0, j));
        }
        if (ch == sequence.charAt(0))
        {
          j = 1;
        }
        else
        {
          chunk.append(ch);
          j = 0;
        }
      }
    }
    while (ch != EOF && j < sequence.length());

    if (j < sequence.length())
      throw new ParseException("Unexpected end of file", charPosition);
    
    return chunk.toString();
  }

  private char readChar() throws IOException
  {
    char ch;
    if (charBuffer == -2) // no char in buffer
    {
      ch = (char)reader.read();
    }
    else
    {
      ch = (char)charBuffer;
      charBuffer = -2;
    }
    charPosition++;
    return ch;
  }

  private void unreadChar(char ch)
  {
    charBuffer = ch;
    charPosition--;
  }

  private BList getChildren(BList list)
  {
    BList children = (BList)list.get(CHILDREN);
    if (children == null)
    {
      children = new BList();
      list.put(CHILDREN, children);
    }
    return children;
  }

  private boolean isSeparator(char ch)
  {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }

  private boolean isValidNameCharacter(char ch)
  {
    return Character.isLetterOrDigit(ch) || ch == ':' || ch == '_' || ch == '-';
  }

  private boolean isSeparatorRequired(StringBuilder buffer)
  {
    if (buffer.length() == 0) return false;
    char last = buffer.charAt(buffer.length() - 1);
    return !isSeparator(last);
  }

  private boolean isMergeSeparators()
  {
    if (options != null)
    {
      return Utils.toBoolean(options.get("merge-separators"));
    }
    return true;
  }
}
