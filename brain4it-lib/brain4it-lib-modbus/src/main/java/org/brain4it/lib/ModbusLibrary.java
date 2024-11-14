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

import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.brain4it.lib.modbus.ModbusConnectFunction;
import org.brain4it.lib.modbus.ModbusDisconnectFunction;
import org.brain4it.lib.modbus.ModbusReadFunction;


/**
 *
 * @author realor
 */
public class ModbusLibrary extends Library
{
  private final Map<String, ModbusMaster> masters =
    Collections.synchronizedMap(new HashMap<String, ModbusMaster>());

  @Override
  public String getName()
  {
    return "Modbus";
  }

  @Override
  public void load()
  {
    functions.put("modbus-connect", new ModbusConnectFunction(this));
    functions.put("modbus-disconnect", new ModbusDisconnectFunction(this));
    functions.put("modbus-read", new ModbusReadFunction(this));
  }

  @Override
  public void unload()
  {
    for (ModbusMaster master : masters.values())
    {
      try
      {
        master.disconnect();
      }
      catch (Exception ex)
      {
      }
    }
  }

  public ModbusMaster getMaster(String masterId)
  {
    return masters.get(masterId);
  }

  public String putMaster(ModbusMaster master)
  {
    UUID uuid = UUID.randomUUID();
    String masterId = Long.toHexString(uuid.getMostSignificantBits()) +
      Long.toHexString(uuid.getLeastSignificantBits());
    masters.put(masterId, master);
    return masterId;
  }

  public ModbusMaster removeMaster(String masterId)
  {
    return masters.remove(masterId);
  }
}
