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

import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Context;
import org.brain4it.lang.BException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 *
 *  Example:
 *  (try
 *    (http "GET" "http://www.google.com")
 *    (ex
 *      "FileNotFoundException" => (throw ex)
 *      "NullPointerException" => (throw "NullValue")
 *      "SQLException" => (eval error-handler)
 *      "IOException" => (eval error-handler)
 *      "*" => (eval error-handler-2)
 *    )
 *    (set found false)
 *  )
 */

public class TryFunction implements Function
{
  public static String ANY = "*";

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    boolean interrupted = false;
    try
    {
      return context.evaluate(args.get(1));
    }
    catch (InterruptedException ex)
    {
      /* InterruptedExceptions can not be catched with the try function to
      ensure that the Executor.kill method always interrupts the execution.
      */
      interrupted = true;
      throw ex;
    }
    catch (Exception ex)
    {
      BList exList = Utils.toBList(ex);
      String exType = (String)exList.get(0);
      boolean catched = false;
      Object catchCode = null;
      BList catchList = (BList)args.get(2);

      if (catchList != null && catchList.size() > 1)
      {
        if (catchList.has(exType))
        {
          catchCode = catchList.get(exType);
          catched = true;
        }
        else if (catchList.has(ANY))
        {
          catchCode = catchList.get(ANY);
          catched = true;
        }
      }
      if (catched)
      {
        BSoftReference reference = 
          (BSoftReference)Utils.getBReference(context, catchList, 0);
        context.setLocal(reference, exList);
        return context.evaluate(catchCode);
      }
      // rethrow unhandled exception
      if (ex instanceof BException)
      {
        throw (BException)ex;
      }
      else
      {
        throw new BException(exList, ex);
      }
    }
    finally
    {
      if (!interrupted && args.size() > 3)
      {
        Object finallyCode = args.get(3);
        context.evaluate(finallyCode);
      }
    }
  }
}
