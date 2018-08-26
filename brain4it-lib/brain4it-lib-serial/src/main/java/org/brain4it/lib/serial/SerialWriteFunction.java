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
import java.io.OutputStream;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.SerialLibrary;

/**
 *
 * @author realor
 */
public class SerialWriteFunction extends SerialBaseFunction
{
  public static final String DEFAULT_ENCODING = "UTF-8";
  
  public SerialWriteFunction(SerialLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String portName = (String)context.evaluate(args.get(1));
    SerialPort port = getPorts().get(portName);
    int numSent = 0;
    if (port != null)
    {
      OutputStream os = port.getOutputStream();
      Object data = context.evaluate(args.get(2));
      if (data instanceof BList)
      {
        BList list = (BList)data;
        for (int i = 0; i < list.size(); i++)
        {
          Object elem = list.get(i);
          if (elem instanceof Number)
          {
            byte b = Utils.toNumber(list.get(i)).byteValue();
            os.write(b);
            numSent++;
          }
        }
      }
      else if (data instanceof Number)
      {
        byte b = ((Number)data).byteValue();
        os.write(b);
        numSent = 1;        
      }
      else
      {
        String charset = (String)context.evaluate(args.get("charset"));
        if (charset == null) charset = DEFAULT_ENCODING;
        String string = Utils.toString(data);
        byte[] bytes = string.getBytes(charset);
        os.write(bytes);
        numSent = bytes.length;
      }
      os.flush();
    }
    return numSent;
  }  
}
