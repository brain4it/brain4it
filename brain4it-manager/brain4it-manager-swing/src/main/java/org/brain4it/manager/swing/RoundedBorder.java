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
package org.brain4it.manager.swing;

/**
 *
 * @author realor
 */
import java.awt.*;
import javax.swing.border.*;

public class RoundedBorder implements Border
{
  private final int radius;
  private final int margin;

  public RoundedBorder(int radius, int margin)
  {
    this.radius = radius;
    this.margin = margin;
  }

  @Override
  public Insets getBorderInsets(Component c)
  {
    return new Insets(margin + radius + 1, margin + radius + 1, 
      margin + radius + 2, margin + radius);
  }

  @Override
  public boolean isBorderOpaque()
  {
    return true;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, 
    int width, int height)
  {
    Graphics2D g2d = (Graphics2D)g;
    g.setColor(c.getBackground().darker());
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.drawRoundRect(x + margin, y + margin, 
      width - 2 * margin - 1, height - 2 * margin - 1, 
      radius, radius);
  }
}
