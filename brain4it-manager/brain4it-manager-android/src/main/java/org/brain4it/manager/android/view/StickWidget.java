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

import org.brain4it.manager.android.DashboardWidget;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.StickWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class StickWidget extends View implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected final Paint linePaint1;
  protected final Paint linePaint2;
  protected final Paint textPaint;
  protected float lastX;
  protected float lastY;
  protected float deltaX;
  protected float deltaY;
  protected boolean firstDraw = true;
  protected String setValueFunction;
  protected int invokeInterval = 100;
  protected FunctionInvoker invoker;
  
  public StickWidget(Context context)
  {
    this(context, null);
  }

  public StickWidget(Context context, AttributeSet attributes)
  {
    super(context, attributes);
   
    float density = context.getResources().getDisplayMetrics().density;    
    
    linePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    linePaint1.setColor(Color.BLACK);
    linePaint1.setStyle(Paint.Style.STROKE);
    linePaint1.setStrokeWidth(density);

    linePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    linePaint2.setColor(Color.BLACK);
    linePaint2.setStyle(Paint.Style.STROKE);
    linePaint2.setStrokeWidth(2 * density);

    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(Color.BLACK);
    textPaint.setTextSize(density * 12);
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    float width = getWidth();
    float height = getHeight();
    float cx = 0.5f * width;
    float cy = 0.5f * height;
    float outerRadius = Math.min(cx, cy);
    float ballRadius = outerRadius / 10;
    float radius = outerRadius - ballRadius;

    if (firstDraw)
    {
      lastX = cx;
      lastY = cy;
      firstDraw = false;
    }
    
    // circle
    canvas.drawCircle(cx, cy, radius, linePaint2);
    // ball
    canvas.drawCircle(lastX, lastY, ballRadius, linePaint2);
    // vertical line
    canvas.drawLine(cx, cy - radius, cx, cy + radius, linePaint1);
    // horizontal line
    canvas.drawLine(cx - radius, cy, cx + radius, cy, linePaint1);

    // line center to ball
    canvas.drawLine(cx, cy, lastX, lastY, linePaint2);

    float fontSize = Math.round(height / 15);
    canvas.drawText("X: " + (int)Math.round(100 * deltaX), 0, fontSize, 
      textPaint);
    canvas.drawText("Y: " + (int)Math.round(100 * deltaY), 0, 2 * fontSize, 
      textPaint);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh)
  {
    super.onSizeChanged(w, h, oldw, oldh);
    firstDraw = true;
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    super.onTouchEvent(event);

    float width = getWidth();
    float height = getHeight();
    float cx = 0.5f * width;
    float cy = 0.5f * height;
    float outerRadius = Math.min(cx, cy);
    float ballRadius = outerRadius / 10;
    float radius = outerRadius - ballRadius;

    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN ||
        event.getAction() == android.view.MotionEvent.ACTION_MOVE)
    {
      lastX = event.getX();
      lastY = event.getY();

      float dx = (lastX - cx) / radius;
      float dy = (cy - lastY) / radius;

      if (dx * dx + dy * dy > 1)
      {
        double angle = Math.atan2(dy, dx);
        dx = (float)Math.cos(angle);
        dy = (float)Math.sin(angle);
        lastX = cx + radius * dx;
        lastY = cy - radius * dy;
      }

      if (dx != deltaX || dy != deltaY)
      {
        deltaX = dx;
        deltaY = dy;
        onChanged();
      }
    }
    else if (event.getAction() == android.view.MotionEvent.ACTION_UP)
    {
      lastX = cx;
      lastY = cy;
      if (deltaX != 0 || deltaY != 0)
      {
        deltaX = 0;
        deltaY = 0;
        onChanged();
      }
    }
    invalidate();

    return true;
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
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
