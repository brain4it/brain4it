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
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import org.brain4it.client.RestClient;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.DataNode;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.Explorer;

/**
 *
 * @author realor
 */
public class MoveDataAction extends ManagerAction
{
  public MoveDataAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, "MoveData");
  }  

  @Override
  public void actionPerformed(ActionEvent e)
  {
    final Explorer explorer = managerApp.getExplorer();
    final DataNode draggedNode = explorer.getDraggedNode();
    final DataNode targetNode = explorer.getTargetNode();
    BList pathList = draggedNode.getModulePathList();
    String targetName = (String)targetNode.getUserObject();
    
    Module module = draggedNode.getModule();    
    RestClient restClient = module.getRestClient();
    String command = "(call (function (path n2) " +
    "(local lst idx obj n1) " +
    "(set n1 (pop path)) " +
    "(set lst (get (global-scope) path)) " +
    "(sync lst " +
    "(set obj (remove lst n1)) " +
    "(set idx (name-index lst n2)) " +
    "(insert lst idx obj) " +
    "(put-name lst idx n1)) null" +
    ") " + Printer.toString(pathList) + 
    " \"" + Utils.escapeString(targetName) + "\")";
    
    restClient.execute(module.getName(), command, new RestClient.Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            DataNode parentNode = (DataNode)draggedNode.getParent();
            int draggedIndex = parentNode.getIndex(draggedNode);
            int targetIndex = parentNode.getIndex(targetNode);
            if (targetIndex > draggedIndex) targetIndex--;
            DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
            
            parentNode.remove(draggedIndex);
            model.nodesWereRemoved(parentNode, 
              new int[]{draggedIndex}, new Object[]{draggedNode});
            
            parentNode.insert(draggedNode, targetIndex);
            model.nodesWereInserted(parentNode, new int[]{targetIndex});
          }
        });
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError((String)getValue(AbstractAction.NAME), ex);
      }
    });
  }
}
