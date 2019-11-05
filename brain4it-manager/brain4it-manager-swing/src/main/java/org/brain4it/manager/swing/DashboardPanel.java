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

package org.brain4it.manager.swing;

import org.brain4it.manager.swing.layout.BoxGridLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.brain4it.client.Invoker;
import org.brain4it.client.Monitor;
import org.brain4it.client.RestClient;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.widgets.WidgetType;
import static org.brain4it.server.ServerConstants.DASHBOARDS_FUNCTION_NAME;

/**
 *
 * @author realor
 */
public class DashboardPanel extends ModulePanel implements Monitor.Listener
{
  private Monitor dashboardsMonitor;
  private Monitor monitor;
  private Invoker invoker;
  private Timer timer;
  private BoxGridLayout boxGridLayout;
  private final HashMap<String, Component> widgets =
    new HashMap<String, Component>();
  private BList dashboards;
  private int dashboardIndex;
  private final String sessionId;

  public DashboardPanel(ManagerApp managerApp, Module module)
  {
    super(managerApp, module);
    initComponents();
    initDashboard();
    sessionId = UUID.randomUUID().toString();

    createDashboardsMonitor();
  }

  @Override
  public String getPanelType()
  {
    return managerApp.getLocalizedMessage("Designer");
  }

  public String getSessionId()
  {
    return sessionId;
  }

  @Override
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
      invoker = new Invoker(getRestClient(), module.getName());
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

  @Override
  public void close()
  {
    unwatchAll();
    if (timer != null)
    {
      timer.cancel();
      timer = null;
    }
    dashboardsMonitor.unwatchAll();
    super.close();
  }

  // DASHBOARDS_FUNCTION_NAME listener
  @Override
  public void onChange(String functionName, final Object value,
    long serverTime)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        loadDashboards(value);
      }
    });
  }

  protected final void createDashboardsMonitor()
  {
    Server server = module.getServer();
    dashboardsMonitor = new Monitor(server.getUrl(), module.getName());
    dashboardsMonitor.setAccessKey(module.getAccessKey());
    dashboardsMonitor.setSessionId(sessionId);
    dashboardsMonitor.watch(DASHBOARDS_FUNCTION_NAME, this);
  }

  protected void loadDashboards(final Object value)
  {
    dashboards = null;
    dashboardComboBox.setModel(new DefaultComboBoxModel<String>());

    DefaultComboBoxModel<String> model =
      (DefaultComboBoxModel<String>) dashboardComboBox.getModel();
    if (value instanceof BList)
    {
      dashboards = (BList)value;
      for (int i = 0; i < dashboards.size(); i++)
      {
        String dashboardName = dashboards.getName(i);
        if (dashboardName == null) dashboardName = "dashboard-" + i;
        model.addElement(dashboardName);
      }
    }
    dashboardComboBox.setModel(model);
    if (dashboards != null && dashboards.size() > 0)
    {
      createDashboard(0);
    }
    else
    {
      // module has no dashboards
      unwatchAll();
      dashboardIndex = 0;
      widgets.clear();
      widgetsPanel.removeAll();

      // show no dashboards message
      boxGridLayout.setGridSize(1, 1);
      boxGridLayout.setStretch(true);
      widgetsPanel.add(new JLabel(
        managerApp.getLocalizedMessage("NoDashboards"),
        JLabel.CENTER), new BoxGridLayout.Constraints(0, 0, 1, 1));
      widgetsPanel.repaint();
    }
  }

  protected void createDashboard(int index)
  {
    try
    {
      unwatchAll();
      dashboardIndex = index;
      widgets.clear();
      widgetsPanel.removeAll();

      BList dashboard = (BList)dashboards.get(index);
      createWidgets((BList)dashboard.get("widgets"));
      layoutWidgets((BList)dashboard.get("layouts"));
      Object value = dashboard.get("polling-interval");
      if (value instanceof Number)
      {
        int pollingInterval = ((Number)value).intValue();
        getMonitor().setPollingInterval(pollingInterval);
      }
      widgetsPanel.repaint();
    }
    catch (Exception ex)
    {
      unwatchAll();
      widgets.clear();
      widgetsPanel.removeAll();
      widgetsPanel.repaint();

      managerApp.showError(
        managerApp.getLocalizedMessage("Dashboard"),
        managerApp.getLocalizedMessage("InvalidDashboardFormat"));
    }
  }

  protected void createWidgets(BList widgetDefinitions)
  {
    if (widgetDefinitions == null) return;

    for (int i = 0; i < widgetDefinitions.size(); i++)
    {
      String name = widgetDefinitions.getName(i);
      BList properties = (BList)widgetDefinitions.get(i);
      Component widget = createWidget(name, properties);
      if (widget != null)
      {
        widgets.put(name, widget);
      }
    }
  }

  protected Component createWidget(String name, BList properties)
  {
    DashboardWidget widget = null;
    String type = (String)properties.get(WidgetType.TYPE);
    try
    {
      widget = DashboardWidgetFactory.getInstance().createWidget(type);
      if (widget != null)
      {
        widget.init(this, name, properties);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    return (Component)widget;
  }

  protected void layoutWidgets(BList layouts)
  {
    if (layouts == null) return;

    BList layout = (BList)layouts.get(0);

    BList dimensions = (BList)layout.get("dimensions");
    int gridWidth = Utils.toNumber(dimensions.get(0)).intValue();
    int gridHeight = Utils.toNumber(dimensions.get(1)).intValue();
    boxGridLayout.setGridSize(gridWidth, gridHeight);
    boxGridLayout.setStretch(Utils.toBoolean(layout.get("stretch")));
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
      Component widget = widgets.get(name);
      if (widget != null)
      {
        widgetsPanel.add(widget,
         new BoxGridLayout.Constraints(x, y, xSize, ySize));
      }
    }
    widgetsPanel.doLayout();
    widgetsPanel.revalidate();
  }

  protected void unwatchAll()
  {
    if (monitor != null)
    {
      monitor.unwatchAll();
      monitor = null;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    toolBar = new javax.swing.JToolBar();
    updateButton = new javax.swing.JButton();
    dashboardComboBox = new javax.swing.JComboBox<>();
    widgetsPanel = new javax.swing.JPanel();

    setLayout(new java.awt.BorderLayout());

    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    updateButton.setIcon(IconCache.getIcon("refresh"));
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    updateButton.setText(bundle.getString("Dashboard.refresh")); // NOI18N
    updateButton.setFocusable(false);
    updateButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        updateButtonActionPerformed(evt);
      }
    });
    toolBar.add(updateButton);

    dashboardComboBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        dashboardComboBoxActionPerformed(evt);
      }
    });
    toolBar.add(dashboardComboBox);

    add(toolBar, java.awt.BorderLayout.PAGE_START);

    widgetsPanel.setBackground(new java.awt.Color(255, 255, 255));
    widgetsPanel.setLayout(null);
    add(widgetsPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void updateButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_updateButtonActionPerformed
  {//GEN-HEADEREND:event_updateButtonActionPerformed
    dashboardsMonitor.unwatchAll();
    dashboardsMonitor.watch(DASHBOARDS_FUNCTION_NAME, this);
  }//GEN-LAST:event_updateButtonActionPerformed

  private void dashboardComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dashboardComboBoxActionPerformed
  {//GEN-HEADEREND:event_dashboardComboBoxActionPerformed
    int index = dashboardComboBox.getSelectedIndex();
    if (index != dashboardIndex)
    {
      createDashboard(index);
    }
  }//GEN-LAST:event_dashboardComboBoxActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox<String> dashboardComboBox;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JButton updateButton;
  private javax.swing.JPanel widgetsPanel;
  // End of variables declaration//GEN-END:variables

  private void initDashboard()
  {
    boxGridLayout = new BoxGridLayout(4, 4);
    widgetsPanel.setLayout(boxGridLayout);
    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentShown(ComponentEvent event)
      {
        managerApp.setAuxiliaryPanel(null);
      }
    });
  }
}
