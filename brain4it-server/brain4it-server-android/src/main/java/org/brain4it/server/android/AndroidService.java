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

package org.brain4it.server.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.os.Environment;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lib.AndroidLibrary;
import org.brain4it.server.module.ModuleManager;
import org.brain4it.server.store.FileSystemStore;
import org.brain4it.server.standalone.HttpServer;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 *
 * @author realor
 */
public class AndroidService extends Service
{
  public static final int DEFAULT_SERVER_PORT = 9999;
  private static AndroidService instance;
  private static final String TAG = "brain4it";
  private static final int NOTIFICATION_ID = 1;
  private static final Logger LOGGER = Logger.getLogger("HttpServer");
  private static final AndroidLogHandler LOG_HANDLER = new AndroidLogHandler();
  private AndroidHttpServer server;

  static
  {
    Logger.getLogger("HttpServer").setLevel(Level.FINE);
    Logger.getLogger("HttpServer").addHandler(LOG_HANDLER);
    Logger.getLogger("ModuleManager").setLevel(Level.FINE);
    Logger.getLogger("ModuleManager").addHandler(LOG_HANDLER);
  }
  
  public static AndroidService getInstance()
  {
    return instance;
  }
  
  public static AndroidLogHandler getLogHandler()
  {
    return LOG_HANDLER;
  }

  public AndroidHttpServer getHttpServer()
  {
    return server;
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    instance = this;
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          LOGGER.info("Initializing server...");
          FileSystemStore store = new FileSystemStore();
          File storageDir = Environment.getExternalStorageDirectory();
          Properties properties = new Properties();
          properties.setProperty(FileSystemStore.BASE_PATH,
            storageDir.getAbsolutePath() + "/brain4it_modules");
          store.init(properties);
          LOGGER.log(Level.INFO, "Using dir: {0}", store.getBasePath());

          SharedPreferences preferences =
            getSharedPreferences("org.brain4it.server", MODE_PRIVATE);
          int serverPort = preferences.getInt("serverPort", DEFAULT_SERVER_PORT);
          String accessKey = preferences.getString("accessKey", null);

          String startingText = getResources().getString(R.string.starting);
          Notification notification = createNotification(startingText);
          startForeground(NOTIFICATION_ID, notification);

          ModuleManager moduleManager =
            new ModuleManager("brain4it", store, accessKey);
          moduleManager.getLibraries().add(new AndroidLibrary());
          server = new AndroidHttpServer(moduleManager, serverPort);
          server.start();
          LOGGER.info("Initialization completed.");
        }
        catch (Exception ex)
        {
          LOGGER.log(Level.SEVERE, "Initialization failure", ex);
          stopSelf();
        }
      }
    };
    thread.start();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    return START_STICKY;
  }
  
  @Override
  public void onDestroy()
  {
    super.onDestroy();
    LOGGER.info("Stopping server...");
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          if (server != null)
          {
            server.stop();
            server = null;
          }
          instance = null;
          refreshActivity();
        }
        catch (Exception ex)
        {
          LOGGER.log(Level.SEVERE, "Stop failure", ex);             
        }
      }
    };
    thread.start();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  private Notification createNotification(String message)
  {
    String title = getResources().getString(R.string.appName);

    Intent targetIntent =
      new Intent(AndroidService.this, ServerActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(
      AndroidService.this, 0, targetIntent,
      PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Builder nb = new Notification.Builder(AndroidService.this);
    nb.setContentTitle(title);
    nb.setSmallIcon(R.drawable.notification);
    nb.setContentText(message);
    nb.setOngoing(true);
    nb.setContentIntent(contentIntent);

    return nb.build();
  }

  private void refreshActivity()
  {
    ServerActivity activity = ServerActivity.getInstance();
    if (activity != null)
    {
      activity.refresh();
    }
  }
  
  public class AndroidHttpServer extends HttpServer
  {
    private String address;

    public AndroidHttpServer(ModuleManager moduleManager, int port)
    {
      super(moduleManager, port);
    }

    @Override
    public String getAddress()
    {
      return address;
    }

    @Override
    protected void onServerListening()
    {
      super.onServerListening();
      
      address = getAddresses().get(0);

      String message = getResources().getString(R.string.listening) + 
        " " + address + ":" + getPort() + "...";

      Notification notification = createNotification(message);
      NotificationManager notificationManager =
        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
      notificationManager.notify(NOTIFICATION_ID, notification);

      refreshActivity();
    }

    @Override
    protected void onStop()
    {
      super.onStop();

      NotificationManager notificationManager =
        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
      notificationManager.cancel(NOTIFICATION_ID);
    }

    private ArrayList<String> getAddresses()
    {
      ArrayList<String> addresses = new ArrayList<String>();
      try
      {
        Enumeration<NetworkInterface> ifaces =
          NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements())
        {
          NetworkInterface iface = ifaces.nextElement();
          Enumeration<InetAddress> addrs = iface.getInetAddresses();
          while (addrs.hasMoreElements())
          {
            InetAddress addr = addrs.nextElement();
            if (!addr.isLoopbackAddress() && !addr.isMulticastAddress())
            {
              String hostAddress = addr.getHostAddress();
              if (!hostAddress.contains(":"))
              {
                addresses.add(hostAddress);
              }
              Log.i(TAG, hostAddress);
            }
          }
        }
      }
      catch (Exception ex)
      {
      }
      if (addresses.isEmpty())
      {
        addresses.add("127.0.0.1");
      }
      return addresses;
    }
  }
}
