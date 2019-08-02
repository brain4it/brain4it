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
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static android.media.AudioManager.STREAM_MUSIC;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import java.util.HashMap;
import java.util.Locale;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;

/**
 *
 * @author realor
 */
public class TtsFunction extends AndroidFunction
{
  private static final double DEFAULT_VOLUME = 50;
  private final TextToSpeech tts;
  private final UtteranceProgressListener progressListener =
  new UtteranceProgressListener()
  {
    @Override
    public void onStart(String utteranceId)
    {
    }

    @Override
    public void onDone(String utteranceId)
    {
      synchronized (TtsFunction.this)
      {
        TtsFunction.this.notifyAll();
      }
    }

    @Override
    public void onError(String utteranceId)
    {
      synchronized (TtsFunction.this)
      {
        TtsFunction.this.notifyAll();
      }
    }
  };

  public TtsFunction()
  {
    AndroidService service = AndroidService.getInstance();
    tts = new TextToSpeech(service, new OnInitListener()
    {
      @Override
      public void onInit(int status)
      {
        if (status == TextToSpeech.SUCCESS)
        {
          tts.setOnUtteranceProgressListener(progressListener);
          Log.i(TAG, "tts initialized");
        }
        else
        {
          Log.e(TAG, "tts not initialized");
        }
      }
    });
  }

  @Override
  public synchronized Object invoke(Context context, BList args)
    throws Exception
  {
    if (tts != null)
    {
      Utils.checkArguments(args, 1);
      String text = Utils.toString(context.evaluate(args.get(1)));

      // set language
      String language = (String)context.evaluate(args.get("language"));
      if (language != null)
      {
        tts.setLanguage(new Locale(language));
      }
      else
      {
        tts.setLanguage(Locale.getDefault());
      }

      // set volume
      double volume = DEFAULT_VOLUME;
      Number number = (Number)context.evaluate(args.get("volume"));
      if (number != null)
      {
        volume = number.doubleValue();
        if (volume > 100) volume = 100;
        else if (volume < 0) volume = 0;
      }
      AndroidService service = AndroidService.getInstance();
      AudioManager audioManager =
        (AudioManager)service.getSystemService(AUDIO_SERVICE);
      int maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC);
      int volumeIndex = (int)Math.round(maxVolume * volume / 100.0);
      audioManager.setStreamVolume(STREAM_MUSIC, volumeIndex, 0);

      // speech text
      HashMap<String, String> map = new HashMap<String, String>();
      map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");
      tts.speak(text, QUEUE_FLUSH, map);
      wait(); // wait for utterance termination

      return text;
    }
    return null;
  }

  @Override
  public void cleanup()
  {
    try
    {
      if (tts != null)
      {
        tts.stop();
        tts.shutdown();
      }
    }
    catch (Exception ex)
    {
      Log.e(TAG, "cleanup error", ex);
    }
  }
}
