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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.brain4it.server.module.ModuleManager;
import org.brain4it.server.store.FileSystemStore;
import org.brain4it.server.store.Store;

/**
 *
 * @author realor
 */
public class Runner
{
  static 
  {
    System.setProperty("java.util.logging.manager", 
      ServerLogManager.class.getName());
  }  
  
  /* properties */
  public static final String PORT_PARAM = "port";
  public static final String SHUTDOWN_FILE_PARAM = "shutdownFile";
  public static final String ACCESS_KEY_PARAM = "accessKey";
  public static final String ACCESS_KEY_FILE_PARAM = "accessKeyFile";
  public static final String LIBRARIES_PARAM = "libraries";
  public static final String KEY_STORE_FILE_PARAM = "keyStoreFile";
  public static final String KEY_STORE_PASS_PARAM = "keyStorePass";
  public static final String KEY_STORE_TYPE_PARAM = "keyStoreType";
  public static final String KEY_PASS_PARAM = "keyPass";
  public static final String SSL_PROTOCOL_PARAM = "sslProtocol";
  public static final String STORE_CLASS_PARAM = "storeClass";
  public static final String MULTI_TENANT_PARAM = "multiTenant";
  public static final String MAX_WAIT_TIME_PARAM = "maxWaitTime";
  public static final String MONITOR_TIME_PARAM = "monitorTime";

  /* default values */
  public static final int DEFAULT_PORT_VALUE = 9999;
  public static final String DEFAULT_KEY_STORE_TYPE_VALUE = "PKCS12";
  public static final String DEFAULT_SSL_PROTOCOL_VALUE = "TLS";
  public static final String DEFAULT_STORE_CLASS_VALUE =
    FileSystemStore.class.getName();

  public static void loadProperties(String args[], Properties properties)
    throws IOException
  {
    for (String arg : args)
    {
      if (arg.startsWith("-"))
      {
        int index = arg.indexOf("=");
        String property;
        String value;
        if (index != -1)
        {
          property = arg.substring(1, index);
          value = arg.substring(index + 1);
        }
        else
        {
          property = arg.substring(1);
          value = "true";
        }
        properties.put(property, value);
      }
      else
      {
        File file = new File(arg);
        FileInputStream fis = new FileInputStream(file);
        try
        {
          properties.load(fis);
        }
        finally
        {
          fis.close();
        }
      }
    }
  }

  public static HttpServer createServer(Properties properties) throws Exception
  {    
    // set libraries
    String libraries = properties.getProperty(LIBRARIES_PARAM);

    // set network properties
    String value = properties.getProperty(PORT_PARAM);
    int port;
    try
    {
      port = (value == null) ? DEFAULT_PORT_VALUE : Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
      throw new NumberFormatException(PORT_PARAM + ": " + value);
    }

    boolean multiTenant =
      Boolean.valueOf(properties.getProperty(MULTI_TENANT_PARAM, "false"));

    // set SSL properties
    SslParameters sslParameters = null;
    String keyStoreFile = properties.getProperty(KEY_STORE_FILE_PARAM);
    if (keyStoreFile != null)
    {
      sslParameters = new SslParameters();
      sslParameters.setKeyStoreFile(keyStoreFile);
      sslParameters.setKeyStoreType(properties.getProperty(KEY_STORE_TYPE_PARAM, 
        DEFAULT_KEY_STORE_TYPE_VALUE));
      sslParameters.setSslProtocol(properties.getProperty(SSL_PROTOCOL_PARAM, 
        DEFAULT_SSL_PROTOCOL_VALUE));
      String keyStorePassword = properties.getProperty(KEY_STORE_PASS_PARAM);
      if (keyStorePassword != null)
      {
        sslParameters.setKeyStorePassword(keyStorePassword.toCharArray());
      }
      String keyPassword = properties.getProperty(KEY_PASS_PARAM);
      if (keyPassword == null)
      {
        keyPassword = keyStorePassword;
      }
      if (keyPassword != null)
      {
        sslParameters.setKeyPassword(keyPassword.toCharArray());
      }
    }

    // set store properties
    String storeClassname = properties.getProperty(STORE_CLASS_PARAM, 
      DEFAULT_STORE_CLASS_VALUE);
    Class<?> storeClass = Class.forName(storeClassname);
    Store store = (Store)storeClass.newInstance();
    store.init(properties);

    String accessKey = properties.getProperty(ACCESS_KEY_PARAM);

    String accessKeyFilename = properties.getProperty(ACCESS_KEY_FILE_PARAM);
    File accessKeyFile = accessKeyFilename == null ?
      null : new File(accessKeyFilename);

    ModuleManager moduleManager = new ModuleManager(HttpServer.SERVER_NAME,
      store, multiTenant, accessKey, accessKeyFile);

    moduleManager.addLibraries(libraries);
    HttpServer server = new HttpServer(moduleManager, port, sslParameters);

    value = properties.getProperty(MAX_WAIT_TIME_PARAM);
    if (value != null)
    {
      int maxWaitTime;
      try
      {
        maxWaitTime = Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
        throw new NumberFormatException(MAX_WAIT_TIME_PARAM + ": " + value);
      }
      server.getRestService().setMaxWaitTime(maxWaitTime);
    }

    value = properties.getProperty(MONITOR_TIME_PARAM);
    if (value != null)
    {
      int monitorTime;
      try
      {
        monitorTime = Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
        throw new NumberFormatException(MONITOR_TIME_PARAM + ": " + value);
      }
      server.getRestService().setMonitorTime(monitorTime);
    }
    return server;
  }

  public static void runServer(final HttpServer server, Properties properties)
    throws Exception
  {
    String shutdownFilename = properties.getProperty(SHUTDOWN_FILE_PARAM);
    if (shutdownFilename != null)
    {
      ShutdownFileMonitor monitor = new ShutdownFileMonitor(shutdownFilename);
      monitor.start();
    }
    
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        server.stop();
        ServerLogManager.resetFinally();
      }
    }));
    server.start();
  }
  
  public static void main(String args[]) throws Exception
  {
    Properties properties = new Properties();
    Runner.loadProperties(args, properties);
    HttpServer server = Runner.createServer(properties);
    Runner.runServer(server, properties);
  }
}
