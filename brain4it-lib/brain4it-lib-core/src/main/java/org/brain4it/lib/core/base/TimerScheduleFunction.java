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
package org.brain4it.lib.core.base;

import org.brain4it.lang.BException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.CoreLibrary;
import org.brain4it.lib.CoreLibrary.Task;

/**
 *
 * @author realor
 */
public class TimerScheduleFunction implements Function
{
  private final CoreLibrary library;

  public TimerScheduleFunction(CoreLibrary library)
  {
    this.library = library;
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);

    BList userFunction = (BList)context.evaluate(args.get(1));
    if (!context.isUserFunction(userFunction))
      throw new BException("Invalid user function");

    long delay = ((Number)context.evaluate(args.get(2))).longValue();

    long period = 0;
    if (args.size() > 3)
    {
      period = ((Number)context.evaluate(args.get(3))).longValue();
    }

    boolean overlap = false;
    if (args.size() > 4)
    {
      overlap = Utils.toBoolean(context.evaluate(args.get(4)));
    }

    Task task = library.createTask(context, userFunction, overlap);
    if (period > 0) // repeat
    {
      library.getTimer().scheduleAtFixedRate(task, delay, period);
    }
    else
    {
      library.getTimer().schedule(task, delay);
    }
    return task.getTaskId();
  }
}
