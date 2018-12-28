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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.SelectWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class SelectWidget extends LinearLayout implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getOptionsFunction;
  protected String getValueFunction;
  protected String setValueFunction;
  protected TextView textView;
  protected Spinner spinner;
  protected String labelText;
  protected String currentValue;
  protected Object currentOptions;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(final String reference, final Object data,
      final long serverTime)
    {
      post(new Runnable()
      {
        @Override
        public void run()
        {
          if (reference.equals(getValueFunction))
          {
            if (invoker == null ||
               (!invoker.isSending() && invoker.updateInvokeTime(serverTime)))
            {
              String newValue = String.valueOf(data);
              if (!newValue.equals(currentValue))
              {
                setSelectedValue(newValue);
              }
            }
          }
          else if (reference.equals(getOptionsFunction))
          {
            if (!Utils.equals(data, currentOptions))
            {
              loadOptions(data);
            }
          }
        }
      });
    }
  };

  protected final Spinner.OnItemSelectedListener actionListener =
    new Spinner.OnItemSelectedListener()
  {
    @Override
    public void onItemSelected(AdapterView<?> av, View view, int index, long l)
    {
      if (invoker != null)
      {
        Option option = (Option)av.getItemAtPosition(index);
        if (option != null)
        {
          invoker.invoke(option.value);
        }
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> av)
    {
    }
  };

  public SelectWidget(Context context)
  {
    this(context, null);
  }

  public SelectWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.CENTER_VERTICAL);

    textView = new TextView(context);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
    addView(textView);

    spinner = new Spinner(context);
    spinner.setLayoutParams(new LinearLayout.LayoutParams(
     LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    spinner.setOnItemSelectedListener(actionListener);

    addView(spinner);
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    SelectWidgetType type =
      (SelectWidgetType)WidgetType.getType(WidgetType.SELECT);

    type.validate(properties);

    labelText = type.getLabel(properties);
    textView.setText(labelText);

    BSoftReference func;
    func = type.getOptionsFunction(properties);
    if (func != null)
    {
      getOptionsFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getOptionsFunction, monitorListener);
      }
    }

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
      spinner.setEnabled(false);
    }
    else
    {
      spinner.setEnabled(true);
      setValueFunction = func.getName();
      if (dashboard != null)
      {
        invoker = new FunctionInvoker(dashboard.getInvoker(), setValueFunction);
      }
    }
  }

  protected void setSelectedValue(String value)
  {
    currentValue = value;

    if (value == null) return;

    SpinnerAdapter adapter = spinner.getAdapter();
    if (adapter == null) return; // options not loaded yet

    spinner.setOnItemSelectedListener(null);
    int size = adapter.getCount();
    boolean found = false;
    int index = 0;
    while (index < size && !found)
    {
      Option option  = (Option)adapter.getItem(index);
      if (option.value.equals(value))
      {
        spinner.setSelection(index);
        found = true;
      }
      else index++;
    }
    if (!found && size > 0)
    {
      spinner.setSelection(0);
    }
    post(new Runnable()
    {
      @Override
      public void run()
      {
        spinner.setOnItemSelectedListener(actionListener);
      }
    });
  }

  protected void loadOptions(Object data)
  {
    spinner.setOnItemSelectedListener(null);

    List<Option> optionList = new ArrayList<Option>();
    try
    {
      BList options = (BList)data;
      for (int i = 0; i < options.size(); i++)
      {
        BList option = (BList)options.get(i);
        optionList.add(new Option(String.valueOf(option.get(0)),
          String.valueOf(option.get(1))));
      }
      currentOptions = options;
    }
    catch (Exception ex)
    {
      // bad data format
    }
    ArrayAdapter<Option> adapter = new ArrayAdapter<Option>(dashboard,
      android.R.layout.simple_spinner_item, optionList);
    adapter.setDropDownViewResource(
      android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    setSelectedValue(currentValue);
  }

  protected class Option
  {
    String value;
    String label;

    protected Option(String value, String label)
    {
      this.value = value;
      this.label = label;
    }

    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof Option)) return false;
      Option item = (Option)o;
      return value.equals(item.value);
    }

    @Override
    public String toString()
    {
      return label;
    }
  }
}
