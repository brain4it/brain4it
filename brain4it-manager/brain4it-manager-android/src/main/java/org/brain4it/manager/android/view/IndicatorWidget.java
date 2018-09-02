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
import android.util.AttributeSet;
import android.util.TypedValue;
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
import org.brain4it.manager.android.FontCache;
import org.brain4it.manager.android.R;
import org.brain4it.manager.widgets.IndicatorWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class IndicatorWidget extends LinearLayout implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected TextView titleTextView;
  protected TextView valueTextView;
  protected TextView unitsTextView;
  protected LinearLayout valueLinearLayout;
  protected int maxValueLength = 0;
  protected IndicatorWidgetType type;

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
          setValue(value);
        }
      });
    }
  };

  public IndicatorWidget(Context context)
  {
    this(context, null);
  }

  public IndicatorWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    setOrientation(LinearLayout.VERTICAL);
    setBackgroundResource(R.drawable.indicator);

    float density = context.getResources().getDisplayMetrics().density;
    
    titleTextView = new TextView(context);
    titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    titleTextView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
    addView(titleTextView);

    valueLinearLayout = new LinearLayout(context);
    valueLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    valueLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    addView(valueLinearLayout);

    valueTextView = new TextView(context);
    valueTextView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1f));
    valueTextView.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
    valueTextView.setIncludeFontPadding(false);
    valueLinearLayout.addView(valueTextView);

    unitsTextView = new TextView(context);
    unitsTextView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0f));
    unitsTextView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

    unitsTextView.setPadding((int)(6 * density), 0, 0, 0);
    valueLinearLayout.addView(unitsTextView);

    valueTextView.addOnLayoutChangeListener(new OnLayoutChangeListener()
    {
      @Override
      public void onLayoutChange(View view,
        int left, int top, int right, int bottom,
        int oldLeft, int oldTop, int oldRight, int oldBottom)
      {
        updateTextSize();
      }
    });
  }

  protected void setValue(Object value)
  {
    String text = value == null ? "" : Utils.toString(value);
    if (text.length() > maxValueLength && maxValueLength > 0)
    {
      text = text.substring(0, maxValueLength);
    }
    valueTextView.setText(text);
    updateTextSize();
  }

  protected void updateTextSize()
  {
    String text = valueTextView.getText().toString();
    int length = maxValueLength == 0 ? text.length() : maxValueLength;
    float fontSize = type.getFontSize(valueTextView.getWidth(),
      valueTextView.getHeight(), length);
    valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    type = (IndicatorWidgetType)WidgetType.getType(WidgetType.INDICATOR);

    type.validate(properties);

    String label = type.getLabel(properties);
    if (label == null || label.length() == 0)
    {
      titleTextView.setVisibility(GONE);
    }
    else
    {
      titleTextView.setText(label);
      titleTextView.setVisibility(VISIBLE);
    }
    String fontFamily = type.getFontFamily(properties);
    if (fontFamily != null)
    {
      valueTextView.setTypeface(FontCache.getFont(dashboard, fontFamily));
    }

    maxValueLength = type.getMaxValueLength(properties);

    unitsTextView.setText(type.getUnits(properties));

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
}
