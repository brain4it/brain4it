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

/**
 *
 * @author realor
 */
public class HTMLFormatter extends Formatter
{
  public static final String HARD_REFERENCE_CLASS = "hard_reference";
  public static final String COMMENT_CLASS = "comment";
  // prefix to consider reference as soft. This prefix is never shown.
  public static final String SOFT_REFERENCE_PREFIX = "%soft_";
  protected boolean highlightFunctions = true;
  protected boolean lastOpenList;

  public boolean isHighlightFunctions()
  {
    return highlightFunctions;
  }

  public void setHighlightFunctions(boolean highlightFunctions)
  {
    this.highlightFunctions = highlightFunctions;
  }

  @Override
  protected void printIndent(int indentSize) throws IOException
  {
    for (int i = 0; i < indentSize; i++)
    {
      writer.write("&nbsp;");
    }
  }

  @Override
  protected void print(Token token) throws IOException
  {
    String type = token.getType();
    String value = token.text;
    String className;

    if (highlightFunctions)
    {
      if (isHardReference(type, value))
      {
        className = HARD_REFERENCE_CLASS;
      }
      else
      {
        className = type.toLowerCase();
      }
      lastOpenList = type.equals(Token.OPEN_LIST);
    }
    else
    {
      className = type.toLowerCase();
    }
    if (value.startsWith(SOFT_REFERENCE_PREFIX))
    {
      value = value.substring(SOFT_REFERENCE_PREFIX.length());
    }
    writer.write("<span class=\"");
    writer.write(className);
    if ((token.getFlags() & TokenizerComments.COMMENT_FLAG) ==
      TokenizerComments.COMMENT_FLAG)
    {
      writer.write(" ");
      writer.write(COMMENT_CLASS);
    }
    writer.write("\">");
    writer.write(value);
    writer.write("</span>");
  }

  @Override
  protected void printCR() throws IOException
  {
    writer.write("<br>");
  }

  @Override
  protected void printSpace() throws IOException
  {
    writer.write(' ');
  }

  protected boolean isHardReference(String type, String value)
  {
    // heuristic method to detect if a reference is a built-in function
    if (lastOpenList && type.equals(Token.REFERENCE))
    {
      if (value.contains("_")) return false;
      if (value.contains("?")) return false;
      if (value.contains("/") && value.length() > 1) return false;
      if ("parameter".equals(value)) return false;
      if ("text".equals(value)) return false;
      if ("value".equals(value)) return false;
      if ("context".equals(value)) return false;
      if ("data".equals(value)) return false;
      if (value.length() > 2) return true;
      if (!Character.isLetter(value.charAt(0))) return true;
      if ("do".equals(value)) return true;
      if ("if".equals(value)) return true;
      if ("or".equals(value)) return true;
    }
    return false;
  }
}
