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
package org.brain4it.manager.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.TimerTask;
import org.brain4it.client.Monitor;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.Module;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.GraphWidgetType;
import org.brain4it.manager.widgets.GraphWidgetType.Model;
import org.brain4it.manager.widgets.GraphWidgetType.TimeRange;
import org.brain4it.manager.widgets.GraphWidgetType.Data;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class GraphWidget extends View implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String label;
  protected int timeRangeIndex = GraphWidgetType.DEFAULT_TIME_RANGE_INDEX;
  protected String datePattern = "dd/MM/yyyy";
  protected String getValueFunction;
  protected String getHistoryFunction;
  protected Collection<String> dataSetNames;

  protected Model model;
  protected TimerTask timerTask;
  protected float lastY = -Float.MAX_VALUE;
  protected int lastTimeRangeIndex = 0;
  protected long lastDownTime;
  protected boolean frozen;
  protected long frozenTime;
  protected Paint linePaint;
  protected Paint fillPaint;
  protected Paint textPaint;

  protected static final int MARGIN = 4;

  protected static final int COLORS[] = {
    Color.BLUE,
    Color.GREEN,
    Color.RED,
    Color.YELLOW,
    Color.MAGENTA,
    Color.CYAN
  };

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object object, 
      long serverTime)
    {
      post(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            model.addCurrentData(object);
          }
          catch (Exception ex)
          {
            // ignore: bad data
          }
          invalidate();
        }
      });
    }
  };

  public GraphWidget(Context context)
  {
    this(context, null);
  }

  public GraphWidget(Context context, AttributeSet attributes)
  {
    super(context, attributes);

    linePaint = new Paint();
    linePaint.setAntiAlias(true);
    linePaint.setColor(Color.BLACK);
    linePaint.setStyle(Paint.Style.STROKE);
    linePaint.setStrokeWidth(3);

    fillPaint = new Paint();
    fillPaint.setAntiAlias(true);
    fillPaint.setColor(Color.BLACK);
    fillPaint.setStyle(Paint.Style.FILL);

    textPaint = new Paint();
    textPaint.setAntiAlias(true);
    textPaint.setColor(Color.BLACK);
    textPaint.setStyle(Paint.Style.FILL);
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
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

    if (dashboard == null)
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
  public void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    float density = getResources().getDisplayMetrics().density;
    float defaultFontSize = 14 * density;

    float margin = MARGIN * density;
    float width = getWidth() - 2 * margin;
    float height = getHeight() - 2 * margin;
    canvas.translate(margin, margin);
    canvas.clipRect(0, 0, width, height);

    canvas.drawColor(Color.WHITE);

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
      defaultFontSize);
    textPaint.setTextSize(fontSize);
    textPaint.setColor(Color.DKGRAY);

    TimeZone timeZone = TimeZone.getDefault();
    int offset = timeZone.getOffset(now);

    long rnow = (long)(Math.round((now + offset) / division) * division) - offset;

    linePaint.setColor(Color.LTGRAY);
    linePaint.setStrokeWidth(0.5f * density);
    for (long time = rnow; time >= now - period - division; time -= division)
    {
      float x = (float)(width - widthPixels * (now - time));
      canvas.drawLine(x, 0, x, height, linePaint);

      Date date = new Date(time);
      float labelWidth;
      int leftX;
      String dateLabel = dateFormat.format(date);
      labelWidth = textPaint.measureText(dateLabel);
      leftX = (int)(x - 0.5 * labelWidth);
      canvas.drawText(dateLabel, leftX, height - defaultFontSize - 4 * density,
        textPaint);
      String hourLabel = hourFormat.format(date);
      labelWidth = textPaint.measureText(hourLabel);
      leftX = (int)(x - 0.5 * labelWidth);
      canvas.drawText(hourLabel, leftX, height - 4 * density, textPaint);
    }

    // paint horizontal grid (values)
    DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.0######");
    double delta = valueDivider(tmax - tmin, 5);
    double rmin = Math.round(tmin / delta) * delta;
    double rmax = Math.round(tmax / delta) * delta;
    fontSize = (float)(0.7f * heightPixels * delta);
    fontSize = Math.min(fontSize, defaultFontSize);
    textPaint.setTextSize(fontSize);

    linePaint.setStrokeWidth(0.5f * density);
    for (double vy = rmin; vy <= rmax; vy += delta)
    {
      double y = padding + heightPixels * (tmax - vy);
      linePaint.setColor(Color.LTGRAY);
      canvas.drawLine(0, (float)y, width, (float)y, linePaint);
      textPaint.setColor(Color.GRAY);
      canvas.drawText(decimalFormat.format(vy),
        4 * density, (float)(y - 2 * density), textPaint);
    }

    // paint data set names
    textPaint.setTextSize(defaultFontSize);

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
    double legendWidth = textPaint.measureText(legend);
    int xoffset = (int)((width - legendWidth) / 2);
    int yoffset = (int)(2 * defaultFontSize + 4 * density);
    int xdelta = 0;
    int i = 0;
    for (String dataSetName : names)
    {
      if (dataSetName != null)
      {
        int dataSetColor = COLORS[i % COLORS.length];
        textPaint.setColor(dataSetColor);
        canvas.drawText(dataSetName, (int)xoffset + xdelta, yoffset, textPaint);
        xdelta += textPaint.measureText(dataSetName + " ");
      }
      i++;
    }

    // paint data
    linePaint.setPathEffect(null);
    i = 0;
    for (String dataSetName : names)
    {
      int dataSetColor = COLORS[i % COLORS.length];
      LinkedList<Data> dataSet = model.getDataSet(dataSetName);
      if (dataSet != null && !dataSet.isEmpty())
      {
        linePaint.setStrokeWidth(1.5f * density);
        linePaint.setColor(dataSetColor);

        Data last = dataSet.getLast();
        Iterator<Data> iter = dataSet.descendingIterator();
        float x1 = width;
        float y1 = (float)(padding + heightPixels * (tmax - last.getValue()));
        while (iter.hasNext() && x1 > 0)
        {
          Data data = iter.next();
          float x2 = (float)(width - widthPixels * (now - data.getTimestamp()));
          float y2 = (float)(padding + heightPixels * (tmax - data.getValue()));
          canvas.drawLine(x1, y1, x2, y2, linePaint);
          x1 = x2;
          y1 = y2;
        }
      }
      i++;
    }
    // paint border
    linePaint.setColor(Color.BLACK);
    linePaint.setStrokeWidth(2 * density);
    canvas.drawRect(0, 0, width, height, linePaint);

    // paint label
    if (label != null)
    {
      textPaint.setTextSize(defaultFontSize);
      textPaint.setColor(Color.BLACK);
      double labelWidth = textPaint.measureText(label);
      canvas.drawText(label, (float)(0.5 * width - 0.5 * labelWidth),
        defaultFontSize + 2 * density, textPaint);
    }
    // paint timeRange
    textPaint.setTextSize(defaultFontSize);
    textPaint.setColor(Color.BLACK);
    String timeRangeName = timeRange.getName();
    double labelWidth =
      textPaint.measureText(timeRangeName);
    canvas.drawText(timeRangeName, (float)(width - labelWidth - 16 * density),
      defaultFontSize + (float)(2 * density), textPaint);

    // frozen indicator
    if (frozen)
    {
      fillPaint.setColor(Color.RED);
      canvas.drawRect(width - 12 * density, 4 * density,
        width - 4 * density, 12 * density, fillPaint);
    }
    // refresh
    if (timerTask == null)
    {
      updateTimerTask();
    }
    canvas.translate(-margin, -margin);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    super.onTouchEvent(event);

    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN ||
        event.getAction() == android.view.MotionEvent.ACTION_MOVE)
    {
      if (lastY == -Float.MAX_VALUE)
      {
        lastY = event.getY();
        lastTimeRangeIndex = timeRangeIndex;
        lastDownTime = System.currentTimeMillis();
      }
      else
      {
        float y = event.getY();
        float density = getResources().getDisplayMetrics().density;
        float delta = (lastY - y) / (10 * density);
        timeRangeIndex = lastTimeRangeIndex + (int)delta;
        if (timeRangeIndex < 0) timeRangeIndex = 0;
        else if (timeRangeIndex >= GraphWidgetType.TIME_RANGES.length)
          timeRangeIndex = GraphWidgetType.TIME_RANGES.length - 1;
        updateTimerTask();
      }
    }
    else if (event.getAction() == android.view.MotionEvent.ACTION_UP)
    {
      lastY = -Float.MAX_VALUE;
      long now = System.currentTimeMillis();
      if (now - lastDownTime < 100)
      {
        frozen = !frozen;
        frozenTime = now;
      }
    }
    invalidate();

    return true;
  }


  protected float valueDivider(double value, int minDivisions)
  {
    value = Math.abs(value);
    double divider = Math.pow(10, Math.floor(Math.log10(value)));
    while (value / divider < minDivisions) divider *= 0.5;

    return (float)divider;
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
        post(new Runnable()
        {
          @Override
          public void run()
          {
            invalidate();
          }
        });
      }
    };
    float density = getResources().getDisplayMetrics().density;
    int margin = (int)(MARGIN * density);
    int width = getWidth() - 2 * margin;
    TimeRange timeRange = GraphWidgetType.TIME_RANGES[timeRangeIndex];
    long period = timeRange.getPeriod();
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
        post(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              Object result = Parser.fromString(resultString);
              model.addHistoryData(result);
              invalidate();
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
