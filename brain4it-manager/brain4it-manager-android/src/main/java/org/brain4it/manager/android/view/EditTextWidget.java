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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.android.DashboardActivity;
import org.brain4it.manager.android.DashboardWidget;
import org.brain4it.manager.android.FontCache;
import org.brain4it.manager.android.R;
import org.brain4it.manager.widgets.EditTextWidgetType;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class EditTextWidget extends LinearLayout implements DashboardWidget
{
  protected DashboardActivity dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected TextView textView;
  protected EditText editText;
  protected int invokeInterval = 100;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value,
      final long serverTime)
    {
      if (value instanceof String)
      {
        post(new Runnable()
        {
          @Override
          public void run()
          {
            if (invoker == null ||
                (!invoker.isSending() && invoker.updateInvokeTime(serverTime)))
            {
              String text = (String)value;
              int selStart = editText.getSelectionStart();
              int selEnd = editText.getSelectionEnd();
              editText.removeTextChangedListener(textWatcher);
              editText.setText(text);
              editText.addTextChangedListener(textWatcher);
              if (selStart > text.length()) selStart = text.length();
              if (selEnd > text.length()) selEnd = text.length();
              editText.setSelection(selStart, selEnd);
            }
          }
        });
      }
    }
  };

  protected TextWatcher textWatcher = new TextWatcher()
  {
    @Override
    public void beforeTextChanged(CharSequence cs, int i, int i1, int i2)
    {
    }

    @Override
    public void onTextChanged(CharSequence cs, int i, int i1, int i2)
    {
    }

    @Override
    public void afterTextChanged(Editable editable)
    {
      if (invoker != null)
      {
        invoker.invoke(editText.getText().toString());
      }
    }
  };

  @Override
  public void init(DashboardActivity dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    EditTextWidgetType type =
      (EditTextWidgetType)WidgetType.getType(WidgetType.EDIT_TEXT);

    type.validate(properties);

    String labelText = type.getLabel(properties);
    if (labelText == null || labelText.length() == 0)
    {
      textView.setVisibility(GONE);
    }
    else
    {
      textView.setVisibility(VISIBLE);
      textView.setText(labelText);
    }

    String fontFamily = type.getFontFamily(properties);
    editText.setTypeface(FontCache.getFont(dashboard, fontFamily));

    int fontSize = type.getFontSize(properties);
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

    invokeInterval = type.getInvokeInterval(properties);

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
      editText.setFocusable(false);
      editText.setFocusableInTouchMode(false);
      editText.setClickable(false);
    }
    else
    {
      setValueFunction = func.getName();
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

  public EditTextWidget(Context context)
  {
    this(context, null);
  }

  public EditTextWidget(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.CENTER_VERTICAL);
    float density = getResources().getDisplayMetrics().density;
    int padding = (int)Math.round(4 * density);
    setPadding(padding, padding, padding, padding);

    textView = new TextView(context);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
      LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    addView(textView);

    editText = new EditText(context);
    editText.setBackgroundResource(R.drawable.edit_text);
    editText.setLayoutParams(new LinearLayout.LayoutParams(
     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    editText.addTextChangedListener(textWatcher);
    editText.setSingleLine(false);
    editText.setVerticalScrollBarEnabled(true);
    editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    editText.setInputType(InputType.TYPE_CLASS_TEXT |
      InputType.TYPE_TEXT_FLAG_MULTI_LINE|
      InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    editText.setGravity(Gravity.LEFT | Gravity.TOP);
    addView(editText);
  }
}
