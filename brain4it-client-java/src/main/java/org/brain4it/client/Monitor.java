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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
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
  private final String serverUrl;
  private final String moduleName;
  private String accessKey;
  private String sessionId;
  private int connectionDelay;
  private int pollingInterval;
  private final HashMap<String, HashSet<Listener>> listeners;
  private Worker worker;
  private static final Logger LOGGER = Logger.getLogger("Monitor");
  private static final Listener[] EMPTY = new Listener[0];

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

  public synchronized void watch(String functionName, Listener listener)
  {
    // adds new listener for functionName
    watch(functionName, listener, false);
  }  
  
  public synchronized void watch(String functionName, Listener listener, 
    boolean replace)
  {
    // if replace is true this method replaces all the previous listeners for
    // functionName by the new listener
    
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
    LOGGER.log(Level.FINEST, "Watch function: {0}", functionName);
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
   * Returns true if there is no listener registered
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
    Socket socket;
    BufferedInputStream input;
    BufferedWriter writer;
    
    @Override
    public void run()
    {
      setName("Monitor.Worker-" + getId());
      LOGGER.log(Level.INFO, "Monitor.Worker start");

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
          URL url = new URL(serverUrl);
          String host = url.getHost();
          int port = url.getPort();
          String path = url.getPath();
          String protocol = url.getProtocol();
          if (port == -1) port = "https".equals(protocol) ? 443 : 80;
          
          socket = createSocket(protocol, host, port);
          socket.setSoTimeout(0);
          writer = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), BPL_CHARSET));          
          try
          {
            writer.write("POST " + path + "/" + moduleName + " HTTP/1.1\r\n");
            setHeader("Host", host + ":" + port);
            setHeader(MONITOR_HEADER, pollingInterval);
            if (accessKey != null)
            {
              setHeader(ACCESS_KEY_HEADER, accessKey);
            }
            if (sessionId != null)
            {
              setHeader(SESSION_ID_HEADER, sessionId);
            }
            String functionNames = getMonitoredFunctionNames();
            setHeader("Content-Length", functionNames.length());
            setHeader("Content-Type", BPL_MIMETYPE +
              "; charset=" + BPL_CHARSET);
            writer.write("\r\n");
            writer.write(functionNames);
            writer.flush();
            
            input = new BufferedInputStream(socket.getInputStream());
            String header;
            try
            {
              do
              {
                header = readLine();
              } while (header.length() > 0);

              String chunk = readChunk();
              while (chunk != null && !end)
              {
                processChunk(chunk);
                chunk = readChunk();
              }
            }
            finally
            {
              input.close();
            }
          }
          finally
          {
            writer.close();
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
      LOGGER.log(Level.INFO, "Monitor.Worker end");
    }
    
    private String readLine() throws IOException
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      int b = input.read();
      while (b != 13 && b != 10 && b != -1) // !CR(13) and !LF(10) and !EOF(-1)
      {
        bos.write(b);
        b = input.read();
      }
      if (b == -1) return null;
      if (b == 13) input.read(); // read LF(10)
      return bos.toString(BPL_CHARSET);
    }

    private String readChunk() throws IOException
    {
      String length = readLine();
      if (length == null) return null;
      int chunkLength = Integer.parseInt(length, 16);
      if (chunkLength == 0) return null;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      while (chunkLength > 0)
      {
        int b = input.read();
        if (b == -1) return null;
        bos.write(b);
        chunkLength--;
      }
      input.read(); // read CR(13)
      input.read(); // read LF(10)
      return bos.toString(BPL_CHARSET);
    }
    
    private void processChunk(String chunk)
    {
      try
      {
        if (chunk.length() == 0) 
          return; // the server keep alive sent at monitorTime interval
        
        Object data = Parser.fromString(chunk);
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
      }
      catch (Exception ex)
      {
        // Ignore
      }
    }

    private Socket createSocket(String protocol, String host, int port) 
      throws IOException
    {
      if ("https".equals(protocol))
      {
        SSLSocketFactory ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
        return ssf.createSocket(host, port);    
      }
      else
      {
        return new Socket(host, port);
      }
    }

    private void setHeader(String header, Object value) throws IOException
    {
      writer.write(header + ": " + value + "\r\n");
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
      try
      {
        if (socket != null)
        {
          socket.close();
        }
      }
      catch (IOException ex)
      {
        // ignore
      }      
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

  public static void main(String[] args)
  {
    //Monitor monitor = new Monitor("http://localhost:9999", "select");
//    Monitor monitor = new Monitor("https://smartcity.santfeliu.cat/brain4it-server-web/modules", "lights");
    Monitor monitor = new Monitor("http://localhost:8084/brain4it-server-web/modules", "select");
    
    Listener listener =  new Listener()
    {
      @Override
      public void onChange(String functionName, Object value, long serverTime)
      {
        System.out.println(">>>[" + Printer.toString(value) + "]");
      }
    };

    monitor.watch("@display", listener);
    monitor.watch("@get-value", listener);
    monitor.watch("@get-options", listener); 
    
    try { Thread.sleep(40000); } catch (Exception ex) {};
    
    monitor.unwatchAll();    
  }
}
