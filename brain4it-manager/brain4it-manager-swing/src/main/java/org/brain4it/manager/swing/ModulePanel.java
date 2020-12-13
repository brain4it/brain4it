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

package org.brain4it.manager.swing;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.brain4it.client.RestClient;
import org.brain4it.io.IOConstants;
import org.brain4it.manager.Module;
import org.brain4it.manager.ModuleEvent;
import org.brain4it.manager.ModuleListener;
import org.brain4it.manager.Server;

/**
 *
 * @author realor
 */
public abstract class ModulePanel extends JPanel implements ModuleListener
{
  protected final ManagerApp managerApp;
  protected final Module module;
  private boolean modified = false;

  public ModulePanel(ManagerApp managerApp, Module module)
  {
    this.managerApp = managerApp;
    this.module = module;
    this.module.addModuleListener(this);
  }

  public abstract String getPanelType();

  public String getPanelName()
  {
    Server server = module.getServer();
    return server.getName() + IOConstants.PATH_REFERENCE_SEPARATOR +
      module.getName();
  }

  public ManagerApp getManagerApp()
  {
    return managerApp;
  }

  public Module getModule()
  {
    return module;
  }

  public boolean closing()
  {
    // invoked from splitter in EDT asking to close the tab
    if (isModified())
    {
      managerApp.getSplitter().showComponent(this);
      String title = getPanelType();
      String message = managerApp.getLocalizedMessage("DiscardChanges",
        new Object[]{getPanelName()});
      int option = JOptionPane.showConfirmDialog(managerApp, message,
        title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.NO_OPTION)
      {
        return false; // false to abort close
      }
    }
    return true;
  }

  public void close() // invoked from splitter in EDT to close the tab
  {
    module.removeModuleListener(ModulePanel.this);
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
    managerApp.updateTab(this);
  }

  public RestClient getRestClient()
  {
    return module.getRestClient();
  }

  @Override
  public void accessKeyChanged(ModuleEvent event)
  {
  }

  @Override
  public void functionsUpdated(ModuleEvent event)
  {
  }

  @Override
  public void globalsUpdated(ModuleEvent event)
  {
  }
}
