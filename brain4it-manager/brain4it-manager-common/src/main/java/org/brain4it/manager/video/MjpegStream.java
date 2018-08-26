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
package org.brain4it.manager.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author realor
 */
public class MjpegStream
{
  private final InputStream input;
  private String boundary;
  private static final String CONTENT_TYPE = "multipart/x-mixed-replace";
  private static final String BOUNDARY = "boundary=";

  private String frameContentType;
  private int frameContentLength;
  private ByteArrayOutputStream output = new ByteArrayOutputStream();
  private byte[] boundaryBytes;

  public MjpegStream(String url) throws Exception
  {
    this(new URL(url));
  }
  
  public MjpegStream(URL url) throws Exception
  {
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.connect();
    String contentType = conn.getHeaderField("Content-Type");
    if (contentType != null && contentType.startsWith(CONTENT_TYPE))
    {
      int index = contentType.indexOf(BOUNDARY);
      if (index == -1) 
        throw new IOException("Boundary not found: " + contentType);
      
      boundary = contentType.substring(index + BOUNDARY.length());
      if (!boundary.startsWith("--")) boundary = "--" + boundary;
      boundaryBytes = boundary.getBytes();
      this.input = conn.getInputStream();
    }
    else
    {
      throw new IOException("Unsupported video stream: " + contentType);
    }
  }
  
  public byte[] readFrame() throws IOException
  {
    readHeader();
    return readUntilNextBoundary();
  }

  public String getFrameContentType()
  {
    return frameContentType;
  }
  
  public int getFrameContentLength()
  {
    return frameContentLength;
  }
  
  public void close() throws IOException
  {
    input.close();
  }
  
  private void readHeader() throws IOException
  {
    frameContentType = null;
    frameContentLength = -1;
    boolean headerFound = false;
    String line = readLine();
    while (line.length() > 0 || !headerFound)
    {
      int index = line.indexOf(":");
      if (index != -1)
      {
        String name = line.substring(0, index);
        String value = line.substring(index + 1).trim();
        if (name.equalsIgnoreCase("Content-Type"))
        {
          frameContentType = value;
          headerFound = true;
        }
        else if (name.equalsIgnoreCase("Content-Length"))
        {
          frameContentLength = Integer.parseInt(value);
          headerFound = true;
        }
      }
      line = readLine();   
    }
  }

  private String readLine() throws IOException
  {
    StringBuilder lineBuffer = new StringBuilder();
    int ch = input.read();
    while (ch != 10 && ch != -1)
    {
      if (ch != 13)
      {
        lineBuffer.append((char)ch);
      }
      ch = input.read();
    }
    return lineBuffer.toString();
  }
  
  private byte[] readUntilNextBoundary() throws IOException
  {
    output.reset();
    int index = 0;
    int b = input.read();
    while (b != -1 && index < boundaryBytes.length)
    {
      if (b == boundary.charAt(index))
      {
        index++;
        b = input.read();
      }
      else
      {
        if (index == 0)
        {
          output.write(b);
          b = input.read();
        }
        else
        {
          output.write(boundaryBytes, 0, index);
          index = 0;
        }
      }
    }
    return index == boundaryBytes.length ? output.toByteArray() : null;
  }  
}
