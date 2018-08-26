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

import android.content.Context;
import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Formatter;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.Utils;
import org.brain4it.manager.CommandHistory;
import org.brain4it.manager.Module;
import org.brain4it.manager.android.view.CodeListView;
import org.brain4it.manager.android.view.EditCode;

public class ConsoleActivity extends ModuleActivity
{
  static final String PROMPT = "> ";
  
  private CodeListView outputList;
  private EditCode inputText;
  private Button parenthesisButton;
  private Button arrowButton;
  private Button functionsButton;
  private Button clearButton;
  private Button historyNextButton;
  private Button historyPreviousButton;
  private Button executeButton;
  private final CommandHistory history = new CommandHistory();
  private final Formatter formatter = new Formatter();
  private final Handler handler = new Handler();

  /**
   * Called when the activity is first created.
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.console);
  
    outputList = (CodeListView)findViewById(R.id.output);
    inputText = (EditCode)findViewById(R.id.input);
    parenthesisButton = (Button)findViewById(R.id.parenthesis_button);
    arrowButton = (Button)findViewById(R.id.arrow_button);
    functionsButton = (Button)findViewById(R.id.functions_button);
    clearButton = (Button)findViewById(R.id.clear_button);
    historyNextButton = (Button)findViewById(R.id.history_next_button);
    historyPreviousButton = (Button)findViewById(R.id.history_previous_button);
    executeButton = (Button)findViewById(R.id.execute_button);

    functionsButton.setText("\u0192");
    clearButton.setText("\u2573");
    historyNextButton.setText("\u2193");
    historyPreviousButton.setText("\u2191");
    
    SharedPreferences preferences = 
      getSharedPreferences(ManagerApplication.PREFERENCES, MODE_PRIVATE);

    int textSize = preferences.getInt("textSize", 15);
    int indentSize = preferences.getInt("indentSize", 2);
    int formatColumns = preferences.getInt("formatColumns", 40);
    
    outputList.setTextSize(textSize);
    inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    formatter.setMaxColumns(formatColumns);
    formatter.setIndentSize(indentSize);
    
    inputText.addTextChangedListener(new TextWatcher()
    {
      @Override
      public void onTextChanged(CharSequence cs, int start, int before, int count)
      {
        boolean crAtEnd = false;
        int i = 0;
        int k = start;
        while (i < count && !crAtEnd)
        {
          if (cs.charAt(k) == '\n' && k + 1 == cs.length())
          {
            crAtEnd = true;
          }
          else
          {
            i++;
            k++;
          }
        }
        if (crAtEnd)
        {
          String command = inputText.getText().toString();
          if (isValidCommand(command))
          {
            execute(command);
          }  
        }
      }

      @Override
      public void beforeTextChanged(CharSequence cs,
              int start, int count, int after)
      {
      }

      @Override
      public void afterTextChanged(Editable editable)
      {        
      }
    });
    
    outputList.setOnItemClickListener(new OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> adapter, View view, 
        int position, long id)
      {
        CodeListView.Item item = 
         (CodeListView.Item)adapter.getItemAtPosition(position);
        int selection = inputText.getSelectionStart();
        if (item.getType() == CodeListView.COMMAND)
        {
          String command = item.getText().substring(PROMPT.length());
          inputText.getText().insert(selection, command);
        }
        else if (item.getType() == CodeListView.RESULT)
        {
          try
          {
            String result = formatter.format(item.getText());
            if (result.endsWith("\n"))
            {
              result = result.substring(0, result.length() - 1);
            }
            inputText.getText().insert(selection, result);
          }
          catch (Exception ex)
          {            
          }
        }
      }
    });

    parenthesisButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        int selection = inputText.getSelectionStart();
        inputText.getText().insert(selection, "()");
        inputText.setSelection(selection + 1);
      }
    });

    arrowButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        int selection = inputText.getSelectionStart();
        inputText.getText().insert(selection, "=>");
        inputText.setSelection(selection + 2);
      }
    });
    
    functionsButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        module.findFunctions(new Module.Callback()
        {
          @Override
          public void actionCompleted(Module module, String action)
          {
            showResult(Printer.toString(module.getFunctions()));
          }
          
          @Override
          public void actionFailed(Module module, String action, Exception ex)
          {
            showError(ex);
          }
        });
      }
    });
    
    clearButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        clearConsole();
      }      
    });
    
    historyNextButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (!history.isEmpty())
        {
          String command = history.getNext();
          inputText.setText(command);
          inputText.setSelection(command.length());
        }
      }
    });

    historyPreviousButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (!history.isEmpty())
        {
          String command = history.getPrevious();
          inputText.setText(command);
          inputText.setSelection(command.length());
        }
      }
    });
    
    executeButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        String command = inputText.getText().toString();
        execute(command);
      }
    });

    inputText.setFunctionNames(module.getFunctionNames());
    outputList.setFunctionNames(module.getFunctionNames());
    if (module.getFunctionNames().isEmpty())
    {
      module.findFunctions(null);
    }
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
        inputText.requestFocus();
        InputMethodManager imm = (InputMethodManager)
          getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
      }
    });
  }

  @Override
  public void onPause()
  {
    super.onPause();    
  }
  
  protected void showResult(String resultString)
  {
    showResult(resultString, false);
  }
  
  protected void showResult(String resultString, boolean formatted)
  {
    try
    {
      if (resultString.startsWith("\""))
      {
        String text = Utils.unescapeString(resultString);
        appendText(CodeListView.STRING, text);
      }
      else
      {
        String text = formatted ? 
          formatter.format(resultString) : resultString;
        appendText(CodeListView.RESULT, text);
      }    
    }
    catch (Exception ex)
    {
    }
  }
  
  protected void showError(Exception ex)
  {
    String text;
    if (ex instanceof RuntimeException)
    {
      text = ex.getMessage();
    }
    else
    {
      text = ex.toString();
    }
    appendText(CodeListView.ERROR, text);                  
  }

  protected void appendText(final int type, final String text)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        outputList.append(type, text);
      }
    });
  }
  
  protected void clearConsole()
  {
    outputList.clear();
    inputText.setText("");
  }

  protected void execute(String command)
  {
    command = purgeCommand(command);
    RestClient client = module.getRestClient();
    client.setMethod("POST");
    client.setPath(module.getName());
    appendText(CodeListView.COMMAND, PROMPT + command);
    history.add(command);
    inputText.setText(null);
    client.setDataString(command);
    client.send(new Callback()
    {
      @Override
      public void onSuccess(RestClient restClient, String resultString)
      {
        showResult(resultString, true);
      }

      @Override
      public void onError(RestClient rc, Exception exception)
      {
        showError(exception);
      }
    });
  }
  
  protected boolean isValidCommand(String command)
  {
    try
    {
      Parser.fromString(command);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  protected String purgeCommand(String command)
  {
    StringBuilder buffer = new StringBuilder(command);
    int i = buffer.length() - 1;
    boolean stop = false;
    while (i >= 0 && !stop)
    {
      char ch = buffer.charAt(i);
      if (ch != ' ' && ch != '\t' && ch != '\n') stop = true;
      else i--;
    }
    buffer.setLength(i + 1);
    return buffer.toString();
  }  
}
