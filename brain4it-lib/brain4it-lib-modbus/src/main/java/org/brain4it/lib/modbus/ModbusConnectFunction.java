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

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import static java.lang.Boolean.TRUE;
import java.net.InetAddress;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.ModbusLibrary;

/**
 *
 * @author realor
 */
public class ModbusConnectFunction extends ModbusFunction
{
  public ModbusConnectFunction(ModbusLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    TcpParameters tcpParameters = new TcpParameters();

    String host = (String)context.evaluate(args.get("host"));
    if (host == null) host = "127.0.0.1";
    tcpParameters.setHost(InetAddress.getByName(host));

    Object value;
    value = context.evaluate(args.get("keep-alive"));
    boolean keepAlive = value == null || TRUE.equals(value);
    tcpParameters.setKeepAlive(keepAlive);

    value = context.evaluate(args.get("port"));
    int port = value == null ? Modbus.TCP_PORT : ((Number)value).intValue();
    tcpParameters.setPort(port);

    ModbusMaster master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
    Modbus.setAutoIncrementTransactionId(true);

    master.connect();

    return library.putMaster(master);
  }
}
