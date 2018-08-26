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

package org.brain4it.manager.android;

import android.text.Editable;
import android.text.TextWatcher;
import org.brain4it.io.IOConstants;

/**
 *
 * @author realor
 */
public class AutoIndenter implements TextWatcher
{
  int position = -1;
  int indentSize = 2;
  
  public int getIndentSize()
  {
    return indentSize;
  }

  public void setIndentSize(int indentSize)
  {
    this.indentSize = indentSize;
  }

  @Override
  public void onTextChanged(CharSequence cs, int start, int before, int count)
  {
    position = -1;
    int i = 0;
    int k = start;
    while (i < count && position == -1)
    {
      if (cs.charAt(k) == '\n' && 
          (k + 1 == cs.length() || isIndentableChar(cs.charAt(k + 1))))
      {
        position = k; // found CR to indent at position
      }
      else
      {
        i++;
        k++;
      }
    }
  }

  @Override
  public void beforeTextChanged(CharSequence cs,
          int start, int count, int after)
  {
  }

  @Override
  public void afterTextChanged(Editable editable)
  {
    if (position != -1)
    {
      String text = editable.toString().substring(0, position);
      int index = text.lastIndexOf('\n');
      String lastLine = text.substring(index + 1);
      if (lastLine.length() > 0)
      {
        StringBuilder spaces = new StringBuilder();
        int i = 0;
        while (i < lastLine.length() && lastLine.charAt(i) == ' ')
        {
          spaces.append(' ');
          i++;
        }
        if (i < lastLine.length() && 
            lastLine.charAt(i) == IOConstants.OPEN_LIST_TOKEN.charAt(0) &&
            !isBalanced(lastLine, spaces.length()))
        {
          for (int j = 0; j < indentSize; j++)
          {
            spaces.append(' ');
          }
        }
        editable.insert(position + 1, spaces.toString());
      }        
    }
  }
  
  private boolean isIndentableChar(char ch)
  {
    return ch != ' ' && ch != IOConstants.CLOSE_LIST_TOKEN.charAt(0);
  }

  private boolean isBalanced(String line, int offset)
  {
    int level = 0;
    for (int i = offset; i < line.length(); i++)
    {
      char ch = line.charAt(i);
      if (ch == IOConstants.OPEN_LIST_TOKEN.charAt(0)) level++;
      else if (ch == IOConstants.CLOSE_LIST_TOKEN.charAt(0)) level--;
    }
    return level == 0;
  }
}
