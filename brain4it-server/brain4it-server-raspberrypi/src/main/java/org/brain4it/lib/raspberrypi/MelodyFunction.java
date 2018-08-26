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

import com.pi4j.wiringpi.SoftTone;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class MelodyFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);    
    int pinNum = Utils.toNumber(context.evaluate(args.get(1))).intValue();
    BList melody = (BList)context.evaluate(args.get(2));
    
    SoftTone.softToneCreate(pinNum);
    try
    {
      for (int note = 0; note < melody.size(); note += 2)
      {
        int freq = Utils.toNumber(melody.get(note)).intValue();
        SoftTone.softToneWrite(pinNum, freq);
        long millis = Utils.toNumber(melody.get(note + 1)).longValue();
        Thread.sleep(millis);
      }
    }
    finally
    {
      SoftTone.softToneStop(pinNum);
    }
    return null;
  }
}
