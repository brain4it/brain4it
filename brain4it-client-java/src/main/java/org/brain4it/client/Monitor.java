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
package org.brain4it.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.io.IOUtils;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.net.SSLUtils;
import static org.brain4it.server.ServerConstants.*;

/**
 * The Monitor class watch for changes of the value returned by exterior
 * functions of a module, and notifies these changes to the interested
 * listeners.
 *
 * @author realor
 */
public class Monitor
{
  private static final Listener[] EMPTY = new Listener[0];

  private final String serverUrl;
  private final String moduleName;
  private String accessKey;
  private String sessionId;
  private int connectionDelay;
  private int pollingInterval;
  private Map<String, Function> functions =
    Collections.<String, Function> emptyMap();
  private final HashMap<String, HashSet<Listener>> listeners;
  private Worker worker;
  private static final Logger LOGGER = Logger.getLogger("Monitor");

  public Monitor(String serverUrl, String moduleName)
  {
    this.serverUrl = serverUrl;
    this.moduleName = moduleName;
    this.connectionDelay = 100;
    this.pollingInterval = 0;
    this.listeners = new HashMap<String, HashSet<Listener>>();
  }

  public String getServerUrl()
  {
    return serverUrl;
  }

  public String getModuleName()
  {
    return moduleName;
  }

  public String getAccessKey()
  {
    return accessKey;
  }

  public void setAccessKey(String accessKey)
  {
    this.accessKey = accessKey;
  }

  public String getSessionId()
  {
    return sessionId;
  }

  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public int getConnectionDelay()
  {
    return connectionDelay;
  }

  public void setConnectionDelay(int connectionDelay)
  {
    this.connectionDelay = connectionDelay;
  }

  public int getPollingInterval()
  {
    return pollingInterval;
  }

  public void setPollingInterval(int pollingInterval)
  {
    this.pollingInterval = pollingInterval < 0 ? 0 : pollingInterval;
  }

  public Map<String, Function> getFunctions()
  {
    return functions;
  }

  public void setFunctions(Map<String, Function> functions)
  {
    this.functions = functions;
  }

  public Worker getWorker()
  {
    return worker;
  }

  public void setWorker(Worker worker)
  {
    this.worker = worker;
  }

  public synchronized void watch(String functionName, Listener listener)
  {
    // adds new listener for functionName
    watch(functionName, listener, false);
  }

  public synchronized void watch(String functionName, Listener listener,
    boolean replace)
  {
    // if replace is true this method replaces all the previous
    // functionName listeners by the new listener

    LOGGER.log(Level.FINEST, "Watch function: {0}", functionName);

    boolean newFunction;
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners == null)
    {
      newFunction = true;
      functionListeners = new HashSet<Listener>();
      listeners.put(functionName, functionListeners);
    }
    else
    {
      if (replace)
      {
        functionListeners.clear();
      }
      newFunction = false;
    }
    functionListeners.add(listener);
    if (newFunction) updateWorker(); // start or reconnect worked thread
  }

  public synchronized void unwatch(String functionName)
  {
    unwatch(functionName, null);
  }

  public synchronized void unwatch(String functionName, Listener listener)
  {
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners != null)
    {
      if (listener == null) // remove all listeners for that function
      {
        listeners.remove(functionName);
        LOGGER.log(Level.FINEST, "Unwatch function listeners: {0}",
          functionName);
        updateWorker(); // stop or reconnect
      }
      else if (functionListeners.remove(listener))
      {
        if (functionListeners.isEmpty())
        {
          listeners.remove(functionName);
        }
        LOGGER.log(Level.FINEST, "Unwatch function listener: {0}",
          functionName);
        updateWorker(); // stop or reconnect
      }
    }
  }

  public synchronized void unwatchAll()
  {
    LOGGER.log(Level.FINEST, "Unwatch all");
    listeners.clear();
    updateWorker(); // stop Worker thread
  }

  public synchronized Listener[] getListeners(String functionName)
  {
    HashSet<Listener> functionListeners = listeners.get(functionName);
    if (functionListeners != null)
    {
      return functionListeners.toArray(new Listener[functionListeners.size()]);
    }
    return EMPTY;
  }

  /**
   * Returns <tt>true</tt> if there are no listeners in this monitor
   *
   * @return <tt>true</tt> if there are no listeners in this monitor
   */
  public synchronized boolean isIdle()
  {
    return listeners.isEmpty();
  }

  protected synchronized void updateWorker()
  {
    if (listeners.isEmpty())
    {
      if (worker != null)
      {
        worker.end(); // stop Worker
        worker = null;
      }
    }
    else // Worker has work
    {
      if (worker == null)
      {
        worker = new Worker();
        worker.start(); // start new Worker
      }
      else
      {
        worker.cancel(); // force Worker reconnect
      }
    }
  }

  protected synchronized String getMonitoredFunctionNames()
  {
    BList list = new BList();
    for (String functionName : listeners.keySet())
    {
      list.add(functionName);
    }
    return Printer.toString(list);
  }

  protected class Worker extends Thread
  {
    boolean end;
    String monitorSessionId;

    @Override
    public void run()
    {
      setName("Monitor.Worker-" + getId());
      LOGGER.log(Level.FINE, "Monitor.Worker start");

      try
      {
        Thread.sleep(connectionDelay); // wait for new watches
      }
      catch (InterruptedException ex)
      {
      }

      while (!end)
      {
        try
        {
          if (serverUrl == null)
          {
            throw new RuntimeException("Invalid url");
          }
          URL url = new URL(serverUrl + "/" + moduleName);
          HttpURLConnection conn = (HttpURLConnection)url.openConnection();
          try
          {
            SSLUtils.skipCertificateValidation(conn);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty(MONITOR_HEADER,
              String.valueOf(pollingInterval));
            conn.setRequestProperty("Content-Type", BPL_MIMETYPE +
               "; charset=" + BPL_CHARSET);
            if (accessKey != null)
            {
              conn.setRequestProperty(ACCESS_KEY_HEADER, accessKey);
            }
            if (sessionId != null)
            {
              conn.setRequestProperty(SESSION_ID_HEADER, sessionId);
            }
            String functionNames = getMonitoredFunctionNames();
            OutputStream os = conn.getOutputStream();
            IOUtils.writeString(functionNames, BPL_CHARSET, os);

            conn.connect();

            BufferedReader reader = new BufferedReader(
              new InputStreamReader(conn.getInputStream(), BPL_CHARSET));
            try
            {
              String chunk = reader.readLine();
              // chunk is null when server sends a 0 size block
              while (chunk != null && !end)
              {
                processChunk(chunk);
                chunk = reader.readLine();
              }
            }
            finally
            {
              reader.close();
            }
          }
          finally
          {
            conn.disconnect();
          }
        }
        catch (ConnectException ex)
        {
          recover(ex);
        }
        catch (IOException ex)
        {
          // Cancelling a HTTPS connection throws a SocketException
        }
      }
      LOGGER.log(Level.FINE, "Monitor.Worker end");
    }

    private void processChunk(String chunk)
    {
      if (chunk.length() == 0) return;
      // empty chunk: the ping sent by the server at monitorPingTime intervals

      try
      {
        Object data = Parser.fromString(chunk, functions);

        if (data instanceof BList)
        {
          BList change = (BList)data;
          String functionName = (String)change.get(0);
          Object value = change.get(1);
          long serverTime = change.size() < 3 ? 0 :
            ((Number)change.get(2)).longValue();

          for (Listener listener : getListeners(functionName))
          {
            listener.onChange(functionName, value, serverTime);
          }
        }
        else if (data instanceof String)
        {
          monitorSessionId = (String)data;
          LOGGER.log(Level.FINE, "monitorSessionId: {0}", monitorSessionId);
        }
      }
      catch (ParseException ex)
      {
        LOGGER.log(Level.WARNING, "Bad data: {0}", chunk);
      }
    }

    private void recover(Exception ex)
    {
      LOGGER.log(Level.WARNING, "Connection error: {0}", ex.toString());
      try
      {
        // wait a 5 seconds and retry connection
        Thread.sleep(5000);
      }
      catch (InterruptedException iex)
      {
        // ignore exception and retry connection
      }
    }

    private void cancel()
    {
      // cancels the current monitoring session asynchronously
      Thread thread = new Thread()
      {
        @Override
        public void run()
        {
          try
          {
            // send unwatch request to server
            if (monitorSessionId != null)
            {
              LOGGER.log(Level.FINE, "Stopping monitor: {0}", monitorSessionId);

              URL url = new URL(serverUrl);
              HttpURLConnection conn = (HttpURLConnection)url.openConnection();
              SSLUtils.skipCertificateValidation(conn);
              conn.setDoInput(true);
              conn.setDoOutput(true);
              conn.setRequestMethod("POST");
              conn.setRequestProperty(MONITOR_HEADER, "0");
              conn.setRequestProperty("Content-Type", BPL_MIMETYPE);

              String data = "\"" + monitorSessionId + "\"";
              byte[] bytes = data.getBytes(BPL_CHARSET);
              OutputStream os = conn.getOutputStream();
              IOUtils.writeBytes(bytes, os);

              conn.connect();
              InputStream is = conn.getInputStream();
              IOUtils.readBytes(is); // read unwatch response
              monitorSessionId = null;
            }
          }
          catch (IOException ex)
          {
            // server down or IO error, worker will end sooner or later
            LOGGER.log(Level.WARNING, "Unwatch failure: {0}", ex.toString());
          }
        }
      };
      thread.start();
    }

    private void end()
    {
      end = true;
      cancel();
    }
  }

  public interface Listener
  {
    public void onChange(String functionName, Object value, long serverTime);
  }
}
