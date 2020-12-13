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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.brain4it.io.IOConstants;
import org.brain4it.io.Token;
import org.brain4it.io.TokenizerComments;
import static org.brain4it.io.TokenizerComments.COMMENT_FLAG;

/**
 *
 * @author realor
 */
public class ColoredTextPane extends JTextPane
{
  private TextAppearance textAppearance;
  private Set<String> functionNames;
  private Set<String> globalNames;
  private Component wrapComponent;

  public ColoredTextPane()
  {
    this(new TextAppearance());
  }

  public ColoredTextPane(TextAppearance textAppearance)
  {
    this.textAppearance = textAppearance;
  }

  public TextAppearance getTextAppearance()
  {
    return textAppearance;
  }

  public void setTextAppearance(TextAppearance textAppearance)
  {
    this.textAppearance = textAppearance;
  }

  public Set<String> getFunctionNames()
  {
    return functionNames;
  }

  public void setFunctionNames(Set<String> functionNames)
  {
    this.functionNames = functionNames;
  }

  public Set<String> getGlobalNames()
  {
    return globalNames;
  }

  public void setGlobalNames(Set<String> globalNames)
  {
    this.globalNames = globalNames;
  }

  public Component getWrapComponent()
  {
    return wrapComponent;
  }

  public void setWrapComponent(Component wrapComponent)
  {
    this.wrapComponent = wrapComponent;
  }

  @Override
  public Dimension getPreferredSize()
  {
    Dimension size = super.getPreferredSize();
    if (wrapComponent != null)
    {
      return new Dimension(wrapComponent.getWidth(), size.height);
    }
    return size;
  }

  public void appendText(String text)
  {
    try
    {
      Document document = getDocument();
      int offset = document.getLength();
      document.insertString(offset, text, null);
      colorize(offset, document.getLength());
    }
    catch (BadLocationException ex)
    {
    }
  }

  public void appendText(String text, AttributeSet attrSet)
  {
    try
    {
      Document document = getDocument();
      int offset = document.getLength();
      document.insertString(offset, text, attrSet);
    }
    catch (BadLocationException ex)
    {
    }
  }

  protected void colorize()
  {
    colorize(0, getText().length());
  }

  protected void colorize(int start, int end)
  {
    try
    {
      HashMap<String, AttributeSet> cache =
        new HashMap<String, AttributeSet>();
      StyledDocument doc = (StyledDocument)getDocument();
      doc.putProperty(
        DefaultEditorKit.EndOfLineStringProperty, "\n");

      String text = getText(start, end - start);
      StringReader reader = new StringReader(text);
      TokenizerComments tokenizer = new TokenizerComments(reader);
      Token token = new Token();
      do
      {
        token = tokenizer.readToken(token);
        String type = token.getType();
        if ((token.getFlags() & COMMENT_FLAG) == COMMENT_FLAG &&
          !type.equals(Token.INVALID))
        {
          type = TextAppearance.COMMENT_TYPE;
        }
        else if (token.isType(Token.REFERENCE))
        {
          String name = token.getText();
          if (functionNames != null && functionNames.contains(name))
          {
            type = TextAppearance.FUNCTION_TYPE;
          }
          else
          {
            int index = name.indexOf(IOConstants.PATH_REFERENCE_SEPARATOR);
            if (index != -1) name = name.substring(0, index);

            if (globalNames != null && globalNames.contains(name))
            {
              type = TextAppearance.GLOBAL_TYPE;
            }
          }
        }
        AttributeSet attrSet = cache.get(type);
        if (attrSet == null)
        {
          attrSet = createAttributeSet(type);
          cache.put(type, attrSet);
        }
        doc.setCharacterAttributes(start + token.getStartPosition(),
          token.getLength(), attrSet, false);
      } while (!token.isType(Token.EOF));
    }
    catch (Exception ex)
    {
      // ignore all exception
    }
  }

  protected AttributeSet createAttributeSet(String type)
  {
    Color color = textAppearance.getColor(type);
    if (color == null) color = getForeground();

    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet attrSet = sc.addAttribute(SimpleAttributeSet.EMPTY,
      StyleConstants.Foreground, color);

    Integer style = textAppearance.getStyle(type);
    if (style != null)
    {
      if ((style & Font.BOLD) != 0)
      {
        attrSet = sc.addAttribute(attrSet, StyleConstants.Bold, true);
      }
      if ((style & Font.ITALIC) != 0)
      {
        attrSet = sc.addAttribute(attrSet, StyleConstants.Italic, true);
      }
    }
    return attrSet;
  }
}
