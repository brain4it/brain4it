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

package org.brain4it.manager.swing.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.brain4it.client.RestClient;
import org.brain4it.lang.BList;
import org.brain4it.io.Parser;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.swing.DataNode;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.Explorer;

/**
 *
 * @author realor
 */
public class ListModulesAction extends ManagerAction
{
  public ListModulesAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME,
      managerApp.getLocalizedMessage("ListModules"));

    ImageIcon icon = IconCache.getIcon("list_modules");
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    final DefaultMutableTreeNode serverNode = managerApp.getSelectedNode();
    Server server = (Server)serverNode.getUserObject();
    RestClient client =
      new RestClient(server.getUrl(), server.getAccessKey());
    client.setConnectionTimeout(10000);
    client.setReadTimeout(10000);
    client.setAccessKey(server.getAccessKey());
    client.listModules(new RestClient.Callback()
    {
      @Override
      public void onSuccess(RestClient client,
        final String resultString)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            showResult(serverNode, resultString);
          }
        });
      }

      @Override
      public void onError(RestClient client, final Exception ex)
      {
        managerApp.showError("ListModules", ex);
      }
    });
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Server);
  }

  protected void showResult(DefaultMutableTreeNode serverNode, 
     String resultString)
  {
    try
    {
      // result use to be a text message
      BList moduleList = (BList)Parser.fromString(resultString);

      Explorer explorer = managerApp.getExplorer();
      DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
      
      Server server = (Server)serverNode.getUserObject();

      // save previous moduleNodes
      HashMap<String, DefaultMutableTreeNode> moduleMap = 
        new HashMap<String, DefaultMutableTreeNode>();
      for (int i = 0; i < serverNode.getChildCount(); i++)
      {
        DefaultMutableTreeNode moduleNode = 
          (DefaultMutableTreeNode)serverNode.getChildAt(i);
        Module module = (Module)moduleNode.getUserObject();
        moduleMap.put(module.getName(), moduleNode);
      }
      // rebuild module list
      serverNode.removeAllChildren();
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
        
        DefaultMutableTreeNode moduleNode = moduleMap.remove(moduleName);
        Module module;
        if (moduleNode == null) // new module
        {
          module = new Module(server, moduleName);
          moduleNode = new DataNode(explorer, module);
        }
        else
        {
          module = (Module)moduleNode.getUserObject();
        }
        module.setMetadata(metadata);
        serverNode.add(moduleNode);
        server.getModules().add(module);
      }
      // close deleted module tabs
      for (DefaultMutableTreeNode moduleNode : moduleMap.values())
      {
        managerApp.closeTabs(moduleNode);
      }

      model.nodeStructureChanged(serverNode);
      TreePath path = new TreePath(serverNode.getPath());
      explorer.expandPath(path);
      managerApp.setWorkspaceModified(true);
      
      String message = managerApp.getLocalizedMessage("ListModules.message", 
        new Object[]{moduleList.size()});
      JOptionPane.showMessageDialog(managerApp, message,
        (String)getValue(NAME), JOptionPane.INFORMATION_MESSAGE);
    }
    catch (Exception ex)
    {
      managerApp.showError("ListModules", ex);
    }
  }
}
