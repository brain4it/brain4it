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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;
import org.brain4it.manager.swing.actions.AddModuleAction;
import org.brain4it.manager.swing.actions.AddServerAction;
import org.brain4it.manager.swing.actions.DuplicateServerAction;
import org.brain4it.manager.swing.actions.CloseTabsAction;
import org.brain4it.manager.swing.actions.CreateModuleAction;
import org.brain4it.manager.swing.actions.DestroyModuleAction;
import org.brain4it.manager.swing.actions.EditModuleAction;
import org.brain4it.manager.swing.actions.EditServerAction;
import org.brain4it.manager.swing.actions.ListModulesAction;
import org.brain4it.manager.swing.actions.ManagerAction;
import org.brain4it.manager.swing.actions.OpenConsoleAction;
import org.brain4it.manager.swing.actions.OpenEditorAction;
import org.brain4it.manager.swing.actions.RemoveNodeAction;
import org.brain4it.manager.swing.actions.RenameWorkspaceAction;
import org.brain4it.manager.swing.actions.ExitAction;
import org.brain4it.manager.swing.actions.NewWorkspaceAction;
import org.brain4it.manager.swing.actions.OpenWorkspaceAction;
import org.brain4it.manager.swing.actions.SaveWorkspaceAction;
import org.brain4it.manager.swing.actions.AboutAction;
import org.brain4it.manager.swing.actions.DocumentationAction;
import org.brain4it.manager.swing.actions.RefreshNodeAction;
import org.brain4it.manager.swing.actions.OpenDashboardAction;
import org.brain4it.manager.swing.actions.OpenDesignerAction;
import org.brain4it.manager.swing.actions.PreferencesAction;
import org.brain4it.manager.swing.actions.RenameDataAction;
import org.brain4it.manager.swing.actions.DeleteDataAction;
import org.brain4it.manager.swing.actions.ExportModuleAction;
import org.brain4it.manager.swing.actions.ImportModuleAction;
import org.brain4it.io.IOConstants;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.actions.MoveDataAction;
import org.brain4it.manager.swing.actions.CloseSplitAction;
import org.brain4it.manager.swing.actions.SplitHorizontalAction;
import org.brain4it.manager.swing.actions.SplitVerticalAction;
import org.brain4it.manager.swing.actions.ToolBarAction;
import org.brain4it.manager.swing.splitter.Splitter;
import org.brain4it.manager.swing.splitter.SplitterEvent;
import org.brain4it.manager.swing.splitter.SplitterListener;
import org.brain4it.manager.swing.splitter.TabComponent;
import org.brain4it.manager.swing.splitter.TabContainer;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

/**
 *
 * @author realor
 */
public class ManagerApp extends javax.swing.JFrame
{
  public static final String TITLE = "Brain4it Manager";
  public static final String WORKSPACE_EXTENSION = ".bws";

  public static final Color BASE_COLOR = new Color(89, 180, 224);

  private static final Preferences preferences = new Preferences();
  private Workspace workspace;
  private DefaultTreeModel treeModel;
  private JMenuBar menuBar;
  private JPopupMenu popupMenu;
  private NewWorkspaceAction newWorkspaceAction;
  private OpenWorkspaceAction openWorkspaceAction;
  private SaveWorkspaceAction saveWorkspaceAction;
  private SaveWorkspaceAction saveWorkspaceAsAction;
  private AboutAction aboutAction;
  private ExitAction exitAction;
  private AddServerAction addServerAction;
  private DuplicateServerAction duplicateServerAction;
  private RenameWorkspaceAction renameWorkspaceAction;
  private EditServerAction editServerAction;
  private AddModuleAction addModuleAction;
  private EditModuleAction editModuleAction;
  private RemoveNodeAction removeNodeAction;
  private RefreshNodeAction refreshNodeAction;
  private CreateModuleAction createModuleAction;
  private DestroyModuleAction destroyModuleAction;
  private ListModulesAction listModulesAction;
  private OpenConsoleAction openConsoleAction;
  private OpenEditorAction openEditorAction;
  private OpenDashboardAction openDashboardAction;
  private OpenDesignerAction openDesignerAction;
  private SplitVerticalAction splitVerticalAction;
  private SplitHorizontalAction splitHorizontalAction;
  private CloseSplitAction closeSplitAction;
  private CloseTabsAction closeTabsAction;
  private CloseTabsAction closeAllTabsAction;
  private DocumentationAction documentationAction;
  private PreferencesAction preferencesAction;
  private RenameDataAction renameDataAction;
  private DeleteDataAction deleteDataAction;
  private MoveDataAction moveDataAction;
  private ExportModuleAction exportModuleAction;
  private ImportModuleAction importModuleAction;
  private FindDialog findDialog;
  private ManagerAction[] contextActions;
  private ResourceBundle resourceBundle;
  private File workspaceFile;
  private boolean workspaceModified = false;
  private static final int[] LOGO_SIZES = new int[]{16, 32, 48, 64, 72, 128};
  private int auxiliaryPanelWidth = 200;


  public ManagerApp()
  {
    Locale locale = new Locale(preferences.getLanguage());
    Locale.setDefault(locale);
    initComponents();
    initApp();
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    designerAuxiliaryPanel = new org.brain4it.manager.swing.DesignerAuxiliaryPanel();
    toolBar = new javax.swing.JToolBar();
    firstSplitPane = new javax.swing.JSplitPane();
    explorerScrollPane = new javax.swing.JScrollPane();
    explorer = new org.brain4it.manager.swing.Explorer();
    secondSplitPane = new javax.swing.JSplitPane();
    splitter = new org.brain4it.manager.swing.splitter.Splitter();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle(TITLE);
    setExtendedState(preferences.getWindowState());
    setName("ManagerApp"); // NOI18N
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        ManagerApp.this.windowClosing(evt);
      }
    });

    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

    explorerScrollPane.setName("Navigator"); // NOI18N

    javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
    explorer.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
    explorer.setRowHeight(20);
    explorerScrollPane.setViewportView(explorer);

    firstSplitPane.setLeftComponent(explorerScrollPane);

    secondSplitPane.setDividerLocation(1000);
    secondSplitPane.setDividerSize(0);
    secondSplitPane.setResizeWeight(1.0);
    secondSplitPane.setLeftComponent(splitter);

    firstSplitPane.setRightComponent(secondSplitPane);

    getContentPane().add(firstSplitPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void windowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_windowClosing
  {//GEN-HEADEREND:event_windowClosing
    exit();
  }//GEN-LAST:event_windowClosing

  public Workspace getWorkspace()
  {
    return workspace;
  }

  public Explorer getExplorer()
  {
    return (Explorer)explorer;
  }

  public DesignerAuxiliaryPanel getDesignerAuxiliaryPanel()
  {
    return designerAuxiliaryPanel;
  }

  public FindDialog getFindDialog()
  {
    return findDialog;
  }

  public Component getAuxiliaryPanel()
  {
    return secondSplitPane.getRightComponent();
  }

  public void setAuxiliaryPanel(Component panel)
  {
    Component prevPanel = secondSplitPane.getRightComponent();
    if (panel != null) // show/change auxiliary panel
    {
      if (panel != prevPanel)
      {
        if (prevPanel == null)
        {
          secondSplitPane.setDividerSize(firstSplitPane.getDividerSize());
          int location = secondSplitPane.getWidth() -
            auxiliaryPanelWidth - firstSplitPane.getDividerSize();
          secondSplitPane.setDividerLocation(location);
        }
        secondSplitPane.setRightComponent(panel);
      }
    }
    else // hide auxiliary panel
    {
      if (prevPanel != null)
      {
        auxiliaryPanelWidth = secondSplitPane.getWidth() -
          secondSplitPane.getDividerLocation() - firstSplitPane.getDividerSize();
        secondSplitPane.setRightComponent(null);
        secondSplitPane.setDividerSize(0);
        secondSplitPane.setDividerLocation(Integer.MAX_VALUE);
      }
    }
  }

  public static Preferences getPreferences()
  {
    return preferences;
  }

  public boolean isWorkspaceModified()
  {
    return workspaceModified;
  }

  public void setWorkspaceModified(boolean workspaceModified)
  {
    this.workspaceModified = workspaceModified;
    saveWorkspaceAction.setEnabled(workspaceModified);
  }

  public Splitter getSplitter()
  {
    return splitter;
  }

  public DefaultMutableTreeNode getSelectedNode()
  {
    TreePath path = explorer.getSelectionPath();
    if (path == null) return null;
    return (DefaultMutableTreeNode)path.getLastPathComponent();
  }

  public void openConsole(Module module)
  {
    ConsolePanel console = new ConsolePanel(this, module);
    ImageIcon icon = IconCache.getIcon("console");
    splitter.addComponent(console, console.getPanelName(), icon);
    module.findFunctions(null);
    module.findGlobals(null);
  }

  public void openDashboard(Module module)
  {
    DashboardPanel dashboard = new DashboardPanel(this, module);
    ImageIcon icon = IconCache.getIcon("dashboard");
    splitter.addComponent(dashboard, dashboard.getPanelName(), icon);
  }

  public void openEditor(Module module, String path)
  {
    EditorPanel editorPanel = null;
    if (path == null || path.trim().length() == 0) return;

    List<Component> components = splitter.getComponentList();
    int tabCount = components.size();
    int i = 0;
    while (i < tabCount && editorPanel == null)
    {
      Component component = components.get(i);
      if (component instanceof ModulePanel)
      {
        ModulePanel modulePanel = (ModulePanel)component;
        if (modulePanel.getModule() == module)
        {
          if (modulePanel instanceof EditorPanel)
          {
            if (path.equals(((EditorPanel)modulePanel).getCurrentPath()))
            {
              editorPanel = (EditorPanel)modulePanel;
            }
          }
        }
      }
      i++;
    }
    if (editorPanel == null)
    {
      editorPanel = new EditorPanel(this, module);

      ImageIcon icon = IconCache.getIcon("editor");
      splitter.addComponent(editorPanel, editorPanel.getPanelName(), icon);

      module.findFunctions(null);
      module.findGlobals(null);

      editorPanel.loadPath(path);
    }
    else
    {
      TabContainer tabContainer = (TabContainer)editorPanel.getParent();
      tabContainer.setSelectedComponent(editorPanel);
    }
  }

  public void openDesigner(Module module, String path)
  {
    DesignerPanel designerPanel = null;
    if (path != null && path.length() > 0)
    {
      List<Component> components = splitter.getComponentList();
      int tabCount = components.size();
      int i = 0;
      while (i < tabCount && designerPanel == null)
      {
        Component component = components.get(i);
        if (component instanceof ModulePanel)
        {
          ModulePanel modulePanel = (ModulePanel)component;
          if (modulePanel.getModule() == module)
          {
            if (modulePanel instanceof DesignerPanel)
            {
              if (path.equals(((DesignerPanel)modulePanel).getCurrentPath()))
              {
                designerPanel = (DesignerPanel)modulePanel;
              }
            }
          }
        }
        i++;
      }
    }
    if (designerPanel == null)
    {
      designerPanel = new DesignerPanel(this, module);

      ImageIcon icon = IconCache.getIcon("designer");
      splitter.addComponent(designerPanel, designerPanel.getPanelName(), icon);

      module.findFunctions(null);

      if (path.length() > 0)
      {
        designerPanel.loadPath(path);
      }
    }
    else
    {
      TabContainer tabContainer = (TabContainer)designerPanel.getParent();
      tabContainer.setSelectedComponent(designerPanel);
    }
  }

  public File getWorkspaceFile()
  {
    return workspaceFile;
  }

  public void setWorkspaceFile(File file)
  {
    this.workspaceFile = file;
    if (file == null)
    {
      setTitle(TITLE);
    }
    else
    {
      setTitle(file.getName() + " - " + TITLE);
    }
  }

  public void updateTabs()
  {
    List<Component> componentList = splitter.getComponentList();
    for (Component component : componentList)
    {
      if (component instanceof ModulePanel)
      {
        ModulePanel modulePanel = (ModulePanel)component;
        updateTab(modulePanel);
      }
    }
  }

  public void updateTab(ModulePanel modulePanel)
  {
    TabComponent tabComponent = splitter.getTabComponent(modulePanel);
    tabComponent.setTitle(modulePanel.getPanelName());
    tabComponent.setModified(modulePanel.isModified());
  }

  public boolean closeTabs()
  {
    return closeTabs(null);
  }

  public boolean closeTabs(DefaultMutableTreeNode treeNode)
  {
    boolean allClosed = true;
    if (treeNode == null)
    {
      treeNode = (DefaultMutableTreeNode)treeModel.getRoot();
    }

    Object userObject = treeNode.getUserObject();
    if (userObject instanceof Module)
    {
      Module module = (Module)userObject;
      for (Component component : splitter.getComponentList())
      {
        if (component instanceof ModulePanel)
        {
          ModulePanel modulePanel = (ModulePanel)component;
          if (modulePanel.getModule() == module)
          {
            if (!splitter.removeComponent(modulePanel))
            {
              allClosed = false;
            }
          }
        }
      }
    }
    else if (userObject instanceof Server)
    {
      Server server = (Server)userObject;
      for (Component component : splitter.getComponentList())
      {
        if (component instanceof ModulePanel)
        {
          ModulePanel modulePanel = (ModulePanel)component;
          if (modulePanel.getModule().getServer() == server)
          {
            if (!splitter.removeComponent(modulePanel))
            {
              allClosed = false;
            }
          }
        }
      }
    }
    else if (userObject instanceof Workspace)
    {
      for (Component component : splitter.getComponentList())
      {
        if (component instanceof ModulePanel)
        {
          ModulePanel modulePanel = (ModulePanel)component;
          if (!splitter.removeComponent(modulePanel))
          {
            allClosed = false;
          }
        }
      }
    }
    return allClosed;
  }

  public void exit()
  {
    if (closeTabs())
    {
      preferences.setLastWorkspaceFile(workspaceFile);
      preferences.setWindowState(getExtendedState());
      if (getExtendedState() == Frame.NORMAL)
      {
        preferences.setWindowWidth(getWidth());
        preferences.setWindowHeight(getHeight());
      }
      else
      {
        preferences.setWindowWidth(800);
        preferences.setWindowHeight(500);
      }
      preferences.setExplorerWidth(firstSplitPane.getDividerLocation());
      Component auxiliarPanel = getAuxiliaryPanel();
      if (auxiliarPanel != null)
      {
        auxiliaryPanelWidth = auxiliarPanel.getWidth();
      }
      preferences.setAuxiliaryPanelWidth(auxiliaryPanelWidth);
      preferences.save();
      askSaveWorkspace();
      System.exit(0);
    }
  }

  public String getLocalizedMessage(String message)
  {
    String localizedMessage;
    try
    {
      localizedMessage = resourceBundle.getString(message);
    }
    catch (MissingResourceException ex)
    {
      localizedMessage = message;
    }
    return localizedMessage;
  }

  public String getLocalizedMessage(String message, Object[] arguments)
  {
    String localizedMessage;
    try
    {
      localizedMessage = resourceBundle.getString(message);
      MessageFormat messageFormat =
        new MessageFormat(localizedMessage, getLocale());
      return messageFormat.format(arguments,
        new StringBuffer(), null).toString();
    }
    catch (MissingResourceException ex)
    {
      localizedMessage = message;
    }
    return localizedMessage;
  }

  public void newWorkspace()
  {
    workspace = new Workspace();
    DefaultMutableTreeNode workspaceNode =
      new DefaultMutableTreeNode(workspace);
    treeModel.setRoot(workspaceNode);
    treeModel.nodeStructureChanged(workspaceNode);
    setWorkspaceFile(null);
    setWorkspaceModified(false);
  }

  public void loadWorkspace(File workspaceFile)
  {
    try
    {
      workspace = Workspace.loadWorkspace(workspaceFile);
      DefaultMutableTreeNode workspaceNode =
        new DefaultMutableTreeNode(workspace);
      for (Server server : workspace.getServers())
      {
        DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(server);
        workspaceNode.add(serverNode);
        for (Module module : server.getModules())
        {
          DefaultMutableTreeNode moduleNode = new DataNode(explorer, module);
          serverNode.add(moduleNode);
        }
      }
      treeModel.setRoot(workspaceNode);
      treeModel.nodeStructureChanged(workspaceNode);
      explorer.expandRow(0);
      setWorkspaceFile(workspaceFile);
      setWorkspaceModified(false);
    }
    catch (Exception ex)
    {
      showError("Error", ex);
    }
  }

  public void saveWorkspace(File workspaceFile)
  {
    try
    {
      Workspace.saveWorkspace(workspace, workspaceFile);
      setWorkspaceFile(workspaceFile);
      setWorkspaceModified(false);
    }
    catch (Exception ex)
    {
      showError("Error", ex);
    }
  }

  public void saveWorkspace()
  {
    if (workspaceFile != null)
    {
      saveWorkspace(workspaceFile);
    }
  }

  public void askSaveWorkspace()
  {
    if (workspaceModified)
    {
      int option = JOptionPane.showConfirmDialog(this,
       getLocalizedMessage("ConfirmSaveWorkspace"),
       getLocalizedMessage("SaveWorkspace"),
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.YES_OPTION)
      {
        saveWorkspace();
      }
    }
  }

  public void showError(final String title, final Exception ex)
  {
    String message = ex.getMessage();
    if (message == null || ex instanceof IOException)
    {
      message = ex.toString();
    }
    if (message.startsWith(IOConstants.OPEN_LIST_TOKEN) &&
        message.endsWith(IOConstants.CLOSE_LIST_TOKEN))
    {
      try
      {
        BList list = (BList)Parser.fromString(message);
        StringBuilder buffer = new StringBuilder();
        if (list.size() > 0)
        {
          buffer.append(String.valueOf(list.get(0)));
          if (list.size() > 1)
          {
            buffer.append(": ");
            buffer.append(String.valueOf(list.get(1)));
          }
          message = buffer.toString();
        }
      }
      catch (Exception ex2)
      {
      }
    }
    showError(title, message);
  }

  public void showError(final String title, final String message)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        String text = message;
        if (text.length() > 100)
        {
          text = text.substring(0, 100) + "…";
        }
        JOptionPane.showMessageDialog(ManagerApp.this, text,
          getLocalizedMessage(title), JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    preferences.load();
    IconCache.setScalingFactor(preferences.getScalingFactor());
    initLookAndFeel();
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        new ManagerApp().setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private org.brain4it.manager.swing.DesignerAuxiliaryPanel designerAuxiliaryPanel;
  private javax.swing.JTree explorer;
  private javax.swing.JScrollPane explorerScrollPane;
  private javax.swing.JSplitPane firstSplitPane;
  private javax.swing.JSplitPane secondSplitPane;
  private org.brain4it.manager.swing.splitter.Splitter splitter;
  private javax.swing.JToolBar toolBar;
  // End of variables declaration//GEN-END:variables

  private void initApp()
  {
    setFrameIcons();
    setSize(preferences.getWindowWidth(), preferences.getWindowHeight());
    auxiliaryPanelWidth = preferences.getAuxiliaryPanelWidth();
    firstSplitPane.setDividerLocation(preferences.getExplorerWidth());
    secondSplitPane.setRightComponent(null);

    JFileChooser.setDefaultLocale(Locale.getDefault());
    JOptionPane.setDefaultLocale(Locale.getDefault());

    int scalingFactor = preferences.getScalingFactor();
    explorer.setRowHeight(20 * scalingFactor);

    resourceBundle =
      ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager");

    newWorkspaceAction = new NewWorkspaceAction(this);
    openWorkspaceAction = new OpenWorkspaceAction(this);
    saveWorkspaceAction = new SaveWorkspaceAction(this, false);
    saveWorkspaceAsAction = new SaveWorkspaceAction(this, true);
    documentationAction = new DocumentationAction(this);
    aboutAction = new AboutAction(this);
    preferencesAction = new PreferencesAction(this);
    closeAllTabsAction = new CloseTabsAction(this, true);
    exitAction = new ExitAction(this);

    addServerAction = new AddServerAction(this);
    duplicateServerAction = new DuplicateServerAction(this);
    renameWorkspaceAction = new RenameWorkspaceAction(this);
    editServerAction = new EditServerAction(this);
    addModuleAction = new AddModuleAction(this);
    closeTabsAction = new CloseTabsAction(this);
    editModuleAction = new EditModuleAction(this);
    removeNodeAction = new RemoveNodeAction(this);
    refreshNodeAction = new RefreshNodeAction(this);
    createModuleAction = new CreateModuleAction(this);
    destroyModuleAction = new DestroyModuleAction(this);
    listModulesAction = new ListModulesAction(this);
    openConsoleAction = new OpenConsoleAction(this);
    openEditorAction = new OpenEditorAction(this);
    openDashboardAction = new OpenDashboardAction(this);
    openDesignerAction = new OpenDesignerAction(this);
    splitVerticalAction = new SplitVerticalAction(this);
    splitHorizontalAction = new SplitHorizontalAction(this);
    closeSplitAction = new CloseSplitAction(this);
    renameDataAction = new RenameDataAction(this);
    deleteDataAction = new DeleteDataAction(this);
    moveDataAction = new MoveDataAction(this);
    exportModuleAction = new ExportModuleAction(this);
    importModuleAction = new ImportModuleAction(this);

    contextActions = new ManagerAction[]{
      addServerAction,
      duplicateServerAction,
      renameWorkspaceAction,
      editServerAction,
      createModuleAction,
      addModuleAction,
      editModuleAction,
      null,
      removeNodeAction,
      destroyModuleAction,
      listModulesAction,
      renameDataAction,
      deleteDataAction,
      null,
      openConsoleAction,
      openEditorAction,
      openDashboardAction,
      openDesignerAction,
      null,
      refreshNodeAction,
      null,
      importModuleAction,
      exportModuleAction,
      null,
      closeTabsAction
    };
    setLocationRelativeTo(null);
    for (ManagerAction action : contextActions)
    {
      if (action != null)
      {
        action.enableFor(null);
      }
    }

    menuBar = new JMenuBar();
    setScreenMenuBarOnMacOs();
    setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(getLocalizedMessage("File"));
    fileMenu.add(newWorkspaceAction);
    fileMenu.add(openWorkspaceAction);
    fileMenu.add(saveWorkspaceAction);
    fileMenu.add(saveWorkspaceAsAction);
    fileMenu.add(new JSeparator());
    fileMenu.add(exitAction);
    menuBar.add(fileMenu);

    JMenu editMenu = new JMenu(getLocalizedMessage("Edit"));
    editMenu.add(addServerAction);
    editMenu.add(duplicateServerAction);
    editMenu.add(editServerAction);
    editMenu.add(listModulesAction);
    editMenu.add(createModuleAction);
    editMenu.add(addModuleAction);
    editMenu.addSeparator();
    editMenu.add(editModuleAction);
    editMenu.add(destroyModuleAction);
    editMenu.addSeparator();
    editMenu.add(removeNodeAction);
    editMenu.addSeparator();
    editMenu.add(preferencesAction);
    menuBar.add(editMenu);

    JMenu viewMenu = new JMenu(getLocalizedMessage("View"));
    viewMenu.add(openConsoleAction);
    viewMenu.add(openEditorAction);
    viewMenu.add(openDashboardAction);
    viewMenu.add(openDesignerAction);
    viewMenu.addSeparator();
    viewMenu.add(splitVerticalAction);
    viewMenu.add(splitHorizontalAction);
    viewMenu.add(closeSplitAction);
    viewMenu.addSeparator();
    viewMenu.add(closeAllTabsAction);
    menuBar.add(viewMenu);

    JMenu helpMenu = new JMenu(getLocalizedMessage("Help"));
    helpMenu.add(documentationAction);
    helpMenu.add(aboutAction);
    menuBar.add(helpMenu);

    toolBar.add(new ToolBarAction(addServerAction));
    toolBar.add(new ToolBarAction(duplicateServerAction));
    toolBar.add(new ToolBarAction(createModuleAction));
    toolBar.add(new ToolBarAction(addModuleAction));
    toolBar.add(new ToolBarAction(listModulesAction));
    toolBar.add(new ToolBarAction(removeNodeAction));
    toolBar.add(new ToolBarAction(openConsoleAction));
    toolBar.add(new ToolBarAction(openEditorAction));
    toolBar.add(new ToolBarAction(openDashboardAction));
    toolBar.add(new ToolBarAction(openDesignerAction));
    toolBar.add(new ToolBarAction(refreshNodeAction));
    toolBar.add(new ToolBarAction(splitVerticalAction));
    toolBar.add(new ToolBarAction(splitHorizontalAction));
    toolBar.add(new ToolBarAction(closeSplitAction));

    explorer.requestFocus();

    popupMenu = new JPopupMenu();

    findDialog = new FindDialog(this);

    workspace = new Workspace();
    DefaultMutableTreeNode workspaceNode =
      new DefaultMutableTreeNode(workspace);

    treeModel = new DefaultTreeModel(workspaceNode);
    explorer.setModel(treeModel);
    explorer.setToggleClickCount(0);
    explorer.setCellRenderer(new NodeRenderer());
    explorer.setUI(new MetalTreeUI());
    ((Explorer)explorer).setMoveDataAction(moveDataAction);
    DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(SINGLE_TREE_SELECTION);
    explorer.setSelectionModel(selectionModel);
    explorer.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent event)
      {
        if (event.getKeyCode() == KeyEvent.VK_ENTER)
        {
          TreePath path = explorer.getSelectionPath();
          if (path != null)
          {
            DefaultMutableTreeNode node =
              (DefaultMutableTreeNode)path.getLastPathComponent();
            executeDefaultAction(node);
          }
        }
        else if (event.getKeyCode() == KeyEvent.VK_DELETE)
        {
          TreePath path = explorer.getSelectionPath();
          if (path != null)
          {
            DefaultMutableTreeNode node =
              (DefaultMutableTreeNode)path.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof Server || userObject instanceof Module)
            {
              removeNodeAction.actionPerformed(
                new ActionEvent(explorer, 0, "remove"));
            }
            else if (node instanceof DataNode)
            {
              deleteDataAction.actionPerformed(
                new ActionEvent(explorer, 0, "delete"));
            }
          }
        }
      }
    });
    explorer.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
          if (e.getClickCount() > 1)
          {
            TreePath path = explorer.getPathForLocation(e.getX(), e.getY());
            if (path != null)
            {
              DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)path.getLastPathComponent();
              executeDefaultAction(node);
            }
          }
        }
        else
        {
          TreePath path = explorer.getPathForLocation(e.getX(), e.getY());
          if (path != null)
          {
            explorer.setSelectionPath(path);
            popupMenu.removeAll();
            boolean haveSeparator = false;
            for (ManagerAction action : contextActions)
            {
              if (action == null)
              {
                haveSeparator = true;
              }
              else
              {
                if (action.isEnabled())
                {
                  if (haveSeparator)
                  {
                    popupMenu.addSeparator();
                  }
                  popupMenu.add(action);
                  haveSeparator = false;
                }
              }
            }
            popupMenu.show(ManagerApp.this.explorer, e.getX(), e.getY());
          }
        }
      }
    });

    splitter.setActiveColor(BASE_COLOR);
    splitter.addSplitterListener(new SplitterListener()
    {
      @Override
      public void componentAdded(SplitterEvent e)
      {
      }

      @Override
      public void componentRemoved(SplitterEvent e)
      {
        Component component = e.getComponent();
        if (component instanceof ModulePanel)
        {
          ((ModulePanel)component).close();
        }
      }

      @Override
      public boolean componentClosing(SplitterEvent event)
      {
        Component component = event.getComponent();
        if (component instanceof ModulePanel)
        {
          ModulePanel modulePanel = (ModulePanel)component;
          return modulePanel.closing();
        }
        return true; // true to close
      }
    });

    File file = preferences.getLastWorkspaceFile();
    if (file != null)
    {
      loadWorkspace(file);
    }
  }

  private static void initLookAndFeel()
  {
    try
    {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName()))
        {
          int scalingFactor = preferences.getScalingFactor();
          FillPainter fillPainter = new FillPainter(BASE_COLOR);
          FillPainter fillPainter2 = new FillPainter(Color.LIGHT_GRAY);
          BorderPainter borderPainter = new BorderPainter(BASE_COLOR, 2);

          UIManager.put("nimbusBase", new Color(140, 130, 100));
          UIManager.put("nimbusFocus", BASE_COLOR);
          UIManager.put("nimbusSelection", BASE_COLOR);
          UIManager.put("nimbusSelectionBackground", BASE_COLOR);
          UIManager.setLookAndFeel(info.getClassName());
          UIDefaults uiDefaults = UIManager.getLookAndFeel().getDefaults();
          uiDefaults.put("ToolBar:Button[MouseOver].backgroundPainter", fillPainter);
          uiDefaults.put("MenuBar:Menu[Selected].backgroundPainter", fillPainter);
          uiDefaults.put("ToolBar:Button[Pressed].backgroundPainter", fillPainter2);
          uiDefaults.put("ToolBar:Button[Focused].backgroundPainter", borderPainter);
          uiDefaults.put("ToolBar:Button[Focused+Pressed].backgroundPainter", fillPainter2);
          uiDefaults.put("ToolBar:Button[Focused+MouseOver].backgroundPainter", fillPainter);
          uiDefaults.put("ToolBar:Button[Disabled].textForeground", new Color(150, 150, 150));
          if (System.getProperty("os.name").toLowerCase().contains("windows"))
          {
            uiDefaults.put("defaultFont",
             new Font("Segoe UI", 0, 12 * scalingFactor));
          }
          else
          {
            Font font = (Font)uiDefaults.get("defaultFont");
            uiDefaults.put("defaultFont", font.deriveFont(12.0f * scalingFactor));
          }
          break;
        }
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(ManagerApp.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void executeDefaultAction(DefaultMutableTreeNode node)
  {
    if (node.getUserObject() instanceof Workspace)
    {
      ActionEvent event =
        new ActionEvent(ManagerApp.this, 0, "renameworkspace");
      renameWorkspaceAction.actionPerformed(event);
    }
    else if (node.getUserObject() instanceof Server)
    {
      ActionEvent event =
        new ActionEvent(ManagerApp.this, 0, "editserver");
      editServerAction.actionPerformed(event);
    }
    else if (node.getUserObject() instanceof Module)
    {
      ActionEvent event =
        new ActionEvent(ManagerApp.this, 0, "openconsole");
      openConsoleAction.actionPerformed(event);
    }
    else if (node instanceof DataNode)
    {
      ActionEvent event =
        new ActionEvent(ManagerApp.this, 0, "openeditor");
      openEditorAction.actionPerformed(event);
    }
  }

  private void setFrameIcons()
  {
    try
    {
      if (System.getProperty("os.name").contains("Mac"))
      {
        ImageIcon icon = new ImageIcon(getClass().getResource(
          "/org/brain4it/manager/swing/resources/icons/logo_128.png"));
        setMacOSIcon(icon);
      }
      else
      {
        ArrayList<Image> icons = new ArrayList<>();
        for (int logoSize : LOGO_SIZES)
        {
          ImageIcon icon = new ImageIcon(getClass().getResource(
            "/org/brain4it/manager/swing/resources/icons/logo_" +
            logoSize + ".png"));
          icons.add(icon.getImage());
        }
        setIconImages(icons);
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(ManagerApp.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void setMacOSIcon(ImageIcon icon)
  {
    try
    {
      Class cls = Class.forName("com.apple.eawt.Application");
      Method method = cls.getMethod("getApplication", new Class[0]);
      Object application = method.invoke(null, new Object[0]);
      method = cls.getMethod("setDockIconImage", new Class[]{Image.class});
      method.invoke(application, new Object[]{icon.getImage()});
    }
    catch (Exception ex)
    {
      Logger.getLogger(ManagerApp.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void setScreenMenuBarOnMacOs()
  {
    if (System.getProperty("os.name").toLowerCase().contains("mac os"))
    {
      try
      {
        Class cls = Class.forName("com.apple.laf.AquaMenuBarUI");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "Brain4it");
        MenuBarUI ui = (MenuBarUI)cls.getConstructor().newInstance();
        menuBar.setUI(ui);
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
  }
}
