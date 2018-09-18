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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.FontCache;
import org.brain4it.manager.swing.RoundedBorder;
import org.brain4it.manager.widgets.IndicatorWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class IndicatorWidget extends JPanel implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected JLabel titleLabel;
  protected ValueLabel valueLabel;
  protected JLabel unitsLabel;
  protected int maxValueLength = 0;
  protected IndicatorWidgetType type;

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
          setValue(value);
        }
      });
    }
  };

  public IndicatorWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    type = (IndicatorWidgetType)WidgetType.getType(WidgetType.INDICATOR);

    type.validate(properties);

    titleLabel.setText(type.getLabel(properties));

    String fontFamily = type.getFontFamily(properties);
    if (fontFamily != null)
    {
      valueLabel.setFont(FontCache.getFont(fontFamily));
    }

    maxValueLength = type.getMaxValueLength(properties);

    unitsLabel.setText(type.getUnits(properties));

    BSoftReference func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
      else
      {
        setValue("21.56");
      }
    }
    else
    {
      setValue("");
    }
  }

  protected void setValue(Object value)
  {
    String text = value == null ? "" : Utils.toString(value);
    if (text.length() > maxValueLength && maxValueLength > 0)
    {
      text = text.substring(0, maxValueLength);
    }
    valueLabel.setText(text);
  }

  private void initComponents()
  {
    setOpaque(false);
    setBorder(new RoundedBorder(10, 3));
    setLayout(new BorderLayout());
    titleLabel = new JLabel();
    titleLabel.setOpaque(false);
    add(titleLabel, BorderLayout.NORTH);

    valueLabel = new ValueLabel();
    valueLabel.setOpaque(false);
    add(valueLabel, BorderLayout.CENTER);

    unitsLabel = new JLabel();
    unitsLabel.setHorizontalAlignment(JLabel.CENTER);
    unitsLabel.setBorder(new EmptyBorder(0, 6, 0, 0));
    add(unitsLabel, BorderLayout.EAST);
  }

  public class ValueLabel extends JLabel
  {
    @Override
    public void paintComponent(Graphics g)
    {
      int length = maxValueLength > 0 ?
        maxValueLength : valueLabel.getText().length();

      if (length == 0) return;

      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

      int height = valueLabel.getHeight();
      int width = valueLabel.getWidth();

      Font font = valueLabel.getFont();
      float fontSize = type.getFontSize(width, height, length);

      // change font size
      g.setFont(font.deriveFont((float)fontSize));

      // get text bounds
      FontMetrics fontMetrics = g.getFontMetrics();
      Rectangle2D bounds = fontMetrics.getStringBounds(getText(), g);
      double boxWidth = bounds.getWidth();
      double boxHeight = bounds.getHeight();

      // align right horizontally and center vertically
      int x = (int)Math.round(width - boxWidth);
      int y = (int)Math.round((height + boxHeight) / 2);
      g.drawString(getText(), x, y - fontMetrics.getDescent());
    }
  }
}
