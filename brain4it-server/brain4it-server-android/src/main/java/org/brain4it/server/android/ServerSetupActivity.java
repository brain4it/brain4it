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

package org.brain4it.server.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *
 * @author realor
 */
public class ServerSetupActivity extends Activity
{
  private EditText serverPortInput;
  private EditText accessKeyInput;  
  private SharedPreferences preferences;  
  private Button okButton;
    
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setTitle(R.string.setup);
    
    setContentView(R.layout.setup);

    preferences = getSharedPreferences("org.brain4it.server", MODE_PRIVATE);

    serverPortInput = (EditText)findViewById(R.id.serverPortInput);
    accessKeyInput = (EditText)findViewById(R.id.accessKeyInput);
    okButton = (Button)findViewById(R.id.serverOkButton);

    okButton.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        saveSetup();
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        finish();
        break;
    }
    return true;
  }  
  
  
  @Override
  public void onResume()
  {
    super.onResume();
    
    try
    {
      int serverPort = preferences.getInt("serverPort", 
        AndroidService.DEFAULT_SERVER_PORT);
      serverPortInput.setText(String.valueOf(serverPort));
      
      accessKeyInput.setText(preferences.getString("accessKey", null));
    }
    catch (Exception ex)
    {      
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
  }
   
  protected void saveSetup()
  {
    SharedPreferences.Editor editor = preferences.edit();
    try
    {
      String serverPortString = serverPortInput.getText().toString();
      int serverPort = Integer.parseInt(serverPortString);
      if (serverPort < 1024 || serverPort > 65535)
        throw new RuntimeException("Invalid port number");
      editor.putInt("serverPort", serverPort);

      String accessKey = accessKeyInput.getText().toString().trim();
      if (accessKey.length() == 0)
      {
        accessKey = null;
      }
      editor.putString("accessKey", accessKey);

      editor.commit();
      finish();
    }
    catch (Exception ex)
    {
      String message = ex instanceof RuntimeException ? 
        ex.getMessage() : ex.toString();
      
      Toast toast = Toast.makeText(ServerSetupActivity.this, message, 
        Toast.LENGTH_LONG);
      toast.show();        
    }
  }
}
