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

import org.brain4it.lib.android.AndroidFunction;
import org.brain4it.lib.android.BeepFunction;
import org.brain4it.lib.android.AudioPwmFunction;
import org.brain4it.lib.android.SensorFunction;
import org.brain4it.lib.android.VibrateFunction;
import org.brain4it.lang.Function;
import org.brain4it.lib.android.AndroidNotifyFunction;
import org.brain4it.lib.android.GpsFunction;
import org.brain4it.lib.android.SpeakerFunction;

/**
 *
 * @author realor
 */
public class AndroidLibrary extends Library
{
  @Override
  public String getName()
  {
    return "Android";
  }  
  
  
  @Override
  public void load()
  {
    functions.put("audio-pwm", new AudioPwmFunction());
    functions.put("android-notify", new AndroidNotifyFunction());
    functions.put("beep", new BeepFunction());
    functions.put("gps", new GpsFunction());
    functions.put("sensor", new SensorFunction());
    functions.put("speaker", new SpeakerFunction());
    functions.put("vibrate", new VibrateFunction());
    if (isAvailable("android.speech.tts.TextToSpeech"))
    {
      Function function = createFunction(
        AndroidFunction.class.getPackage().getName() + ".TtsFunction");
      if (function != null)
      {
        functions.put("tts", function);
      }
    }
  }
  
  @Override
  public void unload()
  {
    for (Function function : functions.values())
    {
      ((AndroidFunction)function).cleanup();
    }
  }
  
  private Function createFunction(String className)
  {
    try
    {
      Class cls = Class.forName(className);
      return (Function)cls.newInstance();
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  private boolean isAvailable(String className)
  {
    try
    {
      Class.forName(className);
      return true;
    }
    catch (Exception ex)
    {      
      return false;
    }
  }
}
