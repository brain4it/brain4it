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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public abstract class ModuleActivity extends Activity
{
  protected Module module;
  private int serverIndex;
  private int moduleIndex;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    app.setupActivity(this, true);

    Intent intent = getIntent();
    if (intent != null)
    {
      serverIndex = intent.getIntExtra("serverIndex", -1);
      if (serverIndex != -1)
      {
        Server server = getWorkspace().getServers().get(serverIndex);
        moduleIndex = intent.getIntExtra("moduleIndex", -1);
        if (moduleIndex != -1)
        {
          module = server.getModules().get(moduleIndex);
          setTitle(server.getName() + " : " + module.getName());
        }
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.module_menu, menu);
    return true;
  }

  @Override
  public void onBackPressed()
  {
    showActivity(ModuleListActivity.class, true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.showDashboard:
        View view = getCurrentFocus();
        if (view != null)
        {
          InputMethodManager imm = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        showActivity(DashboardActivity.class, false);
        break;
      case R.id.showConsole:
        showActivity(ConsoleActivity.class, false);
        break;
      case R.id.showEditor:
        showActivity(EditorActivity.class, false);
        break;
      case android.R.id.home:
        showActivity(ModuleListActivity.class, true);
        break;
    }
    return true;
  }

  public void showActivity(Class activityClass, boolean clearTop)
  {
    Intent intent = new Intent(this, activityClass);
    if (clearTop)
    {
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }
    else
    {
      intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }
    intent.putExtra("serverIndex", serverIndex);
    intent.putExtra("moduleIndex", moduleIndex);
    startActivity(intent);
  }

  private Workspace getWorkspace()
  {
    return ((ManagerApplication)getApplicationContext()).getWorkspace();
  }
}
