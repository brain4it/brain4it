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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.TimerTask;
import javax.swing.JComponent;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.widgets.GaugeWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class GaugeWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String label;
  protected String getValueFunction;
  protected int min;
  protected int max;
  protected int divisions;
  protected int decimals;
  protected BasicStroke stroke1;
  protected BasicStroke stroke2;
  protected double value;
  protected double remoteValue;
  protected TimerTask timerTask;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value,
      long serverTime)
    {
      if (value instanceof Number)
      {
        remoteValue = ((Number)value).doubleValue();
        animateGauge();
      }
    }
  };

  public GaugeWidget()
  {
    int scalingFactor = ManagerApp.getPreferences().getScalingFactor();
    stroke1 = new BasicStroke(scalingFactor);
    stroke2 = new BasicStroke(2 * scalingFactor);
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    GaugeWidgetType type =
      (GaugeWidgetType)WidgetType.getType(WidgetType.GAUGE);

    type.validate(properties);

    label = type.getLabel(properties);
    min = type.getMin(properties);
    max = type.getMax(properties);
    divisions = type.getDivisions(properties);
    decimals = type.getDecimals(properties);

    BSoftReference func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
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
    int size = Math.min(width, height);
    int margin = size / 10;
    int pointRadius = size / 20;
    size -=  margin;
    int cx = width / 2;
    int cy = height / 2;
    int rx = cx - (size / 2);
    int ry = cy - (size / 2);

    g2.setStroke(stroke2);
    
    g2.setColor(Color.BLACK);
    // circle
    g2.drawOval(rx, ry, size, size);

    g2.setStroke(stroke1);
    
    // ball
    g2.drawOval(cx - pointRadius, cy - pointRadius,
      2 * pointRadius, 2 * pointRadius);

    Font font = new Font(Font.DIALOG, Font.PLAIN, size / 15);

    g.setFont(font);

    double step = (double)(max - min) / divisions;
    double radius1 = size / 2;
    double radius2 = radius1 - 0.1 * radius1;
    double radius3 = radius1 - 0.2 * radius1;

    double angle = 225;
    double stepAngle = 270.0 / divisions;
    FontRenderContext rc = g2.getFontRenderContext();

    for (int d = 0; d <= divisions; d++)
    {
      int i = (int)(min + (d * step));
      double radians = Math.toRadians(angle);
      double cosAngle = Math.cos(radians);
      double sinAngle = Math.sin(radians);

      int px = cx + (int)(radius1 * cosAngle);
      int py = cy - (int)(radius1 * sinAngle);
      int qx = cx + (int)(radius2 * cosAngle);
      int qy = cy - (int)(radius2 * sinAngle);
      int tx = cx + (int)(radius3 * cosAngle);
      int ty = cy - (int)(radius3 * sinAngle);

      g2.drawLine(px, py, qx, qy);

      String valueLabel = String.valueOf(i);

      Rectangle2D bounds = font.getStringBounds(valueLabel, rc);
      int ox = (int)(0.5 * bounds.getWidth());

      g2.drawString(valueLabel, tx - ox,
        (int)(ty + 0.8 * 0.5 * bounds.getHeight()));

      angle -= stepAngle;
    }

    double div = Math.pow(10, decimals);
    double roundedValue = Math.round(remoteValue * div) / div;
    String valueString;
    if (decimals == 0)
    {
      valueString = String.valueOf((int)roundedValue);
    }
    else
    {
      valueString = String.valueOf(roundedValue);
    }
    Rectangle2D bounds = font.getStringBounds(valueString, rc);
    int tx = (int)(cx - (0.5 * bounds.getWidth()));
    int ty = (int)(cy + radius3 - bounds.getHeight());
    g2.drawString(valueString, tx, ty);

    if (label != null)
    {
      bounds = font.getStringBounds(label, rc);
      tx = (int)(cx - (0.5 * bounds.getWidth()));
      ty = (int)(cy + radius3 + 0.8 * 0.5 * bounds.getHeight());
      g2.drawString(label, tx, ty);
    }

    double valueRatio = (value - min) / (max - min);
    if (valueRatio > 1) valueRatio = 1;
    else if (valueRatio < 0) valueRatio = 0;

    double valueAngle = 225 - valueRatio * 270;
    double radians = Math.toRadians(valueAngle);
    int vx = cx + (int)(radius3 * Math.cos(radians));
    int vy = cy - (int)(radius3 * Math.sin(radians));

    g2.setStroke(stroke2);

    g2.drawLine(cx, cy, vx, vy);
  }

  private void animateGauge()
  {
    if (timerTask != null)
    {
      timerTask.cancel();
    }

    if (value != remoteValue)
    {
      double dif = Math.abs(remoteValue - value);
      double variation = (max - min) / 100.0;
      if (dif < variation)
      {
        value = remoteValue;
      }
      else
      {
        value = value < remoteValue ?
          value + variation : value - variation;
        timerTask = new TimerTask()
        {
          @Override
          public void run()
          {
            animateGauge();
          }
        };
        if (dashboard != null)
        {
          dashboard.getTimer().schedule(timerTask, 20);
        }
      }
      repaint();
    }
  }
}
