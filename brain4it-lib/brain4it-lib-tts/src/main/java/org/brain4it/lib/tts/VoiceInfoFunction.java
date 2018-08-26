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
import java.util.Map;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lib.TtsLibrary;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class VoiceInfoFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    String voiceName;
    if (args.size() > 1)
    {    
      voiceName = Utils.toString(context.evaluate(args.get(1)));
    }
    else
    {
      voiceName = TtsLibrary.DEFAULT_VOICE;
    }
    Map<String, Voice> voicesInUse = TtsLibrary.getVoicesInUse();
    Voice voice = (Voice)voicesInUse.get(voiceName);
    if (voice == null)
    {
      VoiceManager voiceManager = VoiceManager.getInstance();
      voice = voiceManager.getVoice(voiceName);
    }
    BList voiceInfo = null;
    if (voice != null) 
    {
      voiceInfo = getVoiceInfo(voice);
    }
    return voiceInfo;    
  }  
  
  private BList getVoiceInfo(Voice voice)
  {
    BList voiceInfo = new BList();
    voiceInfo.put("name", voice.getName());
    voiceInfo.put("description", voice.getDescription());
    voiceInfo.put("organization", voice.getOrganization());
    voiceInfo.put("gender", voice.getGender().toString());
    voiceInfo.put("age", voice.getAge().toString());
    voiceInfo.put("domain", voice.getDomain());
    voiceInfo.put("locale", voice.getLocale().toString());
    voiceInfo.put("rate", voice.getRate());
    voiceInfo.put("pitch", voice.getPitch());
    voiceInfo.put("pitch-range", voice.getPitchRange());
    
    return voiceInfo;
  }
}
