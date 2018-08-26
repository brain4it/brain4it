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

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 *
 * @author realor
 */
public class JSplitPaneZero extends JSplitPane
{
  private int dividerDragSize = 9;
  private int dividerDragOffset = 4;
    
  public JSplitPaneZero()
  {
    setDividerSize(1);
    setContinuousLayout(true);
  }

  public int getDividerDragSize()
  {
    return dividerDragSize;
  }

  public void setDividerDragSize(int dividerDragSize)
  {
    this.dividerDragSize = dividerDragSize;
  }

  public int getDividerDragOffset()
  {
    return dividerDragOffset;
  }

  public void setDividerDragOffset(int dividerDragOffset)
  {
    this.dividerDragOffset = dividerDragOffset;
  }
  
  @Override
  public void doLayout()
  {
    super.doLayout();

    // increase divider width or height
    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) getUI()).getDivider();
    Rectangle bounds = divider.getBounds();
    if (orientation == HORIZONTAL_SPLIT)
    {
      bounds.x -= dividerDragOffset;
      bounds.width = dividerDragSize;
    } else
    {
      bounds.y -= dividerDragOffset;
      bounds.height = dividerDragSize;
    }
    divider.setBounds(bounds);
  }

  @Override
  public void updateUI()
  {
    setUI(new SplitPaneWithZeroSizeDividerUI());
    revalidate();
  }

  private class SplitPaneWithZeroSizeDividerUI extends BasicSplitPaneUI
  {
    @Override
    public BasicSplitPaneDivider createDefaultDivider()
    {
      return new ZeroSizeDivider(this);
    }
  }

  private class ZeroSizeDivider extends BasicSplitPaneDivider
  {
    public ZeroSizeDivider(BasicSplitPaneUI ui)
    {
      super(ui);
      super.setBorder(null);
      setBackground(UIManager.getColor("controlShadow"));
    }

    @Override
    public void setBorder(Border border)
    {
      // ignore
    }

    @Override
    public void paint(Graphics g)
    {
      g.setColor(getBackground());
      if (orientation == HORIZONTAL_SPLIT)
      {
        g.drawLine(dividerDragOffset, 0, dividerDragOffset, getHeight() - 1);
      } else
      {
        g.drawLine(0, dividerDragOffset, getWidth() - 1, dividerDragOffset);
      }
    }

    @Override
    protected void dragDividerTo(int location)
    {
      super.dragDividerTo(location + dividerDragOffset);
    }

    @Override
    protected void finishDraggingTo(int location)
    {
      super.finishDraggingTo(location + dividerDragOffset);
    }
  }

  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        JSplitPaneZero z = new JSplitPaneZero();
        z.add(new JTextPane(), JSplitPane.LEFT);
        z.add(new JTextPane(), JSplitPane.RIGHT);
        z.setOrientation(JSplitPane.VERTICAL_SPLIT);
        frame.getContentPane().add(z);
        frame.setVisible(true);
      }
    });
  }
}
