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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.widgets.LedWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class LedWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected JLabel label;
  protected LedComponent ledComponent;
  protected boolean on = false;
  protected Color ledColor;
  
  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      long serverTime)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          on = Utils.toBoolean(value);
          invalidate();
          revalidate();
          repaint();
        }
      });
    }
  };

  public LedWidget()
  {
    initComponents();
  }
  
  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;
    
    LedWidgetType type = 
      (LedWidgetType)WidgetType.getType(WidgetType.LED);

    type.validate(properties);
    
    int rgb = type.getColor(properties);
    ledColor = rgb != -1 ? new Color(rgb) : Color.YELLOW;
    
    label.setText(type.getLabel(properties));

    BSoftReference func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getValue();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }
  }

  private void initComponents()
  {
    setOpaque(false);
    setLayout(new BorderLayout());
    ledComponent = new LedComponent();
    add(ledComponent, BorderLayout.CENTER);
    label = new JLabel();
    add(label, BorderLayout.SOUTH);
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setHorizontalTextPosition(JLabel.CENTER);
  }

  public class LedComponent extends JComponent
  {
    @Override
    public void paintComponent(Graphics g)
    {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
      if (on)
      {
        g2.setColor(ledColor);
      }
      else
      {
        g2.setColor(Color.GRAY); 
      }
      int cx = getWidth() / 2;
      int cy = getHeight() / 2;
      int size = Math.min(cx, cy);
      int margin = (int)(0.3 * (float)size);
      int radius = size - margin;
      g2.fillOval(cx - radius, cy - radius, 2 * radius, 2 * radius);
      g2.setColor(Color.BLACK);
      g2.drawOval(cx - radius, cy - radius, 2 * radius, 2 * radius);
      if (on)
      {
        Color flash = new Color(ledColor.getRed(), ledColor.getGreen(), 
          ledColor.getBlue(), 0);
        g2.setPaint(new RadialGradientPaint(cx, cy, size, 
          new float[]{0.3f, 1.0f}, new Color[]{ledColor, flash}));
        g2.fillOval(cx - size, cy - size, 2 * size, 2 * size);
      }
    }
  }
}
