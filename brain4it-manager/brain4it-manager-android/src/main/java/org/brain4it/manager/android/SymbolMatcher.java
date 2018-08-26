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

import android.graphics.Color;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import org.brain4it.io.IOConstants;

/**
 *
 * @author realor
 */
public class SymbolMatcher
{
  private final String openSymbols;
  private final String closeSymbols;
  private final BackgroundColorSpan matchSpan1 = 
    new BackgroundColorSpan(Color.argb(255, 255, 255, 0));
  private final BackgroundColorSpan matchSpan2 = 
    new BackgroundColorSpan(Color.argb(255, 255, 255, 0));
  private final BackgroundColorSpan unmatchSpan = 
    new BackgroundColorSpan(Color.argb(255, 255, 128, 128));

  public SymbolMatcher()
  {
    this.openSymbols = IOConstants.OPEN_LIST_TOKEN;
    this.closeSymbols = IOConstants.CLOSE_LIST_TOKEN;
  }

  public void updateHighlight(Spannable spannable, int position)
  {
    try
    {
      spannable.removeSpan(matchSpan1);
      spannable.removeSpan(matchSpan2);
      spannable.removeSpan(unmatchSpan);
      
      int length = spannable.length();

      int matchPosition = -2;
      if (position > 0 && position <= length)
      {
        int num = 0;
        char ch = spannable.charAt(position - 1);
        if (isOpenSymbol(ch))
        {
          matchPosition = -1;
          int j = position;
          while (j < length && matchPosition == -1)
          {
            char pch = spannable.charAt(j);
            if (isOpenSymbol(pch) && ch == pch) num++;
            else if (isCloseSymbol(pch) && match(ch, pch))
            {
              if (num == 0) matchPosition = j;
              else num--;
            }
            j++;
          }
        }
        else if (isCloseSymbol(ch))
        {
          matchPosition = -1;
          int j = position - 2;
          while (j >= 0 && matchPosition == -1)
          {
            char pch = spannable.charAt(j);
            if (isCloseSymbol(pch) && ch == pch) num++;
            else if (isOpenSymbol(pch) && match(pch, ch))
            {
              if (num == 0) matchPosition = j;
              else num--;
            }
            j--;
          }
        }
        if (matchPosition >= 0)
        {
          spannable.setSpan(matchSpan1, matchPosition, matchPosition + 1, 
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          spannable.setSpan(matchSpan2, position - 1, position, 
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if (matchPosition == -1)
        {
          spannable.setSpan(unmatchSpan, position - 1, position, 
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      // look symbol right
      if (matchPosition == -2)
      {
        if (position < length)
        {
          int num = 0;
          char ch = spannable.charAt(position);
          if (isOpenSymbol(ch))
          {
            matchPosition = -1;
            int j = position + 1;
            while (j < length && matchPosition == -1)
            {
              char pch = spannable.charAt(j);
              if (isOpenSymbol(pch) && ch == pch) num++;
              else if (isCloseSymbol(pch) && match(ch, pch))
              {
                if (num == 0) matchPosition = j;
                else num--;
              }
              j++;
            }
          }
          else if (isCloseSymbol(ch))
          {
            matchPosition = -1;
            int j = position - 1;
            while (j >= 0 && matchPosition == -1)
            {
              char pch = spannable.charAt(j);
              if (isCloseSymbol(pch) && ch == pch) num++;
              else if (isOpenSymbol(pch) && match(pch, ch))
              {
                if (num == 0) matchPosition = j;
                else num--;
              }
              j--;
            }
          }
          if (matchPosition >= 0)
          {
            spannable.setSpan(matchSpan1, matchPosition, matchPosition + 1, 
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(matchSpan2, position, position + 1, 
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
          else if (matchPosition == -1)
          {
            spannable.setSpan(unmatchSpan, position, position + 1, 
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        }
      }
    }
    catch (Exception ex)
    {
    }
  }
  
  private boolean isOpenSymbol(char ch)
  {
    return openSymbols.indexOf(ch) != -1;
  }

  private boolean isCloseSymbol(char ch)
  {
    return closeSymbols.indexOf(ch) != -1;
  }

  private boolean match(char open, char close)
  {
    return openSymbols.indexOf(open) == closeSymbols.indexOf(close);
  }
}
