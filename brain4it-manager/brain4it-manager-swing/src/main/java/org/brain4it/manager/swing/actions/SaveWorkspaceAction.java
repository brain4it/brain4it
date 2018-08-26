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
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.swing.WorkspaceFileFilter;

/**
 *
 * @author realor
 */
public class SaveWorkspaceAction extends ManagerAction
{
  private final boolean chooseFile;
  
  public SaveWorkspaceAction(ManagerApp managerApp, boolean chooseFile)
  {
    super(managerApp);
    this.chooseFile = chooseFile;
    String key = chooseFile ? "SaveWorkspaceAs" : "SaveWorkspace";
    putValue(AbstractAction.NAME, managerApp.getLocalizedMessage(key));
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    File workspaceFile = managerApp.getWorkspaceFile();
    if (chooseFile || workspaceFile == null)
    {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(workspaceFile);
      fileChooser.setFileFilter(new WorkspaceFileFilter());
      fileChooser.setDialogTitle(
        managerApp.getLocalizedMessage((String)getValue(AbstractAction.NAME)));
      int result = fileChooser.showDialog(
        managerApp, managerApp.getLocalizedMessage("Save"));
      if (result == JFileChooser.APPROVE_OPTION)
      {
        workspaceFile = fileChooser.getSelectedFile();
        if (!workspaceFile.getName().contains("."))
        {
          workspaceFile = new File(workspaceFile.getAbsolutePath() + 
           ManagerApp.WORKSPACE_EXTENSION);
        }
      }
      else workspaceFile = null;
    }

    if (workspaceFile != null)
    {
      managerApp.saveWorkspace(workspaceFile);
    }
  }
}
