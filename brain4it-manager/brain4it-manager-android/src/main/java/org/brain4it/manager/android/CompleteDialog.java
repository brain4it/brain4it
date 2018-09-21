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

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;
import org.brain4it.io.IOConstants;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.TextCompleter;
import org.brain4it.manager.TextCompleter.Candidate;

/**
 *
 * @author realor
 */
public class CompleteDialog extends Dialog
{
  private final Activity activity;
  private final Module module;
  private final EditText editText;
  private final TextCompleter textCompleter;
  private ListView listView;
  private List<Candidate> candidates;
  private String head;
  
  public CompleteDialog(Activity activity, Module module, EditText editText)
  {
    super(activity, R.style.fullScreenDialog);
    this.activity = activity;
    this.module = module;
    this.editText = editText;
    this.textCompleter = new TextCompleter(module);
  }

 @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.complete_dialog);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    
    listView = (ListView)findViewById(R.id.completeList);
    listView.setAdapter(new CandidateAdapter());
    
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
      {
        Candidate candidate = (Candidate)listView.getItemAtPosition(position);
        String candidateName = candidate.getName();
        int index = head.lastIndexOf(IOConstants.PATH_REFERENCE_SEPARATOR);
        String tail;
        if (index == -1)
        {
          tail = candidateName.substring(head.length());
        }
        else
        {
          String lastHead = head.substring(index + 1);
          tail = candidateName.substring(lastHead.length());
        }
        int pos = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(pos, tail);

        dismiss();
			}
		});    
  }
  
  public void showCandidates()
  {
    String text = editText.getText().toString();
    text = text.substring(0, editText.getSelectionEnd());
    head = textCompleter.findHead(text);
    
    try
    {
      if (head.length() > 0)
      {
        BSoftReference.getInstance(head);
      }

      textCompleter.complete(head, new TextCompleter.OnCompleteListener()
      {
        @Override
        public void textCompleted(String head, final List<Candidate> candidates)
        {
          if (candidates.isEmpty())
          {
            ToastUtils.showShort(activity, "No candidates");
          }
          else
          {
            activity.runOnUiThread(new Runnable()
            {
              @Override
              public void run()
              {
                CompleteDialog.this.candidates = candidates;
                CompleteDialog.this.show();         
              }
            });
          }
        }
      });
    }
    catch (Exception ex)
    {
      // ignore, head is not reference
    }
  }
  
  public class CandidateAdapter extends BaseAdapter 
  {
    public CandidateAdapter()
    {
    }

    @Override
    public int getCount()
    {
      return candidates.size();
    }

    @Override
    public Object getItem(int position)
    {
      return candidates.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Candidate candidate = candidates.get(position);
      LayoutInflater inflater = getLayoutInflater();
      View itemView = inflater.inflate(R.layout.candidate_item, parent, false);
      
      ImageView imageView =
        (ImageView)itemView.findViewById(R.id.candidateIcon);
      String type = candidate.getType();
      int resourceId;
      if (type.equals(Utils.FUNCTION_LIST_SUBTYPE) ||
        type.equals(Utils.HARD_REFERENCE_SUBTYPE))
      {
        resourceId = R.drawable.type_function_list;
      }
      else if (type.endsWith(Utils.LIST_TYPE))
      {
        resourceId = R.drawable.type_list;
      }
      else if (type.endsWith(Utils.INTEGER_SUBTYPE) || 
        type.endsWith(Utils.LONG_SUBTYPE) ||
        type.endsWith(Utils.DOUBLE_SUBTYPE))
      {
        resourceId = R.drawable.type_number;
      }
      else if (type.equals(Utils.STRING_TYPE))
      {
        resourceId = R.drawable.type_string;
      }
      else if (type.equals(Utils.BOOLEAN_TYPE))
      {
        resourceId = R.drawable.type_boolean;
      }
      else if (type.equals(Utils.SOFT_REFERENCE_SUBTYPE))
      {
        resourceId = R.drawable.type_reference;
      }
      else
      {
        resourceId = R.drawable.type_atom;
      }
      imageView.setImageResource(resourceId);

      TextView textView = 
        (TextView)itemView.findViewById(R.id.candidateName);
      textView.setText(candidate.getName());
      if (candidate.getType().equals(Utils.HARD_REFERENCE_SUBTYPE))
      {
        textView.setTypeface(null, Typeface.BOLD);
      }
      else
      {
        textView.setTypeface(null, Typeface.NORMAL);        
      }
      return itemView;
    }
  }
}
