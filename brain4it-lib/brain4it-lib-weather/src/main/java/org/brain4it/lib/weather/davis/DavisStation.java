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
package org.brain4it.lib.weather.davis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import org.brain4it.lang.BList;
import org.brain4it.lib.weather.WeatherStation;

/**
 *
 * @author colladorm, realor
 */
public class DavisStation implements WeatherStation
{
  protected static final String IN = "< ";
  protected static final String OUT = "> ";
  protected InputStream in;
  protected OutputStream out;
  
  @Override
  public synchronized BList readData(String address, BList options) 
    throws IOException
  {
    // address format: <host>:<port> or <host>
    // examples: "10.1.1.12:22222" or "10.1.1.12"
    
    if (address == null)
    {
      throw new IOException("host address must be specified");
    }
    
    BList data = null;
    String[] parts = address.split(":");
    String host = parts[0];
    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 22222;
    
    Socket socket = new Socket(host, port);
    try
    {
      socket.setSoTimeout(5000);
      in = socket.getInputStream();
      try
      {
        out = socket.getOutputStream();
        try
        {
          data = execute();
        }
        finally
        {
          cleanup();
          out.close();
        }
      }
      finally
      {
        in.close();
      }
    }
    finally
    {
      socket.close();
    }
    return data;
  }  

  protected void cleanup()
  {
    try
    {
      out.write("quit\n".getBytes());
    } 
    catch (IOException e)
    {
      // ignore
    }
  }

  protected BList execute() throws IOException
  {
    byte[] buf;

    if (!wakeup())
    {
      return null;
    }

    // Determine station type.
    out.write(new byte[]
    {
      'W', 'R', 'D', 0x12, 0x4d, '\n'
    });
    buf = readBytes(2);

    if (buf[0] != Constants.ACK)
    {
      throw new IOException("Invalid response");
    }

    String stationType = getStationType(buf[1]);

    if (stationType == null)
    {
      throw new IOException("Unsupported station type: " + 
        String.valueOf((int) buf[1]));
    } 
    else
    {
      if ("Vantage Pro or Vantage Pro 2".equals(stationType))
      {
        BList current = loop();
        current.put(STATION_INFO, "Vantage Pro or Vantage Pro 2");
        return current;
      } 
      else
      {
        throw new IOException("Unsupported station type: " + 
          String.valueOf((int)buf[1]));
      }
    }
  }

  protected String getStationType(int n)
  {
    String stationType = null;
    switch (n)
    {
      case 0:
        stationType = "Wizard III";
        break;
      case 1:
        stationType = "Wizard II";
        break;
      case 2:
        stationType = "Monitor";
        break;
      case 3:
        stationType = "Perception";
        break;
      case 4:
        stationType = "GroWeather";
        break;
      case 5:
        stationType = "Energy Enviromonitor";
        break;
      case 6:
        stationType = "Health Enviromonitor";
        break;
      case 16:
        stationType = "Vantage Pro or Vantage Pro 2";
        break;
      case 17:
        stationType = "Vantage Vue";
        break;
    }
    return stationType;
  }

  protected void writeString(String string) throws IOException
  {
    out.write(string.getBytes());
    out.flush();
  }

  protected void expectString(String string) throws IOException
  {
    sleep(250);
    int length = string.length();
    byte[] buf = readBytes(length);
    String s = new String(buf);
    if (!string.equals(s))
    {
      throw new IOException("Invalid response: " + escape(s));
    }
  }

  protected byte[] readBytes(int length) throws IOException
  {
    byte[] buf = new byte[length];

    for (int i = 0; i < length; i++)
    {
      int c = in.read();
      if (c != -1)
      {
        buf[i] = (byte) (c & 0xff);
      } else
      {
        throw new IOException("Unexpected EOF");
      }
    }
    return buf;
  }

  protected boolean wakeup() throws IOException
  {
    boolean awake = false;
    int i = 0;
    while (awake == false && i++ < 3)
    {
      writeString("\n");
      try
      {
        expectString("\n\r");
        awake = true;
      } 
      catch (IOException e)
      {
      }
      sleep(2000);
    }
    return awake;
  }

  protected static void sleep(long ms)
  {
    try
    {
      Thread.sleep(ms);
    } 
    catch (InterruptedException ex)
    {
    }
  }

  protected int parseWord(byte[] buf, int offset)
  {
    int firstByte = (0x000000ff & ((int) buf[offset + 1]));
    int secondByte = (0x000000ff & ((int) buf[offset]));
    return (firstByte << 8 | secondByte);
  }

  protected double parseTemperature(byte[] buf, int offset)
  {
    int f = ((int) buf[offset + 1] << 8) | (buf[offset] & 0xff) & 0xffff;
    if (f == 32767)
    {
      return 0.0;
    }
    return VantageUtil.fahrenheit2celcius(f / 10.0);
  }

  protected double parseExtraTemperature(byte[] buf, int offset)
  {
    int f = (int) (buf[offset] & 0xff);
    if (f == 255)
    {
      return 0.0;
    }
    return VantageUtil.fahrenheit2celcius(f - 90.0);
  }

  protected double parseRain(byte[] buf, int offset)
  {
    int word = parseWord(buf, offset);
    return word / 5.0;
  }

  protected int parseBarometer(byte[] buf, int offset)
  {
    int word = parseWord(buf, offset);
    double inchHg = word / 1000.0;

    return VantageUtil.inchHg2millibar(inchHg);
  }

  protected double parseWindSpeed(byte[] buf, int offset)
  {
    int mph = buf[offset];
    return mph != -1 ? mph * 0.45 : 0;
  }

  protected int parseUV(byte[] buf, int offset)
  {
    int uv = buf[offset];
    return (int) (uv == -1 ? 0 : uv / 10.0);
  }

  protected int parseHumidity(byte[] buf, int offset)
  {
    int hum = buf[offset];
    return hum == -1 ? 0 : hum;
  }

  protected BList loop() throws IOException
  {
    writeString("LOOP 1\n");
    if (in.read() != Constants.ACK)
    {
      throw new IOException("Invalid response");
    }

    byte[] buf = readBytes(99);
    if (!CRC16.check(buf, 0, buf.length))
    {
      throw new IOException("CRC error");
    }
    return parseLoopRecord(buf);
  }

  protected BList parseLoopRecord(byte[] buf) throws IOException
  {
    if (buf[0] != 'L' || buf[1] != 'O' || buf[2] != 'O')
    {
      throw new IOException("Invalid response: " + escape(buf, 0, 3, true));
    }

    BList data = new BList();
    Date date = new Date();
    data.put(TIMESTAMP, date.getTime());
    data.put(DATE, date.toString());

    int barometer = parseBarometer(buf, 7);
    data.put(PRESSURE, barometer);

    int humidity = parseHumidity(buf, 33);
    data.put(HUMIDITY, humidity);

    humidity = parseHumidity(buf, 11);
    data.put(HUMIDITY_INSIDE, humidity);

    double temp = parseTemperature(buf, 12);
    data.put(TEMPERATURE, temp);

    temp = parseTemperature(buf, 9);
    data.put(TEMPERATURE_INSIDE, temp);

    double rain = parseRain(buf, 41);
    data.put(RAIN, rain);

    int solar = parseWord(buf, 44);
    if (solar == 32767)
    {
      solar = 0;
    }
    data.put(SOLAR_RADIATION, solar);

    int uv = parseUV(buf, 43);
    data.put(UV_RADIATION, uv);

    double wind = parseWindSpeed(buf, 18);
    data.put(WIND_SPEED_AVG, wind);

    wind = parseWindSpeed(buf, 14);
    data.put(WIND_SPEED, wind);

    int dir = parseWord(buf, 16);
    data.put(WIND_DIRECTION, dir);

    return data;
  }

  protected String escape(String string)
  {
    byte[] bytes = string.getBytes();
    return escape(bytes, 0, bytes.length, true);
  }

  protected String escape(byte[] bytes, int offset, int length, 
    boolean printWritable)
  {
    if (offset > length)
    {
      throw new IllegalArgumentException("offset " + offset + 
        " is greater than length " + length);
    }
    StringBuilder buf = new StringBuilder();
    for (int i = offset; i < (offset + length); i++)
    {
      switch (bytes[i])
      {
        case '\n':
          buf.append(printWritable ? "\\n" : "<0x0a>");
          break;
        case '\r':
          buf.append(printWritable ? "\\r" : "<0x0d>");
          break;
        case '\t':
          buf.append(printWritable ? "\\t" : "<0x09>");
          break;
        case 0x06:
          buf.append(printWritable ? "<ACK>" : "<0x06>");
          break;
        case 0x18:
          buf.append(printWritable ? "<CAN>" : "<0x18>");
          break;
        case 0x21:
          buf.append(printWritable ? "<NAK>" : "<0x21>");
          break;
        default:
          if (bytes[i] < 0x20 || bytes[i] > 0x7e || !printWritable)
          {
            String s = Integer.toHexString((int) bytes[i] & 0x000000ff);
            buf.append("<0x");
            if (s.length() == 1)
            {
              buf.append('0');
            }
            buf.append(s);
            buf.append('>');
          } 
          else
          {
            buf.append((char) bytes[i]);
          }
          break;
      }
    }
    return buf.toString();
  }
}
