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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import org.brain4it.client.Invoker;
import org.brain4it.client.Monitor;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.android.view.BoxGridLayout;
import org.brain4it.manager.widgets.WidgetType;
import static org.brain4it.server.ServerConstants.DASHBOARDS_FUNCTION_NAME;

/**
 *
 * @author realor
 */
public class DashboardActivity extends ModuleActivity
{
  private Monitor monitor;
  private Invoker invoker;
  private Timer timer;
  private Button updateButton;
  private BoxGridLayout grid;
  private Spinner dashboardSpinner;
  private HashMap<String, View> widgets;
  private BList dashboards;
  private int dashboardIndex;
  private String sessionId;

  /**
   * Called when the activity is first created.
   *
   * @param savedInstanceState
   */

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    widgets = new HashMap<String, View>();
    setContentView(R.layout.dashboard);

    grid = (BoxGridLayout)findViewById(R.id.grid);
    updateButton = (Button)findViewById(R.id.update_button);
    updateButton.setText("\u21ba");
    dashboardSpinner = (Spinner)findViewById(R.id.dashboard_spinner);

    sessionId = UUID.randomUUID().toString();
    
    updateButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        loadDashboards();
      }
    });

    dashboardSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
    {
      @Override
      public void onItemSelected(AdapterView<?> av, View view, int index, long l)
      {
        if (dashboardIndex != index)
        {
          createDashboard(index);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> av)
      {
      }
    });

    loadDashboards();
  }
  
  @Override
  protected void onPause()
  {
    super.onPause();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    InputMethodManager imm = (InputMethodManager)
       getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(grid.getWindowToken(), 0);
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    unwatchAll();
    if (timer != null)
    {
      timer.cancel();
      timer = null;
    }
  }
  
  public String getSessionId()
  {
    return sessionId;
  }
  
  public RestClient getRestClient()
  {
    RestClient restClient = module.getRestClient();
    restClient.setSessionId(sessionId);
    return restClient;
  }
  
  public synchronized Monitor getMonitor()
  {
    if (monitor == null && module != null)
    {
      Server server = module.getServer();
      monitor = new Monitor(server.getUrl(), module.getName());
      monitor.setAccessKey(module.getAccessKey());
      monitor.setSessionId(sessionId);
    }
    return monitor;
  }

  public synchronized Invoker getInvoker()
  {
    if (invoker == null && module != null)
    {
      invoker = new Invoker(module.getRestClient(), module.getName());
    }
    return invoker;
  }
  
  public synchronized Timer getTimer()
  {
    if (timer == null)
    {
      timer = new Timer();
    }
    return timer;
  }

  public Module getModule()
  {
    return module;
  }

  public void loadDashboards()
  {
    unwatchAll();
    widgets.clear();
    grid.removeAllViews();
    dashboards = null;
    ArrayAdapter adapter = new ArrayAdapter(DashboardActivity.this,
      android.R.layout.simple_spinner_item, Collections.EMPTY_LIST);
    dashboardSpinner.setAdapter(adapter);

    RestClient restClient = module.getRestClient();
    restClient.invokeFunction(module.getName(), DASHBOARDS_FUNCTION_NAME,
      null, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        try
        {
          final Object result = Parser.fromString(resultString);
          DashboardActivity.this.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              ArrayList<String> dashboardNames = new ArrayList<String>();
              if (result instanceof BList)
              {
                dashboards = (BList)result;
                for (int i = 0; i < dashboards.size(); i++)
                {
                  String dashboardName = dashboards.getName(i);
                  if (dashboardName == null) dashboardName = "dashboard-" + i;
                  dashboardNames.add(dashboardName);
                }
              }
              ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(DashboardActivity.this,
                  android.R.layout.simple_spinner_item,
                  dashboardNames);
              adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
              dashboardSpinner.setAdapter(adapter);
              if (dashboards != null && dashboards.size() > 0)
              {
                createDashboard(0);
              }
            }
          });
        }
        catch (Exception ex)
        {
          ToastUtils.showLong(DashboardActivity.this, ex.toString());
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        ToastUtils.showLong(DashboardActivity.this, ex.toString());
      }
    });
  }
  
  protected void createDashboard(int index)
  {
    dashboardIndex = index;
    unwatchAll();
    grid.removeAllViews();
    widgets.clear();

    BList dashboard = (BList)dashboards.get(index);
    if (dashboard == null) return;

    createWidgets((BList)dashboard.get("widgets"));
    layoutWidgets((BList)dashboard.get("layouts"));
    Object value = dashboard.get("polling-interval");
    if (value instanceof Number)
    {
      int pollingInterval = ((Number)value).intValue();
      getMonitor().setPollingInterval(pollingInterval);
    }
  }

  protected void createWidgets(BList widgetDefinitions)
  {
    if (widgetDefinitions == null) return;

    for (int i = 0; i < widgetDefinitions.size(); i++)
    {
      String name = widgetDefinitions.getName(i);
      BList widgetDefinition = (BList)widgetDefinitions.get(i);
      View widget = createWidget(name, widgetDefinition);
      if (widget != null)
      {
        widgets.put(name, widget);
        grid.addView(widget, new BoxGridLayout.LayoutParams(0, 0, 1, 1));
      }
    }
  }

  protected View createWidget(String name, BList properties)
  {
    DashboardWidget widget = null;
    String type = (String)properties.get(WidgetType.TYPE);

    try
    {
      widget = DashboardWidgetFactory.getInstance().createWidget(type, this);
      if (widget != null)
      {
        widget.init(this, name, properties);
      }
    }
    catch (Exception ex)
    {
      // unsupported widget or init fail: ignore
    }
    return (View)widget;
  }

  protected void layoutWidgets(BList layouts)
  {
    if (layouts == null) return;

    BList layout = (BList)layouts.get(0);

    BList dimensions = (BList)layout.get("dimensions");
    int gridWidth = Utils.toNumber(dimensions.get(0)).intValue();
    int gridHeight = Utils.toNumber(dimensions.get(1)).intValue();
    grid.setGridSize(gridWidth, gridHeight);
    grid.setStretch(Utils.toBoolean(layout.get("stretch")));
    BList widgetLayouts = (BList)layout.get("widgets");
    for (int i = 0; i < widgetLayouts.size(); i++)
    {
      BList widgetLayout = (BList)widgetLayouts.get(i);
      String name = (String)widgetLayout.get(0);
      int x = Utils.toNumber(widgetLayout.get(1)).intValue();
      int y = Utils.toNumber(widgetLayout.get(2)).intValue();
      int xSize = 1;
      int ySize = 1;
      if (widgetLayout.size() > 3)
      {
        xSize = Utils.toNumber(widgetLayout.get(3)).intValue();
        ySize = Utils.toNumber(widgetLayout.get(4)).intValue();
      }
      View widget = widgets.get(name);
      if (widget != null)
      {
        BoxGridLayout.LayoutParams layoutParams =
          (BoxGridLayout.LayoutParams)widget.getLayoutParams();
        layoutParams.x = x;
        layoutParams.y = y;
        layoutParams.xSize = xSize;
        layoutParams.ySize = ySize;
      }
    }
    grid.requestLayout();
  }

  protected void unwatchAll()
  {
    if (monitor != null)
    {
      monitor.unwatchAll();
      monitor = null;
    }
  }
}
