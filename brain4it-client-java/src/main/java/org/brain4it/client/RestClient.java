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

package org.brain4it.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import org.brain4it.io.IOUtils;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.net.SSLUtils;
import static org.brain4it.server.ServerConstants.*;

/**
 * The RestClient class lets you make calls to the REST API of a Brain4it
 * server.
 *
 * @author realor
 */
public class RestClient
{
  private String serverUrl;
  private String accessKey;
  private String sessionId;
  private int connectionTimeout = 10000;
  private int readTimeout = 0;
  private String method;
  private String path;
  private String dataString;
  private String resultString;
  private int status;
  private SendThread thread;
  private Map<String, List<String>> headers;

  public RestClient()
  {
  }

  public RestClient(String serverUrl)
  {
    this(serverUrl, null);
  }

  public RestClient(String serverUrl, String accessKey)
  {
    this.serverUrl = serverUrl;
    this.accessKey = accessKey;
  }

  public String getServerUrl()
  {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl)
  {
    this.serverUrl = serverUrl;
  }

  public String getAccessKey()
  {
    return accessKey;
  }

  public void setAccessKey(String accessKey)
  {
    this.accessKey = accessKey;
  }

  public String getSessionId()
  {
    return sessionId;
  }

  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public int getConnectionTimeout()
  {
    return connectionTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout)
  {
    this.connectionTimeout = connectionTimeout;
  }

  public int getReadTimeout()
  {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout)
  {
    this.readTimeout = readTimeout;
  }

  public String getMethod()
  {
    return method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getDataString()
  {
    return dataString;
  }

  public void setDataString(String dataString)
  {
    this.dataString = dataString;
  }

  public void setData(Object data)
  {
    this.dataString = Printer.toString(data);
  }

  public int getStatus()
  {
    return status;
  }

  public Map<String, List<String>> getHeaders()
  {
    return headers;
  }

  public String getResultString()
  {
    return resultString;
  }

  public Object getResult() throws ParseException
  {
    return Parser.fromString(resultString);
  }

  // synchronous methods
  public String listModules() throws IOException
  {
    this.method = "GET";
    this.path = null;
    this.dataString = null;
    return send();
  }

  public String createModule(String moduleName) throws IOException
  {
    this.method = "PUT";
    this.path = moduleName;
    this.dataString = null;
    return send();
  }

  public String destroyModule(String moduleName) throws IOException
  {
    this.method = "DELETE";
    this.path = moduleName;
    this.dataString = null;
    return send();
  }

  public String get(String moduleName, String modulePath) throws IOException
  {
    this.method = "GET";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = null;
    return send();
  }

  public String put(String moduleName, String modulePath, String value)
    throws IOException
  {
    this.method = "PUT";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = value;
    return send();
  }

  public String delete(String moduleName, String modulePath)
    throws IOException
  {
    this.method = "DELETE";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = null;
    return send();
  }

  public String execute(String moduleName, String command)
    throws IOException
  {
    this.method = "POST";
    this.path = moduleName;
    this.dataString = command;
    return send();
  }

  public String invokeFunction(String moduleName, String functionName,
     String data) throws IOException
  {
    this.method = "POST";
    this.path = moduleName + absolutePath(functionName);
    this.dataString = data;
    return send();
  }

  // asynchronous methods
  public void listModules(Callback callback)
  {
    this.method = "GET";
    this.path = null;
    this.dataString = null;
    send(callback);
  }

  public void createModule(String moduleName, Callback callback)
  {
    this.method = "PUT";
    this.path = moduleName;
    this.dataString = null;
    send(callback);
  }

  public void destroyModule(String moduleName, Callback callback)
  {
    this.method = "DELETE";
    this.path = moduleName;
    this.dataString = null;
    send(callback);
  }

  public void get(String moduleName, String modulePath, Callback callback)
  {
    this.method = "GET";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = null;
    send(callback);
  }

  public void put(String moduleName, String modulePath, String value,
    Callback callback)
  {
    this.method = "PUT";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = value;
    send(callback);
  }

  public void delete(String moduleName, String modulePath, Callback callback)
  {
    this.method = "DELETE";
    this.path = moduleName + absolutePath(modulePath);
    this.dataString = null;
    send(callback);
  }

  public void execute(String moduleName, String cmd, Callback callback)
  {
    this.method = "POST";
    this.path = moduleName;
    this.dataString = cmd;
    send(callback);
  }

  public void invokeFunction(String moduleName, String functionName,
     String data, Callback callback)
  {
    this.method = "POST";
    this.path = moduleName + absolutePath(functionName);
    this.dataString = data;
    send(callback);
  }

  public void send(Callback callback)
  {
    if (thread != null) throw new RuntimeException("Request in process");
    thread = new SendThread(callback);
    thread.start();
  }

  public String send() throws IOException
  {
    if (thread != null) throw new RuntimeException("Request in process");
    return internalSend();
  }

  public void abort()
  {
    if (thread != null)
    {
      thread.aborted = true;
      thread.interrupt();
    }
  }

  public boolean isRequestInProcess()
  {
    return thread != null;
  }

  private String absolutePath(String modulePath)
  {
    if (modulePath == null) throw new RuntimeException("Invalid null path");
    if (modulePath.startsWith("/")) return modulePath;
    return "/" + modulePath;
  }

  private String internalSend() throws IOException
  {
    status = 0;
    headers = null;
    if (serverUrl == null) throw new RuntimeException("Invalid url");
    URL url = (path == null) ?
      new URL(serverUrl) : new URL(serverUrl + "/" + urlEncode(path));
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    try
    {
      SSLUtils.skipCertificateValidation(conn);
      conn.setConnectTimeout(connectionTimeout);
      conn.setReadTimeout(readTimeout);
      conn.setRequestMethod(method);
      conn.setUseCaches(false);
      conn.setDoInput(true);
      conn.setRequestProperty("content-type", BPL_MIMETYPE +
        "; charset=" + BPL_CHARSET);
      if (accessKey != null)
      {
        conn.setRequestProperty(ACCESS_KEY_HEADER, accessKey);
      }
      if (sessionId != null)
      {
        conn.setRequestProperty(SESSION_ID_HEADER, sessionId);
      }
      if (dataString != null && !method.equals("GET"))
      {
        conn.setDoOutput(true);
        byte[] bytes = dataString.getBytes(BPL_CHARSET);
        OutputStream os = conn.getOutputStream();
        IOUtils.writeBytes(bytes, os);
      }

      conn.connect();
      status = conn.getResponseCode();
      headers = conn.getHeaderFields();
      String contentEncoding = conn.getContentEncoding();
      byte[] response;
      try
      {
        response = IOUtils.readBytes(conn.getInputStream());
      }
      catch (IOException ex)
      {
        InputStream errorStream = conn.getErrorStream();
        if (errorStream != null)
        {
          response = IOUtils.readBytes(errorStream);
        }
        else throw ex;
      }
      if (contentEncoding == null) contentEncoding = BPL_CHARSET;
      resultString = new String(response, contentEncoding);
      if (status == HttpURLConnection.HTTP_OK)
      {
        return resultString;
      }
      else // internal server error
      {
        throw new RuntimeException(resultString);
      }
    }
    finally
    {
      conn.disconnect();
    }
  }

  private String urlEncode(String path) throws UnsupportedEncodingException
  {
    StringBuilder buffer = new StringBuilder();
    String parts[] = path.split("/");
    if (parts.length > 0)
    {
      buffer.append(URLEncoder.encode(parts[0], BPL_CHARSET));
      for (int i = 1; i < parts.length; i++)
      {
        buffer.append('/');
        buffer.append(URLEncoder.encode(parts[i], BPL_CHARSET));
      }
    }
    return buffer.toString();
  }

  public interface Callback
  {
    public void onSuccess(RestClient client, String resultString);
    public void onError(RestClient client, Exception ex);
  }

  class SendThread extends Thread
  {
    private final Callback callback;
    private boolean aborted = false;

    SendThread(Callback callback)
    {
      this.callback = callback;
    }

    @Override
    public void run()
    {
      try
      {
        String resultString = internalSend();
        thread = null;
        if (!aborted)
        {
          callback.onSuccess(RestClient.this, resultString);
        }
      }
      catch (Exception ex)
      {
        thread = null;
        if (!aborted)
        {
          callback.onError(RestClient.this, ex);
        }
      }
    }
  }

  public static void main(String args[]) throws Exception
  {
    if (args.length < 3)
    {
      System.out.println(
        "Usage: <method> <url> <accessKey> [<path>] [<value>]");
    }
    else
    {
      String method = args[0];
      String url = args[1];
      String accessKey = args[2];
      RestClient client = new RestClient(url, accessKey);
      client.setMethod(method);
      if (args.length >= 4)
      {
        client.setPath(args[3]);
        if (args.length >= 5)
        {
          client.setDataString(args[4]);
        }
      }
      String result = client.send();
      System.out.println(result);
    }
  }
}
