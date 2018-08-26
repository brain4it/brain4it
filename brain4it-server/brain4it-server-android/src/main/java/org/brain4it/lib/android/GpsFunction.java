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
package org.brain4it.lib.android;

import static android.content.Context.LOCATION_SERVICE;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;
import org.brain4it.server.module.Module;

/**
 *
 * @author realor
 */
public class GpsFunction extends AndroidFunction
{
  private final Map<String, Listener> listeners = Collections.synchronizedMap(
    new HashMap<String, Listener>());
  private LocationManager locationManager;
  private static final long MIN_TIME = 1000; // milliseconds
  private static final float MIN_DISTANCE = 1.0f; // meters
  private HandlerThread handlerThread;

  private static final Structure LOCATION_STRUCTURE =
    new Structure("provider", "timestamp", "longitude", "latitude",
      "altitude", "speed", "accuracy");

  public GpsFunction()
  {
    AndroidService service = AndroidService.getInstance();
    locationManager =
      (LocationManager)service.getSystemService(LOCATION_SERVICE);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);

    String operation = Utils.toString(args.get(1));

    Module module = getModule(context);

    if (operation.equals("register"))
    {
      BList func = (BList)context.evaluate(args.get(2));
      return registerListener(module, func);
    }
    else if (operation.equals("unregister"))
    {
      return unregisterListeners(module);
    }
    return null;
  }

  @Override
  public void cleanup()
  {
    unregisterListeners(null);
    locationManager = null;
  }
  
  private synchronized String registerListener(Module module, BList func)
  {
    Listener listener = listeners.get(module.getName());
    if (listener != null)
    {
      listener.userFunction = func;
    }
    else
    {
      if (handlerThread == null)
      {
        handlerThread = new HandlerThread("GPS handlerThread");
        handlerThread.start();
      }
      listener = new Listener(module, func);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        MIN_TIME, MIN_DISTANCE, listener, handlerThread.getLooper());
      listeners.put(module.getName(), listener);
    }
    return "registered";
  }

  private synchronized String unregisterListeners(Module module)
  {
    Iterator<Listener> iter = listeners.values().iterator();
    while (iter.hasNext())
    {
      Listener listener = iter.next();
      if (module == null || listener.module == module)
      {
        locationManager.removeUpdates(listener);
        iter.remove();
      }
    }
    if (listeners.isEmpty())
    {
      if (handlerThread != null)
      {
        handlerThread.quit();
        handlerThread = null;
      }
    }
    return "unregistered";
  }

  public class Listener implements LocationListener, Executor.Callback
  {
    private final Module module;
    private BList userFunction;
    private boolean waitingCallback;

    public Listener(Module module, BList userFunction)
    {
      this.module = module;
      this.userFunction = userFunction;
      waitingCallback = false;
    }

    @Override
    public void onLocationChanged(Location location)
    {
      try
      {
        // invoke listener function
        if (!waitingCallback)
        {
          Map<String, Function> functions = getFunctions();
          BList data = getLocationData(location);
          BList call = Utils.createFunctionCall(functions, userFunction, data);
          waitingCallback = true;
          Executor.spawn(call, module, functions, this);
        }
      }
      catch (Exception ex)
      {
        Log.e(TAG, ex.getMessage());
        // if module destroyed
        unregisterListeners(module);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onSuccess(Executor executor, Object result)
    {
      waitingCallback = false;
    }

    @Override
    public void onError(Executor executor, Exception ex)
    {
      waitingCallback = false;
    }

    private BList getLocationData(Location location)
    {
      BList data = new BList(LOCATION_STRUCTURE);
      data.put("provider", location.getProvider());
      data.put("timestamp", location.getTime());
      data.put("latitude", (double)location.getLatitude());
      data.put("longitude", (double)location.getLongitude());
      data.put("altitude", (double)location.getAltitude());
      data.put("speed", (double)location.getSpeed());
      data.put("accuracy", (double)location.getAccuracy());
      return data;
    }
  }
}
