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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.server.module.Module;
import org.brain4it.server.module.ModuleManager;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 */
public class MonitorService
{
  private int maxWaitTime = 300; // 300 seconds = 5 minutes
  private int monitorTime = 10; // 10 seconds
  
  private final ModuleManager moduleManager;
  private static final Logger LOGGER = Logger.getLogger("MonitorService");
  
  public MonitorService(ModuleManager moduleManager)
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

  public int getMonitorTime()
  {
    return monitorTime;
  }

  public void setMonitorTime(int monitorTime)
  {
    if (maxWaitTime <= 0)
      throw new IllegalArgumentException("monitorTime: " + monitorTime);

    this.monitorTime = monitorTime;
  }
  
  public void monitor(String path, BList exteriorFunctions,
    BList requestContext, int pollingInterval, PrintWriter writer) 
    throws Exception
  {
    LOGGER.log(Level.INFO, "Monitor start");
    
    PathParser parser = new PathParser(moduleManager, path);
    Module module = parser.getModule();

    if (module == null) throw new Exception("Module is required");

    ArrayList<String> functionNames = getFunctionNames(exteriorFunctions);
    final LinkedBlockingQueue<String> queue =
      new LinkedBlockingQueue(functionNames.size());
    Module.Listener listener = new Module.Listener()
    {
      @Override
      public void onChange(Module module, String functionName)
      {
        synchronized (queue)
        {
          if (!queue.contains(functionName))
          {
            queue.offer(functionName);
          }
        }
      }
    };
    // register module listeners
    for (String functionName : functionNames)
    {
      module.addListener(functionName, listener);
    }
    try
    {
      HashMap<String, Object> lastSentData = new HashMap<String, Object>();
      int monitorMillis = 1000 * monitorTime;
      if (pollingInterval <= 0) pollingInterval = monitorMillis;
      long waitMillis = Math.min(pollingInterval, monitorMillis);
      long lastSentMillis = 0;
      long lastPollMillis = 0;
      do
      {
        long nowMillis = System.currentTimeMillis();
        // Polling at pollingInterval milliseconds
        if (nowMillis - lastPollMillis > pollingInterval)
        {
          requestAllData(functionNames, queue);
          lastPollMillis = nowMillis;
        }
        // get functionName to invoke
        String functionName = queue.poll(waitMillis, TimeUnit.MILLISECONDS);
        if (functionName != null)
        {
          // send data if value returned by function has changed
          if (sendMonitorData(module, functionName, lastSentData,
            requestContext, writer))
          {
            lastSentMillis = System.currentTimeMillis();
          }
        }
        if (nowMillis - lastSentMillis > monitorMillis)
        {
          writer.println(); // send \n to check if connection is still alive
          lastSentMillis = System.currentTimeMillis();
        }
      } while (!writer.checkError()); // flush and check if client is gone
    }
    finally
    {
      // unregister module listeners
      for (String functionName : functionNames)
      {
        module.removeListener(functionName, listener);
      }
    }
    LOGGER.log(Level.INFO, "Monitor end");
  }
  
  private ArrayList<String> getFunctionNames(BList exteriorFunctions)
  {
    ArrayList<String> functionNames = new ArrayList<String>();
    for (int i = 0; i < exteriorFunctions.size(); i++)
    {
      Object element = exteriorFunctions.get(i);
      if (element instanceof String)
      {
        String functionName = (String)element;
        if (isExteriorFunction(functionName))
        {
          functionNames.add(functionName);
        }
      }
    }
    return functionNames;
  }

  private void requestAllData(ArrayList<String> functionNames,
    LinkedBlockingQueue<String> queue)
  {
    synchronized (queue)
    {
      for (String functionName : functionNames)
      {
        if (!queue.contains(functionName))
        {
          queue.offer(functionName);
        }
      }
    }
  }

  private boolean sendMonitorData(Module module, String functionName,
    HashMap<String, Object> lastSentData, BList requestContext, 
    PrintWriter writer) throws Exception
  {
    Object result = null;
    long serverTime = System.currentTimeMillis();
    boolean send;
    try
    {
      Map<String, Function> functions = moduleManager.getFunctions();
      BList code = Utils.createFunctionCall(functions, functionName, 
        requestContext);      
      result = Executor.execute(code, module, functions, maxWaitTime);
      Object last = lastSentData.get(functionName);
      send = !Utils.equals(result, last) ||
        !lastSentData.containsKey(functionName);
    }
    catch (Exception ex)
    {
      send = false;
    }
    if (send)
    {
      if (result instanceof BList) result = ((BList)result).clone(true);
      Printer printer = new Printer(writer);
      BList list = new BList(3);
      list.add(functionName);
      list.add(result);
      list.add(serverTime);
      printer.print(list);
      writer.println(); // \n is the block separator
      lastSentData.put(functionName, result);
    }
    return send;
  }

  private boolean isExteriorFunction(String functionName)
  {
    return functionName.startsWith(EXTERIOR_FUNCTION_PREFIX);
  }
}
