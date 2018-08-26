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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author realor
 */
public class Formatter
{
  protected int indentSize = 2;
  protected int maxColumns = 60;
  protected HashSet<String> notInlineFunctions = new HashSet<String>();
  protected Tokenizer tokenizer;
  protected Writer writer;

  public Formatter()
  {
    notInlineFunctions.add("do");
    notInlineFunctions.add("cond");
    notInlineFunctions.add("for");
    notInlineFunctions.add("while");
  }

  public Set<String> getNotInlineFunctions()
  {
    return notInlineFunctions;
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
    ArrayList<Token> inlineTokens = new ArrayList<Token>();
    int inlineLevel = 0;
    boolean inline = true;
    int indent = 0;
    Token token;

    boolean end = false;
    while (!end)
    {
      token = tokenizer.readToken();
      if (token.isType(Token.EOF))
      {
        end = true;
      }
      else if (token.isType(Token.OPEN_LIST))
      {
        inlineTokens.add(token);
        if (inline)
        {
          inlineLevel++;
        }
        else
        {
          inlineLevel = 1;
          inline = true;
        }
      }
      else if (token.isType(Token.CLOSE_LIST))
      {
        if (inline)
        {
          inlineTokens.add(token);
          inlineLevel--;
          if (inlineLevel == 0)
          {
            printInline(indent, inlineTokens);
            if (!isEOF()) printCR();
            inlineTokens.clear();
            inline = false;
          }
        }
        else // !inline
        {
          indent--;
          indent(indent);
          printToken(token);
          printCR();
        }
      }
      else if (inline)
      {
        inlineTokens.add(token);
        if (isIndentRequired(inlineTokens, indent))
        {
          for (int i = inlineTokens.size() - 1; i >= 0; i--)
          {
            tokenizer.unreadToken(inlineTokens.remove(i));
          }
          indent(indent);
          token = tokenizer.readToken();
          if (token.isType(Token.OPEN_LIST))
          {
            printToken(token);
            token = tokenizer.readToken();
            if (token.isType(Token.REFERENCE) || token.isType(Token.TAG))
            {
              printToken(token);
            }
            else
            {
              tokenizer.unreadToken(token);
            }
            indent++;
          }
          else
          {
            printToken(token); // name
            printSpace();
            token = tokenizer.readToken(); 
            printToken(token); // arrow operator
          }
          printCR();
          inline = false;
        }
      }
      else // !inline
      {
        Token nextToken = tokenizer.readToken();
        if (nextToken.isType(Token.NAME_OPERATOR))
        {
          Token nextNextToken = tokenizer.readToken();
          if (!nextNextToken.isType(Token.OPEN_LIST))
          {
            // nextNextToken is a literal or reference
            indent(indent);
            printToken(token); // name
            printSpace();
            printToken(nextToken); // name operator
            printSpace();
            printToken(nextNextToken);
            printCR();
          }
          else // nextNextToken is a open list
          {
            inline = true;
            inlineLevel = 1;
            inlineTokens.add(token);
            inlineTokens.add(nextToken);
            inlineTokens.add(nextNextToken);
          }
        }
        else
        {
          indent(indent);
          printToken(token);
          printCR();
          tokenizer.unreadToken(nextToken);
        }
      }
    }
    if (inlineTokens.size() > 0)
    {
      printInline(indent, inlineTokens);
      if (!isEOF()) printCR();
    }
    this.writer = null;
  }
  
  protected void printInline(int indent, List<Token> tokens)
    throws IOException
  {
    indent(indent);
    for (int i = 0; i < tokens.size(); i++)
    {
      if (i > 0 && !tokens.get(i - 1).isType(Token.OPEN_LIST) &&
          !tokens.get(i).isType(Token.CLOSE_LIST))
      {
        printSpace();
      }
      printToken(tokens.get(i));
    }
  }

  protected void printSpace() throws IOException
  {
    writer.write(" ");
  }
  
  protected boolean isEOF() throws ParseException, IOException
  {
    Token token = tokenizer.readToken();
    boolean eof = token.isType(Token.EOF);
    tokenizer.unreadToken(token);
    return eof;
  }
  
  protected void printCR() throws IOException
  {
    writer.write("\n");
  }
  
  protected void printToken(Token token) throws IOException
  {
    writer.write(token.getText());
  }
  
  protected void indent(int indent) throws IOException
  {
    int k = indent * indentSize;
    for (int i = 0; i < k; i++)
    {
      writer.write(' ');
    }
  }

  protected boolean isIndentRequired(List<Token> inlineTokens, int indent)
  {
    if (containsNotInlineFunction(inlineTokens)) return true;
    
    int column = indent * indentSize;
    for (int i = 0; i < inlineTokens.size(); i++)
    {
      if (i > 0 && 
          !inlineTokens.get(i - 1).isType(Token.OPEN_LIST) &&
          !inlineTokens.get(i).isType(Token.CLOSE_LIST))
      {
        column++;
      }
      column += inlineTokens.get(i).getLength();
    }
    return column > maxColumns;
  }
  
  protected boolean containsNotInlineFunction(List<Token> inlineTokens)
  {
    boolean inline = true;
    int i = inlineTokens.size() - 1;
    while (i >= 0 && inline)
    {
      Token token = inlineTokens.get(i);
      if (token.isType(Token.REFERENCE))
      {
        inline = !notInlineFunctions.contains(token.text);
      }
      i--;
    }
    return !inline;
  }
}
