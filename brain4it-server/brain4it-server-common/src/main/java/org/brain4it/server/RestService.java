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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lang.BException;
import org.brain4it.lang.Executor;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.server.module.Module;
import org.brain4it.server.module.ModuleManager;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 */
public class RestService
{
  private int maxWaitTime = 300; // 300 seconds = 5 minutes
  private static final Logger LOGGER = Logger.getLogger("RestService");

  private final ModuleManager moduleManager;

  public RestService(ModuleManager moduleManager)
  {
    this.moduleManager = moduleManager;
  }

  public ModuleManager getModuleManager()
  {
    return moduleManager;
  }

  public int getMaxWaitTime()
  {
    return maxWaitTime;
  }

  public void setMaxWaitTime(int maxWaitTime)
  {
    if (maxWaitTime <= 0)
      throw new IllegalArgumentException("maxWaitTime: " + maxWaitTime);

    this.maxWaitTime = maxWaitTime;
  }

  public Object get(String path, String accessKey) throws Exception
  {
    LOGGER.log(Level.FINE, "path: {0}", path);
    Object result;
    PathParser parser = new PathParser(moduleManager, path);

    String moduleName = parser.getModuleName();
    if (moduleName == null)
    {
      result = moduleManager.listModules(parser.getTenant());
    }
    else
    {
      if (!MODULE_METADATA_VAR.equals(parser.getModulePath()))
      {
        checkSecurity(parser, accessKey);
      }
      Module module = parser.getModule();
      BList pathList = parser.getPathList();
      if (pathList == null)
      {
        result = module;
      }
      else
      {
        result = module.get(pathList);
      }
    }
    return result;
  }

  public Object put(String path, Object value, String accessKey)
    throws Exception
  {
    LOGGER.log(Level.FINE, "path: {0} value: {1}", new Object[]{path, value});
    Object result;
    PathParser parser = new PathParser(moduleManager, path);
    checkSecurity(parser, accessKey);

    String moduleName = parser.getModuleName();
    if (moduleName == null)
    {
      result = null;
    }
    else
    {
      BList pathList = parser.getPathList();
      if (pathList == null)
      {
        // module creation or update
        BList data = null;
        if (value instanceof BList)
        {
          data = (BList)value;
        }
        String tenant = parser.getTenant();
        Module module = moduleManager.getModule(tenant, moduleName, false);
        if (module == null)
        {
          // create module because it do not exists
          module = moduleManager.createModule(tenant, moduleName, data);
          result = "Module " + moduleName + " created.";
        }
        else
        {
          // update module because it already exists

          // data is null when the manager app attempts to create a module
          // so must throw an Exception since the module already exists
          if (data == null)
            throw new Exception("Module " + moduleName + " already exists.");

          // load data in the module
          module.init(data);
          result = "Module " + moduleName + " modified.";
        }
        // set access key variable to secure the module
        if (accessKey != null)
        {
          module.put(MODULE_ACCESS_KEY_VAR, accessKey);
        }
      }
      else
      {
        // update element referenced by path
        Module module = parser.getModule();
        result = module.put(pathList, value);
      }
    }
    return result;
  }

  public Object delete(String path, String accessKey) throws Exception
  {
    LOGGER.log(Level.FINE, "path: {0}", path);
    Object result;
    PathParser parser = new PathParser(moduleManager, path);
    checkSecurity(parser, accessKey);

    String moduleName = parser.getModuleName();
    if (moduleName == null)
    {
      result = null;
    }
    else
    {
      BList pathList = parser.getPathList();
      if (pathList == null)
      {
        moduleManager.destroyModule(parser.getTenant(), moduleName);
        result = "Module " + moduleName + " destroyed.";
      }
      else
      {
        Module module = parser.getModule();
        checkSecurity(parser, accessKey);
        result = module.remove(pathList);
      }
    }
    return result;
  }

  public Object execute(String path, Object data, String accessKey,
    BList requestContext) throws Exception
  {
    LOGGER.log(Level.FINE, "path: {0} code: {1}", new Object[]{path, data});
    Object result;
    PathParser parser = new PathParser(moduleManager, path);
    String moduleName = parser.getModuleName();

    if (moduleName == null)
    {
      result = null;
    }
    else
    {
      Module module = parser.getModule();
      String functionName = parser.getModulePath();
      Map<String, Function> functions = moduleManager.getFunctions();
      if (functionName == null)
      {
        // execute data as code
        checkSecurity(parser, accessKey);
        result = Executor.execute(data, module, functions, maxWaitTime);
      }
      else
      {
        // call exterior function passing requestContext and data as arguments
        try
        {
          BList code = module.createExteriorFunctionCall(functionName,
            requestContext, data);
          result = code == null ? null :
            Executor.execute(code, module, functions, maxWaitTime);
        }
        catch (BException ex)
        {
          // an exterior function call is not always trusted so
          // we remove the source information (no code or stack)
          throw ex.removeSourceInfo();
        }
      }
    }
    return result;
  }

  private void checkSecurity(PathParser parser, String accessKey)
    throws Exception
  {
    String serverAccessKey = moduleManager.isMultiTenant() ?
      moduleManager.getAccessKey(parser.getTenant()) :
      moduleManager.getAccessKey();

    if (serverAccessKey == null ||
        serverAccessKey.equals(accessKey)) return;

    if (parser.getModuleName() != null)
    {
      try
      {
        Module module = parser.getModule();
        Object value = module.get(MODULE_ACCESS_KEY_VAR);
        if (value == null) return;

        String moduleAccessKey = String.valueOf(value);
        if (moduleAccessKey.equals(accessKey)) return;
      }
      catch (IOException ex)
      {
        // module not created yet
      }
    }
    throw new SecurityException("Operation not authorized.");
  }
}
