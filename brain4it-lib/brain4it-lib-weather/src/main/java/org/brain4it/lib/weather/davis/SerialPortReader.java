/*
 *  Copyright 2006 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.brain4it.lib.weather.davis;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialPortReader implements SerialPortEventListener
{
  private final SerialPort serialPort;
  private final RingBuffer buffer;

  public SerialPortReader(SerialPort serialPort, RingBuffer buffer)
  {
    this.serialPort = serialPort;
    this.buffer = buffer;
  }

  @Override
  public void serialEvent(SerialPortEvent event)
  {
    if (event.isRXCHAR())
    {
      int len = event.getEventValue();
      try
      {
        int buf[] = serialPort.readIntArray(len);
        for (int i = 0; i < len; i++)
        {
          buffer.write(buf[i]);
        }
      } 
      catch (SerialPortException ex)
      {
        // ignore
      }
    }
  }
}
