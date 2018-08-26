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

package org.brain4it.lang;

/**
 *
 * @author realor
 */
public class BException extends RuntimeException
{
  private final BList list;

  public BException(BList list)
  {
    this(list, null);
  }

  public BException(BList list, Throwable cause)
  {
    super(getMessage(list), cause);
    this.list = list;
  }

  public BException(String type)
  {
    super(type, null);
    this.list = new BList(1);
    list.add(type);
  }
  
  public BException(String type, String message)
  {
    super(type + ": " + message, null);
    this.list = new BList(2);
    list.add(type);
    list.add(message);
  }

  public String getType()
  {
    return (String)list.get(0);
  }

  public BList toList()
  {
    return list;
  }

  private static String getMessage(BList list)
  {
    String message = (String)list.get(0);
    if (list.size() > 1)
    {
      message += ": " + list.get(1);
    }
    return message;
  }
}
