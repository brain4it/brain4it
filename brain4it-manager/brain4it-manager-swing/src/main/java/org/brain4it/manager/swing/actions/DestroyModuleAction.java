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
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.brain4it.client.RestClient;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.Explorer;

/**
 *
 * @author realor
 */
public class DestroyModuleAction extends ManagerAction
{
  public DestroyModuleAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, 
      managerApp.getLocalizedMessage("DestroyModule"));

    ImageIcon icon = IconCache.getIcon("delete");
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    DefaultMutableTreeNode moduleNode = managerApp.getSelectedNode();
    Module module = (Module)moduleNode.getUserObject();
    String message = managerApp.getLocalizedMessage("DestroyModule.question", 
      new Object[]{module.getName()});
    int option = JOptionPane.showConfirmDialog(managerApp, 
      message, (String)getValue(NAME), 
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (option == JOptionPane.YES_OPTION)
    {
      if (managerApp.closeTabs(moduleNode))
      {
        String moduleName = module.getName();
        RestClient client = module.getRestClient();
        client.setConnectionTimeout(10000);
        client.setReadTimeout(10000);
        client.destroyModule(moduleName, new RestClient.Callback()
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
                showResult(resultString);            
              }
            });
          }

          @Override
          public void onError(RestClient client, final Exception ex)
          {
            managerApp.showError("DestroyModule", ex);
          }
        });     
      }
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Module);
  }
  
  protected void showResult(String resultString)
  {
    Explorer explorer = managerApp.getExplorer();    
    DefaultMutableTreeNode moduleNode = managerApp.getSelectedNode();
    Module module = (Module)moduleNode.getUserObject();
    Server server = module.getServer();
    server.getModules().remove(module);

    DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
    DefaultMutableTreeNode serverNode = 
      (DefaultMutableTreeNode)moduleNode.getParent();
    int index = serverNode.getIndex(moduleNode);
    serverNode.remove(moduleNode);
    model.nodesWereRemoved(serverNode,
      new int[]{index}, new Object[]{moduleNode});
    managerApp.setWorkspaceModified(true);

    String message = managerApp.getLocalizedMessage("DestroyModule.result", 
      new Object[]{module.getName()});
    JOptionPane.showMessageDialog(managerApp, message, 
      (String)getValue(NAME), JOptionPane.INFORMATION_MESSAGE);            
  }  
}
