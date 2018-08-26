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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
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
public class RenameDataAction extends ManagerAction
{
  public RenameDataAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("RenameData"));
  }
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    final Explorer explorer = managerApp.getExplorer();
    final DataNode node = (DataNode)managerApp.getSelectedNode();

    final String name = (String)JOptionPane.showInputDialog(
      managerApp, managerApp.getLocalizedMessage("RenameData.newName"),
      managerApp.getLocalizedMessage("RenameData"),
      JOptionPane.QUESTION_MESSAGE, null, null, (String)node.getUserObject());
    if (name == null) return;
    
    Module module = node.getModule();
    BList pathList = node.getModulePathList();
    RestClient restClient = module.getRestClient();
    String command = "(call (function (path newname) " +
    "(local lst idx oldname) " +
    "(set oldname (pop path))" +
    "(set lst (get (global-scope) path))" +
    "(sync lst " +
    "(set idx (name-index lst oldname))" +
    "(if (has lst newname) false" +
    "  (do (put-name lst idx newname) true)" +
    "))) " + Printer.toString(pathList) + " \"" + 
      Utils.escapeString(name) + "\")";
    
    restClient.execute(module.getName(), command, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        if (resultString.equals("true"))
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              node.setUserObject(name);
              DefaultTreeModel model = (DefaultTreeModel)explorer.getModel();
              model.nodeChanged(node);
            }
          });
        }
        else
        {
          managerApp.showError((String)getValue(AbstractAction.NAME), 
            "Name already exists.");        
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError((String)getValue(AbstractAction.NAME), ex);
      }
    });
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && 
      (node instanceof DataNode && 
       !(node.getUserObject() instanceof Module)));
  }
}
