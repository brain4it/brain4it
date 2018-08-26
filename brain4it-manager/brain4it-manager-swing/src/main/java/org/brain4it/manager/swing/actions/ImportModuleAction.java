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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Importer;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.DataNode;
import org.brain4it.manager.swing.ImportModuleDialog;
import org.brain4it.manager.swing.ManagerApp;
import static javax.swing.Action.NAME;
import static org.brain4it.server.ServerConstants.BPL_CHARSET;

/**
 *
 * @author realor
 */
public class ImportModuleAction extends ManagerAction
{
  public ImportModuleAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME,
      managerApp.getLocalizedMessage("ImportModule"));
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    final DataNode moduleNode = (DataNode)managerApp.getSelectedNode();
    managerApp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    ImportModuleDialog dialog = new ImportModuleDialog(managerApp, true);
    dialog.pack();
    dialog.setLocationRelativeTo(managerApp);
    dialog.setVisible(true);
    String url = dialog.getURL();
    if (url != null)
    {
      Importer importer = new Importer(url, BPL_CHARSET)
      {
        @Override
        public void onSuccess(String dataString)
        {
          putData(moduleNode, dataString);
        }

        @Override
        public void onError(Exception ex)
        {
          managerApp.setCursor(Cursor.getDefaultCursor());
          managerApp.showError("ImportModule", ex);
        }
      };
      importer.importData();
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Module);
  }

  private void putData(final DataNode moduleNode, String dataString)
  {
    Module module = (Module)moduleNode.getUserObject();
    RestClient client = module.getRestClient();
    client.setConnectionTimeout(10000);
    client.setReadTimeout(60000);
    client.put(module.getName(), "", dataString, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            moduleNode.explore();
            JOptionPane.showMessageDialog(managerApp, "Import completed.",
              (String)getValue(NAME), JOptionPane.INFORMATION_MESSAGE);
            managerApp.setCursor(Cursor.getDefaultCursor());
          }
        });
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError("ImportModule", ex);
        managerApp.setCursor(Cursor.getDefaultCursor());
      }
    });
  }
}