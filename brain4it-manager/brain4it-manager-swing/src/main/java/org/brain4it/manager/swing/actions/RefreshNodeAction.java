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
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.DataNode;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;

/**
 *
 * @author realor
 */
public class RefreshNodeAction extends ManagerAction
{
  public RefreshNodeAction(ManagerApp managerApp)
  {
    super(managerApp);    
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("RefreshNode"));
    
    ImageIcon icon = IconCache.getIcon("refresh");
    putValue(AbstractAction.SMALL_ICON, icon);
  }  
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    final DataNode node = (DataNode)managerApp.getSelectedNode();
    node.explore();
    if (node.getUserObject() instanceof Module)
    {
      Module module = (Module)node.getUserObject();
      module.loadMetadata(new Module.Callback()
      {
        @Override
        public void actionCompleted(Module module, String action)
        {
          node.update();
          managerApp.setWorkspaceModified(true);
        }

        @Override
        public void actionFailed(Module module, String action, Exception ex)
        {
          managerApp.showError(
            managerApp.getLocalizedMessage("RefreshNode"), ex);
        }
      });
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node instanceof DataNode);
  }  
}
