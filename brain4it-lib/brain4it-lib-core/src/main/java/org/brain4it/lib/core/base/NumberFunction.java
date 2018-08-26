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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

public class NumberFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    Number number;
    Object value = context.evaluate(args.get(1));
    if (value instanceof Number)
    {
      number = (Number)value;
    }
    else if (value instanceof String)
    {
      String text = (String)value;
      Number radix = (Number)context.evaluate(args.get("radix"));
      if (radix != null)
      {
        number = Utils.toNumber(text, radix.intValue());
      }
      else
      {
        String pattern = (String)context.evaluate(args.get("pattern"));
        if (pattern == null)
        {
          number = Utils.toNumber(text);
        }
        else
        {
          DecimalFormat df;
          String localeName = (String)context.evaluate(args.get("locale"));
          if (localeName == null)
          {
            df = new DecimalFormat(pattern);
          }
          else
          {
            Locale locale = new Locale(localeName);
            df = new DecimalFormat(pattern, 
              DecimalFormatSymbols.getInstance(locale));
          }
          number = df.parse(text);
        }
      }
    }
    else throw new RuntimeException("Invalid number");
    
    String type = (String)context.evaluate(args.get("type"));
    if (type != null && number != null)
    {
      if ("integer".equalsIgnoreCase(type)) number = number.intValue();
      else if ("long".equalsIgnoreCase(type)) number = number.longValue();
      else number = number.doubleValue();
    }
    return number;
  }
}
