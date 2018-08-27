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
package org.brain4it.server;

import java.io.IOException;
import org.brain4it.io.IOConstants;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.server.module.Module;
import org.brain4it.server.module.ModuleManager;

/**
 *
 * @author realor
 */
class PathParser
{
  // not multitenant path format: <moduleName>/<modulePath>
  // multitenant path format: <tenant>/<moduleName>/<modulePath>

  private final ModuleManager moduleManager;
  private String tenant;
  private String moduleName;
  private String modulePath;
  private BList pathList;

  public PathParser(ModuleManager moduleManager, String path)
  {
    this.moduleManager = moduleManager;
    // remove starting and ending slashes
    while (path.startsWith("/"))
    {
      path = path.substring(1);
    }
    while (path.endsWith("/"))
    {
      path = path.substring(0, path.length() - 1);
    }

    if (moduleManager.isMultiTenant())
    {
      int index = path.indexOf("/");
      if (index == -1 && path.length() > 0)
      {
        tenant = path;
        path = "";
      }
      else if (index > 0)
      {
        tenant = path.substring(0, index);
        path = path.substring(index + 1);
      }
    }

    int index = path.indexOf("/");
    if (index == -1 && path.length() > 0)
    {
      moduleName = path;
    }
    else if (index > 0)
    {
      moduleName = path.substring(0, index);
      modulePath = path.substring(index + 1).trim();
      if (modulePath.length() == 0)
      {
        modulePath = null;
      }
      else if (modulePath.charAt(0) == '"')
      {
        modulePath = IOConstants.PATH_REFERENCE_SEPARATOR + modulePath;
      }
    }
  }

  public String getTenant()
  {
    return tenant;
  }

  public String getModuleName()
  {
    return moduleName;
  }

  public String getModulePath()
  {
    return modulePath;
  }

  public Module getModule() throws IOException
  {
    return moduleManager.getModule(tenant, moduleName, true);
  }

  public BList getPathList()
  {
    if (pathList == null && modulePath != null)
    {
      pathList = BSoftReference.stringToPath(modulePath, true);
    }
    return pathList;
  }
}
