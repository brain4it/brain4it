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

package org.brain4it.lib.tts;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.MixerAudioPlayer;
import java.util.Map;
import javax.sound.sampled.Mixer;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lib.TtsLibrary;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class TtsFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    Object arg = context.evaluate(args.get(1));
    String text = arg == null ? null : Utils.toString(arg);
    String voiceName = (String)context.evaluate(args.get("voice"));
    
    if (voiceName == null)
    {
      voiceName = TtsLibrary.DEFAULT_VOICE;
    }
    Map<String, Voice> voicesInUse = TtsLibrary.getVoicesInUse();
    Voice voice = voicesInUse.get(voiceName);
    
    if (text == null) // deallocate voice
    {
      if (voice != null)
      {
        voice.deallocate();
        voicesInUse.remove(voiceName);
      }
    }
    else // allocate voice & speak text
    {
      if (voice == null)
      {
        String baseVoiceName;
        int index = voiceName.indexOf(TtsLibrary.VOICE_NAME_SEPARATOR);
        if (index != -1)
        {
          baseVoiceName = voiceName.substring(0, index);
        }
        else
        {
          baseVoiceName = voiceName;
        }
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(baseVoiceName);
        if (voice == null)
          throw new Exception("Voice " + baseVoiceName + " not found.");

        String mixerName = (String)context.evaluate(args.get("mixer"));

        Mixer mixer;
        if (mixerName == null)
        {
          mixer = TtsLibrary.getDefaultMixer();
        }
        else
        {
          mixer = TtsLibrary.findMixer(mixerName);
        }

        voice.setAudioPlayer(new MixerAudioPlayer(mixer));

        voice.allocate();

        voicesInUse.put(voiceName, voice);
      }

      Number volume = Utils.toNumber(context.evaluate(args.get("volume")));
      Number rate = Utils.toNumber(context.evaluate(args.get("rate")));
      Number pitch = Utils.toNumber(context.evaluate(args.get("pitch")));      
      Number pitchRange = Utils.toNumber(context.evaluate(args.get("pitch-range")));      
      
      if (volume != null)
      {
        voice.setVolume(volume.floatValue());
      }
      if (rate != null)
      {
        voice.setRate(rate.floatValue());
      }
      if (pitch != null)
      {
        voice.setPitch(pitch.floatValue());
      }
      if (pitchRange != null)
      {
        voice.setPitchRange(pitchRange.floatValue());
      }

      voice.speak(text);
    }
    return text;
  }
}
