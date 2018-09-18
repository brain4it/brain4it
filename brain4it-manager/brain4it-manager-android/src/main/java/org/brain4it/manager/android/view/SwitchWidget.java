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
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.SwitchWidgetType;
import org.brain4it.manager.widgets.WidgetType;
import static android.content.Context.VIBRATOR_SERVICE;
import org.brain4it.manager.widgets.FunctionInvoker;

/**
 *
 * @author realor
 */
public class SwitchWidget extends Switch implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      final long serverTime)
    {
      post(new Runnable()
      {
        @Override
        public void run()
        {
          if (invoker == null || 
              (!invoker.isSending() && invoker.updateInvokeTime(serverTime)))
          {
            setOnCheckedChangeListener(null);
            setChecked(Utils.toBoolean(value));
            setOnCheckedChangeListener(changeListener);
          }
        }
      });
    }
  };

  protected final OnCheckedChangeListener changeListener =
    new OnCheckedChangeListener()
  {
    @Override
    public void onCheckedChanged(CompoundButton cb, boolean active)
    {
      if (invoker != null)
      {
        invoker.invoke(active);
        Vibrator vibrator =
          (Vibrator)getContext().getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null)
        {
          vibrator.vibrate(30);
        }
      }
    }
  };

  public SwitchWidget(Context context)
  {
    this(context, null);
  }

  public SwitchWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

    setOnCheckedChangeListener(changeListener);
    
    setTextOn("ON");
    setTextOff("OFF");

    float density = getResources().getDisplayMetrics().density;
    int padding = (int)Math.round(4 * density);    
    setPadding(padding, padding, padding, padding);
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    SwitchWidgetType type =
      (SwitchWidgetType)WidgetType.getType(WidgetType.SWITCH);

    type.validate(properties);

    String labelText = type.getLabel(properties);
    setText(labelText);

    BSoftReference func;
    func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }

    func = type.getSetValueFunction(properties);
    if (func == null)
    {
      setEnabled(false);
      setClickable(false);      
    }
    else
    {
      setEnabled(true);
      setClickable(true);
      setValueFunction = func.getName();
      if (dashboard != null)
      {
        invoker = new FunctionInvoker(dashboard.getInvoker(), setValueFunction);
      }
    }
  }
}
