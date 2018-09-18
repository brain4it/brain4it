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
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import org.brain4it.lang.BList;
import org.brain4it.manager.android.DashboardActivity;
import android.util.TypedValue;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.android.FontCache;
import org.brain4it.manager.widgets.ButtonWidgetType;
import org.brain4it.manager.widgets.WidgetType;
import org.brain4it.client.Invoker;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 *
 * @author realor
 */
public class ButtonWidget extends Button implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String onPressedFunction;
  protected String onReleasedFunction;
  protected String buttonId;
  protected Invoker invoker;

  public ButtonWidget(Context context)
  {
    this(context, null);
  }

  public ButtonWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setOnTouchListener(new OnTouchListener()
    {
      @Override
      public boolean onTouch(View view, MotionEvent me)
      {
        if (me.getAction() == MotionEvent.ACTION_DOWN)
        {
          if (invoker != null && onPressedFunction != null)
          {
            invoker.invoke(onPressedFunction, buttonId);
            Vibrator vibrator =
             (Vibrator)getContext().getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) vibrator.vibrate(30);
          }
        }
        else if (me.getAction() == MotionEvent.ACTION_UP)
        {
          if (invoker != null && onReleasedFunction != null)
          {
            invoker.invoke(onReleasedFunction, buttonId);
          }
        }
        return false;
      }
    });
  }

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;
    setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

    ButtonWidgetType type =
      (ButtonWidgetType)WidgetType.getType(WidgetType.BUTTON);

    type.validate(properties);

    setText(type.getLabel(properties));

    String fontFamily = type.getFontFamily(properties);
    setTypeface(FontCache.getFont(dashboard, fontFamily));

    int fontSize = type.getFontSize(properties);
    setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

    BSoftReference func;
    func = type.getOnPressedFunction(properties);
    if (func != null)
    {
      onPressedFunction = func.getName();
    }
    func = type.getOnReleasedFunction(properties);
    if (func != null)
    {
      onReleasedFunction = func.getName();
    }
    if (onPressedFunction != null || onReleasedFunction != null)
    {
      if (dashboard != null)
      {
        invoker = dashboard.getInvoker();
      }
    }
    buttonId = type.getButtonId(properties);
  }
}
