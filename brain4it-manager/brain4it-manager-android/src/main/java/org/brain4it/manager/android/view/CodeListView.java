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
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.brain4it.manager.android.CodeHighlighter;
import org.brain4it.manager.android.R;

/**
 *
 * @author realor
 */
public class CodeListView extends ListView
{
  public static final int COMMAND = 1;
  public static final int RESULT = 2;
  public static final int STRING = 3;
  public static final int ERROR = 4;

  private ArrayList<Item> items = new ArrayList<Item>();
  private CodeAdapter adapter;
  private Set<String> functionNames;
  private int textSize = 15;

  public CodeListView(Context context)
  {
    this(context, null);
  }

  public CodeListView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    adapter = new CodeAdapter(context, items);
    setAdapter(adapter);
  }

  public int getTextSize()
  {
    return textSize;
  }

  public void setTextSize(int textSize)
  {
    this.textSize = textSize;
  }

  public void append(int type, CharSequence text)
  {
    String str = text.toString();
    if (str.endsWith("\n")) str = str.substring(0, str.length() - 1);
    items.add(new Item(type, str));
    adapter.notifyDataSetChanged();
    post(new Runnable()
    {
      @Override
      public void run()
      {
        setSelection(adapter.getCount());
      }
    });
  }

  public void clear()
  {
    items.clear();
    adapter.notifyDataSetChanged();
  }

  public Set<String> getFunctionNames()
  {
    return functionNames;
  }

  public void setFunctionNames(Set<String> functionNames)
  {
    this.functionNames = functionNames;
  }

  public class Item
  {
    int type;
    String text;
    SpannableString spannable;

    public Item(int type, String text)
    {
      this.type = type;
      this.text = text;
    }

    public int getType()
    {
      return type;
    }

    public String getText()
    {
      return text == null ? spannable.toString() : text;
    }
    
    public SpannableString getSpannable()
    {
      if (spannable == null)
      {
        spannable = new SpannableString(text);
        CodeHighlighter codeHighlighter = new CodeHighlighter();
        codeHighlighter.updateHighlight(spannable, functionNames);
        text = null;
      }
      return spannable;
    }
  }

  public class CodeAdapter extends BaseAdapter
  {
    private final Context context;
    private final List<Item> rows;

    public CodeAdapter(Context context, List<Item> rows)
    {
      this.context = context;
      this.rows = rows;
    }

    @Override
    public int getCount()
    {
      return rows.size();
    }

    @Override
    public Object getItem(int position)
    {
      return rows.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      TextView textView;
      if (convertView instanceof TextView)
      {
        textView = (TextView)convertView;
      }
      else
      {
        LayoutInflater inflater =
          (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        textView = (TextView)inflater.inflate(R.layout.code_item, parent, false);
      }
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
      Item item = items.get(position);
      switch (item.type)
      {
        case ERROR:
          textView.setTextColor(Color.RED);
          textView.setText(item.text);
          break;
        case STRING:
          textView.setTextColor(Color.BLUE);
          textView.setText(item.text);
          break;
        default:
          textView.setTextColor(Color.BLACK);
          textView.setText(item.getSpannable());
          break;
      }
      return textView;
    }
  }
}
