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

import static android.media.AudioManager.STREAM_MUSIC;
import static android.content.Context.AUDIO_SERVICE;
import android.media.AudioManager;
import android.media.MediaPlayer;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.server.android.AndroidService;
import org.brain4it.server.android.R;

/**
 *
 * @author realor
 */
public class BeepFunction extends AndroidFunction 
  implements MediaPlayer.OnCompletionListener
{
  @Override
  public synchronized Object invoke(Context context, BList args) throws Exception
  {
    double volume = 50;
    
    if (args.size() > 1)
    {
      Number number = (Number)context.evaluate(args.get(1));
      volume = number.doubleValue();    
      if (volume > 100) volume = 100;
    }
    
    AndroidService service = AndroidService.getInstance();    
    AudioManager audioManager = 
      (AudioManager)service.getSystemService(AUDIO_SERVICE);
    int maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC);
    int volumeIndex = (int)Math.round(maxVolume * volume / 100.0);
    audioManager.setStreamVolume(STREAM_MUSIC, volumeIndex, 0);
    
    MediaPlayer player = MediaPlayer.create(service, R.raw.beep);
    player.setOnCompletionListener(this);
    player.start();
    wait();
    return null;
  }

  @Override
  public void onCompletion(MediaPlayer mediaPlayer)
  {
    mediaPlayer.release();
    synchronized (this)
    {
      notifyAll();
    }
  }
}
