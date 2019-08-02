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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.CellRendererPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.text.TextUtils;
import org.brain4it.manager.widgets.WidgetType;
import static org.brain4it.server.ServerConstants.DASHBOARDS_FUNCTION_NAME;

/**
 *
 * @author realor
 */
public class DesignerPanel extends ModulePanel
{
  private int gridMargin = 4;
  private int gridWidth;
  private int gridHeight;
  private boolean stretch;
  private int pollingInterval;
  private final HashMap<String, WidgetView> widgetsByName =
    new HashMap<String, WidgetView>();
  private final ArrayList<WidgetView> widgetsByPosition =
    new ArrayList<WidgetView>();
  private DashboardWidgetEditor widgetEditor;

  /**
   * Creates new form Editor
   */
  public DesignerPanel(ManagerApp managerApp, Module module)
  {
    super(managerApp, module);
    initComponents();
    initDesigner();
  }

  @Override
  public String getPanelType()
  {
    return managerApp.getLocalizedMessage("Designer");
  }

  @Override
  public String getPanelName()
  {
    String panelName = super.getPanelName();
    String path = getCurrentPath();
    if (path != null)
    {
      if (path.startsWith("/")) panelName += path;
      else panelName += "/" + path;
    }
    return panelName;
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    northPanel = new javax.swing.JPanel();
    pathLabel = new javax.swing.JLabel();
    pathComboBox = new javax.swing.JComboBox<>();
    pathToolBar = new javax.swing.JToolBar();
    loadButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();
    editPanel = new javax.swing.JPanel();
    gridWidthPanel = new javax.swing.JPanel();
    gridWidthLabel = new javax.swing.JLabel();
    gridWidthSpinner = new javax.swing.JSpinner();
    gridHeightPanel = new javax.swing.JPanel();
    gridHeightLabel = new javax.swing.JLabel();
    gridHeightSpinner = new javax.swing.JSpinner();
    pollingIntervalPanel = new javax.swing.JPanel();
    stretchCheckBox = new javax.swing.JCheckBox();
    pollingIntervalLabel = new javax.swing.JLabel();
    pollingIntervalTextField = new javax.swing.JTextField();
    toolBar = new javax.swing.JToolBar();
    deleteButton = new javax.swing.JButton();
    clearAllButton = new javax.swing.JButton();
    widgetsPanel = new WidgetsPanel();

    setLayout(new java.awt.BorderLayout());

    northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    northPanel.setLayout(new java.awt.BorderLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    pathLabel.setText(bundle.getString("Editor.path")); // NOI18N
    northPanel.add(pathLabel, java.awt.BorderLayout.WEST);

    pathComboBox.setEditable(true);
    pathComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>()
    );
    northPanel.add(pathComboBox, java.awt.BorderLayout.CENTER);

    pathToolBar.setFloatable(false);
    pathToolBar.setRollover(true);
    pathToolBar.setBorderPainted(false);

    loadButton.setIcon(IconCache.getIcon("load")
    );
    loadButton.setText(bundle.getString("Editor.load")); // NOI18N
    loadButton.setFocusable(false);
    loadButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    loadButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        loadButtonActionPerformed(evt);
      }
    });
    pathToolBar.add(loadButton);

    saveButton.setIcon(IconCache.getIcon("save"));
    saveButton.setText(bundle.getString("Editor.save")); // NOI18N
    saveButton.setFocusable(false);
    saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    saveButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveButtonActionPerformed(evt);
      }
    });
    pathToolBar.add(saveButton);

    northPanel.add(pathToolBar, java.awt.BorderLayout.LINE_END);

    editPanel.setMinimumSize(new java.awt.Dimension(0, 38));
    editPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    gridWidthPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

    gridWidthLabel.setText(bundle.getString("Designer.gridWidth")); // NOI18N
    gridWidthPanel.add(gridWidthLabel);

    gridWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(4, 1, 20, 1));
    gridWidthSpinner.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        gridWidthSpinnerStateChanged(evt);
      }
    });
    gridWidthPanel.add(gridWidthSpinner);

    editPanel.add(gridWidthPanel);

    gridHeightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

    gridHeightLabel.setText(bundle.getString("Designer.gridHeight")); // NOI18N
    gridHeightPanel.add(gridHeightLabel);

    gridHeightSpinner.setModel(new javax.swing.SpinnerNumberModel(4, 1, 20, 1));
    gridHeightSpinner.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        gridHeightSpinnerStateChanged(evt);
      }
    });
    gridHeightPanel.add(gridHeightSpinner);

    editPanel.add(gridHeightPanel);

    pollingIntervalPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

    stretchCheckBox.setText(bundle.getString("Designer.stretch")); // NOI18N
    stretchCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    stretchCheckBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        stretchCheckBoxActionPerformed(evt);
      }
    });
    pollingIntervalPanel.add(stretchCheckBox);

    pollingIntervalLabel.setText("Polling (ms):");
    pollingIntervalPanel.add(pollingIntervalLabel);

    pollingIntervalTextField.setText("0");
    pollingIntervalTextField.setMinimumSize(new java.awt.Dimension(60, 26));
    pollingIntervalTextField.setPreferredSize(new java.awt.Dimension(60, 26));
    pollingIntervalPanel.add(pollingIntervalTextField);

    editPanel.add(pollingIntervalPanel);

    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    toolBar.setBorderPainted(false);

    deleteButton.setIcon(IconCache.getIcon("delete"));
    deleteButton.setText(bundle.getString("Designer.delete")); // NOI18N
    deleteButton.setFocusable(false);
    deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    deleteButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        deleteButtonActionPerformed(evt);
      }
    });
    toolBar.add(deleteButton);

    clearAllButton.setIcon(IconCache.getIcon("clear"));
    clearAllButton.setText(bundle.getString("Designer.clearAll")); // NOI18N
    clearAllButton.setFocusable(false);
    clearAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    clearAllButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        clearAllButtonActionPerformed(evt);
      }
    });
    toolBar.add(clearAllButton);

    editPanel.add(toolBar);

    northPanel.add(editPanel, java.awt.BorderLayout.SOUTH);

    add(northPanel, java.awt.BorderLayout.PAGE_START);

    widgetsPanel.setBackground(new java.awt.Color(255, 255, 255));
    widgetsPanel.setLayout(null);
    add(widgetsPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
  {//GEN-HEADEREND:event_saveButtonActionPerformed
    saveData();
  }//GEN-LAST:event_saveButtonActionPerformed

  private void loadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadButtonActionPerformed
  {//GEN-HEADEREND:event_loadButtonActionPerformed
    loadData();
  }//GEN-LAST:event_loadButtonActionPerformed

  private void gridWidthSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_gridWidthSpinnerStateChanged
  {//GEN-HEADEREND:event_gridWidthSpinnerStateChanged
    gridWidth = ((Number)gridWidthSpinner.getValue()).intValue();
    setModified(true);
    repaint();
  }//GEN-LAST:event_gridWidthSpinnerStateChanged

  private void gridHeightSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_gridHeightSpinnerStateChanged
  {//GEN-HEADEREND:event_gridHeightSpinnerStateChanged
    gridHeight = ((Number)gridHeightSpinner.getValue()).intValue();
    setModified(true);
    repaint();
  }//GEN-LAST:event_gridHeightSpinnerStateChanged

  private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearAllButtonActionPerformed
  {//GEN-HEADEREND:event_clearAllButtonActionPerformed
    widgetsByName.clear();
    widgetsByPosition.clear();
    setModified(true);
    ((WidgetsPanel)widgetsPanel).setSelectedWidgetView(null);
    repaint();
  }//GEN-LAST:event_clearAllButtonActionPerformed

  private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
  {//GEN-HEADEREND:event_deleteButtonActionPerformed
    ((WidgetsPanel)widgetsPanel).deleteSelectedWidget();
  }//GEN-LAST:event_deleteButtonActionPerformed

  private void stretchCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stretchCheckBoxActionPerformed
  {//GEN-HEADEREND:event_stretchCheckBoxActionPerformed
    stretch = stretchCheckBox.isSelected();
    setModified(true);
    repaint();
  }//GEN-LAST:event_stretchCheckBoxActionPerformed

  public void loadPath(String path)
  {
    pathComboBox.getEditor().setItem(path);
    loadData();
  }

  public String getCurrentPath()
  {
    String path = (String)pathComboBox.getEditor().getItem();
    if (path == null) return null;
    path = path.trim();
    if (path.length() == 0) return null;
    if (path.startsWith("/") && path.length() > 1) return path.substring(1);
    return path;
  }

  @Override
  public void setModified(boolean modified)
  {
    saveButton.setEnabled(modified);
    super.setModified(modified);
  }

  protected void loadData()
  {
    final String path = getCurrentPath();
    if (path == null) return;

    if (isModified())
    {
      int result = JOptionPane.showConfirmDialog(managerApp,
        managerApp.getLocalizedMessage("Editor.discardChanges"), null,
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (result == JOptionPane.NO_OPTION) return;
    }

    RestClient restClient = getRestClient();
    restClient.get(module.getName(), path, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, final String resultString)
      {
        onReadSuccess(path, resultString);
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError("Designer", ex);
      }
    });
  }

  protected void onReadSuccess(final String path, final String data)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          loadDashboard(data);
          addPathToComboBox(path);
          managerApp.updateTab(DesignerPanel.this);
          setModified(false);
        }
        catch (Exception ex)
        {
          managerApp.showError("Designer", ex);
        }
      }
    });
  }

  protected void saveData()
  {
    final String path = getCurrentPath();
    if (path == null)
    {
      getManagerApp().showError("Designer", "Enter path before saving.");
      return;
    }

    String data = saveDashboard();

    RestClient restClient = getRestClient();
    restClient.put(module.getName(), path, data, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        onWriteSuccess(path);
        createDashboardsFunction(path);
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError("Editor.save", ex);
      }
    });
  }

  protected void onWriteSuccess(final String path)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        managerApp.updateTab(DesignerPanel.this);
        addPathToComboBox(path);
        setModified(false);
      }
    });
  }

  protected void createDashboardsFunction(String path)
  {
    module.saveData(DASHBOARDS_FUNCTION_NAME,
      "(function (ctx data) (list " + path + "))", null);
  }

  protected void addPathToComboBox(String path)
  {
    DefaultComboBoxModel<String> model =
     (DefaultComboBoxModel<String>)pathComboBox.getModel();
    if (model.getIndexOf(path) == -1)
    {
      model.addElement(path);
    }
    pathComboBox.getEditor().setItem(path);
    model.setSelectedItem(path);
  }

  protected void loadDashboard(String data) throws Exception
  {
    BList dashboard = (BList)Parser.fromString(data);
    BList widgets = (BList)dashboard.get("widgets");

    DashboardWidgetFactory factory = DashboardWidgetFactory.getInstance();
    widgetsByName.clear();
    widgetsByPosition.clear();
    for (int i = 0; i < widgets.size(); i++)
    {
      String name = widgets.getName(i);
      WidgetView widgetView = new WidgetView();
      widgetView.properties = (BList)widgets.get(i);
      widgetsByName.put(name, widgetView);

      String type = (String)widgetView.properties.get(WidgetType.TYPE);
      DashboardWidget widget = factory.createWidget(type);
      if (widget != null)
      {
        widgetView.widget = widget;
        try
        {
          widget.init(null, name, widgetView.properties);
        }
        catch (Exception ex)
        {
          // ignore
        }
      }
    }
    BList layouts = (BList)dashboard.get("layouts");
    BList layout = (BList)layouts.get(0);
    BList dimensions = (BList)layout.get("dimensions");
    stretch = Utils.toBoolean(layout.get("stretch"));
    stretchCheckBox.setSelected(stretch);
    gridWidth = ((Number)dimensions.get(0)).intValue();
    gridHeight = ((Number)dimensions.get(1)).intValue();
    gridWidthSpinner.setValue(gridWidth);
    gridHeightSpinner.setValue(gridHeight);
    widgets = (BList)layout.get("widgets");
    for (int i = 0; i < widgets.size(); i++)
    {
      BList widgetLayout = (BList)widgets.get(i);
      String name = (String)widgetLayout.get(0);
      WidgetView widgetView = widgetsByName.get(name);
      if (widgetView != null)
      {
        widgetsByPosition.add(widgetView);
        widgetView.name = name;
        widgetView.x = ((Number)widgetLayout.get(1)).intValue();
        widgetView.y = ((Number)widgetLayout.get(2)).intValue();
        widgetView.xSize = 1;
        widgetView.ySize = 1;
        if (widgetLayout.size() >= 5)
        {
          widgetView.xSize = ((Number)widgetLayout.get(3)).intValue();
          widgetView.ySize = ((Number)widgetLayout.get(4)).intValue();
        }
      }
    }
    Object value = dashboard.get("polling-interval");
    if (value instanceof Number)
    {
      pollingInterval = ((Number)value).intValue();
      if (pollingInterval < 0) pollingInterval = 0;
    }
    pollingIntervalTextField.setText(String.valueOf(pollingInterval));
    managerApp.getDesignerAuxiliaryPanel().
      getDashboardWidgetEditor().setWidgetView(null);
    repaint();
  }

  protected String saveDashboard()
  {
    Collections.sort(widgetsByPosition);
    BList dashboard = new BList();
    BList widgets = new BList();
    dashboard.put("widgets", widgets);
    for (WidgetView widgetView : widgetsByName.values())
    {
      widgets.put(widgetView.name, widgetView.properties);
    }
    BList layouts = new BList();
    dashboard.put("layouts", layouts);
    BList layout = new BList();
    layouts.put("mobile-vertical", layout);
    BList dimensions = new BList(2);
    dimensions.add(gridWidth);
    dimensions.add(gridHeight);
    layout.put("dimensions", dimensions);
    layout.put("stretch", stretch);
    BList widgetLayouts = new BList();
    layout.put("widgets", widgetLayouts);
    for (WidgetView widgetView : widgetsByPosition)
    {
      BList entry = new BList(4);
      entry.add(widgetView.name);
      entry.add(widgetView.x);
      entry.add(widgetView.y);
      entry.add(widgetView.xSize);
      entry.add(widgetView.ySize);
      widgetLayouts.add(entry);
    }
    try
    {
      pollingInterval = Integer.parseInt(pollingIntervalTextField.getText());
      dashboard.put("polling-interval", pollingInterval);
    }
    catch (NumberFormatException ex)
    {
    }
    return Printer.toString(dashboard);
  }

  public class WidgetsPanel extends JPanel
    implements MouseListener, MouseMotionListener, FocusListener, KeyListener
  {
    WidgetView selectedWidgetView;
    Rectangle selectionBounds;
    CellRendererPane rendererPane;
    Point moveVector;
    boolean onLeft;
    boolean onRight;
    boolean onTop;
    boolean onBottom;
    boolean drag;

    public WidgetsPanel()
    {
      initComponents();
    }

    @Override
    public void paintComponent(Graphics g)
    {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());

      Graphics2D g2 = (Graphics2D)g;
      int panelWidth = getWidth() - 2 * gridMargin;
      int panelHeight = getHeight() - 2 * gridMargin;
      int cellWidth = panelWidth / gridWidth;
      int cellHeight = panelHeight / gridHeight;
      int xOffset = gridMargin;
      int yOffset = gridMargin;
      if (!stretch)
      {
        int cellSize = Math.min(cellWidth, cellHeight);
        if (cellWidth > cellSize)
        {
          xOffset += (panelWidth - cellSize * gridWidth) / 2;
          cellWidth = cellSize;
        }
        else if (cellHeight > cellSize)
        {
          yOffset += (panelHeight - cellSize * gridHeight) / 2;
          cellHeight = cellSize;
        }
      }
      g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND, 0, new float[]{2f, 2f}, 0));
      g.setColor(Color.GRAY);
      for (int i = 0; i <= gridWidth; i++)
      {
        g.drawLine(
          xOffset + (i * cellWidth), yOffset,
          xOffset + (i * cellWidth), yOffset + (gridHeight * cellHeight));
      }
      for (int i = 0; i <= gridHeight; i++)
      {
        g.drawLine(
          xOffset, yOffset + (i * cellHeight),
          xOffset + (gridWidth * cellWidth), yOffset + (i * cellHeight));
      }

      if (!drag) selectionBounds = null;
      g.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(1));

      for (WidgetView widgetView : widgetsByPosition)
      {
        int x = xOffset + widgetView.x * cellWidth;
        int y = yOffset + widgetView.y * cellHeight;
        int width = widgetView.xSize * cellWidth;
        int height = widgetView.ySize * cellHeight;
        widgetView.bounds = new Rectangle(x, y, width, height);

        if (widgetView == selectedWidgetView)
        {
          if (!drag)
          {
            selectionBounds = widgetView.bounds;
          }
        }
        else
        {
          JComponent component = (JComponent)widgetView.widget;
          rendererPane.paintComponent(g, component, widgetsPanel,
            x, y, width, height, true);
        }
      }
      if (selectionBounds != null)
      {
        JComponent component = (JComponent)selectedWidgetView.widget;
        rendererPane.paintComponent(g, component, widgetsPanel,
          selectionBounds.x, selectionBounds.y,
          selectionBounds.width, selectionBounds.height, true);

        g.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        g2.draw(selectionBounds);
      }
    }

    WidgetView getSelectedWidgetView()
    {
      return selectedWidgetView;
    }

    void setSelectedWidgetView(WidgetView widgetView)
    {
      selectedWidgetView = widgetView;
      widgetEditor.setWidgetView(widgetView);
    }

    private Point getGridCoordinates(int x, int y, boolean round)
    {
      int panelWidth = getWidth() - 2 * gridMargin;
      int panelHeight = getHeight() - 2 * gridMargin;
      int cellWidth = panelWidth / gridWidth;
      int cellHeight = panelHeight / gridHeight;
      int xOffset = gridMargin;
      int yOffset = gridMargin;
      if (!stretch)
      {
        int cellSize = Math.min(cellWidth, cellHeight);
        xOffset = cellWidth > cellSize ?
          gridMargin + (panelWidth - cellSize * gridWidth) / 2 : gridMargin;
        yOffset = cellHeight > cellSize ?
          gridMargin + (panelHeight - cellSize * gridHeight) / 2 : gridMargin;
        cellWidth = cellSize;
        cellHeight = cellSize;
      }
      float xx = (float)(x - xOffset) / (float)cellWidth;
      float yy = (float)(y - yOffset) / (float)cellHeight;

      int dx = (int)(round ? Math.round(xx) : Math.floor(xx));
      int dy = (int)(round ? Math.round(yy) : Math.floor(yy));

      return new Point(dx, dy);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      requestFocus();
      if (e.getButton() == MouseEvent.BUTTON1)
      {
        if (e.getClickCount() == 1)
        {
          Palette palette = managerApp.getDesignerAuxiliaryPanel().getPalette();
          String widgetType = palette.getSelectedWidgetType();
          if (widgetType == null)
          {
            if (selectionBounds != null &&
              (moveVector != null || onTop || onBottom || onLeft || onRight))
            {
              drag = true;
            }
            else
            {
              int x = e.getX();
              int y = e.getY();
              WidgetView widgetView = null;
              Iterator<WidgetView> iter = widgetsByName.values().iterator();
              while (iter.hasNext() && widgetView == null)
              {
                widgetView = iter.next();
                if (widgetView.bounds.contains(x, y))
                {
                  selectionBounds = widgetView.bounds;
                  moveVector = new Point(
                    x - selectionBounds.x, y - selectionBounds.y);
                  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  drag = true;
                }
                else widgetView = null;
              }
              setSelectedWidgetView(widgetView);
              repaint();
            }
          }
          else
          {
            addWidget(widgetType, e.getPoint());
            setCursor(Cursor.getDefaultCursor());
            palette.clearSelectedWidgetType();
          }
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      if (drag && selectionBounds != null)
      {
        int x1 = selectionBounds.x;
        int y1 = selectionBounds.y;
        Point p1 = getGridCoordinates(x1, y1, true);

        int x2 = selectionBounds.x + selectionBounds.width;
        int y2 = selectionBounds.y + selectionBounds.height;
        Point p2 = getGridCoordinates(x2, y2, true);

        int xSize = p2.x - p1.x;
        int ySize = p2.y - p1.y;
        if (selectedWidgetView.x != p1.x ||
            selectedWidgetView.y != p1.y ||
            selectedWidgetView.xSize != xSize ||
            selectedWidgetView.ySize != ySize)
        {
          selectedWidgetView.x = p1.x;
          selectedWidgetView.y = p1.y;
          selectedWidgetView.xSize = xSize;
          selectedWidgetView.ySize = ySize;
          widgetsByPosition.remove(selectedWidgetView);
          widgetsByPosition.add(selectedWidgetView);
          setModified(true);
        }
        setCursor(Cursor.getDefaultCursor());
        repaint();
      }
      drag = false;
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
      Palette palette = managerApp.getDesignerAuxiliaryPanel().getPalette();
      String widgetType = palette.getSelectedWidgetType();
      Point point = e.getPoint();
      if (selectionBounds != null && !drag && widgetType == null)
      {
        onTop = false;
        onBottom = false;
        onLeft = false;
        onRight = false;
        moveVector = null;

        int left = selectionBounds.x;
        int right = selectionBounds.x + selectionBounds.width;
        int top = selectionBounds.y;
        int bottom = selectionBounds.y + selectionBounds.height;
        int margin = 5;

        if (point.x >= left - margin && point.x <= left + margin &&
            point.y >= top - margin && point.y <= top + margin)
        {
          onTop = true;
          onLeft = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        }
        else if (point.x >= right - margin && point.x <= right + margin &&
            point.y >= top - margin && point.y <= top + margin)
        {
          onTop = true;
          onRight = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        }
        else if (point.x >= left - margin && point.x <= left + margin &&
            point.y >= bottom - margin && point.y <= bottom + margin)
        {
          onBottom = true;
          onLeft = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        }
        else if (point.x >= right - margin && point.x <= right + margin &&
            point.y >= bottom - margin && point.y <= bottom + margin)
        {
          onBottom = true;
          onRight = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }
        else if (point.x >= left && point.x <= right &&
                point.y >= top - margin && point.y <= top + margin)
        {
          onTop = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        }
        else if (point.x >= left && point.x <= right &&
                point.y >= bottom - margin && point.y <= bottom + margin)
        {
          onBottom = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        }
        else if (point.y >= top && point.y <= bottom &&
                point.x >= left - margin && point.x <= left + margin)
        {
          onLeft = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        }
        else if (point.y >= top && point.y <= bottom &&
                point.x >= right - margin && point.x <= right + margin)
        {
          onRight = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
        else if (selectionBounds.contains(point))
        {
          moveVector = new Point(
            point.x - selectionBounds.x, point.y - selectionBounds.y);
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else
        {
          setCursor(Cursor.getDefaultCursor());
        }
      }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
      if (selectionBounds != null && drag)
      {
        if (moveVector != null)
        {
          selectionBounds.x = e.getX() - moveVector.x;
          selectionBounds.y = e.getY() - moveVector.y;
        }
        else
        {
          int panelWidth = getWidth() - 2 * gridMargin;
          int panelHeight = getHeight() - 2 * gridMargin;
          int cellWidth = panelWidth / gridWidth;
          int cellHeight = panelHeight / gridHeight;
          int cellSize = Math.min(cellWidth, cellHeight);

          int top = selectionBounds.y;
          int bottom = selectionBounds.y + selectionBounds.height;
          int left = selectionBounds.x;
          int right = selectionBounds.x + selectionBounds.width;

          if (onTop)
          {
            if (e.getY() < bottom - cellSize)
            {
              top = e.getY();
            }
            else
            {
              top = bottom - cellSize;
            }
          }
          else if (onBottom)
          {
            if (e.getY() > top + cellSize)
            {
              bottom = e.getY();
            }
            else
            {
              bottom = top + cellSize;
            }
          }
          if (onLeft)
          {
            if (e.getX() < right - cellSize)
            {
              left = e.getX();
            }
            else
            {
              left = right - cellSize;
            }
          }
          else if (onRight)
          {
            if (e.getX() > left + cellSize)
            {
              right = e.getX();
            }
            else
            {
              right = left + cellSize;
            }
          }
          selectionBounds.setFrameFromDiagonal(left, top, right, bottom);
        }
        repaint();
      }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
      Palette palette = managerApp.getDesignerAuxiliaryPanel().getPalette();
      String widgetType = palette.getSelectedWidgetType();
      if (widgetType == null)
      {
        setCursor(Cursor.getDefaultCursor());
      }
      else
      {
        setCursor(DragSource.DefaultCopyDrop);
      }
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
      if (e.getKeyCode() == KeyEvent.VK_DELETE ||
          e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
      {
        deleteSelectedWidget();
      }
      else if (e.getKeyCode() == KeyEvent.VK_ENTER)
      {
        //showParametersDialog();
      }
    }

    @Override
    public void focusGained(FocusEvent e)
    {
      managerApp.setAuxiliaryPanel(managerApp.getDesignerAuxiliaryPanel());
      addKeyListener(this);
    }

    @Override
    public void focusLost(FocusEvent e)
    {
      removeKeyListener(this);
    }

    private void deleteSelectedWidget()
    {
      if (selectedWidgetView != null)
      {
        widgetsByName.remove(selectedWidgetView.name);
        widgetsByPosition.remove(selectedWidgetView);
        drag = false;
        moveVector = null;
        onLeft = false;
        onRight = false;
        onTop = false;
        onBottom = false;
        setCursor(Cursor.getDefaultCursor());
        setModified(true);
        setSelectedWidgetView(null);
        repaint();
      }
    }

    private void initComponents()
    {
      rendererPane = new CellRendererPane();
      add(rendererPane);
      addMouseListener(this);
      addMouseMotionListener(this);
      addFocusListener(this);
      setFocusable(true);
    }
  }

  @Override
  public void close()
  {
    managerApp.setAuxiliaryPanel(null);
  }

  public void addWidget(String type, Point point)
  {
    try
    {
      Point gridPoint =
        ((WidgetsPanel)widgetsPanel).getGridCoordinates(point.x, point.y, false);
      DashboardWidget widget =
        DashboardWidgetFactory.getInstance().createWidget(type);
      String name = createWidgetName(type);
      BList properties = new BList();
      properties.put(WidgetType.TYPE, type);
      WidgetType widgetType = WidgetType.getType(type);
      if (widgetType != null)
      {
        widgetType.init(properties);
      }
      try
      {
        widget.init(null, name, properties);
      }
      catch (Exception ex)
      {
      }
      WidgetView widgetView = new WidgetView();
      widgetView.name = name;
      widgetView.x = gridPoint.x;
      widgetView.y = gridPoint.y;
      widgetView.xSize = 1;
      widgetView.ySize = 1;
      widgetView.widget = widget;
      widgetView.properties = properties;
      widgetsByName.put(widgetView.name, widgetView);
      widgetsByPosition.add(widgetView);
      setModified(true);
      ((WidgetsPanel)widgetsPanel).setSelectedWidgetView(widgetView);
      repaint();
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  public class WidgetView implements Comparable<WidgetView>
  {
    String name;
    int x;
    int y;
    int xSize;
    int ySize;
    BList properties;
    Rectangle bounds;
    DashboardWidget widget;

    public String getName()
    {
      return name;
    }

    public WidgetType getWidgetType()
    {
      String type = (String)properties.get(WidgetType.TYPE);
      if (type == null) return null;
      return WidgetType.getType(type);
    }

    public BList getProperties()
    {
      return properties;
    }

    public void setProperties(BList properties) throws Exception
    {
      widget.init(null, name, properties);
      this.properties = properties;
      setModified(true);
      widgetsPanel.repaint();
    }

    public DashboardWidget getWidget()
    {
      return widget;
    }

    public DesignerPanel getDesignerPanel()
    {
      return DesignerPanel.this;
    }

    @Override
    public int compareTo(WidgetView other)
    {
      if (y < other.y) return -1;
      else if (y > other.y) return 1;
      else
      {
        if (x < other.x) return -1;
        else if (x > other.x) return 1;
        else return 0;
      }
    }
  }

  private void initDesigner()
  {
    widgetEditor =
      managerApp.getDesignerAuxiliaryPanel().getDashboardWidgetEditor();
    gridWidth = (Integer)this.gridWidthSpinner.getValue();
    gridHeight = (Integer)this.gridWidthSpinner.getValue();

    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentShown(ComponentEvent e)
      {
        managerApp.setAuxiliaryPanel(managerApp.getDesignerAuxiliaryPanel());
        WidgetView widgetView =
          ((WidgetsPanel)widgetsPanel).getSelectedWidgetView();
        widgetEditor.setWidgetView(widgetView);
      }
    });

    pollingIntervalTextField.getDocument().addDocumentListener(
      new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        process();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        process();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        process();
      }

      private void process()
      {
        saveButton.setEnabled(true);
      }
    });

    JTextField textField =
      (JTextField)pathComboBox.getEditor().getEditorComponent();
    TextUtils.updateInputMap(textField);
    textField.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        process();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        process();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        process();
      }

      private void process()
      {
        saveButton.setEnabled(true);
      }
    });

    DropTarget dropTarget = new DropTarget(widgetsPanel, new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetDropEvent event)
      {
        Palette palette = managerApp.getDesignerAuxiliaryPanel().getPalette();
        String widgetType = palette.getSelectedWidgetType();
        Point location = event.getLocation();
        addWidget(widgetType, location);
        event.dropComplete(true);
      }
    });
    dropTarget.setActive(true);
  }

  private String createWidgetName(String type)
  {
    int max = 0;
    for (WidgetView widgetView : widgetsByPosition)
    {
      if (widgetView.name.startsWith(type + "_"))
      {
        try
        {
          int num = Integer.parseInt(
            widgetView.name.substring(type.length() + 1));
          if (num > max) max = num;
        }
        catch (NumberFormatException ex)
        {
          // not a number, ignore it
        }
      }
    }
    return type + "_" + (max + 1);
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearAllButton;
  private javax.swing.JButton deleteButton;
  private javax.swing.JPanel editPanel;
  private javax.swing.JLabel gridHeightLabel;
  private javax.swing.JPanel gridHeightPanel;
  private javax.swing.JSpinner gridHeightSpinner;
  private javax.swing.JLabel gridWidthLabel;
  private javax.swing.JPanel gridWidthPanel;
  private javax.swing.JSpinner gridWidthSpinner;
  private javax.swing.JButton loadButton;
  private javax.swing.JPanel northPanel;
  private javax.swing.JComboBox<String> pathComboBox;
  private javax.swing.JLabel pathLabel;
  private javax.swing.JToolBar pathToolBar;
  private javax.swing.JLabel pollingIntervalLabel;
  private javax.swing.JPanel pollingIntervalPanel;
  private javax.swing.JTextField pollingIntervalTextField;
  private javax.swing.JButton saveButton;
  private javax.swing.JCheckBox stretchCheckBox;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JPanel widgetsPanel;
  // End of variables declaration//GEN-END:variables
}
