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

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public class ServerListActivity extends ListActivity
{
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
    app.setupActivity(this, false);

    setTitle(R.string.servers);

    updateServerList();

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
    registerForContextMenu(listView);
    listView.setOnItemClickListener(new OnItemClickListener()
    {
      @Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
      {
        Intent intent = new Intent(ServerListActivity.this,
          ModuleListActivity.class);
        intent.putExtra("serverIndex", position);
        startActivity(intent);
			}
		});
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    if (getWorkspace() != null)
    {
      updateServerList();
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    ((ManagerApplication)getApplicationContext()).saveWorkspace();
  }

  public void updateServerList()
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        setListAdapter(new ServerAdapter(ServerListActivity.this,
          getWorkspace().getServers()));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.server_list_menu, menu);
    return true;
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu,
    final View view, final ContextMenuInfo menuInfo)
  {
    super.onCreateContextMenu(menu, view, menuInfo);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.server_list_ctx_menu, menu);

    int serverIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
    Server server = getWorkspace().getServers().get(serverIndex);
    menu.setHeaderTitle(server.getName());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.addServer:
        addServer();
        break;
      case R.id.preferences:
        preferences();
        break;
      case R.id.sortServers:
        sortServers();
        break;
      case R.id.threadDump:
        createThreadDump();
        break;
      case R.id.about:
        about();
        break;
      case R.id.exit:
        finish();
        System.exit(0);
        break;
    }
    return true;
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    AdapterView.AdapterContextMenuInfo info =
       (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

    int serverIndex = info.position;
    if (item.getItemId() == R.id.editServer)
    {
      editServer(serverIndex);
    }
    else if(item.getItemId() == R.id.removeServer)
    {
      removeServer(serverIndex);
    }
    return true;
  }

  private void addServer()
  {
    Intent intent = new Intent(ServerListActivity.this,
      ServerSetupActivity.class);
    intent.putExtra("title", getResources().getString(R.string.addServer));
    startActivity(intent);
  }

  private void preferences()
  {
    Intent intent = new Intent(ServerListActivity.this,
      PreferencesActivity.class);
    startActivity(intent);
  }

  private void about()
  {
    Intent intent = new Intent(ServerListActivity.this,
      AboutActivity.class);
    startActivity(intent);
  }

  private void editServer(int serverIndex)
  {
    Intent intent = new Intent(ServerListActivity.this,
      ServerSetupActivity.class);
    intent.putExtra("title", getResources().getString(R.string.editServer));
    intent.putExtra("serverIndex", serverIndex);
    startActivity(intent);
  }

  private void removeServer(int serverIndex)
  {
    getWorkspace().getServers().remove(serverIndex);
    updateServerList();
  }

  private void sortServers()
  {
    Workspace workspace = getWorkspace();
    workspace.sortServersByName();
    updateServerList();
  }

  private void createThreadDump()
  {
    try
    {
      File baseDir = new File(Environment.getExternalStorageDirectory(),
        "brain4it");
      baseDir.mkdirs();
      File file = new File(baseDir + "/threads.txt");
      PrintWriter writer = new PrintWriter(file);

      Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
      for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet())
      {
        Thread thread = entry.getKey();
        writer.println("Thread: " + thread.getName() +
          " id:" + thread.getId() + ":");
        StackTraceElement[] stack = entry.getValue();
        for (StackTraceElement elem : stack)
        {
          writer.println(elem);
        }
        writer.println("-------------------------------------");
      }
      writer.flush();
      writer.close();

      MimeTypeMap myMime = MimeTypeMap.getSingleton();
      String mimeType = myMime.getMimeTypeFromExtension("txt");
      Intent newIntent = new Intent(Intent.ACTION_VIEW);
      newIntent.setDataAndType(Uri.fromFile(file), mimeType);
      newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try
      {
        startActivity(newIntent);
      }
      catch (ActivityNotFoundException e)
      {
        ToastUtils.showLong(this, "No handler for this type of file.");
      }
    }
    catch (Exception ex)
    {
      ToastUtils.showLong(this, ex.toString());
    }
  }

  public class ServerAdapter extends BaseAdapter
  {
    private final Context context;
    private final List<Server> servers;

    public ServerAdapter(Context context, List<Server> servers)
    {
      this.context = context;
      this.servers = servers;
    }

    @Override
    public int getCount()
    {
      return servers.size();
    }

    @Override
    public Object getItem(int position)
    {
      return servers.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      LayoutInflater inflater = getLayoutInflater();
      View itemView = inflater.inflate(R.layout.server_item, parent, false);

      ImageView imageView = (ImageView)itemView.findViewById(R.id.serverIcon);
      imageView.setImageResource(R.drawable.server);

      TextView serverNameView = (TextView)itemView.findViewById(R.id.serverName);
      serverNameView.setText(servers.get(position).getName());

      TextView serverUrlView = (TextView)itemView.findViewById(R.id.serverUrl);
      serverUrlView.setText(servers.get(position).getUrl());

      return itemView;
    }
  }

  private Workspace getWorkspace()
  {
    return ((ManagerApplication)getApplicationContext()).getWorkspace();
  }
}
