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

package org.brain4it.lib.core.date;

import java.util.Calendar;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class DateFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    long time;
    String command = null;
    if (args.size() > 1)
    {
      command = (String)context.evaluate(args.get(1));
    }
    if ("add".equals(command))
    {
      Calendar calendar = getCalendar(context, args);
      calendar.add(getField(context, args), getValue(context, args));
      time = calendar.getTimeInMillis();
    }
    else if ("set".equals(command))
    {
      Calendar calendar = getCalendar(context, args);
      calendar.set(getField(context, args), getValue(context, args));
      time = calendar.getTimeInMillis();
    }
    else
    {
      time = System.currentTimeMillis();
    }
    return time;
  }  

  private Calendar getCalendar(Context context, BList args)
    throws Exception
  {
    Calendar calendar = Calendar.getInstance();
    Number number = (Number)context.evaluate(args.get(2));
    long millis = number.longValue();
    calendar.setTimeInMillis(millis);
    return calendar;
  }
  
  private int getField(Context context, BList args) throws Exception
  {
    String field = (String)context.evaluate(args.get(3));
    if ("year".equals(field)) return Calendar.YEAR;
    if ("day_of_month".equals(field)) return Calendar.DAY_OF_MONTH;
    if ("day_of_week".equals(field)) return Calendar.DAY_OF_WEEK;
    if ("day_of_year".equals(field)) return Calendar.DAY_OF_YEAR;
    if ("hour_of_day".equals(field)) return Calendar.HOUR_OF_DAY;
    if ("hour".equals(field)) return Calendar.HOUR;
    if ("month".equals(field)) return Calendar.MONTH;
    if ("week_of_month".equals(field)) return Calendar.WEEK_OF_MONTH;
    if ("week_of_year".equals(field)) return Calendar.WEEK_OF_YEAR;
    if ("minute".equals(field)) return Calendar.MINUTE;
    if ("second".equals(field)) return Calendar.SECOND;
    if ("millisecond".equals(field)) return Calendar.MILLISECOND;
    throw new Exception("Invalid field name");
  }
  
  private int getValue(Context context, BList args)
    throws Exception
  {
    Number number = (Number)context.evaluate(args.get(4));
    return number.intValue();
  }
}
