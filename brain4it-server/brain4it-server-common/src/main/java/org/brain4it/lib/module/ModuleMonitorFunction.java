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
package org.brain4it.lib.module;

import java.util.Map;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.server.module.Module;

/**
 *
 * @author realor
 */
public class ModuleMonitorFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 4);
    String serverUrl = (String)context.evaluate(args.get(1));
    String moduleName = (String)context.evaluate(args.get(2));
    String functionName = (String)context.evaluate(args.get(3));
    final Object userFunction = (BList)context.evaluate(args.get(4));

    final Module module = (Module)context.getGlobalScope();

    boolean createMonitor = userFunction != null;
    Monitor monitor = module.getMonitor(serverUrl, moduleName, createMonitor);
    if (monitor == null) return null;
    
    Object value = context.evaluate(args.get("polling-interval"));
    if (value instanceof Number)
    {
      monitor.setPollingInterval(((Number)value).intValue());
    }
    
    value = context.evaluate(args.get("access-key"));
    if (value instanceof String)
    {
      monitor.setAccessKey((String)value);
    }
    
    if (userFunction == null)
    {
      monitor.unwatch(functionName); // force Monitor reconect
      if (monitor.isIdle())
      {
        module.removeMonitor(monitor.getServerUrl(), monitor.getModuleName());
      }
      return "unregistered";
    }
    else
    {
      monitor.watch(functionName, new Monitor.Listener()
      {
        @Override
        public void onChange(String functionName, Object value, 
          long serverTime)
        {
          Map<String, Function> functions = 
            module.getModuleManager().getFunctions();
          BList call = Utils.createFunctionCall(functions, userFunction, 
            functionName, value, serverTime);
          Executor.spawn(call, module, functions, null);
        }
      }, true);
      return "registered";
    }
  }
}
