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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lang.Executor;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
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
    LOGGER.log(Level.FINEST, "path: {0}", path);
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
    LOGGER.log(Level.FINEST, "path: {0} value: {1}", new Object[]{path, value});    
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
        BList data = null;
        if (value instanceof BList)
        {
          data = (BList)value;
        }
        Module module =
          moduleManager.getModule(parser.getTenant(), moduleName, false);
        if (module == null)
        {
          // module creation
          moduleManager.createModule(parser.getTenant(), moduleName, data);
          result = "Module " + moduleName + " created.";
        }
        else
        {
          // module update
          if (data == null)
            throw new Exception("Module " + moduleName + " already exists.");

          module.init(data);
          result = "Module " + moduleName + " modified.";
        }
      }
      else
      {
        Module module = parser.getModule();
        result = module.put(pathList, value);
      }
    }
    return result;
  }

  public Object delete(String path, String accessKey) throws Exception
  {
    LOGGER.log(Level.FINEST, "path: {0}", path);
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
    LOGGER.log(Level.FINEST, "path: {0} data: {1}", new Object[]{path, data});
    Object result;
    PathParser parser = new PathParser(moduleManager, path);
    String moduleName = parser.getModuleName();

    if (moduleName == null)
    {
      result = null;
    }
    else
    {
      Object code = null;
      Module module = parser.getModule();
      String functionName = parser.getModulePath();
      Map<String, Function> functions = moduleManager.getFunctions();
      if (functionName == null)
      {
        // execute any code
        checkSecurity(parser, accessKey);
        code = data;
      }
      else if (isExteriorFunction(functionName))
      {
        // call exterior function passing header and data as arguments        
        code = Utils.createFunctionCall(functions, functionName,
          requestContext, data);
      }
      if (code == null) result = null;
      else result = Executor.execute(code, module, functions, maxWaitTime);
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
      catch (Exception ex)
      {
        // module not created yet
      }
    }
    throw new SecurityException("Operation not authorized.");
  }

  private boolean isExteriorFunction(String functionName)
  {
    return functionName.startsWith(EXTERIOR_FUNCTION_PREFIX);
  }
}
