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

package org.brain4it.manager.swing.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.StickWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class StickWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected int lastX;
  protected int lastY;
  protected float deltaX;
  protected float deltaY;
  protected boolean firstDraw = true;
  protected BasicStroke stroke1;
  protected BasicStroke stroke2;
  protected String setValueFunction;
  protected int invokeInterval = 100;
  protected FunctionInvoker invoker;
  
  public StickWidget()
  {
    initComponents();
    int scalingFactor = ManagerApp.getPreferences().getScalingFactor();
    stroke1 = new BasicStroke(scalingFactor);
    stroke2 = new BasicStroke(2 * scalingFactor);
  }
  
  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    StickWidgetType type = 
      (StickWidgetType)WidgetType.getType(WidgetType.STICK);
    
    type.validate(properties);
    
    invokeInterval = type.getInvokeInterval(properties);
    
    BSoftReference func = type.getSetValueFunction(properties);
    if (func != null)
    {
      setValueFunction = func.getValue();
      if (dashboard != null)
      {
        if (invokeInterval == 0)
        {
          invoker = new FunctionInvoker(dashboard.getInvoker(), 
            setValueFunction);          
        }
        else
        {
          invoker = new FunctionInvoker(dashboard.getInvoker(), 
            setValueFunction, dashboard.getTimer(), invokeInterval);
        }
      }
    }    
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);
    
    int width = getWidth();
    int height = getHeight();
    int cx = width / 2;
    int cy = height / 2;
    int outerRadius = Math.min(cx, cy);
    int ballRadius = outerRadius / 10;
    int radius = outerRadius - ballRadius;

    if (firstDraw || dashboard == null)
    {
      lastX = cx;
      lastY = cy;
      firstDraw = false;
    }
    g2.setStroke(stroke2);
    
    g2.setColor(Color.BLACK);
    // circle
    g2.drawOval(cx - radius, cy - radius, 2 * radius, 2 * radius);
    // ball
    g2.drawOval(lastX - ballRadius, lastY - ballRadius, 
      2 * ballRadius, 2 * ballRadius);

    g2.setStroke(stroke1);

    // vertical line
    g2.drawLine(cx, cy - radius, cx, cy + radius);
    // horizontal line
    g2.drawLine(cx - radius, cy, cx + radius, cy);

    
    g2.setStroke(stroke2);
    
    // line center to ball
    g2.drawLine(cx, cy, lastX, lastY);
    
    float fontSize = Math.round(height / 15);
    g2.setFont(g2.getFont().deriveFont(fontSize));    
    g2.drawString("X: " + (int)Math.round(100 * deltaX), 0, fontSize);
    g2.drawString("Y: " + (int)Math.round(100 * deltaY), 0, 2 * fontSize);    
  }
  
  private void initComponents()
  {
    addMouseListener(new MouseListener()
    {
      @Override
      public void mousePressed(MouseEvent event)
      {
        updatePosition(event.getX(), event.getY());
      }      
      
      @Override
      public void mouseReleased(MouseEvent event)
      {
        resetPosition();
      }

      @Override
      public void mouseClicked(MouseEvent e)
      {
      }

      @Override
      public void mouseEntered(MouseEvent e)
      {
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
      }
    });
    
    addMouseMotionListener(new MouseMotionListener()
    {
      @Override
      public void mouseDragged(MouseEvent event)
      {
        updatePosition(event.getX(), event.getY());
      }

      @Override
      public void mouseMoved(MouseEvent e)
      {
      }
    });
    
    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent e)
      {
        resetPosition();
      }
    });
  }

  private void updatePosition(int x, int y)
  {
    int width = getWidth();
    int height = getHeight();
    int cx = width / 2;
    int cy = height / 2;
    int outerRadius = Math.min(cx, cy);
    int ballRadius = outerRadius / 10;
    int radius = outerRadius - ballRadius;
    
    lastX = x;
    lastY = y;

    float dx = (float)(lastX - cx) / (float)radius;
    float dy = (float)(cy - lastY) / (float)radius;

    if (dx * dx + dy * dy > 1)
    {
      double angle = Math.atan2(dy, dx);
      dx = (float)Math.cos(angle);
      dy = (float)Math.sin(angle);
      lastX = (int)(cx + radius * dx);
      lastY = (int)(cy - radius * dy);
    }

    if (dx != deltaX || dy != deltaY)
    {
      deltaX = dx;
      deltaY = dy;
      onChanged();
    } 
    repaint();
  }
  
  private void resetPosition()
  {
    firstDraw = true;
    if (deltaX != 0 || deltaY != 0)
    {
      deltaX = 0;
      deltaY = 0;
      onChanged();
    }
    repaint();
  }
  
  protected void onChanged()
  {
    if (invoker != null)
    {
      BList position = new BList(2);
      position.add(deltaX);
      position.add(deltaY);
      invoker.invoke(position);
    }
  }
}
