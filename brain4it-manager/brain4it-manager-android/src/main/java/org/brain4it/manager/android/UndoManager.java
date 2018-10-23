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
package org.brain4it.manager.android;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.widget.EditText;
import java.util.LinkedList;

/**
 *
 * @author realor
 * 
 * Code adapted from 
 * https://gist.github.com/zeleven/0cfa738c1e8b65b23ff7df1fc30c9f7eb
 * 
 */
public class UndoManager
{
  private final EditText editText;
  private final EditHistory editHistory;
  private final EditTextChangeListener changeListener;
  private boolean isUndoOrRedo;

  public UndoManager(EditText editText) 
  {
    this.editText = editText;
    editHistory = new EditHistory();
    changeListener = new EditTextChangeListener();
    this.editText.addTextChangedListener(changeListener);
  }

  public void disconnect() 
  {
    editText.removeTextChangedListener(changeListener);
  }
  
  public void setMaxHistorySize(int maxHistorySize) 
  {
    editHistory.setMaxHistorySize(maxHistorySize);
  }

  public void clearHistory() 
  {
    editHistory.clear();
  }  
  
  public boolean isUndoEnabled() 
  {
    return (editHistory.position > 0);
  }  
  
  public void undo() 
  {
    EditItem edit = editHistory.getPrevious();
    if (edit == null) return;

    Editable text = editText.getEditableText();
    int start = edit.start;
    int end = start + (edit.after != null ? edit.after.length() : 0);

    isUndoOrRedo = true;
    text.replace(start, end, edit.before);
    isUndoOrRedo = false;

    for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) 
    {
      text.removeSpan(o);
    }

    Selection.setSelection(text, edit.before == null ? 
      start : (start + edit.before.length()));
  }  
 
  public boolean isRedoEnabled() 
  {
    return (editHistory.position < editHistory.history.size());
  }
  
  public void redo() 
  {
    EditItem edit = editHistory.getNext();
    if (edit == null) return;

    Editable text = editText.getEditableText();
    int start = edit.start;
    int end = start + (edit.before != null ? edit.before.length() : 0);

    isUndoOrRedo = true;
    text.replace(start, end, edit.after);
    isUndoOrRedo = false;

    // This will get rid of underlines inserted when editor tries to come
    // up with a suggestion.
    for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) 
    {
      text.removeSpan(o);
    }

    Selection.setSelection(text, edit.after == null ? 
      start : (start + edit.after.length()));
  }
  
  private final class EditHistory
  {
    private int position = 0;
    private int maxHistorySize = -1;

    private final LinkedList<EditItem> history = new LinkedList<EditItem>();

    private void clear()
    {
      position = 0;
      history.clear();
    }

    private void add(EditItem item)
    {
      while (history.size() > position)
      {
        history.removeLast();
      }
      history.add(item);
      position++;

      if (maxHistorySize >= 0)
      {
        trimHistory();
      }
    }

    private void setMaxHistorySize(int maxHistorySize)
    {
      this.maxHistorySize = maxHistorySize;
      if (this.maxHistorySize >= 0)
      {
        trimHistory();
      }
    }

    private void trimHistory()
    {
      while (history.size() > maxHistorySize)
      {
        history.removeFirst();
        position--;
      }

      if (position < 0)
      {
        position = 0;
      }
    }

    private EditItem getCurrent()
    {
      if (position == 0)
      {
        return null;
      }
      return history.get(position - 1);
    }

    private EditItem getPrevious()
    {
      if (position == 0)
      {
        return null;
      }
      position--;
      return history.get(position);
    }

    private EditItem getNext()
    {
      if (position >= history.size())
      {
        return null;
      }

      EditItem item = history.get(position);
      position++;
      return item;
    }
  }

  private final class EditItem
  {
    private int start;
    private CharSequence before;
    private CharSequence after;

    public EditItem(int start, CharSequence before, CharSequence after)
    {
      this.start = start;
      this.before = before;
      this.after = after;
    }

    @Override
    public String toString()
    {
      return "EditItem{"
              + "start=" + start
              + ", before=" + before
              + ", after=" + after
              + '}';
    }
  }

  enum ActionType
  {
    INSERT, DELETE, PASTE, NOT_DEF;
  }

  private final class EditTextChangeListener implements TextWatcher
  {
    private CharSequence beforeChange;
    private CharSequence afterChange;
    private ActionType lastActionType = ActionType.NOT_DEF;
    private long lastActionTime = 0;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, 
      int after)
    {
      if (isUndoOrRedo)
      {
        return;
      }

      beforeChange = s.subSequence(start, start + count);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
      if (isUndoOrRedo)
      {
        return;
      }

      afterChange = s.subSequence(start, start + count);
      makeBatch(start);
    }

    private void makeBatch(int start)
    {
      ActionType at = getActionType();
      EditItem editItem = editHistory.getCurrent();
      if ((lastActionType != at || ActionType.PASTE == at
           || System.currentTimeMillis() - lastActionTime > 1000)
           || editItem == null)
      {
        editHistory.add(new EditItem(start, beforeChange, afterChange));
      }
      else
      {
        if (at == ActionType.DELETE)
        {
          editItem.start = start;
          editItem.before = TextUtils.concat(beforeChange, editItem.before);
        }
        else
        {
          editItem.after = TextUtils.concat(editItem.after, afterChange);
        }
      }
      lastActionType = at;
      lastActionTime = System.currentTimeMillis();
    }

    private ActionType getActionType()
    {
      if (!TextUtils.isEmpty(beforeChange) && TextUtils.isEmpty(afterChange))
      {
        return ActionType.DELETE;
      }
      else if (TextUtils.isEmpty(beforeChange) && !TextUtils.isEmpty(afterChange))
      {
        return ActionType.INSERT;
      }
      else
      {
        return ActionType.PASTE;
      }
    }

    @Override
    public void afterTextChanged(Editable s)
    {
    }
  }
}
