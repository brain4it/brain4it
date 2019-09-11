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

import java.io.IOException;
import org.brain4it.lib.weather.davis.Constants.LoopEntry;
import static org.brain4it.lib.weather.davis.Constants.*;

/**
 * Misc helper functions.
 *
 */
public final class VantageUtil
{
  private VantageUtil()
  {
  }

  /**
   * Convert degrees Celcius to degrees Fahrenheit.
   *
   * @param celcius the temperature in Celcius.
   * @return the temperature in Fahrenheit.
   */
  private static double celcius2fahrenheit(double celcius)
  {
    return celcius * 9 / 5 + 32;
  }

  /**
   * Convert degrees Fahrenheit to degrees Celcius.
   *
   * @param f the temperature in Fahrenheit.
   * @return the temperature in Celcius.
   */
  private static double fahrenheit2celcius(double f)
  {
    return (f - 32) * 5 / 9.0;
  }

  /**
   * Convert pressure as inch Hg to millibar.
   *
   * @param inchHg the inch Hg value to convert.
   * @return the pressure as millibar.
   */
  private static int inchHg2millibar(double inchHg)
  {
    return (int)Math.round(inchHg / 0.02953007);
  }

  /**
   * Convert velocity meter/second to miles/hour.
   *
   * @param ms meter per second
   * @return miles per hour
   */
  private static int ms2mph(double ms)
  {
    return (int)Math.round(ms * 2.24);
  }

  /**
   * Calculate wind chill. The "Chilled" air temperature can also be expressed
   * as a function of wind velocity and ambient air temperature.
   *
   * @param tempC temperature in degrees Celcius
   * @param windSpeed wind speed in meters per second (m/s).
   * @return chilled air temperature
   */
  private static double calculateWindChill(final double tempC,
    final double windSpeed)
  {
    double tempF = celcius2fahrenheit(tempC);
    double mph = ms2mph(windSpeed);
    if (tempF < 50.0 && mph > 3.0)
    {
      // Wind chill is only defined for temperatures below 50F and
      // wind speed above 3 MPH.
      double chillF = 35.74 + (0.6215 * tempF) - (35.75 * Math.pow(mph, 0.16)) +
         (0.4275 * tempF * Math.pow(mph, 0.16));
      double chillC = (Math.round(fahrenheit2celcius(chillF) * 10)) / 10.0;
      return chillC;
    }
    return tempC;
  }

  private static int parseWord(byte[] buf, int offset)
  {
    int firstByte = (0x000000ff & ((int)buf[offset + 1]));
    int secondByte = (0x000000ff & ((int)buf[offset]));
    return (firstByte << 8 | secondByte);
  }

  private static double parseTemperature(byte[] buf, int offset)
  {
    int f = ((int)buf[offset + 1] << 8) | (buf[offset] & 0xff) & 0xffff;
    if (f == 32767)
    {
      return 0.0;
    }

    double c = VantageUtil.fahrenheit2celcius(f / 10.0);

    return (double)Math.round(c * 100d) / 100d;
  }

  private static double parseExtraTemperature(byte[] buf, int offset)
  {
    int f = (int)(buf[offset] & 0xff);
    if (f == 255)
    {
      return 0.0;
    }
    return VantageUtil.fahrenheit2celcius(f - 90.0);
  }

  private static double parseRain(byte[] buf, int offset)
  {
    // This value is sent as number of rain clicks (0.2mm or 0.078in).
    // For example, 256 can represent 2.56 inches/hour.
    // So rain in mm: value * 0.2
    int word = parseWord(buf, offset);
    double value = word * 0.2;
    return (double)Math.round(value * 100d) / 100d;
  }

  private static int parseBarometer(byte[] buf, int offset)
  {
    int word = parseWord(buf, offset);
    double inchHg = word / 1000.0;

    return VantageUtil.inchHg2millibar(inchHg);
  }

  private static double parseWindSpeed(byte[] buf, int offset)
  {
    int mph = buf[offset];
    int kh = (int)((mph != -1 ? mph * 0.45 : 0) * 1.60934);
    return kh;
  }

  private static int parseUV(byte[] buf, int offset)
  {
    int uv = buf[offset];
    return (int)(uv == -1 ? 0 : uv / 10.0);
  }

  private static int parseHumidity(byte[] buf, int offset)
  {
    int hum = buf[offset];
    return hum == -1 ? 0 : hum;
  }

  public static Object parse(int method, byte[] buf, int offset)
    throws IOException
  {
    switch (method)
    {
      case WORD_METHOD:
        return parseWord(buf, offset);

      case SOLAR_RADIATION_METHOD:
        int solar = parseWord(buf, offset);
        if (solar == 32767)
        {
          solar = 0;
        }
        return solar;

      case TEMPERATURE_METHOD:
        return parseTemperature(buf, offset);

      case EXTRA_TEMPERATURE_METHOD:
        return parseExtraTemperature(buf, offset);

      case PRESSURE_METHOD:
        return parseBarometer(buf, offset);

      case HUMIDITY_METHOD:
        return parseHumidity(buf, offset);

      case DAY_RAIN_METHOD:
        return parseRain(buf, offset);

      case WIND_SPEED_METHOD:
        return parseWindSpeed(buf, offset);

      case UV_INDEX_METHOD:
        return parseUV(buf, offset);

      default:
        throw new IOException("VantageUtil.select: Invalid method");
    }
  }

  public static LoopEntry[] getStationLoopEntries(String station)
    throws IOException
  {
    int i = 0;
    while (i < STATION_MODELS.length)
    {
      if (STATION_MODELS[i].name.equals(station))
      {
        return STATION_MODELS[i].loopEntries;
      }
      i++;
    }
    throw new IOException("Station model not supported: " + station);
  }
}
