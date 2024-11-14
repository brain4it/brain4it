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
package org.brain4it.lib.modbus;

import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.ModbusLibrary;

/**
 *
 * @author realor
 */
public class ModbusReadFunction extends ModbusFunction
{
  public ModbusReadFunction(ModbusLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    String masterId = (String)context.evaluate(args.get(1));
    if (masterId == null) throw new Exception("masterId is required");

    String registers = (String)context.evaluate(args.get("registers"));
    if (registers == null) registers = "holding";

    Object value;
    value = context.evaluate(args.get("server"));
    int server = value == null ? 1 : ((Number)value).intValue();

    value = context.evaluate(args.get("address"));
    int address = value == null ? 0 : ((Number)value).intValue();

    value = context.evaluate(args.get("quantity"));
    int quantity = value == null ? 10 : ((Number)value).intValue();

    ModbusMaster master = library.getMaster(masterId);
    if (master == null) throw new Exception("Invalid masterId");
    BList result = null;
    switch (registers)
    {
      case "holding":
        result = toList(master.readHoldingRegisters(server, address, quantity));
        break;

      case "input":
        result = toList(master.readInputRegisters(server, address, quantity));
        break;

      case "coils":
        result = toList(master.readCoils(server, address, quantity));
        break;

      case "discrete":
        result = toList(master.readDiscreteInputs(server, address, quantity));
        break;

    }
    return result;
  }

  private BList toList(int[] registers)
  {
    BList list = new BList(registers.length);
    for (int i = 0; i < registers.length; i++)
    {
      list.add(registers[i]);
    }
    return list;
  }

  private BList toList(boolean[] registers)
  {
    BList list = new BList(registers.length);
    for (int i = 0; i < registers.length; i++)
    {
      list.add(registers[i]);
    }
    return list;
  }
}
