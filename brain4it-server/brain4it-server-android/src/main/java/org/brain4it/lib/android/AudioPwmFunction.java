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
import static android.media.AudioFormat.CHANNEL_IN_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioTrack.MODE_STREAM;
import android.media.AudioTrack;
import android.media.AudioManager;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;

/**
 *
 * @author realor
 */
public class AudioPwmFunction extends AndroidFunction
{
  public static final double DEFAULT_PERIOD = 16;
  private SoundThread soundThread;

  @Override
  public synchronized Object invoke(Context context, BList args) 
    throws Exception
  {
    Utils.checkArguments(args, 1);    
    double volume = 0; // [0..100]
    double period = DEFAULT_PERIOD; // millis (> 1)
    double leftPulse = 0; // millis [0..period]
    double rightPulse = 0; // millis [0..period]
    Number number;

    if (args.size() > 1)
    {
      number = (Number)context.evaluate(args.get(1));
      volume = number.doubleValue();
      if (volume < 0) volume = 0;
      else if (volume > 100) volume = 100;

      if (args.size() > 2)
      {
        number = (Number)context.evaluate(args.get(2));
        period = number.doubleValue();
        if (period < 1) period = 1;

        if (args.size() > 3)
        {
          number = (Number)context.evaluate(args.get(3));
          leftPulse = number.doubleValue();
          if (leftPulse < 0) leftPulse = 0;
          else if (leftPulse > period) leftPulse = period;

          if (args.size() > 4)
          {
            number = (Number)context.evaluate(args.get(4));
            rightPulse = number.doubleValue();
            if (rightPulse < 0) rightPulse = 0;
            else if (rightPulse > period) rightPulse = period;
          }
        }
      }
    }
    if (soundThread == null && volume > 0)
    {
      soundThread = new SoundThread();
      soundThread.setParameters(volume, period, leftPulse, rightPulse);
      soundThread.start();
    }
    else if (soundThread != null)
    {
      soundThread.setParameters(volume, period, leftPulse, rightPulse);
    }
    return null;
  }

  @Override
  public void cleanup()
  {
    if (soundThread != null)
    {
      soundThread.setParameters(0, DEFAULT_PERIOD, 0, 0);
      try
      {
        soundThread.join();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  class SoundThread extends Thread
  {
    double volume = 0; // [0..100]
    double period = DEFAULT_PERIOD; // millis (> 1)
    double leftPulse = 0; // millis [0..period]
    double rightPulse = 0; // millis [0..period]

    SoundThread()
    {
      setPriority(getThreadGroup().getMaxPriority());
    }

    public synchronized void setParameters(double volume, double period,
      double leftPulse, double rightPulse)
    {
      this.volume = volume;
      this.period = period;
      this.leftPulse = leftPulse;
      this.rightPulse = rightPulse;

      AndroidService service = AndroidService.getInstance();
      AudioManager audioManager =
        (AudioManager)service.getSystemService(AUDIO_SERVICE);
      int maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC);
      int volumeIndex = (int)Math.round(maxVolume * volume / 100.0);
      audioManager.setStreamVolume(STREAM_MUSIC, volumeIndex, 0);
    }

    @Override
    public void run()
    {
      int sampleRate = AudioTrack.getNativeOutputSampleRate(STREAM_MUSIC);
      int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
        CHANNEL_IN_STEREO, ENCODING_PCM_16BIT);
      // create an audiotrack object
      AudioTrack audioTrack = new AudioTrack(STREAM_MUSIC,
        sampleRate, CHANNEL_IN_STEREO,
        ENCODING_PCM_16BIT, bufferSize, MODE_STREAM);

      short samples[] = new short[bufferSize];

      // start audio
      audioTrack.play();

      int sampleIndex = 0;
      int samplesPerCycle = 0;
      int leftSamples = 0;
      int rightSamples = 0;

      while (volume > 0)
      {
        for (int i = 0; i < bufferSize; i += 2)
        {
          if (sampleIndex == 0) // start new cycle
          {
            synchronized (this)
            {
              samplesPerCycle =
                (int)Math.round((double)sampleRate * period / 1000);
              leftSamples =
                (int)Math.round(samplesPerCycle * leftPulse / period);
              rightSamples =
                (int)Math.round(samplesPerCycle * rightPulse / period);
            }
          }
          // left channel
          if (sampleIndex < leftSamples)
          {
            samples[i] = (short)32767;
          }
          else
          {
            samples[i] = -32768;
          }
          // right channel
          if (sampleIndex < rightSamples)
          {
            samples[i + 1] = (short)32767;
          }
          else
          {
            samples[i + 1] = -32768;
          }

          sampleIndex++;
          if (sampleIndex > samplesPerCycle)
          {
            sampleIndex = 0;
          }
        }
        audioTrack.write(samples, 0, bufferSize);
      }
      audioTrack.stop();
      audioTrack.release();

      soundThread = null;
    }
  };
}
