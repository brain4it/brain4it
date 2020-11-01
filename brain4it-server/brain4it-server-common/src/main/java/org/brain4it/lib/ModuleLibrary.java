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

package org.brain4it.lib;

import org.brain4it.lib.module.ModuleNotifyFunction;
import org.brain4it.lib.module.ModuleSaveFunction;
import org.brain4it.lib.module.ModuleNameFunction;
import org.brain4it.lib.module.ModuleInitFunction;
import org.brain4it.lib.module.ModuleStopFunction;
import org.brain4it.lib.module.ModuleSnapshotsFunction;
import org.brain4it.lib.module.ModuleLoadFunction;
import org.brain4it.lib.module.ModuleDeleteFunction;
import org.brain4it.lib.module.ModuleMonitorFunction;
import org.brain4it.lib.module.ModuleStartFunction;
import org.brain4it.lib.module.RemoteCallFunction;

/**
 *
 * @author realor
 */
public class ModuleLibrary extends Library
{
  @Override
  public String getName()
  {
    return "Module";
  }

  @Override
  public void load()
  {
    functions.put("module-name", new ModuleNameFunction());
    functions.put("module-init", new ModuleInitFunction());
    functions.put("module-load", new ModuleLoadFunction());
    functions.put("module-save", new ModuleSaveFunction());
    functions.put("module-delete", new ModuleDeleteFunction());
    functions.put("module-snapshots", new ModuleSnapshotsFunction());
    functions.put("module-start", new ModuleStartFunction());
    functions.put("module-stop", new ModuleStopFunction());
    functions.put("module-monitor", new ModuleMonitorFunction());
    functions.put("module-notify", new ModuleNotifyFunction());
    functions.put("remote-call", new RemoteCallFunction());

    // backward compatibility
    functions.put("module-publish", functions.get("module-notify"));    
  }  
}
