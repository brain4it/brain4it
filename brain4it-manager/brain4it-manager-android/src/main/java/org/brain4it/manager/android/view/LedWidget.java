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
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.LedWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class LedWidget extends LinearLayout implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected TextView textView;
  protected LedView ledView;
  protected boolean on = false;
  protected Paint linePaint;
  protected Paint onPaint;
  protected Paint offPaint;
  protected Paint flashPaint;
  
  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, long serverTime)
    {
      post(new Runnable()
      {
        @Override
        public void run()
        {
          on = Utils.toBoolean(value);
          ledView.postInvalidate();
        }
      });
    }
  };
  
  public LedWidget(Context context)
  {
    this(context, null);
  }

  public LedWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    float density = context.getResources().getDisplayMetrics().density;    
    
    linePaint = new Paint();
    linePaint.setAntiAlias(true);
    linePaint.setColor(Color.BLACK);
    linePaint.setStyle(Paint.Style.STROKE);
    linePaint.setStrokeWidth(density);
    
    onPaint = new Paint();
    onPaint.setAntiAlias(true);
    onPaint.setColor(Color.YELLOW);
    onPaint.setStyle(Paint.Style.FILL);
    
    offPaint = new Paint();
    offPaint.setAntiAlias(true);
    offPaint.setColor(Color.GRAY);
    offPaint.setStyle(Paint.Style.FILL);

    flashPaint = new Paint();
    flashPaint.setAntiAlias(true);
    flashPaint.setColor(Color.YELLOW);
    flashPaint.setStyle(Paint.Style.FILL);
        
    setOrientation(LinearLayout.VERTICAL);

    ledView = new LedView(context);
    ledView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, 0, 1f));
    addView(ledView);
    
    textView = new TextView(context);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
    addView(textView);
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    LedWidgetType type = 
      (LedWidgetType)WidgetType.getType(WidgetType.LED);

    type.validate(properties);
    
    int rgb = type.getColor(properties);
    if (rgb != -1)
    {
      int argb = 0xFF << 24 | rgb;
      onPaint.setColor(argb);
    }
    
    textView.setText(type.getLabel(properties));

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
  
  public class LedView extends View
  {
    public LedView(Context context)
    {
      super(context);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
      super.onDraw(canvas);

      int width = canvas.getWidth();
      int height = canvas.getHeight();
      
      int cx = width / 2;
      int cy = height / 2;
      int size = Math.min(cx, cy);
      int margin = (int)(0.3 * (float)size);
      int radius = size - margin;
      if (on)
      {
        flashPaint.setShader(new RadialGradient(cx, cy, size, 
          onPaint.getColor(), onPaint.getColor() & 0x00FFFFFF, 
          TileMode.CLAMP));
      }
      canvas.drawCircle(cx, cy, radius, on ? onPaint : offPaint);
      canvas.drawCircle(cx, cy, radius, linePaint);
      if (on)
      {
        canvas.drawCircle(cx, cy, size, flashPaint);
      }
    }
  }
}
