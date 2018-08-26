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

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;
import java.text.ParseException;
import org.brain4it.lang.Utils;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.brain4it.io.IOConstants.CLOSE_LIST_TOKEN;
import static org.brain4it.io.IOConstants.NAME_OPERATOR_TOKEN;
import static org.brain4it.io.IOConstants.OPEN_LIST_TOKEN;
import static org.brain4it.io.IOConstants.PATH_REFERENCE_SEPARATOR;
import org.brain4it.lang.BList;

/**
 *
 * @author realor
 */
public class Tokenizer
{
  private final Reader reader;
  private final StringBuilder buffer = new StringBuilder();
  private final StringBuilder pathBuffer = new StringBuilder();
  private int charBuffer = -2;
  private int charPosition = -1;
  private Stack<Token> tokens;

  public Tokenizer(Reader reader)
  {
    this.reader = reader;
  }

  public Token readToken() throws IOException
  {
    return readToken(null);
  }

  public Token readToken(Token token) throws IOException
  {
    if (tokens == null || tokens.isEmpty())
    {
      token = readNextToken(token);
    }
    else
    {
      if (token == null)
      {
        token = tokens.pop();
      }
      else
      {
        tokens.pop().copyTo(token);
      }
    }
    return token;
  }

  public void unreadToken(Token token)
  {
    if (tokens == null)
    {
      tokens = new Stack<Token>();
    }
    tokens.push(token);
  }

  private Token readNextToken(Token token) throws IOException
  {
    if (token == null)
    {
      token = new Token();
    }
    else
    {
      token.type = null;
      token.text = null;
      token.object = null;
      token.startPosition = charPosition;
    }
    int state = 0;
    buffer.setLength(0);
    pathBuffer.setLength(0);
    boolean end = false;

    do
    {
      char ch = readChar();

      switch (state)
      {
        case 0: // Expecting new token
          if (ch == (char)-1)
          {
            token.startPosition = charPosition;
            token.endPosition = charPosition;
            token.type = Token.EOF;
            token.text = "";
            unreadChar(ch);
            end = true;
          }
          else if (ch == OPEN_LIST_TOKEN.charAt(0))
          {
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            token.type = Token.OPEN_LIST;
            token.text = OPEN_LIST_TOKEN;
            end = true;
          }
          else if (ch == CLOSE_LIST_TOKEN.charAt(0))
          {
            token.startPosition = charPosition;
            token.endPosition = charPosition + 1;
            token.type = Token.CLOSE_LIST;
            token.text = CLOSE_LIST_TOKEN;
            end = true;
          }
          else if (isSeparator(ch))
          {
            // skip
          }
          else if (ch == '"') // start STRING
          {
            buffer.append(ch);
            token.startPosition = charPosition;
            state = 1;
          }
          else if (ch == PATH_REFERENCE_SEPARATOR)
          {
            pathBuffer.append(ch);
            buffer.setLength(0);
            token.type = Token.REFERENCE;
            token.startPosition = charPosition;
            token.object = new BList();
            state = 3;
          }
          else // start NUMBER, BOOLEAN, NULL or REFERENCE
          {
            buffer.append(ch);
            token.startPosition = charPosition;
            state = 2;
          }
          break;

        case 1: // Processing String
          if (ch == (char)-1)
          {
            // unterminated string
            token.endPosition = charPosition;
            token.type = Token.INVALID;
            try
            {
              token.object = Utils.unescapeString(buffer.toString());
            }
            catch (ParseException ex)
            {
            }
            token.text = buffer.toString();
            end = true;
          }
          else if (ch == '\n' || ch == '\r' || ch == '\t')
          {
            token.endPosition = charPosition + 1;
            token.type = Token.INVALID;
            try
            {
              token.object = Utils.unescapeString(buffer.toString());
            }
            catch (ParseException ex)
            {
            }
            token.text = buffer.toString();
            unreadChar(ch);
            end = true;
          }
          else if (ch == '"' && buffer.charAt(buffer.length() - 1) != '\\')
          {
            buffer.append(ch);
            token.endPosition = charPosition + 1;
            token.type = Token.STRING;
            try
            {
              token.object = Utils.unescapeString(buffer.toString());
            }
            catch (ParseException ex)
            {
              token.type = Token.INVALID;
            }
            token.text = buffer.toString();
            end = true;
          }
          else
          {
            buffer.append(ch);
          }
          break;

        case 2: // Processing token
          if (isSeparator(ch) ||
              ch == OPEN_LIST_TOKEN.charAt(0) || 
              ch == CLOSE_LIST_TOKEN.charAt(0) ||
              ch == (char)-1)
          {
            String text = buffer.toString();
            if (token.isType(Token.INVALID))
            {
              // return INVALID token
            }
            else if (Boolean.TRUE.toString().equals(text))
            {
              token.type = Token.BOOLEAN;
              token.object = TRUE;
            }
            else if (Boolean.FALSE.toString().equals(text))
            {
              token.type = Token.BOOLEAN;
              token.object = FALSE;
            }
            else if (String.valueOf((Object)null).equals(text))
            {
              token.type = Token.NULL;
            }
            else if ("Infinity".equals(text))
            {
              token.type = Token.NUMBER;
              token.object = Double.POSITIVE_INFINITY;
            }
            else if ("-Infinity".equals(text))
            {
              token.type = Token.NUMBER;
              token.object = Double.NEGATIVE_INFINITY;
            }
            else if ("NaN".equals(text))
            {
              token.type = Token.NUMBER;
              token.object = Double.NaN;
            }
            else if (mayBeNumber(text))
            {
              try
              {
                token.object = Utils.toNumber(text);
                token.type = Token.NUMBER;
              }
              catch (NumberFormatException ex)
              {
                token.type = Token.INVALID;
              }
            }
            else
            {
              Tag tag = Tag.parseTag(text);
              if (tag != null)
              {
                token.object = tag;
                token.type = Token.TAG;
              }
              else
              {
                token.type = Token.REFERENCE;
              }
            }
            token.text = text;
            token.endPosition = charPosition;
            unreadChar(ch);
            end = true;
          }
          else if (ch == '"')
          {
            buffer.append(ch);
            token.type = Token.INVALID;
          }
          else if (ch == PATH_REFERENCE_SEPARATOR)
          {
            token.type = Token.REFERENCE;         
            token.object = new BList();
            unreadChar(ch);
            state = 3;
          }
          else
          {
            // add char to token
            buffer.append(ch);
            if (isNameOperator())
            {
              token.object = null;
              token.type = Token.NAME_OPERATOR;
              token.endPosition = charPosition + 1;
              token.text = NAME_OPERATOR_TOKEN;
              end = true;
            }
          }
          break;
        case 3: // path reference name or index
          if (isSeparator(ch) ||
              ch == OPEN_LIST_TOKEN.charAt(0) || 
              ch == CLOSE_LIST_TOKEN.charAt(0) ||
              ch == PATH_REFERENCE_SEPARATOR ||
              ch == (char)-1)
          {
            pathBuffer.append(buffer);            
            String text = buffer.toString();
            if (text.length() > 0)
            {
              if (Character.isDigit(text.charAt(0)))
              {
                try
                {
                  ((BList)token.object).add(Integer.parseInt(text));  
                }
                catch (NumberFormatException ex)
                {
                  ((BList)token.object).add(text);                            
                  token.type = Token.INVALID;                    
                }
              }
              else 
              {
                if (text.startsWith("\"") && text.endsWith("\""))
                {
                  try
                  {
                    ((BList)token.object).add(Utils.unescapeString(text));
                  }
                  catch (ParseException ex)
                  {
                    ((BList)token.object).add(text);
                    token.type = Token.INVALID;
                  }
                }
                else
                {
                  ((BList)token.object).add(text);
                  if (text.contains("\""))
                  {
                    token.type = Token.INVALID;
                  }
                }
              }
            }
            if (ch == PATH_REFERENCE_SEPARATOR)
            {
              pathBuffer.append(ch);
              buffer.setLength(0);
            }
            else
            {
              token.endPosition = charPosition;
              token.text = pathBuffer.toString();
              if (token.object instanceof BList)
              {
                if (((BList)token.object).size() == 0)
                {
                  token.object = null;
                }
              }
              unreadChar(ch);
              end = true;
            }
          }
          else if (ch == '"')
          {
            buffer.append(ch);
            state = 4;
          }
          else
          {
            buffer.append(ch);
          }
          break;
        case 4: // path reference name string
          if (ch == (char)-1)
          {
            // unterminated string
            pathBuffer.append(buffer);
            token.endPosition = charPosition;
            token.type = Token.INVALID;
            token.text = pathBuffer.toString();
            end = true;
          }
          else if (ch == '\n' || ch == '\r' || ch == '\t')
          {
            pathBuffer.append(buffer);
            token.endPosition = charPosition + 1;
            token.type = Token.INVALID;
            token.text = pathBuffer.toString();
            unreadChar(ch);
            end = true;
          }
          else if (ch == '"' && buffer.charAt(buffer.length() - 1) != '\\')
          {
            buffer.append(ch);
            state = 3;
          }
          else
          {
            buffer.append(ch);
          }
          break;          
        default:
      }
    }
    while (!end);
    return token;
  }

  private boolean isNameOperator()
  {
    boolean isNameOperator = true;
    int i = 0;
    while (i < buffer.length() && isNameOperator)
    {
      isNameOperator = buffer.charAt(i) == NAME_OPERATOR_TOKEN.charAt(i);
      i++;
    }
    return isNameOperator;
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
    return ch <= ' ' || ch == '\u00a0';
  }

  private boolean mayBeNumber(String value)
  {
    char firstChar = value.charAt(0);
    if (Character.isDigit(firstChar)) return true;
    if (value.length() == 1) return false;

    if (firstChar == '.')
    {
      char secondChar = value.charAt(1);
      return Character.isDigit(secondChar);        
    }
    else if (firstChar == '+' || firstChar == '-')
    {
      char secondChar = value.charAt(1);
      if (Character.isDigit(secondChar)) return true;
      if (value.length() == 2) return false;
      if (secondChar == '.')
      {
        char thirdChar = value.charAt(2);
        return Character.isDigit(thirdChar);
      }
    }
    return false;
  }  
}
