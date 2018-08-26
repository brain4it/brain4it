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

package org.brain4it.lib.raspberrypi;

import com.pi4j.wiringpi.Gpio;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class GpioModeFunction implements Function
{
  public static final String IN = "in";
  public static final String OUT = "out";
  public static final String PWM = "pwm";
  public static final String UP = "up";
  public static final String DOWN = "down";
  public static final String TRI = "tri";
  public static final String OFF = "off";

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);    
    
    int pinNum = Utils.toNumber(context.evaluate(args.get(1))).intValue();
    String mode = Utils.toString(context.evaluate(args.get(2)));
    if (mode == null)
    {
      throw new Exception("Mode can not be null");
    }
    switch (mode)
    {
      case IN:
        Gpio.pinMode(pinNum, Gpio.INPUT);
        return IN;
      case OUT:
        Gpio.pinMode(pinNum, Gpio.OUTPUT);
        return OUT;
      case PWM:
        Gpio.pinMode(pinNum, Gpio.PWM_OUTPUT);
        Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
        return PWM;
      case UP:
        Gpio.pullUpDnControl(pinNum, Gpio.PUD_UP);
        return UP;
      case DOWN:
        Gpio.pullUpDnControl(pinNum, Gpio.PUD_DOWN);
        return DOWN;
      case TRI:
      case OFF:
        Gpio.pullUpDnControl(pinNum, Gpio.PUD_OFF);
        return TRI;
      default:
        throw new Exception("Invalid mode " + mode);
    }
  }
}
