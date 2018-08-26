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

package org.brain4it.lib.core.list;

import java.util.Arrays;
import java.util.Comparator;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

public class SortFunction implements Function
{
  static final Comparator DEFAULT_COMPARATOR = new Comparator()
  {
    @Override
    public int compare(Object value1, Object value2)
    {
      return Utils.compare(value1, value2);
    }    
  };

  @Override
  public Object invoke(final Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    BList result = new BList();
    BList list = (BList)context.evaluate(args.get(1));
    Comparator comparator = DEFAULT_COMPARATOR;
    if (args.size() >= 3)
    {
      final BList func = (BList)context.evaluate(args.get(2));
      if (context.isUserFunction(func))
      {
        comparator = new Comparator()
        {
          @Override
          public int compare(Object o1, Object o2)
          {
            try
            {
              BList args = new BList(2);
              args.add(o1);
              args.add(o2);
              Object result = context.invokeUserFunction(func, args);
              return Utils.toNumber(result).intValue();
            }
            catch (Exception ex)
            { 
              throw new RuntimeException("Compare error", ex);
            }
          }
        };
      }
      else throw new Exception("Invalid comparator function");
    }
    Object[] array = list.toArray();
    Arrays.sort(array, comparator);
    for (Object element : array)
    {
      result.add(element);
    }
    return result;
  }
}
