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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.brain4it.client.Monitor;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.widgets.GraphWidgetType;
import org.brain4it.manager.widgets.GraphWidgetType.Data;
import org.brain4it.manager.widgets.GraphWidgetType.Model;
import org.brain4it.manager.widgets.GraphWidgetType.TimeRange;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class GraphWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String label;
  protected int timeRangeIndex = GraphWidgetType.DEFAULT_TIME_RANGE_INDEX;
  protected String datePattern = "dd/MM/yyyy";
  protected String getValueFunction;
  protected String getHistoryFunction;
  protected Collection<String> dataSetNames;

  protected Model model;
  protected TimerTask timerTask;
  protected int mouseY = -Integer.MAX_VALUE;
  protected int lastTimeRangeIndex = 0;
  protected boolean frozen;
  protected long frozenTime;

  protected static final int MARGIN = 4;

  protected static final Color COLORS[] = {
    Color.BLUE,
    Color.GREEN,
    Color.RED,
    Color.ORANGE,
    Color.YELLOW,
    Color.MAGENTA,
    Color.PINK,
    Color.CYAN
  };

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object object, 
      long serverTime)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            model.addCurrentData(object);
            repaint();
          }
          catch (Exception ex)
          {
            // ignore: bad data
          }
        }
      });
    }
  };
  
  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    model = new Model();

    GraphWidgetType type =
      (GraphWidgetType)WidgetType.getType(WidgetType.GRAPH);
    
    type.validate(properties);
    
    label = type.getLabel(properties);

    timeRangeIndex = type.getTimeRangeIndex(properties);

    model.setMaxData(type.getMaxData(properties));

    dataSetNames = type.getDataSetNames(properties);
    
    datePattern = type.getDateFormat(properties);

    BSoftReference func;
    func = type.getGetHistoryFunction(properties);
    if (func != null)
    {
      getHistoryFunction = func.getName();
      if (dashboard != null)
      {
        loadHistory();
      }
    }
    
    func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getName();
      if (dashboard != null && getHistoryFunction == null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }

    if (dashboard != null)
    {
      addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          if (e.getClickCount() > 1)
          {
            frozen = !frozen;
            if (frozen)
            {
              frozenTime = System.currentTimeMillis();
            }
            repaint();
          }
          else
          {
            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            mouseY = e.getY();
            lastTimeRangeIndex = timeRangeIndex;
          }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
          setCursor(Cursor.getDefaultCursor());
          mouseY = -Integer.MAX_VALUE;
        }
      });

      addMouseMotionListener(new MouseMotionAdapter()
      {
        @Override
        public void mouseDragged(MouseEvent e)
        {
          if (mouseY != -Integer.MAX_VALUE)
          {
            double density = ManagerApp.getPreferences().getScalingFactor();
            int y = e.getY();
            int delta = (int)((mouseY - y) / (10 * density));
            timeRangeIndex = lastTimeRangeIndex + delta;
            if (timeRangeIndex < 0) timeRangeIndex = 0;
            else if (timeRangeIndex >= GraphWidgetType.TIME_RANGES.length)
              timeRangeIndex = GraphWidgetType.TIME_RANGES.length - 1;
            repaint();
            updateTimerTask();
          }
        }
      });

      addComponentListener(new ComponentAdapter()
      {
        @Override
        public void componentResized(ComponentEvent e)
        {
          updateTimerTask();
        }
      });
    }
    else
    {
      frozen = true;
      String dataSetName = null;
      if (!dataSetNames.isEmpty())
      {
        dataSetName = dataSetNames.iterator().next();
      }
      TimeRange timeline = GraphWidgetType.TIME_RANGES[timeRangeIndex];
      long period = timeline.getPeriod();
      double incr = period / 25.0;
      for (long timestamp = -period; timestamp <= 0;  timestamp += incr)
      {
        double value = Math.sin((double)timestamp / (5 * incr));
        model.addData(dataSetName, new Data(value, timestamp));
      }
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    double density = ManagerApp.getPreferences().getScalingFactor();
    Font defaultFont = getFont();
            
    int margin = (int)(MARGIN * density);
    int width = getWidth() - 2 * margin;
    int height = getHeight() - 2 * margin;
    g2.translate(margin, margin);
    g2.clipRect(0, 0, width, height);

    g2.setColor(Color.WHITE);
    g2.fillRect(0, 0, width, height);

    TimeRange timeRange = GraphWidgetType.TIME_RANGES[timeRangeIndex];
    long period = timeRange.getPeriod();

    long now = frozen ? frozenTime : System.currentTimeMillis();

    double padding = (double)height / 6.0;
    double tmax, tmin;
    if (model.getMax() == model.getMin())
    {
      tmax = model.getMax() + 0.5;
      tmin = model.getMin() - 0.5;
    }
    else
    {
      tmax = model.getMax();
      tmin = model.getMin();
    }
    double widthPixels = (double)width / (double)period;
    double heightPixels = (double)(height - 2 * padding) / (tmax - tmin);

    // paint vertical grid (time)
    String hourPattern = period < 2000 ? "HH:mm:ss:SSS" : "HH:mm:ss";
    SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
    SimpleDateFormat hourFormat = new SimpleDateFormat(hourPattern);

    double division = timeRange.getDivision();
    int divisionPixels = (int)(1.2 * widthPixels * division);
    float fontSize = Math.min(divisionPixels / hourPattern.length(), 
      defaultFont.getSize());
    g2.setFont(defaultFont.deriveFont(fontSize));

    g2.setStroke(new BasicStroke((float)density));
    g2.setColor(Color.LIGHT_GRAY);
    
    TimeZone timeZone = TimeZone.getDefault();
    int offset = timeZone.getOffset(now);

    long rnow = (long)(Math.round((now + offset) / division) * division) - offset;

    for (long time = rnow; time >= now - period - division; time -= division)
    {
      int x = (int)(width - widthPixels * (now - time));
      g2.setColor(Color.LIGHT_GRAY);
      g2.drawLine(x, 0, x, height);

      Date date = new Date(time);
      double labelWidth;
      int leftX;
      g2.setColor(Color.DARK_GRAY);
      String dateLabel = dateFormat.format(date);
      labelWidth = getFontMetrics(g2.getFont()).
        getStringBounds(dateLabel, g2).getWidth();
      leftX = (int)Math.round(x - 0.5 * labelWidth);
      g2.drawString(dateLabel, leftX, 
        (int)(height - g2.getFont().getSize() - 4 * density));

      String hourLabel = hourFormat.format(date);
      labelWidth = getFontMetrics(g2.getFont()).
        getStringBounds(hourLabel, g2).getWidth();
      leftX = (int)Math.round(x - 0.5 * labelWidth);
      g2.drawString(hourLabel, leftX, (int)(height - 4 * density));
    }
    
    // paint horizontal grid (values)
    DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.0######");
    g2.setStroke(new BasicStroke((float)(1 * density)));
    
    double delta = valueDivider(tmax - tmin, 5);
    double rmin = Math.round(tmin / delta) * delta;
    double rmax = Math.round(tmax / delta) * delta;
    fontSize = (float)(0.7f * heightPixels * delta);
    fontSize = Math.min(fontSize, defaultFont.getSize());
    g2.setFont(defaultFont.deriveFont(fontSize));

    for (double vy = rmin; vy <= rmax; vy += delta)
    {
      double y = padding + heightPixels * (tmax - vy);
      g2.setColor(Color.LIGHT_GRAY);
      g2.drawLine(0, (int)y, width, (int)y);
      g2.setColor(Color.DARK_GRAY);
      g2.drawString(decimalFormat.format(vy), 
        (int)(4 * density), (int)(y - 2 * density));
    }

    // paint data set names
    g2.setFont(defaultFont);
    Collection<String> names = dataSetNames.isEmpty() ? 
      model.getDataSetNames() : dataSetNames;
    StringBuilder buffer = new StringBuilder();
    for (String dataSetName : names)
    {
      if (dataSetName != null)
      {
        if (buffer.length() > 0) buffer.append(" ");
        buffer.append(dataSetName);
      }
    }
    String legend = buffer.toString();
    double legendWidth = 
      g2.getFontMetrics().getStringBounds(legend, g2).getWidth();
    int xoffset = (int)((width - legendWidth) / 2);
    int yoffset = (int)(2 * defaultFont.getSize() + 4 * density);
    int xdelta = 0;
    int i = 0;
    for (String dataSetName : names)
    {
      if (dataSetName != null)
      {
        Color dataSetColor = COLORS[i % COLORS.length];
        g2.setColor(dataSetColor);
        g2.drawString(dataSetName, (int)xoffset + xdelta, yoffset);
        xdelta += g2.getFontMetrics().
          getStringBounds(dataSetName + " ", g2).getWidth();
      }
      i++;
    }
    
    // paint data
    i = 0;
    for (String dataSetName : names)
    {
      LinkedList<Data> dataSet = model.getDataSet(dataSetName);
      if (dataSet != null && !dataSet.isEmpty())
      {
        g2.setStroke(new BasicStroke(1.5f * (float)density,
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color dataSetColor = COLORS[i % COLORS.length];
        g2.setColor(dataSetColor);

        Data last = dataSet.getLast();
        Iterator<Data> iter = dataSet.descendingIterator();
        int x1 = width;
        int y1 = (int)(padding + heightPixels * (tmax - last.getValue()));
        while (iter.hasNext() && x1 > 0)
        {
          Data data = iter.next();
          int x2 = (int)(width - widthPixels * (now - data.getTimestamp()));
          int y2 = (int)(padding + heightPixels * (tmax - data.getValue()));
          g2.drawLine(x1, y1, x2, y2);
          x1 = x2;
          y1 = y2;
        }
      }
      i++;
    }
    // paint border
    g2.setStroke(new BasicStroke((float)(3 * density), 
      BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    g2.setColor(Color.BLACK);
    g2.drawRect(0, 0, width - 1, height - 1);

    // paint label
    if (label != null)
    {
      g2.setFont(defaultFont.deriveFont(Font.BOLD));
      g2.setColor(Color.BLACK);
      double labelWidth =
        g2.getFontMetrics().getStringBounds(label, g2).getWidth();
      g2.drawString(label, (int)(0.5 * width - 0.5 * labelWidth),
        defaultFont.getSize() + (int)(2 * density));
    }
    // paint timeRange
    g2.setFont(defaultFont);
    g2.setColor(Color.BLACK);
    String timeRangeName = timeRange.getName();
    double labelWidth =
      g2.getFontMetrics().getStringBounds(timeRangeName, g2).getWidth();
    g2.drawString(timeRangeName, 
      (int)Math.round(width - labelWidth - 16 * density),
      Math.round(defaultFont.getSize() + 2 * density));
    
    // frozen indicator
    if (frozen)
    {
      g2.setColor(Color.RED);
      g2.fillRect((int)(width - 12 * density), (int)(4 * density), 
        (int)(8 * density), (int)(8 * density));
    }
    // refresh
    if (timerTask == null)
    {
      updateTimerTask();
    }
    g2.translate(-margin, -margin);
  }

  protected double valueDivider(double value, int minDivisions)
  {
    value = Math.abs(value);
    double divider = Math.pow(10, Math.floor(Math.log10(value)));
    while (value / divider < minDivisions) divider *= 0.5;

    return divider;
  }

  protected void updateTimerTask()
  {
    if (timerTask != null)
    {
      timerTask.cancel();
    }
    timerTask = new TimerTask()
    {
      @Override
      public void run()
      {
        invalidate();
        revalidate();
        repaint();
      }
    };
    TimeRange timeRange = GraphWidgetType.TIME_RANGES[timeRangeIndex];
    long period = timeRange.getPeriod();
    double density = ManagerApp.getPreferences().getScalingFactor();
    int margin = (int)(MARGIN * density);
    int width = getWidth() - 2 * margin;
    long repaintPeriod = Math.max(period / width, 16);
    if (dashboard != null)
    {
      dashboard.getTimer().scheduleAtFixedRate(timerTask, 0, repaintPeriod);
    }
  }

  protected void loadHistory()
  {
    Module module = dashboard.getModule();
    RestClient restClient = module.getRestClient();
    restClient.invokeFunction(module.getName(), getHistoryFunction, null,
    new Callback()
    {
      @Override
      public void onSuccess(RestClient client, final String resultString)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              Object result = Parser.fromString(resultString);
              model.addHistoryData(result);
              repaint();
            }
            catch (Exception ex)
            {
              // ignore: bad data
            }
            finally
            {
              if (getValueFunction != null)
              {
                Monitor monitor = dashboard.getMonitor();
                monitor.watch(getValueFunction, monitorListener);
              }
            }
          }
        });
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
      }
    });
  }
}
