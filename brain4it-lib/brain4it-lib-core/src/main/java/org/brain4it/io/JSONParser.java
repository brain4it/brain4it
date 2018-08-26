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
public class JSONParser
{
  public static final String EOF = "EOF";
  public static final String OPEN_BRACKETS = "[";
  public static final String CLOSE_BRACKETS = "]";
  public static final String OPEN_BRACES = "{";
  public static final String CLOSE_BRACES = "}";  
  public static final String COMMA = ",";
  public static final String COLON = ":";
  public static final String STRING = "STRING";
  public static final String NUMBER = "NUMBER";
  public static final String BOOLEAN = "BOOLEAN";
  public static final String NULL = "null";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  
  private final Reader reader;
  private final Token token;
  private final Stack<BList> stack;
  private int charBuffer = -2;
  private int charPosition = -1;
  
  public JSONParser(Reader reader)
  {
    this.reader = reader;
    this.token = new Token();
    stack = new Stack<BList>();
  }
  
  public Object parse() throws IOException, ParseException
  {
    Object result = null;
    readToken(token);
    String slotName = null;
    boolean end = false;
    
    do
    {
      if (token.isType(OPEN_BRACES) || token.isType(OPEN_BRACKETS))
      {
        BList list = new BList();
        if (stack.isEmpty())
        {
          result = list;
        }
        else
        {
          BList currentList = (BList)stack.peek();
          if (slotName == null)
          {
            currentList.add(list);
          }
          else
          {
            currentList.put(slotName, list);
            slotName = null;
          }
        }
        stack.push(list);
      }
      else if (token.isType(CLOSE_BRACES) || token.isType(CLOSE_BRACKETS))
      {
        if (stack.size() > 0)
        {
          stack.pop();
          if (stack.isEmpty())
          {
            end = true;
          }
        }
        else throw new ParseException("Unmatched parentheses", charPosition);        
      }
      else if (token.isType(COMMA))
      {
        // ignore
      }
      else if (token.isType(COLON))
      {
        BList currentList = stack.peek();
        if (currentList.size() == 0)
          throw new ParseException("Missing slot name", charPosition);
              
        Object nameObject = currentList.remove(currentList.size() - 1);
        slotName = Utils.toString(nameObject);
      }
      else // Literals
      {
        Object object = token.getValue();        
        if (stack.isEmpty())
        {
          result = object;
          end = true;
        }
        else
        {
          BList currentList = stack.peek();
          if (slotName == null)
          {
            currentList.add(object);
          }
          else
          {
            currentList.put(slotName, object);
            slotName = null;
          }
        }
      }
      readToken(token);
    }
    while (!token.isType(EOF) && !end);
    
    if (!(end && token.isType(EOF)))
      throw new ParseException("Unmatched parenthesis", charPosition);    
    
    return result;
  }
  
  public static Object fromString(String json) throws ParseException
  {
    CharArrayReader caReader = new CharArrayReader(json.toCharArray());
    JSONParser objectReader = new JSONParser(caReader);
    try
    {
      return objectReader.parse();
    }
    catch (IOException ex)
    {
      return null;
    }
  }
  
  private Token readToken(Token token) throws IOException, ParseException
  {
    if (token == null)
    {
      token = new Token();
    }
    token.type = null;
    token.buffer.setLength(0);
    token.value = null;
    token.startPosition = charPosition;

    int state = 0;
    boolean end = false;

    do
    {
      char ch = readChar();

      switch (state)
      {
        case 0: // Expecting new token
          if (ch == (char)-1)
          {
            token.type = EOF;
            end = true;
            token.startPosition = charPosition;
            token.endPosition = charPosition;
            unreadChar(ch);
          }
          else if (ch == '[')
          {
            token.type = OPEN_BRACKETS;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (ch == ']')
          {
            token.type = CLOSE_BRACKETS;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (ch == '{')
          {
            token.type = OPEN_BRACES;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (ch == '}')
          {
            token.type = CLOSE_BRACES;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (ch == ',')
          {
            token.type = COMMA;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (ch == ':')
          {
            token.type = COLON;
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            end = true;
          }
          else if (isSeparator(ch))
          {
            // skip
          }
          else if (ch == '"') // start STRING
          {
            token.type = STRING;
            token.startPosition = charPosition;
            state = 1;
          }
          else // start NUMBER, BOOLEAN OR NULL
          {
            token.buffer.append(ch);
            token.startPosition = charPosition;
            state = 2;
          }
          break;

        case 1: // Processing String
          if (ch == (char)-1)
          {
            throw new ParseException("Unexpected end of file", charPosition);
          }
          else if (ch == '\n' || ch == '\r' || ch == '\t')
          {
            throw new ParseException("Invalid character: 0x" +
              Integer.toHexString((int)ch), charPosition);
          }
          else if (ch == '\\')
          {
            token.buffer.append(readEscapeChar());
          }
          else if (ch == '"')
          {
            token.value = token.buffer.toString();
            token.endPosition = charPosition + 1;
            end = true;
          }
          else
          {
            token.buffer.append(ch);
          }
          break;

        case 2: // Processing token
          if (isSeparator(ch) ||
              ch == '[' || ch == ']' ||
              ch == '{' || ch == '}' ||   
              ch == ',' || ch == '"' || ch == (char)-1)
          {
            String value = token.buffer.toString();
            if (TRUE.equals(value) || FALSE.equals(value))
            {
              token.type = BOOLEAN;
              token.value = Boolean.valueOf(token.buffer.toString());
            }
            else if (NULL.equals(value))
            {
              token.type = NULL;
              token.value = null;
            }
            else if (isNumber(value))
            {
              try
              {
                token.value = Utils.toNumber(value);
                token.type = NUMBER;
              }
              catch (NumberFormatException ex)
              {
                throw new ParseException("Invalid number: " + value, charPosition);
              }
            }
            else throw 
              new ParseException("Unexpected token: " + value, charPosition);
            token.endPosition = charPosition;
            unreadChar(ch);
            end = true;
          }
          else
          {
            // add char to token
            token.buffer.append(ch);
          }
          break;
        default:
      }
    }
    while (!end);
    return token;
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
  
  private boolean isSeparator(char ch)
  {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }

  private boolean isNumber(String value)
  {
    char firstChar = value.charAt(0);
    if (Character.isDigit(firstChar)) return true;
    if (value.length() > 1)
    {
      if (firstChar == '-' || firstChar == '.')
      {
        char secondChar = value.charAt(1);
        return Character.isDigit(secondChar);
      }
    }
    return false;
  }
  
  private char readEscapeChar() throws IOException, ParseException
  {
    char ch = (char)reader.read();
    charPosition++;
    if (ch == 'n') return '\n';
    if (ch == 'r') return '\r';
    if (ch == 't') return '\t';
    if (ch == '\\') return '\\';
    if (ch == 'b') return ' ';
    if (ch == 'f') return '\f';
    if (ch == '\'') return '\'';
    if (ch == '"') return '"';
    if (ch == 'u')
    {
      StringBuilder builder = new StringBuilder();
      builder.append((char)reader.read());
      builder.append((char)reader.read());
      builder.append((char)reader.read());
      builder.append((char)reader.read());
      charPosition += 4;
      return (char)Integer.parseInt(builder.toString(), 16);
    }
    if (ch == 'x')
    {
      StringBuilder builder = new StringBuilder();
      builder.append((char)reader.read());
      builder.append((char)reader.read());
      charPosition += 2;
      return (char)Integer.parseInt(builder.toString(), 16);
    }
    throw new ParseException("Invalid character: \\" + ch, charPosition);
  }
  
  class Token
  {
    String type;
    StringBuilder buffer = new StringBuilder();
    Object value;
    int startPosition;
    int endPosition;

    public String getType()
    {
      return type;
    }

    public boolean isType(String type)
    {
      return this.type.equals(type);
    }

    public Object getValue()
    {
      return value;
    }

    public int getStartPosition()
    {
      return startPosition;
    }

    public int getEndPosition()
    {
      return endPosition;
    }

    public int getLength()
    {
      return endPosition - startPosition;
    }

    @Override
    public String toString()
    {
      Object v = getValue();
      return (v == null) ? type : type + "[" + v + "]";
    }
  }
}
