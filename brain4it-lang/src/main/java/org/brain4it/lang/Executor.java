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
package org.brain4it.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author realor
 */
public class Executor implements Callable
{
  private static final Map<Integer, Executor> EXECUTORS =
    Collections.synchronizedMap(new HashMap<Integer, Executor>());
  private static ThreadPoolExecutor threadPool;
  private static int idSequence = -1;
  private static final String THREAD_NAME_SEPARATOR = ":";

  private int executorId;
  private final Object code;
  private final Context context;
  private Callback callback;
  private Future future;
  private long startMillis;
  private Thread thread;
  private String threadName;

  Executor(Object code, BList globalScope, Map<String, Function> functions)
  {
    this.context = new Context(globalScope, functions);
    this.code = code;
  }

  public int getExecutorId()
  {
    return executorId;
  }

  public Context getContext()
  {
    return context;
  }

  public Object getCode()
  {
    return code;
  }

  public Callback getCallback()
  {
    return callback;
  }

  public void setCallback(Callback callback)
  {
    this.callback = callback;
  }

  public boolean isDone()
  {
    if (future == null) return false;
    return future.isDone();
  }

  public boolean isCancelled()
  {
    if (future == null) return false;
    return future.isCancelled();
  }

  public boolean isRunning()
  {
    if (future == null) return false;
    return !future.isDone() && !future.isCancelled();
  }

  public long getStartMillis()
  {
    return startMillis;
  }

  public Thread getThread()
  {
    return thread;
  }

  @Override
  public Object call() throws Exception
  {
    try
    {
      registerThread();
      startMillis = System.currentTimeMillis();
      Object result = context.evaluate(code);
      if (callback != null) callback.onSuccess(this, result);
      return result;
    }
    catch (Exception ex)
    {
      if (callback != null) callback.onError(this, ex);
      throw ex;
    }
    finally
    {
      EXECUTORS.remove(executorId);
      unregisterThread();
    }
  }

  private void setExecutorId()
  {
    synchronized (EXECUTORS)
    {
      do
      {
        idSequence++;
        if (idSequence > Short.MAX_VALUE)
        {
          idSequence = 0;
        }
      } while (EXECUTORS.containsKey(idSequence));
      executorId = idSequence;
      EXECUTORS.put(executorId, this);
    }
  }

  private void registerThread()
  {
    thread = Thread.currentThread();
    threadName = thread.getName();
    thread.setName(threadName + THREAD_NAME_SEPARATOR +
      context.getGlobalScope() + "/" + executorId);
  }

  private void unregisterThread()
  {
    thread.setName(threadName);
    thread = null;
    threadName = null;
  }

  private boolean kill()
  {
    // not started yet
    if (future == null) return false;

    // do not kill current thread
    if (thread == Thread.currentThread()) return false;

    if (thread != null)
    {
      try
      {
        thread.interrupt();
      }
      catch (Exception ex)
      {
      }
    }
    return true;
  }

  public interface Callback
  {
    public void onSuccess(Executor executor, Object result);
    public void onError(Executor executor, Exception ex);
  }

  /* static methods */

  public synchronized static void init()
  {
    Executor.threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
      60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
  }

  public synchronized static void shutdown()
  {
    if (threadPool != null)
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
      threadPool = null;
    }
  }

  public static Executor getExecutor(int executorId, BList globalScope)
  {
    Executor executor = EXECUTORS.get(executorId);
    if (executor == null) return null;
    if (executor.getContext().getGlobalScope() != globalScope) return null;
    return executor;
  }

  public static ArrayList<Executor> getExecutors()
  {
    synchronized (EXECUTORS)
    {
      ArrayList<Executor> executors = new ArrayList<Executor>();
      executors.addAll(EXECUTORS.values());
      return executors;
    }
  }

  public static ArrayList<Executor> getExecutors(BList globalScope)
  {
    synchronized (EXECUTORS)
    {
      ArrayList<Executor> executors = new ArrayList<Executor>();
      for (Executor executor: EXECUTORS.values())
      {
        if (executor.getContext().getGlobalScope() == globalScope)
        {
          executors.add(executor);
        }
      }
      return executors;
    }
  }

  public static Object execute(Object code, BList globalScope,
    Map<String, Function> functions, int seconds) throws Exception
  {
    if (threadPool == null)
      throw new RuntimeException("Executor not initialized!");

    Executor executor = new Executor(code, globalScope, functions);
    executor.setExecutorId();
    executor.future = threadPool.submit(executor);
    Object result;
    try
    {
      if (seconds == 0)
      {
        result = executor.future.get();
      }
      else
      {
        result = executor.future.get(seconds, TimeUnit.SECONDS);
      }
      return result;
    }
    catch (ExecutionException ex)
    {
      // throw original exception
      Throwable t = ex.getCause();
      if (t instanceof Exception) throw (Exception)t;
      throw new Exception(ex.getMessage());
    }
  }

  public static int spawn(Object code, BList globalScope,
    Map<String, Function> functions, Callback callback)
  {
    if (threadPool == null)
      throw new RuntimeException("Executor not initialized!");

    Executor executor = new Executor(code, globalScope, functions);
    executor.setExecutorId();
    executor.setCallback(callback);
    executor.future = threadPool.submit(executor);
    return executor.getExecutorId();
  }

  public static boolean kill(int executorId, BList globalScope)
  {
    Executor executor = getExecutor(executorId, globalScope);
    if (executor != null)
    {
      return executor.kill();
    }
    return false;
  }

  public static boolean killAll(BList globalScope)
  {
    ArrayList<Executor> executors = getExecutors(globalScope);
    boolean cancelled = false;
    for (Executor executor : executors)
    {
      if (executor.kill())
      {
        cancelled = true;
      }
    }
    return cancelled;
  }
}
