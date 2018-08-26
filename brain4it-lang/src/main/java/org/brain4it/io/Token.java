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

/**
 *
 * @author realor
 */
public class Token
{
  public static final String EOF = "EOF";
  public static final String NULL = "NULL";
  public static final String NUMBER = "NUMBER";
  public static final String STRING = "STRING";
  public static final String BOOLEAN = "BOOLEAN";
  public static final String REFERENCE = "REFERENCE";
  public static final String OPEN_LIST = "OPEN_LIST";
  public static final String CLOSE_LIST = "CLOSE_LIST";
  public static final String NAME_OPERATOR = "NAME_OPERATOR";
  public static final String TAG = "TAG";
  public static final String INVALID = "INVALID";

  String type;
  String text;
  int startPosition;
  int endPosition;
  Object object;
  int flags;

  public String getType()
  {
    return type;
  }

  public String getText()
  {
    return text;
  }
  
  public boolean isType(String type)
  {
    return type.equals(this.type);
  }

  public Object getObject()
  {
    return object;
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
  
  public int getFlags()
  {
    return flags;
  }

  public void copyTo(Token token)
  {
    token.type = type;
    token.text = text;
    token.startPosition = startPosition;
    token.endPosition = endPosition;
    token.object = object;
    token.flags = flags;
  }
  
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("[");
    buffer.append(type);
    buffer.append(", \"");
    buffer.append(text);
    buffer.append("\", ");
    buffer.append(startPosition);
    buffer.append(", ");
    buffer.append(endPosition);
    buffer.append(", ");
    buffer.append(Integer.toHexString(flags));
    buffer.append("]");
    return buffer.toString();
  }
}
