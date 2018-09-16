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
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import java.util.HashMap;
import org.brain4it.io.Importer;
import org.brain4it.manager.Workspace;
import static org.brain4it.server.ServerConstants.BPL_CHARSET;

/**
 *
 * @author realor
 */
public class ModuleListActivity extends ListActivity
{
  private Server server;
  private int serverIndex;
  
  /**
   * Called when the activity is first created.
   *
   * @param icicle
   */
  @Override
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    app.setupActivity(this, true);
    
    Intent intent = getIntent();
    if (intent != null)
    {
      serverIndex = intent.getIntExtra("serverIndex", -1);
      if (serverIndex != -1)
      {
        server = getWorkspace().getServers().get(serverIndex);    
        setTitle(server.getName());
        updateModuleList();

        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView<?> parent, View view,
            int moduleIndex, long id)
          {
            openModule(moduleIndex);
          }
        });
      }
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    updateModuleList();
  }
  
  public void updateModuleList()
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        setListAdapter(new ModuleAdapter(ModuleListActivity.this,
          server.getModules()));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.module_list_menu, menu);
    return true;
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu,
    final View view, final ContextMenu.ContextMenuInfo menuInfo)
  {
    int moduleIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
    Module module = server.getModules().get(moduleIndex);
    menu.setHeaderTitle(server.getName() + " : " + module.getName());
    menu.add(0, 1, 0, R.string.editModule);
    menu.add(0, 2, 0, R.string.importModule);
    menu.add(0, 3, 0, R.string.removeModule);
    menu.add(0, 4, 0, R.string.destroyModule);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.listModules:
        listModules();
        break;
      case R.id.addModule:
        addModule();
        break;
      case R.id.createModule:
        createModule();
        break;
      case android.R.id.home:
        finish();
        break;
    }
    return true;
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    AdapterView.AdapterContextMenuInfo info =
       (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

    int moduleIndex = info.position;

    switch (item.getItemId())
    {
    // editModule
      case 1:
        editModule(moduleIndex);
        break;
    // importModule
      case 2:
        importModule(moduleIndex);
        break;
    // removeModule
      case 3:
        removeModule(moduleIndex);
        break;
    // destroyModule
      case 4:
        destroyModule(moduleIndex);
        break;
      default:
        break;
    }
    return true;
  }

  private void listModules()
  {
    RestClient restClient = server.getRestClient();
    restClient.listModules(new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        try
        {
          BList moduleList = (BList)Parser.fromString(resultString);

          HashMap<String, Module> moduleMap = new HashMap<String, Module>();
          // save previous modules
          for (int i = 0; i < server.getModules().size(); i++)
          {
            Module module = server.getModules().get(i);
            moduleMap.put(module.getName(), module);
          }
          // rebuild module list
          server.getModules().clear();
          for (int i = 0; i < moduleList.size(); i++)
          {
            String moduleName;
            BList metadata;
            Object info = moduleList.get(i);
            if (info instanceof BList)
            {
              BList moduleInfo = (BList)info;
              moduleName = (String)moduleInfo.get(0);
              metadata = (BList)moduleInfo.get(1);
            }
            else if (info instanceof String)
            {
              moduleName = (String)info;
              metadata = null;
            }
            else throw new Exception("Invalid module list");

            Module module = moduleMap.get(moduleName);
            if (module == null)
            {
              module = new Module(server, moduleName);
            }
            module.setMetadata(metadata);
            server.getModules().add(module);
          }
          updateModuleList();
          String message = String.format(
            getResources().getString(R.string.modulesFound),
            moduleList.size());
          ToastUtils.showLong(ModuleListActivity.this, message);
        }
        catch (Exception ex)
        {
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(ModuleListActivity.this, ex.toString());
      }
    });
  }

  private void addModule()
  {
    Intent intent = new Intent(ModuleListActivity.this, ModuleSetupActivity.class);
    intent.putExtra("title", getResources().getString(R.string.addModule));
    intent.putExtra("createModule", false);
    intent.putExtra("serverIndex", serverIndex);
    startActivity(intent);
  }

  private void createModule()
  {
    Intent intent = new Intent(ModuleListActivity.this, ModuleSetupActivity.class);
    intent.putExtra("title", getResources().getString(R.string.createModule));
    intent.putExtra("createModule", true);
    intent.putExtra("serverIndex", serverIndex);
    startActivity(intent);
  }

  private void editModule(int moduleIndex)
  {
    Intent intent = new Intent(ModuleListActivity.this, ModuleSetupActivity.class);
    intent.putExtra("title", getResources().getString(R.string.editModule));
    intent.putExtra("createModule", false);
    intent.putExtra("serverIndex", serverIndex);
    intent.putExtra("moduleIndex", moduleIndex);
    startActivity(intent);
  }

  private void openModule(int moduleIndex)
  {
    Intent intent = new Intent(this, DashboardActivity.class);
    intent.putExtra("serverIndex", serverIndex);
    intent.putExtra("moduleIndex", moduleIndex);
    startActivity(intent);
  }

  private void removeModule(int moduleIndex)
  {
    server.getModules().remove(moduleIndex);
    updateModuleList();
  }

  private void destroyModule(final int moduleIndex)
  {
    final Module module = server.getModules().get(moduleIndex);
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle(R.string.destroyModule);
    String message = String.format(getResources().getString(
      R.string.confirmDestroyModule), module.getName());
    dialog.setMessage(message);
    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        RestClient restClient = module.getRestClient();
        restClient.destroyModule(module.getName(), new Callback()
        {
          @Override
          public void onSuccess(RestClient client, String resultString)
          {
            ToastUtils.showLong(ModuleListActivity.this, resultString);
            removeModule(moduleIndex);
          }

          @Override
          public void onError(RestClient client, Exception ex)
          {
            ToastUtils.showLong(ModuleListActivity.this, ex.toString());
          }
        });
      }
    });
    dialog.setNegativeButton(R.string.no, null);
    dialog.show();
  }

  private void importModule(final int moduleIndex)
  {
    final Module module = server.getModules().get(moduleIndex);
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle(R.string.enterUrl);
    // Set up the input
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT |
      InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    dialog.setView(input);

    dialog.setPositiveButton(R.string.imp, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        String url = input.getText().toString();
        Importer importer = new Importer(url, BPL_CHARSET)
        {
          @Override
          protected void onSuccess(String dataString)
          {
            putData(module, dataString);
          }

          @Override
          protected void onError(Exception ex)
          {
            ToastUtils.showLong(ModuleListActivity.this, ex.toString());
          }
        };
        importer.importData();
      }
    });
    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        dialog.cancel();
      }
    });
    dialog.show();
  }

  private void putData(Module module, String dataString)
  {
    RestClient client = module.getRestClient();
    client.setConnectionTimeout(10000);
    client.setReadTimeout(10000);
    client.put(module.getName(), "", dataString, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        ToastUtils.showLong(ModuleListActivity.this, "Import completed.");
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(ModuleListActivity.this, ex.toString());
      }
    });
  }

  public class ModuleAdapter extends BaseAdapter
  {
    private final Context context;
    private final List<Module> modules;

    public ModuleAdapter(Context context, List<Module> modules)
    {
      this.context = context;
      this.modules = modules;
    }

    @Override
    public int getCount()
    {
      return modules.size();
    }

    @Override
    public Object getItem(int position)
    {
      return modules.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Module module = modules.get(position);
      LayoutInflater inflater = getLayoutInflater();
      View itemView = inflater.inflate(R.layout.module_item, parent, false);

      ImageView imageView = (ImageView)itemView.findViewById(R.id.moduleIcon);
      int iconResource = getModuleResource(module);
      imageView.setImageResource(iconResource);

      String description = getModuleDescription(module);
      TextView descriptionView =
        (TextView)itemView.findViewById(R.id.moduleDescription);
      descriptionView.setText(description);

      TextView moduleNameView = (TextView)itemView.findViewById(R.id.moduleName);
      moduleNameView.setText(module.getName());

      return itemView;
    }
  }

  private String getModuleDescription(Module module)
  {
    BList metadata = module.getMetadata();
    if (metadata != null)
    {
      Object value = metadata.get("description");
      if (value instanceof String)
      {
        return (String)value;
      }
    }
    return "Module " + module.getName();
  }

  private int getModuleResource(Module module)
  {
    BList metadata = module.getMetadata();
    if (metadata != null)
    {
      Object value = metadata.get("icon");
      if ("air_conditioning".equals(value))
      {
        return R.drawable.air_conditioning;
      }
      else if ("domotics".equals(value))
      {
        return R.drawable.domotics;
      }
      else if ("energy".equals(value))
      {
        return R.drawable.energy;
      }
      else if ("garbage_collection".equals(value))
      {
        return R.drawable.garbage_collection;
      }
      else if ("irrigation".equals(value))
      {
        return R.drawable.irrigation;
      }
      else if ("lighting".equals(value))
      {
        return R.drawable.lighting;
      }
      else if ("math".equals(value))
      {
        return R.drawable.math;
      }
      else if ("mobility".equals(value))
      {
        return R.drawable.mobility;
      }
      else if ("public_lighting".equals(value))
      {
        return R.drawable.public_lighting;
      }
      else if ("robotics1".equals(value))
      {
        return R.drawable.robotics1;
      }
      else if ("robotics2".equals(value))
      {
        return R.drawable.robotics2;
      }
      else if ("sensors".equals(value))
      {
        return R.drawable.sensors;
      }
      else if ("weather".equals(value))
      {
        return R.drawable.weather;
      }
    }
    return R.drawable.module;
  }

  private Workspace getWorkspace()
  {
    return ((ManagerApplication)getApplicationContext()).getWorkspace();
  }
}
