/*
 *  Copyright 2006 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.brain4it.lib.weather.davis;

public class RingBuffer
{
  private static final int BUFFER_SIZE = 2048;

  private final int[] buffer = new int[BUFFER_SIZE];
  private int readIndex = 0;
  private int writeIndex = 0;

  public void write(int b)
  {
    buffer[writeIndex++ % BUFFER_SIZE] = b;
  }

  public void write(int[] b)
  {
    for (int i = 0; i < b.length; i++)
    {
      write(b[i]);
    }
  }

  public int read()
  {
    for (int i = 0; i < 3; i++)
    {
      if (readIndex < writeIndex)
      {
        return buffer[readIndex++ % BUFFER_SIZE];
      } 
      else
      {
        // Data not ready yet, wait a while.
        try
        {
          Thread.sleep(1200);
        } 
        catch (InterruptedException e)
        {
          // Ignore.
        }
      }
    }
    return -1;
  }

  public int[] read(int len)
  {
    int[] b = new int[len];
    for (int i = 0; i < len; i++)
    {
      b[i] = read();
    }
    return b;
  }

  public void clear()
  {
    readIndex = 0;
    writeIndex = 0;
  }
}
