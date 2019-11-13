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

package org.brain4it.server.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.EOFException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import org.brain4it.lang.Executor;
import org.brain4it.server.MonitorService;
import org.brain4it.server.RestService;
import org.brain4it.server.module.ModuleManager;
/**
 *
 * @author realor
 */
public class HttpServer
{
  private final ModuleManager moduleManager;
  private final RestService restService;
  private final MonitorService monitorService;
  private final int port;
  private SslParameters sslParameters;
  private ServerSocket serverSocket;
  private Thread thread;
  private int keepAliveTime = 5; // 5 seconds
  private final Set<Socket> connections =
    Collections.synchronizedSet(new HashSet<Socket>());
  static final String SERVER_NAME = "Brain4it";
  private static final Logger LOGGER = Logger.getLogger("HttpServer");

  protected HttpServer(ModuleManager moduleManager, int port)
  {
    this.moduleManager = moduleManager;
    this.restService = new RestService(moduleManager);
    this.monitorService = new MonitorService(moduleManager);
    this.port = port;
  }

  protected HttpServer(ModuleManager moduleManager, int port,
    SslParameters sslParameters)
  {
    this(moduleManager, port);
    this.sslParameters = sslParameters;
  }

  public int getKeepAliveTime()
  {
    return keepAliveTime;
  }

  public void setKeepAliveTime(int keepAliveTime)
  {
    if (keepAliveTime <= 0)
      throw new IllegalArgumentException("keepAliveTime: " + keepAliveTime);

    this.keepAliveTime = keepAliveTime;
  }

  public ModuleManager getModuleManager()
  {
    return moduleManager;
  }

  public RestService getRestService()
  {
    return restService;
  }

  public MonitorService getMonitorService()
  {
    return monitorService;
  }

  public SslParameters getSslParameters()
  {
    return sslParameters;
  }

  public String getAddress()
  {
    return serverSocket.getInetAddress().getHostAddress();
  }

  public int getPort()
  {
    return port;
  }

  public synchronized void start() throws Exception
  {
    onStart();

    if (thread != null) return;

    Executor.init();

    moduleManager.init();

    serverSocket = createServerSocket(port);

    thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0,
          Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>());

        onServerListening();
        do
        {
          try
          {
            Socket socket = serverSocket.accept();
            connections.add(socket);
            threadPool.execute(new Handler(socket));
          }
          catch (IOException ex)
          {
            // ignore
          }
        } while (!serverSocket.isClosed());
        onServerShutdown();
        // shutdown HttpServer thread pool
        try
        {
          shutdownThreadPool(threadPool);
        }
        catch (Exception ex)
        {
          onError("Shutdown thread pool", ex);
        }
        // kill executors and save modules
        try
        {
          moduleManager.destroy();
        }
        catch (IOException ex)
        {
          onError("Destroy module manager", ex);
        }
        // shutdown Executor thread pool
        try
        {
          Executor.shutdown();
        }
        catch (Exception ex)
        {
          onError("Shutdown executors", ex);
        }
        onStop();
      }
    });
    thread.setName(SERVER_NAME + " server");
    thread.start();
  }

  public synchronized void stop()
  {
    if (thread == null)
    {
      // already stopped
      onStop();
    }
    else
    {
      closeConnections();
      closeServerSocket();
      waitForThread();
    }
  }

  public boolean isRunning()
  {
    return thread != null;
  }

  protected ServerSocket createServerSocket(int port) throws Exception
  {
    if (sslParameters == null)
    {
      return new ServerSocket(port);
    }
    else
    {
      KeyStore keyStore = KeyStore.getInstance(sslParameters.getKeyStoreType());
      keyStore.load(new FileInputStream(sslParameters.getKeyStoreFile()),
        sslParameters.getKeyStorePassword());
      KeyManagerFactory kmf =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, sslParameters.getKeyPassword());
      final SSLContext sc =
        SSLContext.getInstance(sslParameters.getSslProtocol());
      sc.init(kmf.getKeyManagers(), null, null);
      SSLServerSocketFactory ssocketFactory = sc.getServerSocketFactory();
      return ssocketFactory.createServerSocket(port);
    }
  }

  protected void waitForThread()
  {
    try
    {
      thread.join();
    }
    catch (InterruptedException ex)
    {
      // ignore
    }
    finally
    {
      thread = null;
    }
  }

  protected void closeServerSocket()
  {
    if (serverSocket != null)
    {
      try
      {
        serverSocket.close();
      }
      catch (IOException ex)
      {
        // ignore
      }
    }
  }

  protected void closeConnections()
  {
    Socket[] sockets = connections.toArray(new Socket[0]);
    for (Socket socket : sockets)
    {
      try
      {
        socket.close();
      }
      catch (IOException ex)
      {
        // ignore
      }
    }
  }

  protected void shutdownThreadPool(ThreadPoolExecutor threadPool)
  {
    threadPool.shutdown();
    while (!threadPool.isTerminated())
    {
      try
      {
        threadPool.awaitTermination(100, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e)
      {
        return;
      }
      threadPool.shutdownNow();
    }
  }

  class Handler implements Runnable
  {
    private final Socket socket;

    public Handler(Socket socket)
    {
      this.socket = socket;
    }

    @Override
    public void run()
    {
      try
      {
        LOGGER.log(Level.FINEST,
          "New handler on socket {0}", socket.getPort());
        socket.setSoTimeout(keepAliveTime * 1000);
        boolean keepAlive = true;
        while (keepAlive)
        {
          HttpRequest request = new HttpRequest(socket);
          HttpResponse response = new HttpResponse(socket);
          try
          {
            request.read(); // may throw: BadRequestException,
            // EOFException, SocketTimeoutException or IOException

            LOGGER.log(Level.FINEST, "{0} {1} on socket {2}", new Object[]{
              request.getMethod(), request.getPath(), socket.getPort()});

            SAHttpDispatcher dispatcher = new SAHttpDispatcher(
              request, response, restService, monitorService);
            onServe(request, response);
            // dispatch request and generate response
            dispatcher.dispatch();
            // flush buffered response
            response.finish();
            keepAlive = request.isKeepAlive();
          }
          catch (BadRequestException ex)
          {
            // Bad HTTP request
            LOGGER.log(Level.FINEST, "Bad request on socket {0}",
              socket.getPort());
            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            response.setStatusMessage("BAD_REQUEST");
            response.finish();
            keepAlive = false;
          }
          catch (EOFException ex)
          {
            // client has gone
            LOGGER.log(Level.FINEST, "EOF on socket {0}", socket.getPort());
            keepAlive = false;
          }
          catch (SocketTimeoutException ex)
          {
            // no more requests in keepAliveTime
            LOGGER.log(Level.FINEST, "Timeout on socket {0}", socket.getPort());
            keepAlive = false;
          }
          catch (IOException ex)
          {
            // IO error: SocketException
            LOGGER.log(Level.FINEST, "I/O Error on socket {0}",
              socket.getPort());
            keepAlive = false;
          }
        }
      }
      catch (IOException ex)
      {
        // ignore
      }
      finally
      {
        try
        {
          LOGGER.log(Level.FINEST, "Close socket {0}", socket.getPort());
          socket.close();
        }
        catch (IOException ex)
        {
          // ignore
        }
        finally
        {
          connections.remove(socket);
        }
      }
    }
  }

  protected void onStart()
  {
    LOGGER.log(Level.INFO, "Starting server");
  }

  protected void onServerListening()
  {
    LOGGER.log(Level.INFO, "Server listening on port {0}", new Object[]{port});
  }

  protected void onServerShutdown()
  {
    LOGGER.log(Level.INFO, "Server shutdown");
  }

  protected void onStop()
  {
    LOGGER.log(Level.INFO, "Server stopped.");
  }

  protected void onServe(HttpRequest request, HttpResponse response)
  {
  }

  protected void onError(String task, Exception ex)
  {
    LOGGER.log(Level.SEVERE, "{0}: {1}", new Object[]{task, ex.toString()});
  }
}
