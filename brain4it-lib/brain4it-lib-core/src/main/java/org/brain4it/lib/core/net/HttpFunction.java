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

package org.brain4it.lib.core.net;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.io.Parser;
import org.brain4it.lang.Utils;
import org.brain4it.io.JSONParser;
import org.brain4it.io.JSONPrinter;
import org.brain4it.io.Printer;
import org.brain4it.io.XMLParser;
import org.brain4it.io.XMLPrinter;
import org.brain4it.lang.Function;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 *
 */
public class HttpFunction implements Function
{
  private static final String CHARSET = "UTF-8";
  private static final int CONNECT_TIMEOUT = 30000;
  private static final int READ_TIMEOUT = 30000;

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);
    String method = (String)context.evaluate(args.get(1));
    String surl = (String)context.evaluate(args.get(2));
    String inputString = null;
    String inputFormat = (String)context.evaluate(args.get("input-format"));
    if (args.size() >= 4 && args.getName(3) == null) // deprecated
    {
      Object body = context.evaluate(args.get(3));
      inputString = toString(body, inputFormat);
    }
    else if (args.has("body"))
    {
      Object body = context.evaluate(args.get("body"));
      inputString = toString(body, inputFormat);
    }
    BList properties = (BList)context.evaluate(args.get("properties"));
    Number number = (Number)context.evaluate(args.get("connect-timeout"));
    int connectTimeout = number == null ? CONNECT_TIMEOUT: number.intValue();
    number = (Number)context.evaluate(args.get("read-timeout"));
    int readTimeout = number == null ? READ_TIMEOUT : number.intValue();
    String downloadFilename =
      (String)context.evaluate(args.get("download-file"));
    String uploadFilename =
      (String)context.evaluate(args.get("upload-file"));

    URL url = new URL(surl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    if (conn instanceof HttpsURLConnection)
    {
      HttpsURLConnection sconn = (HttpsURLConnection)conn;
      sconn.setHostnameVerifier(new HostnameVerifier()
      {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
          // skip hostname check
          return true;
        }
      });
    }
    conn.setUseCaches(false);
    conn.setConnectTimeout(connectTimeout);
    conn.setReadTimeout(readTimeout);
    conn.setRequestMethod(method);
    conn.setDoInput(true);
    setRequestProperties(conn, properties);
    try
    {
      if (inputString != null)
      {
        conn.setDoOutput(true);
        String contentEncoding = getContentEncoding(properties);
        OutputStream os = conn.getOutputStream();
        os.write(inputString.getBytes(contentEncoding));
        os.flush();
      }
      else if (uploadFilename != null)
      {
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        FileInputStream is = new FileInputStream(uploadFilename);
        try
        {
          writeStream(is, os);
        }
        finally
        {
          is.close();
        }
        os.flush();
      }
      int responseCode = conn.getResponseCode();
      String responseMessage = conn.getResponseMessage();
      String contentType = conn.getContentType();
      BList headerProperties = getHeaderProperties(conn);

      BList result = new BList();
      result.put("status", responseCode);
      result.put("message", responseMessage);
      result.put("headers", headerProperties);

      if (downloadFilename == null)
      {
        if (isTextContentType(contentType))
        {
          byte[] response = readResponse(conn);
          Object body;
          String contentEncoding = conn.getContentEncoding();
          if (contentEncoding == null)
          {
            contentEncoding = getContentEncoding(contentType);
          }
          if (response != null)
          {
            String outputString = new String(response, contentEncoding);
            String outputFormat =
              (String)context.evaluate(args.get("output-format"));
            if (outputFormat != null)
            {
              body = toObject(outputString, outputFormat);
            }
            else body = outputString;
            result.put("body", body);
          }
        }
      }
      else
      {
        FileOutputStream os = new FileOutputStream(downloadFilename);
        try
        {
          writeStream(conn.getInputStream(), os);
        }
        catch (IOException ex)
        {
        }
        finally
        {
          os.close();
        }
      }
      return result;
    }
    finally
    {
      conn.disconnect();
    }
  }

  protected String toString(Object data, String format)
  {
    String text;
    if ("bpl".equals(format))
    {
      text = Printer.toString(data);
    }
    else if ("json".equals(format))
    {
      text = JSONPrinter.toString(data);
    }
    else if ("xml".equals(format))
    {
      text = XMLPrinter.toString(data);
    }
    else // string
    {
      text = Utils.toString(data);
    }
    return text;
  }

  protected Object toObject(String text, String format)
  {
    Object data = null;
    try
    {
      if ("bpl".equals(format))
      {
        data = Parser.fromString(text);
      }
      else if ("json".equals(format))
      {
        data = JSONParser.fromString(text);
      }
      else if ("xml".equals(format))
      {
        data = XMLParser.fromString(text);
      }
      else // string
      {
        data = text;
      }
    }
    catch (Exception ex)
    {
      data = ex.toString();
    }
    return data;
  }

  protected BList getHeaderProperties(HttpURLConnection conn)
  {
    BList properties = new BList();
    int i = 1; // 0 is status line, may return null
    String key = conn.getHeaderFieldKey(i);
    while (key != null)
    {
      String value = conn.getHeaderField(key);
      properties.put(key.toLowerCase(), value);
      i++;
      key = conn.getHeaderFieldKey(i);
    }
    return properties;
  }

  protected boolean isTextContentType(String contentType)
  {
    if (contentType == null) return false;
    else if (contentType.startsWith("text/")) return true;
    else if (contentType.startsWith("application/json")) return true;
    return false;
  }

  protected void setRequestProperties(HttpURLConnection conn, BList properties)
  {
    if (properties != null)
    {
      for (int i = 0; i < properties.size(); i++)
      {
        String name = properties.getName(i);
        String value = String.valueOf(properties.get(i));
        conn.setRequestProperty(name, value);
      }
    }

    String requestMethod = conn.getRequestMethod();
    if (requestMethod.equals("PUT") || requestMethod.equals("POST"))
    {
      if (getContentType(properties) == null)
      {
        conn.setRequestProperty("content-Type", BPL_MIMETYPE +
          "; charset=" + BPL_CHARSET);
      }
    }
  }

  protected String getContentType(BList properties)
  {
    if (properties != null)
    {
      int index = Utils.getIndexOfNameCI(properties, "content-Type");
      if (index != -1)
      {
        return (String)properties.get(index);
      }
    }
    return null;
  }

  protected String getContentEncoding(BList properties)
  {
    return getContentEncoding(getContentType(properties));
  }

  protected String getContentEncoding(String contentType)
  {
    if (contentType != null)
    {
      int index = contentType.toLowerCase().indexOf("charset=");
      if (index != -1)
      {
        return contentType.substring(index + 8);
      }
    }
    return CHARSET;
  }

  protected byte[] readResponse(HttpURLConnection conn)
  {
    byte[] response = null;
    try
    {
      response = readBytes(conn.getInputStream());
    }
    catch (IOException ex)
    {
      InputStream errorStream = conn.getErrorStream();
      if (errorStream != null)
      {
        try
        {
          response = readBytes(errorStream);
        }
        catch (IOException ex2)
        {
        }
      }
    }
    return response;
  }

  protected byte[] readBytes(InputStream is) throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try
    {
      writeStream(is, os);
    }
    finally
    {
      is.close();
    }
    return os.toByteArray();
  }

  protected void writeStream(InputStream is, OutputStream os)
    throws IOException
  {
    byte[] buffer = new byte[1024];
    int numRead = is.read(buffer);
    while (numRead > 0)
    {
      os.write(buffer, 0, numRead);
      numRead = is.read(buffer);
    }
  }
}