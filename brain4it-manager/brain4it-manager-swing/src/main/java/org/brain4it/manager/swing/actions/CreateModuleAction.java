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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.swing.DataNode;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.ModuleDialog;
import org.brain4it.manager.swing.Explorer;
import static javax.swing.Action.NAME;

/**
 *
 * @author realor
 */
public class CreateModuleAction extends ManagerAction
{
  public CreateModuleAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME,
      managerApp.getLocalizedMessage("CreateModule"));

    ImageIcon icon = IconCache.getIcon("create_module");
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    DefaultMutableTreeNode serverNode = managerApp.getSelectedNode();
    Server server = (Server)serverNode.getUserObject();
    final Module module = new Module(server);
    module.randomAccessKey();

    ModuleDialog dialog = new ModuleDialog(managerApp, true);
    dialog.setTitle((String)getValue(AbstractAction.NAME));
    dialog.setLocationRelativeTo(managerApp);
    dialog.setModule(module);
    dialog.setVisible(true);
    if (dialog.isAccepted())
    {
      managerApp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      RestClient client = server.getRestClient();
      client.setConnectionTimeout(10000);
      client.setReadTimeout(10000);
      client.createModule(module.getName(), new Callback()
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
              showResult(module, resultString);
            }
          });
        }

        @Override
        public void onError(RestClient client, final Exception ex)
        {
          managerApp.setCursor(Cursor.getDefaultCursor());
          managerApp.showError(
            managerApp.getLocalizedMessage("CreateModule"), ex);
        }
      });
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Server);
  }

  protected void showResult(final Module module, String resultString)
  {
    Explorer explorer = managerApp.getExplorer();
    DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
    DefaultMutableTreeNode serverNode = managerApp.getSelectedNode();
    Server server = (Server)serverNode.getUserObject();
    server.getModules().add(module);

    DefaultMutableTreeNode moduleNode = new DataNode(explorer, module);
    serverNode.add(moduleNode);
    model.nodesWereInserted(serverNode,
      new int[]
      {
        serverNode.getChildCount() - 1
      });

    TreePath path = new TreePath(moduleNode.getPath());
    explorer.setSelectionPath(path);
    explorer.scrollPathToVisible(path);
    managerApp.setWorkspaceModified(true);

    module.saveAccessKeyAndMetadata(server.getAccessKey(), new Module.Callback()
    {
      @Override
      public void actionCompleted(Module module, String action)
      {
        managerApp.setCursor(Cursor.getDefaultCursor());
        String message = managerApp.getLocalizedMessage("CreateModule.result",
          new Object[]{module.getName()});
        JOptionPane.showMessageDialog(managerApp, message,
          (String)getValue(NAME), JOptionPane.INFORMATION_MESSAGE);
      }

      @Override
      public void actionFailed(Module module, String action, Exception error)
      {
        managerApp.setCursor(Cursor.getDefaultCursor());
        managerApp.showError(
          managerApp.getLocalizedMessage("CreateModule"), error);
      }
    });
  }
}
