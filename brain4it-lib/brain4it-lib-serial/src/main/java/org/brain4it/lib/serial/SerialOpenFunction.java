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
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.SerialLibrary;

/**
 *
 * @author realor
 */
public class SerialOpenFunction extends SerialBaseFunction
{
  public static final String BAUD_RATE = "baud-rate";
  public static final String DATA_BITS = "data-bits";
  public static final String STOP_BITS = "stop-bits";
  public static final String PARITY = "parity";
  
  public static final String NO_PARITY = "none";
  public static final String EVEN_PARITY = "even";
  public static final String ODD_PARITY = "odd";
  public static final String MARK_PARITY = "mark";
  public static final String SPACE_PARITY = "space";
    
  public SerialOpenFunction(SerialLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String portName = (String)context.evaluate(args.get(1));
    SerialPort port = getPorts().get(portName);
    if (port == null)
    {
      port = SerialPort.getCommPort(portName);
      Object value;
      value = context.evaluate(args.get(BAUD_RATE));      
      if (value != null)
      {
        port.setBaudRate(Utils.toNumber(value).intValue());
      }
      value = context.evaluate(args.get(DATA_BITS));      
      if (value != null)
      {
        port.setNumDataBits(Utils.toNumber(value).intValue());
      }
      value = context.evaluate(args.get(STOP_BITS));      
      if (value != null)
      {
        port.setNumStopBits(Utils.toNumber(value).intValue());
      }
      value = context.evaluate(args.get(PARITY)); 
      if (value != null)
      {
        if (value.equals(NO_PARITY))
        {
          port.setParity(SerialPort.NO_PARITY);
        }
        else if (value.equals(EVEN_PARITY))
        {
          port.setParity(SerialPort.EVEN_PARITY);          
        }
        else if (value.equals(ODD_PARITY))
        {
          port.setParity(SerialPort.ODD_PARITY);          
        }
        else if (value.equals(MARK_PARITY))
        {
          port.setParity(SerialPort.MARK_PARITY);          
        }
        else if (value.equals(SPACE_PARITY))
        {
          port.setParity(SerialPort.SPACE_PARITY);          
        }
      }
      getPorts().put(portName, port);
      port.openPort();
      return true;
    }
    return false;
  }
}
