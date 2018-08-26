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

package org.brain4it.manager;

import java.util.ArrayList;

/**
 *
 * @author realor
 */
public class CommandHistory
{
  private static final int DEFAULT_HISTORY_SIZE = 20;
  private int historySize;
  private ArrayList<String> history;
  private int historyIndex;

  public CommandHistory()
  {
    this(DEFAULT_HISTORY_SIZE);
  }

  public CommandHistory(int size)
  {
    this.historySize = size;
    this.history = new ArrayList<String>(size);
  }
  
  public void add(String command)
  {
    if (command != null && command.trim().length() > 0)
    {
      int index = history.indexOf(command);
      if (index != -1)
      {
        history.remove(index);
      }
      else if (history.size() > historySize)
      {
        history.remove(0);
      }
      history.add(command);
      historyIndex = history.size();
    }
  }
  
  public boolean isEmpty()
  {
    return history.isEmpty();
  }
  
  public String getPrevious()
  {
    if (history.isEmpty()) return null;
    
    if (historyIndex <= 0) 
    {
      historyIndex = history.size() - 1;
    }
    else
    {
      historyIndex--;
    }
    return history.get(historyIndex);
  }

  public String getNext()
  {
    if (history.isEmpty()) return null;
    
    if (historyIndex >= history.size() - 1) 
    {
      historyIndex = 0;
    }
    else
    {
      historyIndex++;
    }
    return history.get(historyIndex);
  }  
}
