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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.android.FontCache;
import org.brain4it.manager.android.R;
import org.brain4it.manager.widgets.DisplayWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class DisplayWidget extends TextView implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected int lines = 1;
  protected static Typeface lcdFont;
  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, Object value, long serverTime)
    {
      try
      {
        updateText(Utils.toString(value));
      }
      catch (Exception ex)
      {
        updateText("");        
      }
    }
  };
  
  public DisplayWidget(Context context)
  {
    this(context, null);
  }

  public DisplayWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setBackgroundResource(R.drawable.display);
    setTextColor(Color.WHITE);
  }
  
  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;
    
    DisplayWidgetType type = 
      (DisplayWidgetType)WidgetType.getType(WidgetType.DISPLAY);    
    
    type.validate(properties);
    
    this.lines = type.getLines(properties);

    String fontFamily = type.getFontFamily(properties);
    setTypeface(FontCache.getFont(dashboard, fontFamily));
    
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
  
  protected void updateText(final String value)
  {
    post(new Runnable()
    {
      @Override
      public void run()
      {
        setText(value);
      }
    });
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, 
    int right, int bottom)
  {
    super.onLayout(changed, left, top, right, bottom);
    float density = getResources().getDisplayMetrics().density;
    int padding = 8;
    int height = bottom - top - 2 * (int)(padding * density);
    int lineHeight = (int)(0.75f * (float)height / lines);
    setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
  }  
}
