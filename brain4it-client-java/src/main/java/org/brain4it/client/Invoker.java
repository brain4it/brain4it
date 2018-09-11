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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lang.Utils;

/**
 * The Invoker class serializes multiple HTTP calls to exterior functions of
 * a module to ensure that they are processed in the same order in which they
 * were generated.
 *
 * @author realor
 *
 */
public class Invoker
{
  private final RestClient restClient;
  private final String moduleName;
  private final LinkedList<Call> queue = new LinkedList<Call>();
  private Worker worker;
  private static final Logger LOGGER = Logger.getLogger("Invoker");

  public Invoker(RestClient restClient, String moduleName)
  {
    this.restClient = restClient;
    this.moduleName = moduleName;
  }

  public synchronized boolean invoke(String functionName, Object value)
  {
    return invoke(functionName, value, false, null);
  }

  public synchronized boolean invoke(String functionName, Object value,
    boolean coalesce, RestClient.Callback callback)
  {
    return invoke(functionName, value, coalesce, 0, 0, callback);
  }

  public synchronized boolean invoke(String functionName, Object value,
    boolean coalesce, int connectTimeout, int readTimeout,
    RestClient.Callback callback)
  {
    if (functionName == null)
      throw new RuntimeException("functionName is required");

    boolean found = false;

    if (coalesce && callback != null)
    {
      // update value of call from the same callback
      Iterator<Call> iter = queue.iterator();
      while (iter.hasNext() && !found)
      {
        Call call = iter.next();
        if (callback == call.getCallback())
        {
          call.value = value; // set next value to send
          found = true;
        }
      }
    }
    if (!found)
    {
      // add new call to queue
      queue.offer(
        new Call(functionName, value, connectTimeout, readTimeout, callback));
    }
    if (worker == null)
    {
      worker = new Worker();
      worker.start();
    }
    notify(); // wake up worker

    return !found;
  }

  protected class Worker extends Thread
  {
    @Override
    public void run()
    {
      setName("Invoker.Worker-" + getId());
      LOGGER.log(Level.FINE, "Invoker.Worker start");

      Call call = getCall();
      while (call != null)
      {
        invoke(call);
        call = getCall();
      }
      synchronized (Invoker.this)
      {
        worker = null;
      }
      LOGGER.log(Level.FINE, "Invoker.Worker end");
    }
  }

  protected synchronized Call getCall()
  {
    Call call = queue.poll();
    if (call == null)
    {
      try
      {
        // Invoker is terminated if it do not receives a Call for 1 second
        wait(1000);
        call = queue.poll();
      }
      catch (InterruptedException ex)
      {
      }
    }
    return call;
  }

  protected void invoke(Call call)
  {
    try
    {
      Object value = call.value;
      String valueString;
      if (value instanceof String)
      {
        valueString = "\"" + Utils.escapeString((String)value) + "\"";
      }
      else
      {
        valueString = Utils.toString(value);
      }
      restClient.setConnectionTimeout(call.getConnectTimeout());
      restClient.setReadTimeout(call.getReadTimeout());

      String resultString = restClient.invokeFunction(moduleName,
        call.functionName, valueString);

      LOGGER.log(Level.FINE, "call function: {0}, value: {1}, result: {2}",
        new Object[]{call.functionName, valueString, resultString});

      if (call.callback != null)
      {
        call.callback.onSuccess(restClient, resultString);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "call function: {0}, error: {1}",
        new Object[]{call.functionName, ex.toString()});

      if (call.callback != null)
      {
        call.callback.onError(restClient, ex);
      }
    }
  }

  protected class Call
  {
    String functionName;
    Object value;
    int connectTimeout;
    int readTimeout;
    RestClient.Callback callback;

    Call(String functionName, Object value,
      int connectTimeout, int readTimeout, RestClient.Callback callback)
    {
      this.functionName = functionName;
      this.value = value;
      this.connectTimeout = connectTimeout;
      this.readTimeout = readTimeout;
      this.callback = callback;
    }

    public String getFunctionName()
    {
      return functionName;
    }

    public Object getValue()
    {
      return value;
    }

    public int getConnectTimeout()
    {
      return connectTimeout;
    }

    public int getReadTimeout()
    {
      return readTimeout;
    }

    public RestClient.Callback getCallback()
    {
      return callback;
    }
  }
}
