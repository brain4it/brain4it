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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import org.brain4it.io.Token;
import static org.brain4it.io.TokenizerComments.COMMENT_FLAG;

public class ColoredView extends PlainView
{
  private final ColoredEditorKit kit;
  private Color indentLineColor = new Color(230, 230, 230);

  public ColoredView(Element elem, ColoredEditorKit kit)
  {
    super(elem);
    this.kit = kit;
  }

  @Override
  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1)
    throws BadLocationException
  {
    Component component = getContainer();
    ColoredDocument document = (ColoredDocument)getDocument();
    Segment text = getLineBuffer();

    int t = findIndexOfTokenContaining(p0);
    while (p0 < p1)
    {
      Token token = getToken(t);
      int p2 = getToken(t + 1).getStartPosition();
      if (p2 > p1)
      {
        p2 = p1;
      }
      document.getText(p0, p2 - p0, text);
      String type = token.getType();
      if ((token.getFlags() & COMMENT_FLAG) == COMMENT_FLAG &&
        !type.equals(Token.INVALID))
      {
        type = TextAppearance.COMMENT_TYPE;
      }
      else if (type.equals(Token.REFERENCE))
      {
        Set<String> functionNames = kit.getFunctionNames();
        String name = token.getText();
        if (functionNames != null && functionNames.contains(name))
        {
          type = TextAppearance.FUNCTION_TYPE;
        }
      }
      g.setFont(kit.getFontOf(type, component));
      g.setColor(kit.getColorOf(type, component));
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST,
        100);
      if (kit.getIndentSize() > 0)
      {
        drawIndentLines(text, x, y, g2);
      }
      x = Utilities.drawTabbedText(text, x, y, g, this, p0);
      p0 = p2;
      t++;
    }
    document.repaintAll(component); // repaint all when necessary
    return x;
  }

  @Override
  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1)
          throws BadLocationException
  {
    return drawUnselectedText(g, x, y, p0, p1);
  }

  protected void drawIndentLines(Segment text, int x, int y, Graphics2D g)
  {
    if (text.count == 0 || text.charAt(0) != ' ') return;
    
    Container container = (Container)getContainer();
    if (!(container instanceof JComponent)) return;

    JComponent component = (JComponent)container;
    int top = component.getInsets().top;
    int left = component.getInsets().left;
    if (x != left) return;

    Color color = g.getColor();
    int charWidth = g.getFontMetrics().charWidth(' ');
    int charHeight = g.getFontMetrics().getHeight();
    int indentSize = kit.getIndentSize();
    int length = (text.length() / indentSize) * indentSize;
    for (int i = 0; i < length; i += indentSize)
    {
      int column = x + i * charWidth;
      g.setColor(indentLineColor);
      g.drawLine(column, top + y - charHeight, column, top + y - 1);
    }
    g.setColor(color);
  }

  private int findIndexOfTokenContaining(int p0)
  {
    ColoredDocument doc = (ColoredDocument) getDocument();
    ArrayList<Token> tokens = doc.getTokens();
    boolean found = false;
    int index = 0;
    while (!found && index < tokens.size() - 1)
    {
      Token token1 = (Token) tokens.get(index);
      Token token2 = (Token) tokens.get(index + 1);
      if (token1.getStartPosition() <= p0 && p0 < token2.getStartPosition())
      {
        found = true;
      }
      else
      {
        index++;
      }
    }
    return found ? index : 0;
  }

  private Token getToken(int index)
  {
    ColoredDocument doc = (ColoredDocument) getDocument();
    ArrayList<Token> tokens = doc.getTokens();
    if (index >= tokens.size())
    {
      index = tokens.size() - 1;
    }
    return tokens.get(index);
  }
}
