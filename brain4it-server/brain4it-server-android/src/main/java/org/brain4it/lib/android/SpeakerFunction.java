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
package org.brain4it.lib.android;

import static android.content.Context.AUDIO_SERVICE;
import android.media.AudioManager;
import static android.media.AudioManager.MODE_IN_CALL;
import static android.media.AudioManager.MODE_NORMAL;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;

/**
 *
 * @author realor
 */
public class SpeakerFunction extends AndroidFunction
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    boolean speakerOn;
    AndroidService service = AndroidService.getInstance();      
    AudioManager audioManager = 
      (AudioManager)service.getSystemService(AUDIO_SERVICE);

    if (args.size() > 1)
    {
      speakerOn = Utils.toBoolean(context.evaluate(args.get(1)));

      if (speakerOn)
      {
        audioManager.setMode(MODE_IN_CALL);      
        audioManager.setSpeakerphoneOn(true);
      }
      else
      {
        audioManager.setMode(MODE_NORMAL);      
        audioManager.setSpeakerphoneOn(false);        
      }
    }
    else
    {
      speakerOn = audioManager.isSpeakerphoneOn();
    }
    return speakerOn;
  } 
}
