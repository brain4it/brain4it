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

package org.brain4it.server.standalone;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import static org.brain4it.server.ServerConstants.URL_CHARSET;

/**
 *
 * @author realor
 */
public class HttpRequest
{
  private final InputStream is;
  private InputStream bodyStream;
  private Reader bodyReader;
  private String method;
  private String path;
  private String version;
  private final HashMap<String, String> headers = new HashMap<String, String>();
  private String characterEncoding = "UTF-8"; // default value
  private final String remoteAddress;
  private final int remotePort;
  private final Socket socket;

  public HttpRequest(Socket socket) throws IOException
  {
    this.socket = socket;
    remoteAddress = socket.getInetAddress().getHostAddress();
    remotePort = socket.getPort();
    is = new BufferedInputStream(socket.getInputStream());
  }
  
  public Socket getSocket()
  {
    return socket;
  }

  public String getMethod()
  {
    return method;
  }

  public String getPath()
  {
    return path;
  }

  public String getVersion()
  {
    return version;
  }

  public String getRemoteAddress()
  {
    return remoteAddress;
  }

  public int getRemotePort()
  {
    return remotePort;
  }
  
  public String getHeader(String name)
  {
    return headers.get(name.toLowerCase());
  }

  public Collection<String> getHeaderNames()
  {
    return headers.keySet();
  }

  public String getCharacterEncoding()
  {
    return characterEncoding;
  }
  
  public InputStream getInputStream() throws IOException
  {
    if (bodyStream == null)
    {
      int length = 0;
      String slength = headers.get("content-length");
      if (slength != null)
      {
        try
        {
          length = Integer.parseInt(slength);
        }
        catch (NumberFormatException ex)
        {
        }
      }
      bodyStream = new BodyInputStream(is, length);
    }
    return bodyStream;
  }
  
  public Reader getReader() throws IOException
  {
    if (bodyReader == null)
    {
      String contentType = headers.get("content-type");
      if (contentType != null)
      {
        int index = contentType.indexOf("charset=");
        if (index != -1)
        {
          characterEncoding = contentType.substring(index + 8).trim();
        }
      }
      bodyReader = new InputStreamReader(getInputStream(), characterEncoding);
    }
    return bodyReader;
  }

  public void read() throws IOException
  {
    readMethodUri(is);
    readHeader(is);
  }

  public void close() throws IOException
  {
    is.close();
  }
  
  private void readMethodUri(InputStream in) throws IOException
  {
    String request = readLine(in);
    String[] parts = request.split(" ");
    if (parts.length != 3) 
      throw new BadRequestException(request);
    method = parts[0];
    try
    {
      path = URLDecoder.decode(parts[1], URL_CHARSET);
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new BadRequestException(ex.toString());
    }
    version = parts[2];
  }

  private void readHeader(InputStream in) throws IOException
  {
    String str;
    do
    {
      str = readLine(in);
      int index = str.indexOf(":");
      if (index > 0)
      {
        String name = str.substring(0, index).trim().toLowerCase();
        String value = str.substring(index + 1).trim();
        headers.put(name.toLowerCase(), value);
      }
    } while (str.length() > 0);
  }
  
  private String readLine(InputStream in) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int lineBreak = 0;
    int ch = in.read();
    while (ch != -1 && lineBreak != 2)
    {
      if (ch == 13)
      {
        lineBreak = 1;
        ch = in.read();
      }
      else if (ch == 10 && lineBreak == 1)
      {
        lineBreak = 2; // end
      }
      else
      {
        out.write(ch);
        ch = in.read();
      }
    }
    return out.toString(); // ASCII Encoding
  }
  
  static class BodyInputStream extends InputStream
  {
    private final InputStream is;
    private final int length;
    private int totalRead;
    
    BodyInputStream(InputStream is, int length)
    {
      this.is = is;
      this.length = length;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException 
    {
      if (b == null)
      {
        throw new NullPointerException();
      }
      else if (off < 0 || len < 0 || len > b.length - off) 
      {
        throw new IndexOutOfBoundsException();
      } 
      else if (len == 0) 
      {
        return 0;
      }
      else if (totalRead >= length)
      {
        return -1;
      }
      int numRead = is.read(b, off, len);
      if (numRead != -1)
      {
        totalRead += numRead;
      }
      return numRead;
    }
    
    @Override
    public final int read() throws IOException
    {
      int b = (totalRead < length) ? is.read() : -1;
      if (b != -1) totalRead++;
      return b;
    }
  }
}
