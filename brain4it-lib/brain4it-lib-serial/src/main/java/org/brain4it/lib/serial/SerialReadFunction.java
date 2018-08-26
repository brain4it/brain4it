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

package org.brain4it.lib.serial;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.SerialLibrary;

/**
 *
 * @author realor
 */
public class SerialReadFunction extends SerialBaseFunction
{
  public static final String MODE = "mode";
  public static final String COUNT = "count";
  public static final String BYTES_MODE = "bytes";
  public static final String LINE_MODE = "line";

  public SerialReadFunction(SerialLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String portName = (String)context.evaluate(args.get(1));
    SerialPort port = getPorts().get(portName);
    if (port != null)
    {
      String mode = (String)context.evaluate(args.get(MODE));
      if (mode == null) mode = BYTES_MODE;
      
      InputStream is = port.getInputStream();
      if (LINE_MODE.equals(mode))
      {
        return readLine(is);
      }
      else if (BYTES_MODE.equals(mode))
      {
        Object value = context.evaluate(args.get(COUNT));
        int count;
        if (value == null)
        {
          count = 0;
        }
        else
        {
          count = Utils.toNumber(value).intValue();
        }
        return readBytes(is, count);
      }
      throw new RuntimeException("Invalid mode");
    }
    return null;
  }

  private BList readBytes(InputStream is, int count)
    throws IOException, InterruptedException
  {
    BList data = new BList();
    if (count == 0) count = is.available();
    while (count > 0 && !Thread.interrupted())
    {
      if (is.available() == 0)
      {
        Thread.sleep(100);
      }
      else
      {
        int b = is.read();
        data.add(b);
      }
      count--;
    }
    return data;
  }

  private String readLine(InputStream is)
    throws IOException, InterruptedException
  {
    StringBuilder buffer = new StringBuilder();
    boolean eol = false;
    while (!eol && !Thread.interrupted())
    {
      int available = is.available();
      if (available == 0)
      {
        Thread.sleep(100);
      }
      else
      {
        int ch = is.read();
        if (ch == 13 || ch == 10)
        {
          eol = true;
        }
        else if (ch != 0)
        {
          buffer.append((char)ch);
        }
      }
    }
    return buffer.toString();
  }
}
