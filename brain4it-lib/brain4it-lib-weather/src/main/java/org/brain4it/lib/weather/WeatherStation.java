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
package org.brain4it.lib.weather;

import java.io.IOException;
import org.brain4it.lang.BList;

/**
 *
 * @author realor
 */
public interface WeatherStation
{
  public static final String STATION_TYPE = "station-type";
  public static final String TIMESTAMP = "timestamp";
  public static final String DATE = "date";
  public static final String PRESSURE = "pressure";
  public static final String HUMIDITY = "humidity";
  public static final String HUMIDITY_INSIDE = "humidity-inside";
  public static final String TEMPERATURE = "temperature";
  public static final String TEMPERATURE_INSIDE = "temperature-inside";
  public static final String RAIN = "rain";
  public static final String SOLAR_RADIATION = "solar-radiation";
  public static final String UV_RADIATION = "uv-radiation";
  public static final String WIND_SPEED = "wind-speed";
  public static final String WIND_SPEED_AVG = "wind-speed-avg";
  public static final String WIND_DIRECTION = "wind-direction";

  public BList readData(String address, BList options) throws IOException;
}
