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

import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import static org.brain4it.lib.core.list.MatchFunction.LIST_VARIABLE_SUFFIX;

/**
 *
 * @author realor
 */
public class MergeFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    Object result;
    Object data = context.evaluate(args.get(1));
    if (data instanceof BList)
    {
      BList list = (BList)data;
      BList map = (BList)context.evaluate(args.get(2));
      result = new BList();
      merge(list, (BList)result, map);
    }
    else if (data instanceof BSoftReference)
    {
      BSoftReference reference = (BSoftReference)data;
      String variableName = reference.getValue();
      BList map = (BList)context.evaluate(args.get(2));
      if (map.has(variableName))
      {
        result = map.get(variableName);
      }
      else
      {
        result = data;
      }
    }
    else
    {
      result = data;
    }
    return result;
  }  
  
  void merge(BList source, BList target, BList map)
  {
    for (int i = 0; i < source.size(); i++)
    {
      String variableName = null;
      String name = source.getName(i);
      Object value = source.get(i);
      if (value instanceof BSoftReference)
      {
        BSoftReference reference = (BSoftReference)value;
        variableName = reference.getValue();
        if (map.has(variableName))
        {
          value = map.get(variableName);
        }
      }
      else if (value instanceof BList)
      {
        BList child = (BList)value;
        value = new BList();
        merge(child, (BList)value, map);
      }
      
      if (name == null)
      {
        if (variableName != null && value instanceof BList && 
            variableName.endsWith(LIST_VARIABLE_SUFFIX))
        {
          target.addAll((BList)value);
        }
        else
        {
          target.add(value);
        }
      }
      else
      {
        target.put(name, value);        
      }
    }
  }
}
