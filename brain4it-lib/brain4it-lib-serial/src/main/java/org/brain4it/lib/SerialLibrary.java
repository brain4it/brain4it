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

package org.brain4it.lib;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.lib.serial.SerialCloseFunction;
import org.brain4it.lib.serial.SerialOpenFunction;
import org.brain4it.lib.serial.SerialPortsFunction;
import org.brain4it.lib.serial.SerialReadFunction;
import org.brain4it.lib.serial.SerialWriteFunction;

/**
 *
 * @author realor
 */
public class SerialLibrary extends Library
{
  private final Map<String, SerialPort> ports = 
    Collections.synchronizedMap(new HashMap<String, SerialPort>());
  
  @Override
  public String getName()
  {
    return "Serial";
  }

  @Override
  public void load()
  {
    functions.put("serial-ports", new SerialPortsFunction(this));
    functions.put("serial-open", new SerialOpenFunction(this));
    functions.put("serial-close", new SerialCloseFunction(this));
    functions.put("serial-read", new SerialReadFunction(this));
    functions.put("serial-write", new SerialWriteFunction(this));
  }

  @Override
  public void unload()
  {
    for (SerialPort port : ports.values())
    {
      port.closePort();
    }
    ports.clear();
  }
  
  public Map<String, SerialPort> getPorts()
  {
    return ports;
  }
}
