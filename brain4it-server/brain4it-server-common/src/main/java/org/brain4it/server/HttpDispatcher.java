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
package org.brain4it.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import org.brain4it.io.JSONPrinter;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.io.XMLPrinter;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 */
public abstract class HttpDispatcher
{
  public void dispatch() throws IOException
  {
    try
    {
      String method = getMethod();
      String path = getPath();
      String accessKey = getRequestHeader(ACCESS_KEY_HEADER);

      setResponseHeader("Access-Control-Allow-Origin", "*");
      setResponseHeader("Access-Control-Expose-Headers", SERVER_TIME_HEADER);

      switch (method)
      {
        case "OPTIONS":
          setResponseHeader("Allow", "GET,PUT,DELETE,POST,OPTIONS");
          setResponseHeader("Access-Control-Allow-Methods",
            "GET,PUT,DELETE,POST,OPTIONS");
          setResponseHeader("Access-Control-Allow-Headers",
            ACCESS_KEY_HEADER + "," + MONITOR_HEADER + "," +
            SESSION_ID_HEADER);
          break;
        case "GET":
        {
          Object result = getRestService().get(path, accessKey);
          sendResult(result);
          break;
        }
        case "PUT":
        {
          Object data = readData();
          Object result = getRestService().put(path, data, accessKey);
          sendResult(result);
          break;
        }
        case "DELETE":
        {
          Object result = getRestService().delete(path, accessKey);
          sendResult(result);
          break;
        }
        case "POST":
        {
          String monitor = getRequestHeader(MONITOR_HEADER);
          Object data = readData();
          BList requestContext = getContextList();
          if (monitor == null)
          {
            // execute code fragment or exterior function
            Object result = getRestService().execute(path, data, accessKey,
              requestContext);
            sendResult(result, requestContext);
          }
          else
          {
            if (data instanceof BList) // start monitoring session
            {
              setCharacterEncoding(BPL_CHARSET);
              setResponseHeader("Transfer-Encoding", "chunked");
              setResponseHeader("X-Content-Type-Options", "nosniff");
              int pollingInterval = Integer.parseInt(monitor);
              BList functions = (BList)data;
              getMonitorService().watch(path, functions, requestContext,
                pollingInterval, getResponseWriter());
              // do not returns until a stop signal or network error
            }
            else if (data instanceof String) // stop monitoring session
            {
              String monitorSessionId = (String)data;
              sendResult(getMonitorService().unwatch(monitorSessionId));
            }
            else throw new IOException("Invalid monitor service argument");
          }
          break;
        }
        default:
          setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
          setStatusMessage("BAD METHOD");
          break;
      }
    }
    catch (Exception ex)
    {
      if (!isCommitted())
      {
        sendError(ex);
      }
    }
  }

  public Object readData() throws ParseException, IOException
  {
    Object data;
    String contentType = getRequestHeader("Content-Type");
    Reader reader = getRequestReader();
    if (contentType == null || contentType.startsWith(BPL_MIMETYPE))
    {
      Map<String, Function> functions =
        getRestService().getModuleManager().getFunctions();
      Parser parser = new Parser(reader, functions);
      data = parser.parse();
    }
    else // return data as string
    {
      StringBuilder buffer = new StringBuilder();
      try
      {
        int ch = reader.read();
        while (ch != -1)
        {
          buffer.append((char)ch);
          ch = reader.read();
        }
      }
      finally
      {
        reader.close();
      }
      data = buffer.toString();
    }
    return data;
  }

  public void sendResult(Object result) throws IOException
  {
    sendResult(result, null);
  }

  public void sendResult(Object result, BList requestContext)
    throws IOException
  {
    setStatusCode(HttpURLConnection.HTTP_OK);
    setStatusMessage("OK");

    long serverTime = System.currentTimeMillis();
    setResponseHeader(SERVER_TIME_HEADER, String.valueOf(serverTime));

    BList headers = requestContext == null ?
      null : (BList)requestContext.get(RESPONSE_HEADERS);

    String contentType = headers == null ?
      null : (String)headers.get("content-type");

    if (contentType == null || contentType.startsWith(BPL_MIMETYPE))
    {
      setResponseHeader("Content-Type",
        BPL_MIMETYPE + "; charset=" + BPL_CHARSET);
      setCharacterEncoding(BPL_CHARSET);
      Printer printer = new Printer(getResponseWriter());
      printer.print(result);
    }
    else if (contentType.startsWith("application/json"))
    {
      setResponseHeader("Content-Type", "application/json; charset=UTF-8");
      setCharacterEncoding("UTF-8");
      if (result instanceof String)
      {
        getResponseWriter().write((String) result);
      }
      else
      {
        JSONPrinter printer = new JSONPrinter(getResponseWriter());
        printer.print(result);
      }
    }
    else if (contentType.startsWith("text/xml"))
    {
      setResponseHeader("Content-Type", "text/xml; charset=UTF-8");
      setCharacterEncoding("UTF-8");
      if (result instanceof String)
      {
        getResponseWriter().write((String) result);
      }
      else
      {
        XMLPrinter printer = new XMLPrinter(getResponseWriter());
        printer.print(result);
      }
    }
    else
    {
      setCharacterEncoding("UTF-8");
      getResponseWriter().write(String.valueOf(result));
    }
  }

  public void sendError(Exception ex) throws IOException
  {
    setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    setStatusMessage("INTERNAL_SERVER_ERROR");
    setResponseHeader("Content-Type",
      BPL_MIMETYPE + "; charset=" + BPL_CHARSET);
    setCharacterEncoding(BPL_CHARSET);
    Printer printer = new Printer(getResponseWriter());
    printer.print(Utils.toBList(ex));
  }

  public BList getContextList()
  {
    BList context = new BList(REQUEST_CONTEXT_STRUCTURE);
    BList headers = new BList();
    for (String headerName : getRequestHeaderNames())
    {
      headers.put(headerName, getRequestHeader(headerName));
    }
    context.put(REQUEST_HEADERS, headers);
    context.put(REMOTE_ADDRESS, getRemoteAddress());
    context.put(REMOTE_PORT, getRemotePort());
    return context;
  }

  public abstract String getPath();

  public abstract String getMethod();

  protected abstract boolean isCommitted();

  protected abstract void setStatusCode(int code);

  protected abstract void setStatusMessage(String message);

  protected abstract void setCharacterEncoding(String charset);

  protected abstract String getRemoteAddress();

  protected abstract int getRemotePort();

  protected abstract String getRequestHeader(String name);

  protected abstract Collection<String> getRequestHeaderNames();

  protected abstract void setResponseHeader(String name, String value);

  protected abstract Collection<String> getResponseHeaderNames();

  protected abstract Reader getRequestReader() throws IOException;

  protected abstract PrintWriter getResponseWriter() throws IOException;

  protected abstract RestService getRestService();

  protected abstract MonitorService getMonitorService();
}
