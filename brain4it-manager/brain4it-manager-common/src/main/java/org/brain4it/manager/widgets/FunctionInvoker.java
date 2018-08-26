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
package org.brain4it.manager.widgets;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.brain4it.client.Invoker;
import org.brain4it.client.RestClient;
import static org.brain4it.server.ServerConstants.SERVER_TIME_HEADER;

/**
 *
 * @author realor
 */
public class FunctionInvoker implements RestClient.Callback
{
  private final Invoker invoker;
  private final String functionName;
  private long invokeTime;
  private int sending;
  private Object nextValue;
  private InvokeTask invokeTask;
  
  public FunctionInvoker(Invoker invoker, String functionName)
  {
    this.invoker = invoker;
    this.functionName = functionName;
  }

  public FunctionInvoker(Invoker invoker, String functionName, Timer timer,
     long invokeInterval)
  {
    this.invoker = invoker;
    this.functionName = functionName;
    this.invokeTask = new InvokeTask();
    timer.scheduleAtFixedRate(invokeTask, 0, invokeInterval);
  }
  
  public synchronized void invoke(Object value)
  {
    if (invokeTask == null)
    {
      sendValue(value);
    }
    else
    {
      nextValue = value;
    }
  }
  
  public synchronized boolean isSending()
  {
    return sending > 0 || nextValue != null;
  }
    
  public synchronized boolean updateInvokeTime(long serverTime)
  {
    if (serverTime > invokeTime)
    {
      invokeTime = serverTime;
      return true;
    }
    return false;
  }
  
  @Override
  public synchronized void onSuccess(RestClient restClient, String resultString)
  {
    long serverTime = 0;
    Map<String, List<String>> headers = restClient.getHeaders();
    if (headers != null)
    {
      List<String> serverTimeValues = headers.get(SERVER_TIME_HEADER);
      if (serverTimeValues != null)
      {
        serverTime = Long.parseLong(serverTimeValues.get(0));
      }
    }
    invokeTime = serverTime;
    sending--;
  }

  @Override
  public synchronized void onError(RestClient restClient, Exception ex)
  {
    sending--;
  }
   
  protected synchronized void sendNextValue()
  {
    if (nextValue != null)
    {
      Object value = nextValue;
      nextValue = null;
      sendValue(value);
    }
  }
 
  protected void sendValue(Object value)
  {
    if (invoker.invoke(functionName, value, true, this))
    {
      sending++;
    }    
  }
  
  class InvokeTask extends TimerTask
  {
    @Override
    public void run()
    {
      sendNextValue();
    }
  }
}
