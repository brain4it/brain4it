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

import android.app.AlertDialog;
import android.content.Context;
import static android.content.Context.MODE_PRIVATE;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Formatter;
import org.brain4it.manager.android.view.EditCode;

/**
 *
 * @author realor
 */
public class EditorActivity extends ModuleActivity
{
  private EditText pathInputText;
  private ImageButton loadButton;
  private ImageButton saveButton;
  private EditCode inputText;
  private final Formatter formatter = new Formatter();
  private Handler handler = new Handler();
  private boolean modified = false;

  /**
   * Called when the activity is first created.
   *
   * @param icicle
   */

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    app.setupActivity(this, true);
    
    setContentView(R.layout.editor);

    pathInputText = (EditText)findViewById(R.id.path);
    loadButton = (ImageButton)findViewById(R.id.load_button);
    saveButton = (ImageButton)findViewById(R.id.save_button);
    inputText = (EditCode)findViewById(R.id.input);
        
    inputText.setFunctionNames(module.getFunctionNames());
    if (module.getFunctionNames().isEmpty())
    {
      module.findFunctions(null);
    }

    SharedPreferences preferences = 
      getSharedPreferences(ManagerApplication.PREFERENCES, MODE_PRIVATE);

    int textSize = preferences.getInt("textSize", 15);
    int indentSize = preferences.getInt("indentSize", 2);
    int formatColumns = preferences.getInt("formatColumns", 40);

    inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    inputText.getAutoIndenter().setIndentSize(indentSize);
    formatter.setMaxColumns(formatColumns);
    formatter.setIndentSize(indentSize);
    
    inputText.addTextChangedListener(new TextWatcher()
    {
      @Override
      public void onTextChanged(CharSequence cs, int start, int count, int after)
      {
      }

      @Override
      public void beforeTextChanged(CharSequence cs,
              int start, int before, int count)
      {
      }

      @Override
      public void afterTextChanged(Editable edtbl)
      {
        modified = true;
      }
    });

    loadButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        loadData();
      }
    });

    saveButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (inputText.length() > 0)
        {
          saveData();
        }
      }
    });
  }

  @Override
  public void onResume()
  {
    super.onResume();
    handler.post(new Runnable()
    {
      @Override
      public void run()
      {
        if (inputText.length() > 0)
        {
          inputText.requestFocus();
          InputMethodManager imm = (InputMethodManager)
           getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
        }
        else
        {
          pathInputText.requestFocus();
          InputMethodManager imm = (InputMethodManager)
           getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(pathInputText, InputMethodManager.SHOW_IMPLICIT);
        }
      }
    });
  }

  @Override
  public void onPause()
  {
    super.onPause();
  }
  
  protected void loadData()
  {
    final String path = pathInputText.getText().toString();
    if (path == null || path.trim().length() == 0) return;

    if (modified)
    {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      String message = String.format(
        getResources().getString(R.string.confirmDiscardChanges), 
        module.getName());
      dialog.setMessage(message);
      dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          loadData(path);
        }
      });
      dialog.setNegativeButton(R.string.no, null);
      dialog.show();
    }
    else
    {
      loadData(path);
    }
  }

  protected void loadData(String path)
  {
    RestClient restClient = module.getRestClient();
    restClient.get(module.getName(), path, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        onReadSuccess(resultString);
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(EditorActivity.this, ex.toString());
      }
    });
  }
  
  protected void onReadSuccess(String resultString)
  {
    try
    {
      final String data = formatter.format(resultString);
      runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          inputText.setText(data);
          modified = false;
          ToastUtils.showLong(EditorActivity.this,
            getResources().getString(R.string.loadSuccessfull));
        }
      });
    }
    catch (Exception ex)
    {
      ToastUtils.showLong(EditorActivity.this, ex.toString());
    }    
  }
  
  protected void saveData()
  {
    final String path = pathInputText.getText().toString();
    if (path == null || path.trim().length() == 0) return;

    RestClient restClient = module.getRestClient();
    String data = inputText.getText().toString();

    restClient.put(module.getName(), path, data, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        ToastUtils.showLong(EditorActivity.this,
          getResources().getString(R.string.saveSuccessfull));
        modified = false;
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(EditorActivity.this, ex.toString());
      }
    });
  }
}
