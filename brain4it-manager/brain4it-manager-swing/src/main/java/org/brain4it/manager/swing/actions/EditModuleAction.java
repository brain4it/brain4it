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
import org.brain4it.lang.BList;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.IconCache;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.ModuleDialog;
import org.brain4it.manager.swing.Explorer;

/**
 *
 * @author realor
 */
public class EditModuleAction extends ManagerAction
{
  public EditModuleAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("EditModule"));

    ImageIcon icon = IconCache.getIcon("edit");
    putValue(AbstractAction.SMALL_ICON, icon);
  }  
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    ModuleDialog dialog = new ModuleDialog(managerApp, true);
    dialog.setNameEditable(false);
    dialog.setLocationRelativeTo(managerApp);
    DefaultMutableTreeNode moduleNode = managerApp.getSelectedNode();
    Module module = (Module)moduleNode.getUserObject();
    dialog.setModuleName(module.getName());
    dialog.setIndentityKey(module.getAccessKey());
    dialog.setMetadata(module.getMetadata());
    dialog.setVisible(true);
    
    if (dialog.isAccepted())
    {
      module.setName(dialog.getModuleName());
      module.setAccessKey(dialog.getAccessKey());
      BList metadata = dialog.getMetadata();
      module.setMetadata(metadata);
      
      Explorer explorer = managerApp.getExplorer();
      DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
      DefaultMutableTreeNode serverNode = 
        (DefaultMutableTreeNode)moduleNode.getParent();
      model.nodesChanged(serverNode, new int[]{serverNode.getIndex(moduleNode)});
      managerApp.setWorkspaceModified(true);

      module.saveMetadata(metadata, new Module.Callback()
      {
        @Override
        public void actionCompleted(Module module, String action)
        {
        }

        @Override
        public void actionFailed(Module module, String action, Exception error)
        {
          managerApp.showError(
           managerApp.getLocalizedMessage("EditModule"), error);
        }
      });
    }
  }
  
  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Module);
  }  
}
