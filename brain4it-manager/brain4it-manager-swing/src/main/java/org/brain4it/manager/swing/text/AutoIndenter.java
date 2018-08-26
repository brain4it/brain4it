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

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.brain4it.io.IOConstants;

/**
 *
 * @author realor
 */
public class AutoIndenter implements DocumentListener
{
  private final JTextComponent textComponent;
  private boolean enabled;
  private int indentSize = 2;
  
  public AutoIndenter(JTextComponent textComponent)
  {
    this.textComponent = textComponent;
  }
  
  public boolean isEnabled()
  {
    return enabled;
  }
  
  public void setEnabled(boolean enabled)
  {
    if (this.enabled != enabled)
    {
      if (enabled)
      {
        textComponent.getDocument().addDocumentListener(this);
      }
      else
      {
        textComponent.getDocument().removeDocumentListener(this);
      }
      this.enabled = enabled;
    }
  }

  public int getIndentSize()
  {
    return indentSize;
  }

  public void setIndentSize(int indentSize)
  {
    this.indentSize = indentSize;
  }

  private void indentCursor()
  {
    try
    {
      int pos = textComponent.getCaretPosition();
      if (pos == 0) return;
      String text = textComponent.getText(0, pos - 1);
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
        textComponent.getDocument().insertString(pos, spaces.toString(), null);
      }
    }
    catch (BadLocationException ex)
    {      
      // ignore
    }
  }

  @Override
  public void insertUpdate(DocumentEvent e)
  {
    try
    {
      int offset = e.getOffset();
      int length = e.getLength();
      String text = e.getDocument().getText(offset, length);
      if (text.indexOf((char)10) != -1 || text.indexOf((char)13) != -1)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            indentCursor();            
          }
        });
      }
    }
    catch (BadLocationException ex)
    {
      // ignore
    }
  }

  @Override
  public void removeUpdate(DocumentEvent e)
  {
  }

  @Override
  public void changedUpdate(DocumentEvent e)
  {
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
