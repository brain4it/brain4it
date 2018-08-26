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
import org.brain4it.io.JSONPrinter;
import org.brain4it.io.Printer;
import org.brain4it.io.XMLPrinter;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

public class StringFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    Object value = context.evaluate(args.get(1));
    
    if (value instanceof Number)
    {
      Number number = (Number)value;
      String pattern = (String)context.evaluate(args.get("pattern"));
      if (pattern != null)
      {
        if ("unicode".equals(pattern))
        {
          return new String(Character.toChars(number.intValue()));
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
          return df.format(number.doubleValue());
        }
      }
      else
      {
        Number radix = (Number)context.evaluate(args.get("radix"));
        if (radix == null) radix = 10;

        boolean unsigned = 
          Utils.toBoolean(context.evaluate(args.get("unsigned")));

        return Utils.toString(number, radix.intValue(), unsigned);
      }
    }
    else if (value instanceof String)
    {
      if (Utils.toBoolean(args.get("escape")))
      {
        return "\"" + Utils.escapeString((String)value) + "\"";
      }
      else
      {
        return (String)value;
      }
    }
    else if (value instanceof BList)
    {
      String format = (String)args.get("format");
      if ("json".equals(format))
      {
        return JSONPrinter.toString(value);
      }
      else if ("xml".equals(format))
      {
        return XMLPrinter.toString(value);        
      }
      else // bpl
      {
        return Printer.toString(value);        
      }
    }
    else // booleans, references and null
    {
      return Utils.toString(value);
    }
  }
}
