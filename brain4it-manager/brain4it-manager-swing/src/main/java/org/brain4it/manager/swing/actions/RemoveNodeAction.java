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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.Explorer;
import static javax.swing.Action.NAME;

/**
 *
 * @author realor
 */
public class RemoveNodeAction extends ManagerAction
{
  public RemoveNodeAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("RemoveNode"));

    ImageIcon icon = IconCache.getIcon("close");
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Explorer explorer = managerApp.getExplorer();
    DefaultMutableTreeNode node = managerApp.getSelectedNode();

    String message = managerApp.getLocalizedMessage("RemoveNode.question",
      new Object[]{node.getUserObject()});
    int option = JOptionPane.showConfirmDialog(managerApp,
      message, (String)getValue(NAME),
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (option == JOptionPane.YES_OPTION)
    {
      if (!(node.getUserObject() instanceof Workspace))
      {
        if (managerApp.closeTabs(node))
        {
          Object userObject = node.getUserObject();
          if (userObject instanceof Server)
          {
            Server server = (Server)userObject;
            server.getWorkspace().getServers().remove(server);
          }
          else if (userObject instanceof Module)
          {
            Module module = (Module)userObject;
            module.getServer().getModules().remove(module);
          }
          DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
          int index = parent.getIndex(node);
          parent.remove(node);
          model.nodesWereRemoved(parent,
            new int[]{index}, new Object[]{node});
          managerApp.setWorkspaceModified(true);
        }
      }
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null &&
      (node.getUserObject() instanceof Server ||
       node.getUserObject() instanceof Module));
  }
}
