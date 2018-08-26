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

import com.pi4j.wiringpi.Gpio;
import org.brain4it.lib.raspberrypi.DelayFunction;
import org.brain4it.lib.raspberrypi.GpioModeFunction;
import org.brain4it.lib.raspberrypi.GpioPulseInFunction;
import org.brain4it.lib.raspberrypi.GpioPwmConfigFunction;
import org.brain4it.lib.raspberrypi.GpioPwmFunction;
import org.brain4it.lib.raspberrypi.GpioReadFunction;
import org.brain4it.lib.raspberrypi.GpioWriteFunction;
import org.brain4it.lib.raspberrypi.MelodyFunction;
import org.brain4it.lib.raspberrypi.SoftPwmFunction;

/**
 *
 * @author realor
 */
public class RaspberryPiLibrary extends Library
{
  @Override
  public String getName()
  {
    return "RaspberryPi";
  }

  @Override
  public void load()
  {
    if (Gpio.wiringPiSetup() == 0)
    {
      functions.put("gpio-mode", new GpioModeFunction());
      functions.put("gpio-read", new GpioReadFunction());
      functions.put("gpio-write", new GpioWriteFunction());
      functions.put("gpio-pwmc",new GpioPwmConfigFunction());
      functions.put("gpio-pwm", new GpioPwmFunction());
      functions.put("gpio-pulse-in", new GpioPulseInFunction());
      functions.put("soft-pwm", new SoftPwmFunction());
      functions.put("melody", new MelodyFunction());
      functions.put("delay", new DelayFunction());
    }
    else throw new RuntimeException("WiringPi setup failed!");
  }
}