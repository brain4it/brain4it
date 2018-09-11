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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author realor
 */
public class HttpResponse
{
  private String version = "HTTP/1.1";
  private int statusCode = HttpURLConnection.HTTP_OK;
  private String statusMessage = "OK";
  private final HashMap<String, String> headers = new HashMap<String, String>();
  private boolean committed;
  private boolean chunked;
  private boolean finished;
  private String characterEncoding = "UTF-8";
  private final PrintWriter writer;
  private final Socket socket;

  public HttpResponse(Socket socket) throws IOException
  {
    this.socket = socket;
    this.writer = new PrintWriter(new ResponseWriter(socket.getOutputStream()));
    setHeader("Server", HttpServer.SERVER_NAME);
  }

  public Socket getSocket()
  {
    return socket;
  }

  public int getStatusCode()
  {
    return statusCode;
  }

  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  public String getStatusMessage()
  {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage)
  {
    this.statusMessage = statusMessage;
  }

  public String getHeader(String name)
  {
    return headers.get(name.toLowerCase());
  }

  public void setHeader(String name, String value)
  {
    name = name.toLowerCase();
    headers.put(name, value);
    if ("transfer-encoding".equals(name))
    {
      chunked = "chunked".equalsIgnoreCase(value);
    }
  }

  public Collection<String> getHeaderNames()
  {
    return headers.keySet();
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getCharacterEncoding()
  {
    return characterEncoding;
  }

  public void setCharacterEncoding(String charsetEncoding)
  {
    this.characterEncoding = charsetEncoding;
  }

  public boolean isCommitted()
  {
    return committed;
  }

  public boolean isChunked()
  {
    return chunked;
  }

  public boolean isFinished()
  {
    return finished;
  }

  public PrintWriter getWriter()
  {
    return writer;
  }

  public void finish()
  {
    if (!finished)
    {
      finished = true;
      writer.flush();
    }
  }

  public class ResponseWriter extends Writer
  {
    private final OutputStream out;
    private final StringBuilder buffer = new StringBuilder();

    ResponseWriter(OutputStream out)
    {
      this.out = out;
    }

    @Override
    public void write(String str)
    {
      buffer.append(str);
    }

    @Override
    public void write(char[] cbuf, int offset, int length)
    {
      buffer.append(cbuf, offset, length);
    }

    @Override
    public void flush() throws IOException
    {
      if (!committed)
      {
        // send response header
        writeString(version + " " + statusCode + " " + statusMessage);
        writeCRLF();

        for (String headerName : headers.keySet())
        {
          if (!headerName.equals("content-length"))
          {
            writeString(headerName + ": " + headers.get(headerName));
            writeCRLF();
          }
        }
      }
      if (chunked)
      {
        if (!committed)
        {
          writeCRLF(); // headers termination
          committed = true;
        }
        if (buffer.length() > 0)
        {
          // send buffer as a chunk
          byte[] data = buffer.toString().getBytes(characterEncoding);
          buffer.setLength(0);
          writeString(Integer.toHexString(data.length)); // chunk size
          writeCRLF();
          out.write(data); // chunk data
          writeCRLF();
          out.flush();
        }
        if (finished)
        {
          writeString("0");
          writeCRLF();
          writeCRLF();
          out.flush();
        }
      }
      else // not chunked
      {
        if (!committed)
        {
          committed = true;
          byte[] data = buffer.toString().getBytes(characterEncoding);
          buffer.setLength(0);
          writeString("content-length: " + data.length);
          writeCRLF();
          writeCRLF(); // headers termination
          out.write(data);
          out.flush();
        }
      }
    }

    @Override
    public void close() throws IOException
    {
      flush();
      out.close();
    }

    private void writeString(String data) throws IOException
    {
      out.write(data.getBytes(characterEncoding));
    }

    private void writeCRLF() throws IOException
    {
      out.write(13);
      out.write(10);
    }
  }
}