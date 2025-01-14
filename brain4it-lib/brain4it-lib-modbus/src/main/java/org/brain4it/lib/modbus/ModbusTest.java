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
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.msg.request.ReadHoldingRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;

import java.net.InetAddress;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author realor
 */
public class ModbusTest
{

  static public void main(String[] args)
  {

    Modbus.log().addHandler(new Handler()
    {
      @Override
      public void publish(LogRecord record)
      {
        System.out.println(record.getLevel().getName() + ": " + record.getMessage());
      }

      @Override
      public void flush()
      {
        //do nothing
      }

      @Override
      public void close() throws SecurityException
      {
        //do nothing
      }
    });
    Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

    try
    {
      TcpParameters tcpParameters = new TcpParameters();
      //tcp parameters have already set by default as in example
      tcpParameters.setHost(InetAddress.getByName("10.70.2.70"));
      //tcpParameters.setHost("10.70.2.73"InetAddress.getLocalHost());

      tcpParameters.setKeepAlive(true);
      tcpParameters.setPort(Modbus.TCP_PORT);

      //if you would like to set connection parameters separately,
      // you should use another method: createModbusMasterTCP(String host, int port, boolean keepAlive);
      ModbusMaster m = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
      Modbus.setAutoIncrementTransactionId(true);


      try
      {
        // since 1.2.8
        if (!m.isConnected())
        {
          m.connect();
        }

        // at next string we receive ten registers from a slave with id of 1 at offset of 0.
        int count = 0;
        while (count < 1000) {

          int slaveId = 1;
          int offset = 14011;
          int quantity = 20;

          int[] registerValues = m.readHoldingRegisters(slaveId, offset, quantity);

          for (int value : registerValues)
          {
            System.out.println("Address: " + offset++ + ", Value: " + value);
          }
          java.lang.Thread.sleep(1000);
          count++;
        }

        m.writeSingleRegister(1, 14011, 0);

        // also since 1.2.8.4 you can create your own request and process it with the master
//        offset = 0;
//        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest();
//        request.setServerAddress(1);
//        request.setStartAddress(offset);
//        request.setTransactionId(0);
//        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) m.processRequest(request);
//        // you can get either int[] containing register values or byte[] containing raw bytes.
//        for (int value : response.getRegisters())
//        {
//          System.out.println("Address: " + offset++ + ", Value: " + value);
//        }
      }
      catch (ModbusProtocolException e)
      {
        e.printStackTrace();
      }
      catch (ModbusNumberException e)
      {
        e.printStackTrace();
      }
      catch (ModbusIOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          m.disconnect();
        }
        catch (ModbusIOException e)
        {
          e.printStackTrace();
        }
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
