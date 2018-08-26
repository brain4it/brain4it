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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.Explorer;
import org.brain4it.manager.swing.ServerDialog;

/**
 *
 * @author realor
 */
public class AddServerAction extends ManagerAction
{
  public AddServerAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("AddServer"));

    ImageIcon icon = IconCache.getIcon("add_server");
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    ServerDialog dialog = new ServerDialog(managerApp, true);
    dialog.setTitle((String)getValue(AbstractAction.NAME));
    dialog.setLocationRelativeTo(managerApp);
    dialog.setVisible(true);

    if (dialog.isAccepted())
    {
      Explorer explorer = managerApp.getExplorer();
      DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
      DefaultMutableTreeNode workspaceNode =
        (DefaultMutableTreeNode)model.getRoot();
      Workspace workspace = managerApp.getWorkspace();
      Server server = new Server(workspace);
      workspace.getServers().add(server);
      server.setName(dialog.getServerName());
      server.setUrl(dialog.getServerUrl());
      server.setAccessKey(dialog.getAccessKey());
      DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(server);
      workspaceNode.add(serverNode);
      model.nodesWereInserted(workspaceNode,
        new int[]{workspaceNode.getChildCount() - 1});

      TreePath path = new TreePath(serverNode.getPath());
      explorer.setSelectionPath(path);
      explorer.scrollPathToVisible(path);
      managerApp.setWorkspaceModified(true);
    }
  }
}
