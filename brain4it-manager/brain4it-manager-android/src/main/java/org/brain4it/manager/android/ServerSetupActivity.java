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
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public class ServerSetupActivity extends Activity
{
  private Server server;
  private EditText serverNameInput;
  private EditText serverUrlInput;
  private EditText accessKeyInput;
  private Button okButton;
  
  /**
   * Called when the activity is first created.
   * @param icicle
   */
  @Override
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    app.setupActivity(this, true);
    
    setContentView(R.layout.server_setup);

    serverNameInput = (EditText)findViewById(R.id.serverNameInput);
    serverUrlInput = (EditText)findViewById(R.id.serverUrlInput);
    accessKeyInput = (EditText)findViewById(R.id.accessKeyInput);
    okButton = (Button)findViewById(R.id.serverOkButton);

    Intent intent = getIntent();
    if (intent != null)
    {
      String title = intent.getStringExtra("title");
      if (title != null) setTitle(title);
      int serverIndex = intent.getIntExtra("serverIndex", -1);
      if (serverIndex != -1)
      {
        server = getWorkspace().getServers().get(serverIndex);      
        serverNameInput.setText(server.getName());
        serverUrlInput.setText(server.getUrl());
        accessKeyInput.setText(server.getAccessKey());
      }

      okButton.setOnClickListener(new Button.OnClickListener()
      {
        @Override
        public void onClick(View view)
        {
          if (server == null)
          {
            server = new Server(getWorkspace());
            getWorkspace().getServers().add(server);
          }
          server.setName(serverNameInput.getText().toString());
          server.setUrl(serverUrlInput.getText().toString());
          server.setAccessKey(accessKeyInput.getText().toString());
          finish();
        }
      });    
    }
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

  private Workspace getWorkspace()
  {
    return ((ManagerApplication)getApplicationContext()).getWorkspace();
  }
}
