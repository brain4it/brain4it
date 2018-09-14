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

import static org.brain4it.lib.weather.WeatherStation.*;

/**
*
* @author colladorm
*/

public class Constants
{
  public static final byte ACK = 0x06;
  public static final byte NAK = 0x21;

  /* read methods */
  public static final int WORD_METHOD = 0;
  public static final int PRESSURE_METHOD = 1;
  public static final int TEMPERATURE_METHOD = 2;
  public static final int EXTRA_TEMPERATURE_METHOD = 3;
  public static final int HUMIDITY_METHOD = 4;
  public static final int WIND_SPEED_METHOD = 5;
  public static final int UV_INDEX_METHOD = 6;
  public static final int SOLAR_RADIATION_METHOD = 7;
  public static final int DAY_RAIN_METHOD = 8;

  /* Station model names */
  public static final String WIZARDIII = "Wizard III";
  public static final String WIZARDII = "Wizard II";
  public static final String MONITOR = "Monitor";
  public static final String PERCEPTION = "Perception";
  public static final String GROWEATHER = "GroWeather";
  public static final String ENERGY = "Energy Enviromonitor";
  public static final String HEALTH = "Health Enviromonitor";
  public static final String PRO = "Vantage Pro or Vantage Pro 2";
  public static final String VUE = "Vantage Vue";
  
  public static final StationModel[] STATION_MODELS = 
  {
    new StationModel(PRO, new LoopEntry[]
    {
      new LoopEntry(PRESSURE, PRESSURE_METHOD, 7),
	    new LoopEntry(TEMPERATURE, TEMPERATURE_METHOD, 12),
      new LoopEntry(TEMPERATURE_INSIDE, TEMPERATURE_METHOD, 9),
	    new LoopEntry(HUMIDITY, HUMIDITY_METHOD, 33),
      new LoopEntry(HUMIDITY_INSIDE, HUMIDITY_METHOD, 11),
	    new LoopEntry(WIND_SPEED, WIND_SPEED_METHOD, 14),
	    new LoopEntry(WIND_DIRECTION, WORD_METHOD, 16),
	    new LoopEntry(UV_INDEX, UV_INDEX_METHOD, 43),
	    new LoopEntry(SOLAR_RADIATION, SOLAR_RADIATION_METHOD, 44),
	    new LoopEntry(RAIN, DAY_RAIN_METHOD, 50)
    })
  };
  
  static public class StationModel
  {
    String name;
    LoopEntry[] loopEntries;
    
    public StationModel(String name, LoopEntry[] loopEntries)
    {
      this.name = name;
      this.loopEntries = loopEntries;
    }
  }
  
  static public class LoopEntry
  {
    String magnitude;
    int method;
    int offset;

    LoopEntry(String magnitude, int method, int offset)
    {
      this.magnitude = magnitude;
      this.method = method;
      this.offset = offset;
    }
  }
}
