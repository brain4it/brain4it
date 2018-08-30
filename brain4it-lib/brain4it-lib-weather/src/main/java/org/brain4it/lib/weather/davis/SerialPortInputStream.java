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

import java.io.IOException;
import java.io.InputStream;

public class SerialPortInputStream extends InputStream
{
  private final RingBuffer buffer;

  public SerialPortInputStream(RingBuffer buffer)
  {
    this.buffer = buffer;
  }

  @Override
  public int read() throws IOException
  {
    return buffer.read();
  }

  @Override
  public void close() throws IOException
  {
    buffer.clear();
    super.close();
  }
}
