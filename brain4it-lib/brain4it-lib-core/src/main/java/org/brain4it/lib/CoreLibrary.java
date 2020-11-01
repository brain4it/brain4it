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

package org.brain4it.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.core.base.*;
import org.brain4it.lib.core.list.*;
import org.brain4it.lib.core.bit.*;
import org.brain4it.lib.core.net.*;
import org.brain4it.lib.core.text.*;
import org.brain4it.lib.core.math.*;
import org.brain4it.lib.core.logical.*;
import org.brain4it.lib.core.date.*;
import static org.brain4it.io.IOConstants.CALL_FUNCTION_NAME;
import static org.brain4it.io.IOConstants.QUOTE_FUNCTION_NAME;
import static org.brain4it.io.IOConstants.COMMENT_FUNCTION_NAME;
import static org.brain4it.io.IOConstants.FUNCTION_FUNCTION_NAME;

public class CoreLibrary extends Library
{
  private int taskIdSequence = -1;
  private final Timer timer = new Timer();
  private final Map<Integer, Task> tasks =
    Collections.synchronizedMap(new HashMap<Integer, Task>());

  @Override
  public String getName()
  {
    return "Core";
  }

  @Override
  public void load()
  {
    functions.put(COMMENT_FUNCTION_NAME, new CommentFunction());
    functions.put("assert", new AssertFunction());
    functions.put("set", new SetFunction());
    functions.put("if", new IfFunction());
    functions.put("cond", new CondFunction());
    functions.put("for", new ForFunction());
    functions.put(CALL_FUNCTION_NAME, new CallFunction());
    functions.put("delete", new DeleteFunction());
    functions.put("do", new DoFunction());
    functions.put(QUOTE_FUNCTION_NAME, new QuoteFunction());
    functions.put("eval", new EvalFunction());
    functions.put("when", new WhenFunction());
    functions.put("while", new WhileFunction());
    functions.put("kill", new KillFunction());
    functions.put("sleep", new SleepFunction());
    functions.put("spawn", new SpawnFunction());
    functions.put("reference", new ReferenceFunction());
    functions.put("reference-list", new ReferenceListFunction());
    functions.put("local", new LocalFunction());
    functions.put("string", new StringFunction());
    functions.put("number", new NumberFunction());
    functions.put("boolean", new BooleanFunction());
    functions.put("type-of", new TypeOfFunction());
    functions.put("subtype-of", new SubtypeOfFunction());
    functions.put("functions", new FunctionsFunction());
    functions.put("exists", new ExistsFunction());
    functions.put("watch", new WatchFunction());
    functions.put("parse", new ParseFunction());
    functions.put("sync", new SyncFunction());
    functions.put("wait", new WaitFunction());
    functions.put("notify", new NotifyFunction());
    functions.put("try", new TryFunction());
    functions.put("throw", new ThrowFunction());
    functions.put("executors", new ExecutorsFunction());
    functions.put(FUNCTION_FUNCTION_NAME, new FunctionFunction());
    functions.put("functions", new FunctionsFunction());
    functions.put("help", new HelpFunction());
    functions.put("global-scope", new GlobalScopeFunction());
    functions.put("local-scope", new LocalScopeFunction());
    functions.put("set-local", new SetLocalFunction());
    functions.put("system", new SystemFunction());

    functions.put("and", new AndFunction());
    functions.put("or", new OrFunction());
    functions.put("not", new NotFunction());
    functions.put("=", new EqualsFunction());
    functions.put("==", new NameEqualsFunction());
    functions.put("===", new ExactEqualsFunction());
    functions.put("!=", new DifferentFunction());
    functions.put("<", new LessThanFunction());
    functions.put(">", new GreaterThanFunction());
    functions.put("<=", new LessOrEqualToFunction());
    functions.put(">=", new GreaterOrEqualToFunction());

    functions.put("bit-and", new BitAndFunction());
    functions.put("bit-or", new BitOrFunction());
    functions.put("bit-not", new BitNotFunction());
    functions.put("bit-xor", new BitXorFunction());
    functions.put("bit-shift", new BitShiftFunction());

    functions.put("+", new SumFunction());
    functions.put("-", new SubtractFunction());
    functions.put("*", new MultiplyFunction());
    functions.put("/", new DivisionFunction());
    functions.put("div", new IntegerDivisionFunction());
    functions.put("mod", new ModulusFunction());
    functions.put("random", new RandomFunction());
    functions.put("++", new IncrementFunction());
    functions.put("--", new DecrementFunction());

    String pkg = SumFunction.class.getPackage().getName();
    createFunction(pkg, "abs", Math.class, "abs", double.class);
    createFunction(pkg, "acos", Math.class, "acos", double.class);
    createFunction(pkg, "asin", Math.class, "asin", double.class);
    createFunction(pkg, "atan", Math.class, "atan", double.class);
    createFunction(pkg, "ceil", Math.class, "ceil", double.class);
    createFunction(pkg, "cos", Math.class, "cos", double.class);
    createFunction(pkg, "floor", Math.class, "floor", double.class);
    createFunction(pkg, "log10", Math.class, "log10", double.class);
    createFunction(pkg, "logn", Math.class, "log", double.class);
    createFunction(pkg, "pow", Math.class, "pow", double.class, double.class);
    createFunction(pkg, "round", Math.class, "round", double.class);
    createFunction(pkg, "sin", Math.class, "sin", double.class);
    createFunction(pkg, "sqrt", Math.class, "sqrt", double.class);
    createFunction(pkg, "tan", Math.class, "tan", double.class);

    functions.put("list", new ListFunction());
    functions.put("clone", new CloneFunction());
    functions.put("get", new GetFunction());
    functions.put("get-name", new GetNameFunction());
    functions.put("has", new HasFunction());
    functions.put("put", new PutFunction());
    functions.put("put-name", new PutNameFunction());
    functions.put("push", new PushFunction());
    functions.put("pop", new PopFunction());
    functions.put("first", new FirstFunction());
    functions.put("last", new LastFunction());
    functions.put("size", new SizeFunction());
    functions.put("empty", new EmptyFunction());
    functions.put("reverse", new ReverseFunction());
    functions.put("insert", new InsertFunction());
    functions.put("remove", new RemoveFunction());
    functions.put("locate", new LocateFunction());
    functions.put("name-index", new NameIndexFunction());
    functions.put("find", new FindFunction());
    functions.put("append", new AppendFunction());
    functions.put("for-each", new ForEachFunction());
    functions.put("apply", new ApplyFunction());
    functions.put("sort", new SortFunction());
    functions.put("sublist", new SublistFunction());
    functions.put("names", new NamesFunction());
    functions.put("match", new MatchFunction());
    functions.put("merge", new MergeFunction());

    functions.put("concat", new ConcatenateFunction());
    functions.put("downcase", new DownCaseFunction());
    functions.put("upcase", new UpCaseFunction());
    functions.put("trim", new TrimFunction());
    functions.put("length", new LengthFunction());
    functions.put("substring", new SubstringFunction());
    functions.put("locate-string", new LocateStringFunction());
    functions.put("split", new SplitFunction());
    functions.put("match-string", new MatchStringFunction());
    functions.put("replace-string", new ReplaceStringFunction());

    functions.put("http", new HttpFunction());

    functions.put("date", new DateFunction());
    functions.put("parse-date", new ParseDateFunction());
    functions.put("format-date", new FormatDateFunction());

    functions.put("timer-schedule", new TimerScheduleFunction(this));
    functions.put("timer-cancel", new TimerCancelFunction(this));
    functions.put("timer-tasks", new TimerTasksFunction(this));

    // deprecated functions names
    if ("true".equals(System.getProperty("skynet-names")))
    {
      functions.put("seq", functions.get("do"));
      functions.put("foreach", functions.get("for-each"));
      functions.put("getname", functions.get("get-name"));
      functions.put("putname", functions.get("put-name"));
      functions.put("typeof", functions.get("type-of"));
      functions.put("indexof", functions.get("index-of"));
      functions.put("substr", functions.get("substring"));
      functions.put("findstr", functions.get("find-string"));
      functions.put("log", functions.get("log10"));
      functions.put("ln", functions.get("logn"));
      functions.put("clear", functions.get("delete"));
      functions.put("delete", functions.get("remove"));
    }
  }

  @Override
  public void unload()
  {
    timer.cancel();
    tasks.clear();
  }

  public Timer getTimer()
  {
    return timer;
  }

  public Task createTask(Context context, BList userFunction, boolean overlap)
  {
    synchronized (tasks)
    {
      do
      {
        taskIdSequence++;
        if (taskIdSequence > Short.MAX_VALUE) taskIdSequence = 0;
      } while (tasks.containsKey(taskIdSequence));

      int taskId = taskIdSequence;
      Task task = new Task(taskId, context, userFunction, overlap);
      tasks.put(taskId, task);
      return task;
    }
  }

  public Task getTask(int taskId)
  {
    return tasks.get(taskId);
  }

  public Task removeTask(int taskId)
  {
    return tasks.remove(taskId);
  }

  public Collection<Task> getTasks(Context context)
  {
    ArrayList<Task> contextTasks = new ArrayList<Task>();
    synchronized (tasks)
    {
      for (Task task : tasks.values())
      {
        if (task.getGlobalScope() == context.getGlobalScope())
        {
          contextTasks.add(task);
        }
      }
    }
    return contextTasks;
  }

  public static class Task extends TimerTask implements Executor.Callback
  {
    private final int taskId;
    private final BList globalScope;
    private final Map<String, Function> contextFunctions;
    private final BList userFunction;
    private final boolean overlap;
    private final BList call;
    private boolean running;

    Task(int taskId, Context context, BList userFunction, boolean overlap)
    {
      this.taskId = taskId;
      this.globalScope = context.getGlobalScope();
      this.contextFunctions = context.getFunctions();
      this.userFunction = userFunction;
      this.overlap = overlap;
      this.call = Utils.createFunctionCall(contextFunctions, userFunction);
    }

    public int getTaskId()
    {
      return taskId;
    }

    public BList getGlobalScope()
    {
      return globalScope;
    }

    public BList getUserFunction()
    {
      return userFunction;
    }

    public boolean isOverlap()
    {
      return overlap;
    }

    @Override
    public void run()
    {
      if (canExecute())
      {
        Executor.spawn(call, globalScope, contextFunctions, this);
      }
    }

    @Override
    public synchronized void onSuccess(Executor executor, Object result)
    {
      running = false;
    }

    @Override
    public synchronized void onError(Executor executor, Exception ex)
    {
      running = false;
    }

    private synchronized boolean canExecute()
    {
      boolean canExecute = overlap || !running;
      if (!running) running = true;
      return canExecute;
    }
  }
}
