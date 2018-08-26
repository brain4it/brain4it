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
import java.awt.Font;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ColoredEditorKit extends StyledEditorKit 
  implements ViewFactory
{
  private TextAppearance textAppearance;  
  private Set<String> functionNames;
  
  public ColoredEditorKit()
  {
    this(new TextAppearance());
  }

  public ColoredEditorKit(TextAppearance textAppearance)
  {
    this.textAppearance = textAppearance;
  }
  
  @Override
  public Document createDefaultDocument()
  {
    return new ColoredDocument();
  }

  @Override
  public ViewFactory getViewFactory()
  {
    return this;
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
    
  @Override
  public View create(Element elem)
  {
    return new ColoredView(elem, this);
  }

  public Font getFontOf(String type, Component component)
  {
    Font font = component.getFont();
    Integer style = textAppearance.getStyle(type);
    if (style != null && font.getStyle() != style)
    {
      font = font.deriveFont(style);
    }
    return font;
  }
  
  public Color getColorOf(String type, Component component)
  {
    Color color = (Color)textAppearance.getColor(type);
    if (color == null) color = component.getForeground();
    return color;
  }  
}