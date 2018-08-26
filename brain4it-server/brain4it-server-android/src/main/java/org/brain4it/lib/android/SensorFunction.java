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

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_PRESSURE;
import static android.hardware.Sensor.TYPE_ROTATION_VECTOR;
import static android.hardware.Sensor.TYPE_PROXIMITY;
import static android.hardware.Sensor.TYPE_ALL;
import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_GRAVITY;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.brain4it.lang.BException;
import org.brain4it.lang.Context;
import org.brain4it.lang.Executor;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;
import org.brain4it.server.module.Module;

/**
 *
 * @author realor
 */
public class SensorFunction extends AndroidFunction
{
  public static final float PI = 3.14159265359f;
	public static final float RADIANS_TO_DEGREES = 180.0f / PI;
  
  private final Map<String, Listener> listeners = Collections.synchronizedMap(
    new HashMap<String, Listener>());
  private SensorManager sensorManager;

  private static final Structure DEFAULT_STRUCTURE = 
    new Structure("type", "timestamp", "value");
  private static final Structure XYZ_STRUCTURE = 
    new Structure("type", "timestamp", "x", "y", "z");
  private static final Structure ROTATION_STRUCTURE = 
    new Structure("type", "timestamp", "yaw", "pitch", "roll");

  private static final SensorType[] SENSOR_TYPES = new SensorType[]
  {
    new SensorType(TYPE_ACCELEROMETER, "accelerometer", XYZ_STRUCTURE),
    new SensorType(TYPE_GYROSCOPE, "gyroscope", XYZ_STRUCTURE),
    new SensorType(TYPE_MAGNETIC_FIELD, "magnetic", XYZ_STRUCTURE),
    new SensorType(TYPE_GRAVITY, "gravity", XYZ_STRUCTURE),
    new SensorType(TYPE_ROTATION_VECTOR, "rotation", ROTATION_STRUCTURE),
    new SensorType(TYPE_LIGHT, "light"),
    new SensorType(TYPE_PRESSURE, "pressure"),
    new SensorType(TYPE_PROXIMITY, "proximity"),
    new SensorType(TYPE_AMBIENT_TEMPERATURE, "temperature"),
    new SensorType(TYPE_RELATIVE_HUMIDITY, "humidity")
  };

  public SensorFunction()
  {
    AndroidService service = AndroidService.getInstance();
    sensorManager =
      (SensorManager)service.getSystemService(SENSOR_SERVICE);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    
    String operation = Utils.toString(args.get(1));

    Module module = getModule(context);

    if ("register".equals(operation))
    {
      String typeName = (String)context.evaluate(args.get(2));
      BList func = (BList)context.evaluate(args.get(3));
      return registerListener(module, typeName, func);
    }
    else if ("unregister".equals(operation))
    {
      if (args.size() > 2)
      {
        String typeName = (String)context.evaluate(args.get(2));
        return unregisterListener(module, typeName);
      }
      else
      {
        return unregisterListeners(module);
      }
    }
    else if ("listeners".equals(operation))
    {
      return getListeners(module);
    }
    else if ("types".equals(operation))
    {
      return getSensorTypes();
    }
    return null;
  }

  @Override
  public void cleanup()
  {
    unregisterListeners(null);
    sensorManager = null;
  }

  private SensorType getSensorType(int type)
  {
    int i = 0;
    while (i < SENSOR_TYPES.length && SENSOR_TYPES[i].type != type)
    {
      i++;
    }
    return i == SENSOR_TYPES.length ? null : SENSOR_TYPES[i];
  }
  
  private SensorType getSensorType(String typeName)
  {
    int i = 0;
    while (i < SENSOR_TYPES.length && 
           !SENSOR_TYPES[i].typeName.equals(typeName))
    {
      i++;
    }
    return i == SENSOR_TYPES.length ? null : SENSOR_TYPES[i];
  }

  private synchronized String registerListener(
    Module module, String typeName, BList func) throws Exception
  {
    String listenerId = getListenerId(module, typeName);
    Listener listener = listeners.get(listenerId);
    if (listener != null)
    {
      listener.userFunction = func;
      return "registered";
    }
    else
    {
      SensorType sensorType = getSensorType(typeName);
      if (sensorType != null)
      {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType.type);
        if (sensor != null)
        {
          listener = new Listener(module, sensorType, func);
          sensorManager.registerListener(listener, sensor, SENSOR_DELAY_FASTEST);
          listeners.put(listenerId, listener);
          return "registered";
        }
        throw new BException("SensorNotAvaliable", typeName);
      }
      throw new BException("UnsupportedSensorType", typeName);
    }
  }

  private synchronized String unregisterListener(Module module, String typeName)
  {
    String listenerId = getListenerId(module, typeName);
    Listener listener = listeners.remove(listenerId);
    if (listener != null)
    {
      sensorManager.unregisterListener(listener);
      return "unregistered";
    }
    return "not found";
  }

  private synchronized String unregisterListeners(Module module)
  {
    Iterator<Listener> iter = listeners.values().iterator();
    while (iter.hasNext())
    {
      Listener listener = iter.next();
      if (module == null || listener.module == module)
      {
        sensorManager.unregisterListener(listener);
        iter.remove();
      }
    }
    return "unregistered";
  }

  private synchronized BList getListeners(Module module)
  {
    BList list = new BList();
    Iterator<Listener> iter = listeners.values().iterator();
    while (iter.hasNext())
    {
      Listener listener = iter.next();
      if (module == null || listener.module == module)
      {
        list.add(listener.sensorType.typeName);
      }
    }
    return list;
  }
  
  private String getListenerId(Module module, String typeName)
  {
    return module.getName() + " " + typeName;
  }
  
  private synchronized BList getSensorTypes()
  {
    HashSet<String> typeNames = new HashSet<String>();
    List<Sensor> sensorList = sensorManager.getSensorList(TYPE_ALL);
    for (Sensor sensor : sensorList)
    {
      int type = sensor.getType();
      SensorType sensorType = getSensorType(type);
      if (sensorType != null)
      {
        typeNames.add(sensorType.typeName);
      }
    }
    BList list = new BList();
    for (String typeName : typeNames)
    {
      list.add(typeName);
    }
    return list;
  }
  
  public class Listener implements SensorEventListener, Executor.Callback
  {
    private final Module module;
    private final SensorType sensorType;
    private BList userFunction;
    private boolean waitingCallback;
    private float[] rotationVector;
    private float[] rotationMatrix;

    public Listener(Module module, SensorType sensorType, BList userFunction)
    {
      this.module = module;
      this.sensorType = sensorType;
      this.userFunction = userFunction;
      waitingCallback = false;
      if ("rotation".equals(sensorType.typeName))
      {
        rotationVector = new float[3];
        rotationMatrix = new float[9];
      }
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event)
    {
      try
      {
        // invoke listener function
        if (!waitingCallback)
        {
          Map<String, Function> functions = getFunctions();
          BList data = getSensorData(event);
          BList call = Utils.createFunctionCall(functions, userFunction, data);
          waitingCallback = true;
          Executor.spawn(call, module, functions, this);
        }
      }
      catch (Exception ex)
      {
        Log.e(TAG, ex.getMessage());
        // if module destroyed
        unregisterListener(module, sensorType.typeName);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor event, int accuracy)
    {
    }

    @Override
    public void onSuccess(Executor executor, Object result)
    {
      waitingCallback = false;
    }

    @Override
    public void onError(Executor executor, Exception exception)
    {
      waitingCallback = false;
    }

    private BList getSensorData(SensorEvent event)
    {
      long timestamp = System.currentTimeMillis() +
       (event.timestamp / 1000000L - SystemClock.elapsedRealtime());
      float[] values = event.values;
      BList data = new BList(sensorType.dataStructure);
      data.put(0, sensorType.typeName);
      data.put(1, (Long)timestamp);
      if ("rotation".equals(sensorType.typeName))
      {
        System.arraycopy(event.values, 0, rotationVector, 0, 3);
        SensorManager.getRotationMatrixFromVector(rotationMatrix, 
          rotationVector);
        SensorManager.getOrientation(rotationMatrix, rotationVector);
        data.put(2, Double.valueOf(rotationVector[0] * RADIANS_TO_DEGREES));
        data.put(3, Double.valueOf(rotationVector[1] * RADIANS_TO_DEGREES));
        data.put(4, Double.valueOf(rotationVector[2] * RADIANS_TO_DEGREES));        
      }
      else
      {
        for (int i = 2; i < sensorType.dataStructure.size(); i++)
        {
          data.put(i, Double.valueOf(values[i - 2]));
        }
      }
      return data;
    }
  }

  public static class SensorType
  {
    int type;
    String typeName;
    Structure dataStructure;

    SensorType(int type, String typeName)
    {
      this(type, typeName, DEFAULT_STRUCTURE);
    }

    SensorType(int type, String typeName, Structure dataStructure)
    {
      this.type = type;
      this.typeName = typeName;
      this.dataStructure = dataStructure;
    }
  }
}
