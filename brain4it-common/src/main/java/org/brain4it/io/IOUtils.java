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
package org.brain4it.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author realor
 */
public class IOUtils
{
  /**
   * Reads fully an InputStream and returns its content as an array of bytes.
   * @param is the InputStream to read
   * @return a byte array that contains the data read from the InputStream
   * @throws IOException
   */
  public static byte[] readBytes(InputStream is) throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    copy(is, os);

    return os.toByteArray();
  }

  /**
   * Reads fully an InputStream and returns its content as a String.
   *
   * @param is the InputStream to read
   * @param charset the InputStream charset
   * @return a string that contains the data read from the InputStream.
   * @throws IOException
   */
  public static String readString(InputStream is, String charset)
    throws IOException
  {
    try
    {
      char[] buffer = new char[1024];
      StringBuilder out = new StringBuilder();
      Reader in = new InputStreamReader(is, charset);
      int numRead = in.read(buffer);
      while (numRead != -1)
      {
        out.append(buffer, 0, numRead);
        numRead = in.read(buffer);
      }
      return out.toString();
    }
    finally
    {
      is.close();
    }
  }

  /**
   * Writes bytes into an OutputStream and closes that stream.
   *
   * @param os the OutputStream to write to
   * @param bytes the bytes to write
   * @throws IOException
   */
  public static void writeBytes(byte[] bytes, OutputStream os)
    throws IOException
  {
    try
    {
      os.write(bytes);
      os.flush();
    }
    finally
    {
      os.close();
    }
  }

  /**
   * Writes a String into an OutputStream and closes that stream.
   *
   * @param os the OutputStream to write to
   * @param data the string to write
   * @param charset the string charset
   * @throws IOException
   */
  public static void writeString(String data, String charset, OutputStream os)
   throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(os, charset);
    try
    {
      writer.append(data);
    }
    finally
    {
      writer.close();
    }
  }

  /**
   * Writes the contents from an InputStream into an OutputStream and closes
   * both streams.
   * @param is the source InputStream
   * @param os the target OutputStream
   * @throws IOException
   */
  public static void copy(InputStream is, OutputStream os) throws IOException
  {
    try
    {
      try
      {
        byte[] buffer = new byte[1024];
        int numRead = is.read(buffer);
        while (numRead > 0)
        {
          os.write(buffer, 0, numRead);
          numRead = is.read(buffer);
        }
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    finally
    {
      is.close();
    }
  }

  public static boolean isValidURL(String urlString)
  {
    return isValidURL(urlString, false);
  }

  public static boolean isValidURL(String urlString, boolean onlyHttp)
  {
    try
    {
      URL url = new URL(urlString);
      String protocol = url.getProtocol();
      if (onlyHttp)
      {
        if (!protocol.equals("http") && !protocol.equals("https"))
          return false;
      }
      if (protocol.equals("file"))
      {
        String file = url.getFile();
        return file != null && file.length() > 1;
      }
      else
      {
        String host = url.getHost();
        return host != null && host.length() > 0;
      }
    }
    catch (MalformedURLException ex)
    {
      return false;
    }
  }
}
