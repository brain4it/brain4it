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
package org.brain4it.server.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author realor
 */
public class AndroidLogHandler extends java.util.logging.Handler
{
  private SimpleDateFormat dateFormat = 
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final Formatter formatter = new Formatter()
  {
    @Override
    public synchronized String format(LogRecord record)
    {
      String message = formatMessage(record);
      return "[" + dateFormat.format(new Date()) + "] " + message;
    }  
  };
  
  @Override
  public void publish(LogRecord record)
  {
    String message = formatter.format(record);
    ServerActivity activity = ServerActivity.getInstance();
    if (activity != null)
    {
      activity.logMessage(message);
    }
  }

  @Override
  public void flush()
  {
  }

  @Override
  public void close() throws SecurityException
  {
  }
}
