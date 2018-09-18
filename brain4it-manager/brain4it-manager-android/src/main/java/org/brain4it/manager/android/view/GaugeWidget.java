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
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.util.TimerTask;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.GaugeWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class GaugeWidget extends View implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String label;
  protected String getValueFunction;
  protected int min;
  protected int max;
  protected int divisions;
  protected int decimals;
  protected double value;
  protected double remoteValue;
  protected TimerTask timerTask;
  protected Paint linePaint1;
  protected Paint linePaint2;
  protected Paint textPaint;
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

  public GaugeWidget(Context context)
  {
    this(context, null);
  }

  public GaugeWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    float density = context.getResources().getDisplayMetrics().density;    
    
    linePaint1 = new Paint();
    linePaint1.setColor(Color.BLACK);
    linePaint1.setStyle(Paint.Style.STROKE);
    linePaint1.setFlags(Paint.ANTI_ALIAS_FLAG);
    linePaint1.setStrokeWidth(density);

    linePaint2 = new Paint();
    linePaint2.setColor(Color.BLACK);
    linePaint2.setStyle(Paint.Style.STROKE);
    linePaint2.setFlags(Paint.ANTI_ALIAS_FLAG);
    linePaint2.setStrokeWidth(2 * density);

    textPaint = new Paint();
    textPaint.setColor(Color.BLACK);
    textPaint.setStyle(Paint.Style.FILL_AND_STROKE);    
    textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
  }
  
  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
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
  public void onDraw(Canvas canvas)
  {
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    int size = Math.min(width, height);
    int margin = size / 10;
    int pointRadius = size / 20;
    size -=  margin;
    int cx = width / 2;
    int cy = height / 2;
    float gaugeRadius = size / 2;

//    float paintWidth = Math.max(2f, (float)size / 400f);
//    linePaint1.setStrokeWidth(paintWidth);
//    linePaint2.setStrokeWidth(3 * paintWidth);
    textPaint.setTextSize(size / 15);
    
    // circle    
    canvas.drawCircle(cy, cy, gaugeRadius, linePaint2);
   
    // ball
    canvas.drawCircle(cx, cy, pointRadius, linePaint1);
    
    double step = (double)(max - min) / divisions;
    double radius1 = size / 2;
    double radius2 = radius1 - 0.1 * radius1;
    double radius3 = radius1 - 0.2 * radius1;

    double angle = 225;
    double stepAngle = 270.0 / divisions;
    Rect bounds = new Rect();
    
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
 
      canvas.drawLine(px, py, qx, qy, linePaint1);
      
      String valueLabel = String.valueOf(i);
      
      textPaint.getTextBounds(valueLabel, 0, valueLabel.length(), bounds);

      int ox = (int)(bounds.width() / 2);
      
      canvas.drawText(valueLabel, tx - ox, 
        (float)(ty + 0.8 * 0.5 * bounds.height()), textPaint);
      
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
    textPaint.getTextBounds(valueString, 0, valueString.length(), bounds);    
    int tx = (int)(cx - (0.5 * bounds.width()));
    int ty = (int)(cy + radius3 - 1.5 * bounds.height());
    canvas.drawText(valueString, tx, ty, textPaint); 

    if (label != null)
    {
      textPaint.getTextBounds(label, 0, label.length(), bounds);
      tx = (int)(cx - (0.5 * bounds.width()));
      ty = (int)(cy + radius3 + 0.8 * 0.5 * bounds.height());
      canvas.drawText(label, tx, ty, textPaint); 
    }
    
    double valueRatio = (value - min) / (max - min);
    if (valueRatio > 1) valueRatio = 1;
    else if (valueRatio < 0) valueRatio = 0;
    
    double valueAngle = 225 - valueRatio * 270;
    double radians = Math.toRadians(valueAngle);
    int vx = cx + (int)(radius3 * Math.cos(radians));
    int vy = cy - (int)(radius3 * Math.sin(radians));
    
    canvas.drawLine(cx, cy, vx, vy, linePaint2);
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
      postInvalidate();
    }
  }
}
