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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Scroller;
import java.util.Set;
import org.brain4it.manager.android.AutoIndenter;
import org.brain4it.manager.android.CodeHighlighter;
import org.brain4it.manager.android.SymbolMatcher;

/**
 *
 * @author realor
 */
public class EditCode extends EditText implements OnGestureListener
{
  private int updateTime = 200;
  private long lastCodeUpdate = 0;
  private long lastMatchUpdate = 0;
  private float cursorWidthDP = 1.5f;

  private final SymbolMatcher symbolMatcher = new SymbolMatcher();
  private final CodeHighlighter codeHighlighter = new CodeHighlighter();
  private final AutoIndenter autoIndenter = new AutoIndenter();
  private Set<String> functionNames;
  private final Handler updateHandler = new Handler();
  private GestureDetector gestureDetector;
  private Scroller scroller;
  private Paint highlightPaint;
  private Path highlightPath;

  public EditCode(Context context)
  {
    super(context, null);
  }

  public EditCode(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setFilters(new InputFilter[0]);
    setHorizontallyScrolling(true);
    setFocusable(true);
    setFocusableInTouchMode(true);
    setClickable(true);
    scroller = new Scroller(getContext());
    gestureDetector = new GestureDetector(getContext(), this);
    highlightPaint = new Paint();
    float density = Resources.getSystem().getDisplayMetrics().density;
    highlightPaint.setStrokeWidth(density * cursorWidthDP);    
    highlightPath = new Path();
    addTextChangedListener(autoIndenter);
    addTextChangedListener(new TextWatcher()
    {
      @Override
      public void onTextChanged(CharSequence cs, int start, int before, int count)
      {
      }

      @Override
      public void beforeTextChanged(CharSequence cs,
              int start, int count, int after)
      {
      }

      @Override
      public void afterTextChanged(Editable editable)
      {
        updateCodeHighlight();
      }
    });
  }

  public Set<String> getFunctionNames()
  {
    return functionNames;
  }

  public void setFunctionNames(Set<String> functionNames)
  {
    this.functionNames = functionNames;
  }

  public int getUpdateTime()
  {
    return updateTime;
  }

  public void setUpdateTime(int updateTime)
  {
    this.updateTime = updateTime;
  }
  
  public AutoIndenter getAutoIndenter()
  {
    return autoIndenter;
  }

  @Override
  public void onDraw(Canvas canvas)
  {
    int paddingLeft = getCompoundPaddingLeft();
    int paddingBottom = this.getPaddingTop();
    
    canvas.save();
    canvas.translate(paddingLeft, paddingBottom);
    
    Layout layout = getLayout();
    int selStart = getSelectionStart();
    int selEnd = getSelectionEnd();

    Path highlight = null;
    if (selStart < selEnd)
    {
      highlight = highlightPath;
      highlightPaint.setColor(getHighlightColor());
      highlightPaint.setStyle(Paint.Style.FILL);
      layout.getSelectionPath(selStart, selEnd, highlight);
    }
    layout.draw(canvas, highlight, highlightPaint, 0);

    if (selStart == selEnd)
    {
      if (isFocused() && isCursorVisible() && isVisibleBlink())
      {
        highlight = highlightPath;
        highlightPaint.setColor(Color.BLACK);
        highlightPaint.setStyle(Paint.Style.STROKE);
        layout.getCursorPath(selStart, highlight, getText());
        canvas.drawPath(highlight, highlightPaint);
      }
    }
    canvas.restore();
  }
  
  protected boolean isVisibleBlink()
  {
    long nowMillis = System.currentTimeMillis();
    return nowMillis % 1000 < 500 || 
           nowMillis - lastCodeUpdate < updateTime || 
           nowMillis - lastMatchUpdate < updateTime;
  }
  
  @Override
  public boolean onTextContextMenuItem(int id)
  {
    if (id == android.R.id.paste)
    {
      ClipboardManager clipboard = (ClipboardManager)getContext().
        getSystemService(Context.CLIPBOARD_SERVICE);
      if (clipboard.hasPrimaryClip())
      {
        ClipData clip = clipboard.getPrimaryClip();
        String text = clip.getItemAt(0).getText().toString();
        getText().insert(getSelectionStart(), text);
      }
      return true;
    }
    else
    {
      return super.onTextContextMenuItem(id);
    }
  }
  
  @Override
  protected void onScrollChanged(final int horiz, final int vert,
    final int oldHoriz, final int oldVert)
  {
    cancelUpdate(codeUpdateRunnable);
    codeUpdateRunnable.run();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh)
  {
    cancelUpdate(codeUpdateRunnable);
    codeUpdateRunnable.run();
  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd)
  {
    updateMatchHighlight();
  }

  public void updateCodeHighlight()
  {
    cancelUpdate(codeUpdateRunnable);
    cancelUpdate(matchUpdateRunnable);

    long now = System.currentTimeMillis();
    updateHandler.postDelayed(codeUpdateRunnable,
      now - lastCodeUpdate > updateTime ? 0 : updateTime);
    updateHandler.postDelayed(matchUpdateRunnable,
      now - lastMatchUpdate > updateTime ? 0 : updateTime);
  }

  public void updateMatchHighlight()
  {
    if (symbolMatcher != null)
    {
      cancelUpdate(matchUpdateRunnable);
      long now = System.currentTimeMillis();
      updateHandler.postDelayed(matchUpdateRunnable,
        now - lastMatchUpdate > updateTime ? 0 : updateTime);
    }
  }
  
  @Override
  public void onShowPress(MotionEvent me)
  {
  }

  @Override
  public void onLongPress(MotionEvent me)
  {
  }  
  
  @Override
  public boolean onDown(MotionEvent me)
  {
    return true;
  }
  
  @Override
  public boolean onSingleTapUp(MotionEvent me)
  {
		if (isEnabled())
    {
			((InputMethodManager) getContext().getSystemService(
					Context.INPUT_METHOD_SERVICE)).showSoftInput(this,
					InputMethodManager.SHOW_IMPLICIT);
		}
		return true;
  }

  @Override
  public boolean onScroll(MotionEvent me1, MotionEvent me2,
     float velocityX, float velocityY)
  {
    return true;
  }

  @Override
  public boolean onFling(MotionEvent me1, MotionEvent me2,
     float velocityX, float velocityY)
  {
		if (scroller != null)
    {
      scroller.fling(getScrollX(), getScrollY(), -(int)velocityX,
         -(int)velocityY, 0, 0, 0, 
         (getLineCount() * getLineHeight()) - getHeight());
    }
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    super.onTouchEvent(event);
    scroller.abortAnimation();
    return gestureDetector.onTouchEvent(event);
  }
  
  @Override
  public void computeScroll()
  {
    if (scroller.computeScrollOffset())
    {
      scrollTo(scroller.getCurrX(), scroller.getCurrY());
    }
  }
  
  protected final Runnable matchUpdateRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      symbolMatcher.updateHighlight(getEditableText(),
        getSelectionStart());
      lastMatchUpdate = System.currentTimeMillis();
    }
  };

  protected final Runnable codeUpdateRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      Layout layout = getLayout();
      if (layout != null)
      {
        int height = getHeight();
        int scrollY = getScrollY();

        int firstLine = layout.getLineForVertical(scrollY);
        int lastLine = layout.getLineForVertical(scrollY + height);
        int vstart, vend;
        if (firstLine == 0 && lastLine == 0)
        {
          vstart = 0;
          vend = length();
        }
        else
        {
          vstart = getPositionForLine(firstLine);
          vend = getPositionForLine(lastLine + 1);
        }
        codeHighlighter.updateHighlight(getText(),
          vstart, vend, functionNames);
      }
      lastCodeUpdate = System.currentTimeMillis();
    }
  };

  protected int getPositionForLine(int line)
  {
    int position = 0;
    int row = 0;
    Editable text = getText();
    while (row < line && position < text.length())
    {
      char ch = text.charAt(position);
      if (ch == '\n')
      {
        row++;
      }
      position++;
    }
    return position;
  }

  protected void cancelUpdate(Runnable callback)
  {
    updateHandler.removeCallbacks(callback);
  }
}
