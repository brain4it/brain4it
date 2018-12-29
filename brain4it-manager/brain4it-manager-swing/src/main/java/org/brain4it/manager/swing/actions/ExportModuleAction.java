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
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.ManagerApp;
import javax.swing.filechooser.FileFilter;
import org.brain4it.io.Exporter;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import static javax.swing.Action.NAME;
import static org.brain4it.server.ServerConstants.*;
/**
 *
 * @author realor
 */
public class ExportModuleAction extends ManagerAction
{
  public ExportModuleAction(ManagerApp managerApp)
  {
    super(managerApp);
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage("ExportModule"));
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    DefaultMutableTreeNode moduleNode = managerApp.getSelectedNode();
    Module module = (Module)moduleNode.getUserObject();

    JFileChooser fileChooser = new JFileChooser();
    FileFilter[] filters = fileChooser.getChoosableFileFilters();
    if (filters.length > 0)
    {
      fileChooser.removeChoosableFileFilter(filters[0]);
    }
    fileChooser.addChoosableFileFilter(new FileFilter()
    {
      @Override
      public boolean accept(File f)
      {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".snp");
      }

      @Override
      public String getDescription()
      {
        return "Brain4it snapshot (*.snp)";
      }
    });
    if (filters.length > 0)
    {
      fileChooser.addChoosableFileFilter(filters[0]);
    }
    fileChooser.setSelectedFile(new File(module.getName() + ".snp"));
    fileChooser.setDialogTitle(managerApp.getLocalizedMessage("ExportModule"));
    int result = fileChooser.showDialog(
      managerApp, managerApp.getLocalizedMessage("Export"));
    if (result == JFileChooser.APPROVE_OPTION)
    {
      final File file = fileChooser.getSelectedFile();
      if (file.exists())
      {
        int option = JOptionPane.showConfirmDialog(managerApp,
          managerApp.getLocalizedMessage("FileExistsOverwrite"),
          managerApp.getLocalizedMessage("Export"), JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.NO_OPTION) return;
      }
      managerApp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      RestClient client = module.getRestClient();
      client.setConnectionTimeout(10000);
      client.setReadTimeout(10000);
      client.get(module.getName(), "", new Callback()
      {
        @Override
        public void onSuccess(RestClient client, String dataString)
        {
          try
          {
            // remove accessKey variable
            BList data = (BList)Parser.fromString(dataString);
            data.remove(MODULE_ACCESS_KEY_VAR);
            dataString = Printer.toString(data);
          }
          catch (Exception ex)
          {
            // ignore
          }
          exportData(file, dataString);
        }

        @Override
        public void onError(RestClient client, Exception ex)
        {
          managerApp.setCursor(Cursor.getDefaultCursor());
          managerApp.showError((String)getValue(NAME), ex);
        }
      });
    }
  }

  @Override
  public void enableFor(DefaultMutableTreeNode node)
  {
    setEnabled(node != null && node.getUserObject() instanceof Module);
  }

  private void exportData(File file, String dataString)
  {
    Exporter exporter = new Exporter(file, BPL_CHARSET)
    {
      @Override
      public void onSuccess(String data)
      {
        managerApp.setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(managerApp, 
          managerApp.getLocalizedMessage("ExportCompleted"),
          (String)getValue(NAME), JOptionPane.INFORMATION_MESSAGE);
      }

      @Override
      public void onError(Exception ex)
      {
        managerApp.setCursor(Cursor.getDefaultCursor());
        managerApp.showError((String)getValue(NAME), ex);
      }
    };
    exporter.exportData(dataString);
  }
}

