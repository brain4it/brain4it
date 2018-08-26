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
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.RangeWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class RangeWidget extends LinearLayout implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected TextView textView;
  protected SeekBar seekBar;
  protected String labelText;
  protected int min;
  protected int max;
  protected boolean tracking;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      final long serverTime)
    {
      if (value instanceof Number)
      {
        post(new Runnable()
        {
          @Override
          public void run()
          {
            if (!tracking)
            {
              if (invoker == null || 
                 (!invoker.isSending() && invoker.updateInvokeTime(serverTime)))
              {
                int remoteValue = ((Number)value).intValue();
                int value = remoteValue - min;
                textView.setText(labelText + " " + (min + value));
                seekBar.setOnSeekBarChangeListener(null);
                seekBar.setProgress(value);     
                seekBar.setOnSeekBarChangeListener(changeListener);
              }
            }
          }
        });
      }
    }
  };
  
  protected OnSeekBarChangeListener changeListener = 
          new OnSeekBarChangeListener()
  {
    @Override
    public void onProgressChanged(SeekBar sb, int value, boolean bln)
    {
      textView.setText(labelText + " " + (min + value));
    }

    @Override
    public void onStartTrackingTouch(SeekBar sb)
    {
      tracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar sb)
    {
      tracking = false;
      if (invoker != null)
      {
        int value = seekBar.getProgress() + min;    
        invoker.invoke(value);
      }
    }
  };
  
  
  public RangeWidget(Context context)
  {
    this(context, null);
  }  
  
  public RangeWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    
    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.CENTER_VERTICAL);
    
    textView = new TextView(context);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
    addView(textView);
    
    seekBar = new SeekBar(context);
    seekBar.setLayoutParams(new LinearLayout.LayoutParams(
     LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    
    addView(seekBar);    
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    RangeWidgetType type = 
      (RangeWidgetType)WidgetType.getType(WidgetType.RANGE);
    
    type.validate(properties);    
    
    labelText = type.getLabel(properties);
    textView.setText(labelText);
    
    BSoftReference func;
    func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getValue();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }

    func = type.getSetValueFunction(properties);
    if (func == null)
    {
      seekBar.setEnabled(false);
    }
    else
    {
      seekBar.setEnabled(true);
      setValueFunction = func.getValue();
      if (dashboard != null)
      {
        invoker = new FunctionInvoker(dashboard.getInvoker(), setValueFunction);
      }
    }

    min = type.getMin(properties);

    max = type.getMax(properties);
            
    seekBar.setMax(max - min);
    if (dashboard != null)
    {
      seekBar.setOnSeekBarChangeListener(changeListener);      
    }
  }
}
