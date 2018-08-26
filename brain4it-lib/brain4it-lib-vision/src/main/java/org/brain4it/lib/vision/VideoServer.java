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
package org.brain4it.lib.vision;

import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import static org.brain4it.server.ServerConstants.URL_CHARSET;

/**
 *
 * @author realor
 */
public class VideoServer extends Thread
{
  public static final int DEFAULT_PORT = 8080;

  protected Webcam webcam;
  protected int port;
  protected static final String BOUNDARY = "8ba75cdE3425";
  static final Logger LOGGER = Logger.getLogger("VideoServer");
  protected ServerSocket serverSocket;

  public VideoServer(Webcam webcam)
  {
    this.webcam = webcam;
    this.port = DEFAULT_PORT;
  }

  public VideoServer(Webcam webcam, int port)
  {
    this.webcam = webcam;
    this.port = port;
  }

  public Webcam getWebcam()
  {
    return webcam;
  }

  public int getPort()
  {
    return port;
  }

  @Override
  public void run()
  {
    try
    {
      webcam.open();
      serverSocket = new ServerSocket(port);
      LOGGER.log(Level.INFO, "video server started on port {0}", port);

      do
      {
        final Socket socket = serverSocket.accept();
        Thread dispatchThread = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            dispatch(socket);
          }
        });
        dispatchThread.start();
      } while (!serverSocket.isClosed());
    }
    catch (IOException ex)
    {
    }
    finally
    {
      webcam.close();
      LOGGER.log(Level.INFO, "video server on port {0} stopped.", port);
    }
  }

  public void finish()
  {
    try
    {
      serverSocket.close();
    }
    catch (Exception ex)
    {
    }
  }

  protected void dispatch(Socket socket)
  {
    try
    {
      InputStream is = socket.getInputStream();
      try
      {
        Request request = readRequest(is);
        Map<String, String> headers = readHeaders(is);
        if ("GET".equals(request.method))
        {
          OutputStream os = socket.getOutputStream();
          try
          {
            writeLine(os, "HTTP/1.1 200 OK");
            writeLine(os,
              "Content-Type: multipart/x-mixed-replace;boundary=" + BOUNDARY);
            writeLine(os);
            while (!serverSocket.isClosed())
            {
              BufferedImage image = webcam.getImage();
              writeLine(os, "\n--" + BOUNDARY);
              writeLine(os, "Content-Type: image/jpeg");
              writeLine(os);
              ImageIO.write(image, "jpeg", os);
              os.flush();
            }
          }
          finally
          {
            os.close();
          }
        }
      }
      finally
      {
        is.close();
      }
    }
    catch (IOException ex)
    {
      // ignore
    }
    finally
    {
      try
      {
        socket.close();
      }
      catch (IOException ex)
      {        
      }
    }
  }

  protected Request readRequest(InputStream in) throws IOException
  {
    String line = readLine(in);
    String[] parts = line.split(" ");
    Request request = new Request();
    request.method = parts[0];
    request.path = URLDecoder.decode(parts[1], URL_CHARSET);
    request.version = parts[2];
    return request;
  }

  protected Map<String, String> readHeaders(InputStream in) throws IOException
  {
    Map<String, String> headers = new HashMap<String, String>();
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
    return headers;
  }

  protected String readLine(InputStream in) throws IOException
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

  protected void writeLine(OutputStream os, String line) throws IOException
  {
    os.write((line + "\n").getBytes());
  }

  protected void writeLine(OutputStream os) throws IOException
  {
    os.write("\n".getBytes());
  }

  protected class Request
  {
    String method;
    String path;
    String version;

    @Override
    public String toString()
    {
      return method + " " + path + " " + version;
    }
  }
}
