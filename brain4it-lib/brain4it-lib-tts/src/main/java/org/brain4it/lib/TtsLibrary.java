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

import com.sun.speech.freetts.Voice;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import org.brain4it.lib.tts.MixersFunction;
import org.brain4it.lib.tts.TtsFunction;
import org.brain4it.lib.tts.VoiceInfoFunction;
import org.brain4it.lib.tts.VoicesFunction;

/**
 *
 * @author realor
 */
public class TtsLibrary extends Library
{
  public static final String DEFAULT_VOICE = "kevin16";
  public static final String VOICE_NAME_SEPARATOR = "/";
  private static Mixer defaultMixer;
  private static final HashMap<String, Voice> voicesInUse = 
    new HashMap<String, Voice>();

  @Override
  public String getName()
  {
    return "Speech";
  }
  
  @Override
  public void load()
  {
    System.setProperty(
      "com.sun.speech.freetts.audio.AudioPlayer.drainWorksProperly", "true");

    if (System.getProperty("freetts.voices") == null)
    {
      System.setProperty("freetts.voices", 
        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
    }

    String mixerName = System.getProperty("freetts.mixer");
    if (mixerName != null)
    {
      defaultMixer = findMixer(mixerName);
    }
    
    functions.put("tts", new TtsFunction());
    functions.put("voices", new VoicesFunction());
    functions.put("voice-info", new VoiceInfoFunction());
    functions.put("mixers", new MixersFunction());
  }
  
  public static Mixer getDefaultMixer()
  {
    return defaultMixer;
  }
  
  public static Mixer findMixer(String name)
  {
    Mixer mixer = null;
    Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
    int i = 0;
    while (mixer == null && i < mixerInfos.length)
    {
      Mixer.Info mixerInfo = mixerInfos[i];
      if (mixerInfo.getName().contains(name))
      {
        mixer = AudioSystem.getMixer(mixerInfo);        
      }
      i++;
    }
    return mixer;
  }
  
  public static Map<String, Voice> getVoicesInUse()
  {
    return voicesInUse;
  }
}
