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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author realor
 */
public class Formatter
{
  protected Configuration configuration;
  protected Tokenizer tokenizer;
  protected Writer writer;
  protected TokenList baseList;
  protected TokenList currentList;
  protected int indentLevel;
  protected boolean written;

  public Formatter()
  {
    configuration = new Configuration();
    configuration.setDefaults();
  }

  public Formatter(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public Configuration getConfiguration()
  {
    return configuration;
  }

  public String format(String code) throws ParseException
  {
    Reader stringReader = new StringReader(code);
    Writer stringWriter = new StringWriter();
    try
    {
      format(stringReader, stringWriter);
    }
    catch (IOException ex)
    {
    }
    return stringWriter.toString();
  }

  public void format(Reader reader, Writer writer)
    throws IOException, ParseException
  {
    this.tokenizer = new TokenizerComments(reader);
    this.writer = writer;
    this.baseList = null;
    this.currentList = null;
    this.indentLevel = 0;
    this.written = false;
    boolean inline = false;
    boolean nameOperator = false;
    Token token;

    boolean end = false;
    while (!end)
    {
      token = tokenizer.readToken();
      if (token.isType(Token.EOF))
      {
        if (baseList != null)
        {
          baseList.print();
        }
        end = true;
      }
      else if (inline)
      {
        addToken(token);
        if (currentList == null) // closed list
        {
          if (nameOperator)
          {
            printSpace();
          }
          else
          {
            nextLine();
          }
          baseList.print();
          baseList = null;
          inline = false;
          nameOperator = false;
        }
        else if (baseList.isTooLarge() || baseList.isBreakRequired())
        {
          baseList.releaseExcedent();
          nextLine();
          baseList.print();
          baseList = null;
          currentList = null;
          indentLevel++;
          inline = false;
          nameOperator = false;
        }
      }
      else // !inline
      {
        if (token.isType(Token.OPEN_LIST))
        {
          baseList = new TokenList(token);
          currentList = baseList;
          inline = true;
        }
        else if (token.isType(Token.CLOSE_LIST))
        {
          indentLevel--;
          nextLine();
          printToken(token);
        }
        else if (token.isType(Token.NAME_OPERATOR))
        {
          printSpace();
          printToken(token);
          nameOperator = true;
        }
        else
        {
          if (nameOperator)
          {
            printSpace();
            nameOperator = false;
          }
          else
          {
            nextLine();
          }
          printToken(token);
        }
      }
    }
  }

  void addToken(Token token)
  {
    if (token.isType(Token.OPEN_LIST))
    {
      TokenList tokenList = new TokenList(token, currentList);
      currentList.add(tokenList);
      currentList = tokenList;
    }
    else if (token.isType(Token.CLOSE_LIST))
    {
      currentList.add(token);
      currentList = currentList.parent;
    }
    else
    {
      currentList.add(token);
    }
  }

  void nextLine() throws IOException
  {
    if (written)
    {
      printCR();
      for (int i = 0; i < this.indentLevel; i++)
      {
        printIndent(this.configuration.indentSize);
      }
    }
  }

  void printToken(Token token) throws IOException
  {
    print(token);
    written = true;
  }

  protected void printCR() throws IOException
  {
    writer.write("\n");
  }

  protected void printIndent(int indentSize) throws IOException
  {
    for (int i = 0; i < indentSize; i++)
    {
      writer.write(" ");
    }
  }

  protected void print(Token token) throws IOException
  {
    writer.write(token.getText());
  }

  protected void printSpace() throws IOException
  {
    writer.write(" ");
  }

  protected class TokenList
  {
    protected ArrayList elements = new ArrayList();
    protected TokenList parent;

    protected TokenList(Token openToken)
    {
      elements.add(openToken);
    }

    protected TokenList(Token openToken, TokenList parent)
    {
      this.parent = parent;
      elements.add(openToken);
    }

    protected boolean isBreakRequired()
    {
      String functionName = getFunctionName();
      if (!configuration.notInlineFunctions.contains(functionName))
        return false;

      Integer arguments = configuration.inlineArguments.get(functionName);
      if (arguments == null) return true;

      return getCompletedArguments() >= arguments;
    }

    protected boolean isTooLarge()
    {
      return getLength() > configuration.maxColumns;
    }

    protected void releaseExcedent()
    {
      String functionName = getFunctionName();
      if (functionName == null)
      {
        unreadTokens(1);
      }
      else
      {
        Integer arguments = configuration.inlineArguments.get(functionName);
        if (arguments == null)
        {
          unreadTokens(2);
        }
        else
        {
          int count = arguments + 2;
          if (baseList.getLength(count) < configuration.maxColumns)
          {
            unreadTokens(count);
          }
          else
          {
            unreadTokens(2);
          }
        }
      }
    }

    protected void add(Object elem)
    {
      elements.add(elem);
    }

    protected void unreadTokens(int tokensLeft)
    {
      for (int i = elements.size() - 1; i >= tokensLeft; i--)
      {
        Object elem = elements.get(i);
        if (elem instanceof Token)
        {
          Token token = (Token)elem;
          tokenizer.unreadToken(token);
        }
        else if (elem instanceof TokenList)
        {
          TokenList tokenList = (TokenList)elem;
          tokenList.unreadTokens(0);
        }
        elements.remove(i);
      }
    }

    protected boolean isClosed()
    {
      int size = elements.size();
      Object last = elements.get(size - 1);
      if (last instanceof Token)
      {
        Token token = (Token)last;
        return token.isType(Token.CLOSE_LIST);
      }
      return false;
    }

    protected int getCompletedArguments()
    {
      int size = elements.size();
      Object last = elements.get(size - 1);
      if (last instanceof Token)
      {
        Token token = (Token)last;
        return token.isType(Token.CLOSE_LIST) ? size - 3 : size - 2;
      }
      else // TokenList
      {
        TokenList tokenList = (TokenList)last;
        return tokenList.isClosed() ? size - 2 : size - 3;
      }
    }

    protected int getLength()
    {
      return getLength(elements.size());
    }

    protected int getLength(int count)
    {
      int length = 0;
      for (int i = 0; i < count; i++)
      {
        Object elem = elements.get(i);
        if (i > 1 &&
          !(elem instanceof Token && ((Token)elem).isType(Token.CLOSE_LIST)))
        {
          length++; // space
        }
        if (elem instanceof Token)
        {
          Token token = (Token)elem;
          length += token.getLength();
        }
        else // TokenList
        {
          TokenList tokenList = (TokenList)elem;
          length += tokenList.getLength();
        }
      }
      return length;
    }

    protected void print() throws IOException
    {
      for (int i = 0; i < elements.size(); i++)
      {
        Object elem = elements.get(i);
        if (i > 1 &&
          !(elem instanceof Token && ((Token)elem).isType(Token.CLOSE_LIST)))
        {
          printSpace();
        }
        if (elem instanceof Token)
        {
          Token token = (Token)elem;
          printToken(token);
        }
        else // TokenList
        {
          TokenList tokenList = (TokenList)elem;
          tokenList.print();
        }
      }
    }

    protected String getFunctionName()
    {
      if (elements.size() < 2) return null;
      Object elem = elements.get(1);
      if (!(elem instanceof Token)) return null;
      Token token = (Token)elem;
      if (!token.isType(Token.REFERENCE)) return null;
      return token.getText();
    }
  }

  public static class Configuration
  {
    int indentSize = 2;
    int maxColumns = 20;
    HashMap<String, Integer> inlineArguments = new HashMap<String, Integer>();
    HashSet<String> notInlineFunctions = new HashSet<String>();

    public Configuration()
    {
    }

    public void setDefaults()
    {
      indentSize = 2;
      maxColumns = 20;

      notInlineFunctions.add("do");
      notInlineFunctions.add("cond");
      notInlineFunctions.add("for");
      notInlineFunctions.add("while");

      inlineArguments.put("function", 1);
      inlineArguments.put("if", 1);
      inlineArguments.put("set", 1);
      inlineArguments.put("while", 1);
      inlineArguments.put("for", 3);
      inlineArguments.put("for-each", 2);
      inlineArguments.put("apply", 2);
    }

    public Set<String> getNotInlineFunctions()
    {
      return notInlineFunctions;
    }

    public HashMap<String, Integer> getInlineArguments()
    {
      return inlineArguments;
    }

    public int getIndentSize()
    {
      return indentSize;
    }

    public void setIndentSize(int indentSize)
    {
      this.indentSize = indentSize;
    }

    public int getMaxColumns()
    {
      return maxColumns;
    }

    public void setMaxColumns(int maxColumns)
    {
      this.maxColumns = maxColumns;
    }
  }

  public static void main(String[] args)
  {
    try
    {
      Formatter formatter = new Formatter();
      System.out.println(formatter.format("8 (do 5 6) (* 5 6)"));
    }
    catch (Exception ex)
    {
    }
  }
}