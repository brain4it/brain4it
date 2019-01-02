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
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public class ModuleSetupActivity extends Activity
{
  private Server server;
  private Module module;
  private EditText moduleNameInput;
  private EditText accessKeyInput;
  private Button okButton;
  private boolean createModule;

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

    setContentView(R.layout.module_setup);

    moduleNameInput = (EditText)findViewById(R.id.moduleNameInput);
    accessKeyInput = (EditText)findViewById(R.id.accessKeyInput);
    okButton = (Button)findViewById(R.id.moduleOkButton);

    Intent intent = getIntent();
    if (intent != null)
    {
      String title = intent.getStringExtra("title");
      if (title != null) setTitle(title);
      createModule = intent.getBooleanExtra("createModule", false);
      int serverIndex = intent.getIntExtra("serverIndex", -1);
      if (serverIndex != -1)
      {
        server = getWorkspace().getServers().get(serverIndex);
        int moduleIndex = intent.getIntExtra("moduleIndex", -1);
        if (moduleIndex != -1)
        {
          module = server.getModules().get(moduleIndex);
          moduleNameInput.setText(module.getName());
          accessKeyInput.setText(module.getAccessKey());
          moduleNameInput.setEnabled(false);
          accessKeyInput.requestFocus();
        }
        else
        {
          module = new Module(server);
          if (createModule)
          {
            module.randomAccessKey();
            accessKeyInput.setText(module.getAccessKey());
          }
        }

        okButton.setOnClickListener(new Button.OnClickListener()
        {
          @Override
          public void onClick(View view)
          {
            if (createModule)
            {
              createModule();
            }
            else if (module.getName() == null)
            {
              addModule();
            }
            else
            {
              editModule();
            }
          }
        });
      }
    }
  }

  private void createModule()
  {
    final String moduleName = moduleNameInput.getText().toString().trim();
    if (moduleName.length() == 0)
    {
      ToastUtils.showLong(this,
        getResources().getString(R.string.moduleNameMandatory));
      return;
    }

    final String accessKey = accessKeyInput.getText().toString();
    RestClient restClient = server.getRestClient();
    restClient.createModule(moduleName, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        ToastUtils.showLong(ModuleSetupActivity.this, resultString);
        module.setName(moduleName);
        module.setAccessKey(accessKey);
        server.getModules().add(module);
        module.saveAccessKey(server.getAccessKey(), null);
        finish();
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(ModuleSetupActivity.this, ex.toString());
      }
    });
  }

  private void addModule()
  {
    String moduleName = moduleNameInput.getText().toString().trim();
    if (moduleName.length() == 0)
    {
      ToastUtils.showLong(this,
        getResources().getString(R.string.moduleNameMandatory));
      return;
    }
    String accessKey = accessKeyInput.getText().toString();
    module.setName(moduleName);
    module.setAccessKey(accessKey);
    server.getModules().add(module);
    finish();
  }

  private void editModule()
  {
    String currentAccessKey = module.getAccessKey();
    String accessKey = accessKeyInput.getText().toString();
    module.setAccessKey(accessKey);
    module.saveAccessKey(currentAccessKey, null);
    finish();
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
