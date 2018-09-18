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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.brain4it.lang.BHardReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.BReference;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;

/**
 *
 * @author realor
 */
public class FunctionsFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    String filter;
    if (args.size() > 1)
    {
      filter = (String)context.evaluate(args.get(1));
    }
    else
    {
      filter = ".*";
    }
    ArrayList<BReference> selection = new ArrayList<BReference>();
    Map<String, Function> functions = context.getFunctions();
    Set<Map.Entry<String, Function>> entrySet = functions.entrySet();
    Iterator<Map.Entry<String, Function>> iter = entrySet.iterator();
    while (iter.hasNext())
    {
      Map.Entry<String, Function> entry = iter.next();
      String name = entry.getKey();
      Function function = entry.getValue();
      if (name.matches(filter))
      {
        selection.add(new BHardReference(name, function));
      }
    }
    Collections.sort(selection, new Comparator()
    {
      @Override
      public int compare(Object o1, Object o2)
      {
        String name1 = ((BHardReference)o1).getName();
        String name2 = ((BHardReference)o2).getName();
        return name1.compareTo(name2);
      }
    });
    return Utils.toBList(selection);
  }
}
