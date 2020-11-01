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

package org.brain4it.server.module;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.brain4it.client.Monitor;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BHardReference;
import org.brain4it.lang.Executor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BPathReference;
import org.brain4it.lang.BReference;
import org.brain4it.lang.BSingleReference;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.server.store.Store;
import org.brain4it.server.store.Entry;
import static org.brain4it.server.store.Store.*;
import static org.brain4it.server.ServerConstants.*;
import static org.brain4it.io.IOConstants.CALL_FUNCTION_NAME;
import static org.brain4it.io.IOConstants.QUOTE_FUNCTION_NAME;

/**
 *
 * @author realor
 */
public final class Module extends BList
{
  public static final String DEFAULT_SNAPSHOT = "head";
  public static final String SNAPSHOT_EXTENSION = ".snp";
  public static final String SNAPSHOT_VERSION_SEPARATOR = "_";
  public static final String SNAPSHOT_VERSION_DATE_FORMAT =
    "yyyyMMddHHmmss";
  public static final int MAX_WAIT_TIME = 60;
  public static final String CHARSET_ENCODING = "UTF-8";

  private final ModuleManager moduleManager;
  private final String tenant;
  private final String name;
  private final HashMap<String, HashSet<Listener>> listeners;
  private final HashMap<String, Monitor> monitors;

  private static BHardReference callReference;
  private static BHardReference quoteReference;

  Module(ModuleManager moduleManager, String tenant, String name)
  {
    this.moduleManager = moduleManager;
    this.tenant = tenant;
    this.name = name;
    this.listeners = new HashMap<String, HashSet<Listener>>();
    this.monitors = new HashMap<String, Monitor>();
  }

  public ModuleManager getModuleManager()
  {
    return moduleManager;
  }

  public String getTenant()
  {
    return tenant;
  }

  public String getName()
  {
    return name;
  }

  public String getFullName()
  {
    return tenant == null ? name : tenant + PATH_SEPARATOR + name;
  }

  public void init()
  {
    init(null);
  }

  public void init(BList data)
  {
    removeAll();
    if (data != null)
    {
      HashSet<BList> visited = new HashSet<BList>();
      changeBaseList(data, data, visited);
      addAll(data);
    }
  }

  public void loadSnapshot() throws Exception
  {
    loadSnapshot(null);
  }

  public void loadSnapshot(String snapshot) throws Exception
  {
    // Warn: Executors running at this point!
    if (snapshot == null)
    {
      snapshot = DEFAULT_SNAPSHOT;
    }
    Store store = moduleManager.getStore();
    String path = getSnapshotPath(snapshot);
    InputStreamReader reader =
      new InputStreamReader(store.readEntry(path), CHARSET_ENCODING);
    try
    {
      Parser parser = new Parser(reader, moduleManager.getFunctions());
      BList scope = (BList)parser.parse();
      init(scope);
    }
    finally
    {
      reader.close();
    }
  }

  public void saveSnapshot() throws Exception
  {
    saveSnapshot(null, false);
  }

  public void saveSnapshot(String snapshot) throws Exception
  {
    saveSnapshot(snapshot, false);
  }

  public void saveSnapshot(boolean backup) throws Exception
  {
    saveSnapshot(null, backup);
  }

  public void saveSnapshot(String snapshot, boolean backup) throws Exception
  {
    Store store = moduleManager.getStore();
    if (snapshot == null)
    {
      snapshot = DEFAULT_SNAPSHOT;
    }
    String path = getSnapshotPath(snapshot);
    if (backup)
    {
      Entry entry = store.getEntry(path);
      if (entry != null)
      {
        SimpleDateFormat df =
          new SimpleDateFormat(SNAPSHOT_VERSION_DATE_FORMAT);
        String newName = snapshot + SNAPSHOT_VERSION_SEPARATOR +
          df.format(new Date(entry.getLastModified()));
        store.renameEntry(getFullName(), snapshot + SNAPSHOT_EXTENSION,
          newName + SNAPSHOT_EXTENSION);
      }
    }
    OutputStreamWriter writer =
      new OutputStreamWriter(store.writeEntry(path), CHARSET_ENCODING);
    try
    {
      Printer printer = new Printer(writer);
      printer.print(this);
    }
    finally
    {
      writer.close();
    }
  }

  public void deleteSnapshots(String pattern) throws Exception
  {
    if (pattern == null)
    {
      pattern = String.valueOf(WILDCARD);
    }
    Store store = moduleManager.getStore();
    String path = getFullName();
    List<Entry> entries = store.listEntries(path, pattern + SNAPSHOT_EXTENSION);
    for (Entry entry : entries)
    {
      store.deleteEntry(entry.getPath());
    }
  }

  public List<Entry> getSnapshots() throws IOException
  {
    return getSnapshots(null);
  }

  public List<Entry> getSnapshots(String pattern) throws IOException
  {
    if (pattern == null)
    {
      pattern = String.valueOf(WILDCARD);
    }
    Store store = moduleManager.getStore();
    return store.listEntries(getFullName(), pattern + SNAPSHOT_EXTENSION);
  }

  public void start() throws Exception
  {
    try
    {
      Object code = get(MODULE_START_VAR);
      if (code != null)
      {
        Executor.execute(code, this,
         moduleManager.getFunctions(), MAX_WAIT_TIME);
      }
    }
    catch (Exception ex)
    {
      throw new Exception("Start failed: " + ex.toString(), ex);
    }
  }

  public void stop() throws Exception
  {
    try
    {
      removeMonitors();

      Object code = get(MODULE_STOP_VAR);
      if (code != null)
      {
        Executor.execute(code, this,
          moduleManager.getFunctions(), MAX_WAIT_TIME);
      }
    }
    catch (Exception ex)
    {
      throw new Exception("Stop failed: " + ex.toString(), ex);
    }
    finally
    {
      Executor.killAll(this);
    }
  }

  public synchronized void addListener(String functionName, Listener listener)
  {
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners == null)
    {
      functionListeners = new HashSet<Listener>();
      listeners.put(functionName, functionListeners);
      functionListeners.add(listener);
    }
    else if (!functionListeners.contains(listener))
    {
      functionListeners.add(listener);
    }
  }

  public synchronized void removeListener(String functionName,
    Listener listener)
  {
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners != null)
    {
      functionListeners.remove(listener);
    }
  }

  public synchronized Listener[] getListeners(String functionName)
  {
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners == null) return new Listener[0];
    return functionListeners.toArray(new Listener[functionListeners.size()]);
  }

  public int notifyChange(String functionName)
  {
    Listener[] functionListeners = getListeners(functionName);
    for (Listener listener : functionListeners)
    {
      listener.onChange(this, functionName);
    }
    return functionListeners.length;
  }

  public synchronized Monitor getMonitor(String serverUrl, String moduleName,
    boolean create)
  {
    String target = serverUrl + "/" + moduleName;
    Monitor monitor = monitors.get(target);
    if (monitor == null && create)
    {
      monitor = new Monitor(serverUrl, moduleName);
      monitor.setFunctions(moduleManager.getFunctions());
      monitors.put(target, monitor);
    }
    return monitor;
  }

  public synchronized void removeMonitor(String serverUrl, String moduleName)
  {
    String target = serverUrl + "/" + moduleName;
    Monitor monitor = monitors.get(target);
    if (monitor != null)
    {
      monitor.unwatchAll();
      monitors.remove(target);
    }
  }

  public synchronized void removeMonitors()
  {
    // stop all monitors
    for (Monitor monitor : monitors.values())
    {
      monitor.unwatchAll();
    }
    monitors.clear();
  }

  @Override
  public String toString()
  {
    return getFullName();
  }

  /**
   * Creates the code to call an exterior function
   *
   * @param functionName the function name or function path to execute
   * @param requestContext the request context
   * @param data the function data argument
   * @return the code to call the exterior function or null if functionName is
   * not an exterior function. An exterior function must have a name that starts
   * with @ or any of its parent lists must have a name that starts with @.
   */
  public BList createExteriorFunctionCall(String functionName,
    BList requestContext, Object data)
  {
    try
    {
      Object userFunction = null;
      BSoftReference reference = BSoftReference.getInstance(functionName);
      if (reference instanceof BSingleReference)
      {
        String referenceName = reference.getName();
        if (referenceName.startsWith(EXTERIOR_FUNCTION_PREFIX))
        {
          userFunction = get(referenceName);
        }
      }
      else if (reference instanceof BPathReference)
      {
        BList path = ((BPathReference)reference).getInternalPath();
        for (int i = 0; i < path.size(); i++)
        {
          Object elem = path.get(i);
          if (elem instanceof String)
          {
            String referenceName = (String)elem;
            if (referenceName.startsWith(EXTERIOR_FUNCTION_PREFIX))
            {
              userFunction = get(path);
              break;
            }
          }
        }
      }

      if (!Utils.isUserFunction(userFunction)) return null;

      if (callReference == null)
      {
        Function callFunction =
          moduleManager.getFunctions().get(CALL_FUNCTION_NAME);
        if (callFunction == null) return null;
        callReference = new BHardReference(CALL_FUNCTION_NAME, callFunction);
      }

      if (quoteReference == null)
      {
        Function quoteFunction =
          moduleManager.getFunctions().get(QUOTE_FUNCTION_NAME);
        if (quoteFunction == null) return null;
        quoteReference = new BHardReference(QUOTE_FUNCTION_NAME, quoteFunction);
      }

      BList call = new BList();
      call.add(callReference);
      call.add(userFunction);
      call.add(requestContext);

      if (data instanceof BList || data instanceof BReference)
      {
        BList quotedData = new BList();
        quotedData.add(quoteReference);
        quotedData.add(data);
        call.add(quotedData);
      }
      else if (data != null)
      {
        call.add(data);
      }
      return call;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  /* private methods */

  private void changeBaseList(BList list, BList base, HashSet<BList> visited)
  {
    if (!visited.contains(list))
    {
      visited.add(list);

      for (int i = 0; i < list.size(); i++)
      {
        Object value = list.get(i);
        if (value instanceof BList)
        {
          BList child = (BList)value;
          if (child == base)
          {
            list.put(i, this);
          }
          else
          {
            changeBaseList(child, base, visited);
          }
        }
      }
    }
  }

  private String getSnapshotPath(String snapshot)
  {
    return getFullName() + PATH_SEPARATOR + snapshot + SNAPSHOT_EXTENSION;
  }

  public interface Listener
  {
    public void onChange(Module module, String functionName);
  }
}
