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

package org.brain4it.manager.swing.text;

import java.awt.Color;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author realor
 */
public class SymbolMatcher implements DocumentListener, CaretListener
{
  private final JTextComponent textComponent;
  private final String openSymbols;
  private final String closeSymbols;
  private Color matchColor = Color.YELLOW;
  private Color unmatchColor = new Color(255, 128, 128);
  private Object matchTag1;
  private Object matchTag2;
  private Object unmatchTag;
  private boolean enabled;

  public SymbolMatcher(JTextComponent textComponent,
    String openSymbols, String closeSymbols)
  {
    if (!isValidSymbols(openSymbols, closeSymbols))
      throw new RuntimeException("Invalid symbols");

    this.textComponent = textComponent;
    this.openSymbols = openSymbols;
    this.closeSymbols = closeSymbols;
  }

  public Color getMatchColor()
  {
    return matchColor;
  }

  public void setMatchColor(Color matchColor)
  {
    this.matchColor = matchColor;
  }

  public Color getUnmatchColor()
  {
    return unmatchColor;
  }

  public void setUnmatchColor(Color unmatchColor)
  {
    this.unmatchColor = unmatchColor;
  }

  public boolean isEnabled()
  {
    return enabled;
  }
  
  public void setEnabled(boolean enabled)
  {
    if (this.enabled != enabled)
    {
      Document document = textComponent.getDocument();
      if (enabled)
      {
        document.addDocumentListener(this);
        textComponent.addCaretListener(this);
      }
      else
      {
        document.removeDocumentListener(this);
        textComponent.removeCaretListener(this);
      }
      this.enabled = enabled;
    }
  }
  
  private void updateHighlight()
  {
    try
    {
      Highlighter highlighter = textComponent.getHighlighter();
      if (matchTag1 != null) highlighter.removeHighlight(matchTag1);
      if (matchTag2 != null) highlighter.removeHighlight(matchTag2);
      if (unmatchTag != null) highlighter.removeHighlight(unmatchTag);

      String text = textComponent.getText();
      int length = text.length();

      int matchPosition = -2;
      int position = textComponent.getCaretPosition();
      // look symbol left
      if (position > 0 && position <= length)
      {
        int num = 0;
        char ch = text.charAt(position - 1);
        if (isOpenSymbol(ch))
        {
          matchPosition = -1;
          int j = position;
          while (j < length && matchPosition == -1)
          {
            char pch = text.charAt(j);
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
            char pch = text.charAt(j);
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
          matchTag1 = highlighter.addHighlight(matchPosition, matchPosition + 1,
            new DefaultHighlighter.DefaultHighlightPainter(matchColor));

          matchTag2 = highlighter.addHighlight(position - 1, position,
            new DefaultHighlighter.DefaultHighlightPainter(matchColor));
        }
        else if (matchPosition == -1)
        {
          unmatchTag = highlighter.addHighlight(position - 1, position,
            new DefaultHighlighter.DefaultHighlightPainter(unmatchColor));
        }
      }
      // look symbol right
      if (matchPosition == -2)
      {
        if (position < length)
        {
          int num = 0;
          char ch = text.charAt(position);
          if (isOpenSymbol(ch))
          {
            matchPosition = -1;
            int j = position + 1;
            while (j < length && matchPosition == -1)
            {
              char pch = text.charAt(j);
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
              char pch = text.charAt(j);
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
            matchTag1 = highlighter.addHighlight(matchPosition, matchPosition + 1,
              new DefaultHighlighter.DefaultHighlightPainter(matchColor));

            matchTag2 = highlighter.addHighlight(position, position + 1,
              new DefaultHighlighter.DefaultHighlightPainter(matchColor));
          }
          else if (matchPosition == -1)
          {
            unmatchTag = highlighter.addHighlight(position, position + 1,
              new DefaultHighlighter.DefaultHighlightPainter(unmatchColor));
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

  private boolean isValidSymbols(String openSymbols, String closeSymbols)
    throws RuntimeException
  {
    boolean valid = true;
    if (openSymbols != null && closeSymbols != null)
    {
      if (openSymbols.length() == closeSymbols.length())
      {
        String symbols = openSymbols + closeSymbols;
        int len = symbols.length();
        int i = 0;
        while (i < len && valid)
        {
          int j = i + 1;
          while (j < len && valid)
          {
            valid = symbols.charAt(i) != symbols.charAt(j);
            j++;
          }
          i++;
        }
      }
    }
    return valid;
  }

  @Override
  public void insertUpdate(DocumentEvent e)
  {
    updateHighlight();
  }

  @Override
  public void removeUpdate(DocumentEvent e)
  {
    updateHighlight();    
  }

  @Override
  public void changedUpdate(DocumentEvent e)
  {
    updateHighlight();
  }

  @Override
  public void caretUpdate(CaretEvent e)
  {
    updateHighlight();
  }
}
