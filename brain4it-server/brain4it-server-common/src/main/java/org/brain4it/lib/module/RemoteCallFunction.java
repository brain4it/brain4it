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
package org.brain4it.lib.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import org.brain4it.io.IOUtils;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.net.SSLUtils;
import org.brain4it.server.ServerConstants;
import org.brain4it.server.module.Module;
import org.brain4it.server.module.ModuleManager;
import static org.brain4it.server.ServerConstants.BPL_CHARSET;
import static org.brain4it.server.ServerConstants.BPL_MIMETYPE;
import static org.brain4it.server.ServerConstants.MODULE_SETUP_VAR;
import static org.brain4it.server.ServerConstants.REMOTE_ADDRESS;
import static org.brain4it.server.ServerConstants.REMOTE_PORT;
import static org.brain4it.server.ServerConstants.REQUEST_CONTEXT_STRUCTURE;
import static org.brain4it.server.ServerConstants.REQUEST_HEADERS;

/**
 *
 * @author realor
 *
 * Usages:
 * (remote-call ("url" => "http://server:host/path-to-function") args)
 * (remote-call ("url" => "https://server:host/path-to-function"
 *               "request-headers" => ("access-key" => ...)) args)
 * (remote-call ("module" => "math" "function" => "fn/@distance") args)
 * (remote-call ("tenant" => "libs" "module" => "math" "function" => "@distance"
 *               "request-headers" => (...)) args)
 * (remote-call "https://server:host/path-to-function" args)
 * (remote-call "server_alias:/path-to-function" args)
 *
 * (remote-call /rc/vector_length 12.8 9.6)
 *
 */
public class RemoteCallFunction implements Function
{
  public static final String URL = "url";
  public static final String TENANT = "tenant";
  public static final String MODULE = "module";
  public static final String FUNCTION = "function";
  public static final String REMOTE_MODULES = "remote-modules";

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    Object firstArg = context.evaluate(args.get(1));

    BList functionSetup = null;
    if (firstArg instanceof String)
    {
      functionSetup = getFunctionSetupFromString(context, (String)firstArg);
    }
    else if (firstArg instanceof BList)
    {
      functionSetup = (BList)firstArg;
    }
    else throw new BException("InvalidFunctionSetup",
      "function setup must be a string or a list");

    Object data = getFunctionData(context, args);

    if (functionSetup.has(URL))
    {
      return networkCall(context, functionSetup, data);
    }
    else
    {
      return directCall(context, functionSetup, data);
    }
  }

  protected Object networkCall(Context context, BList functionSetup, Object data)
    throws IOException, ParseException
  {
    String surl = (String)functionSetup.get(URL);
    URL url = new URL(surl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    SSLUtils.skipCertificateValidation(conn);
    conn.setUseCaches(false);
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(30000);
    conn.setRequestProperty("Content-Type", BPL_MIMETYPE +
      "; charset=" + BPL_CHARSET);

    BList requestHeaders = (BList)functionSetup.get(REQUEST_HEADERS);
    if (requestHeaders != null)
    {
      Structure structure = requestHeaders.getStructure();
      if (structure != null)
      {
        int count = structure.nameCount();
        for (int i = 0; i < count; i++)
        {
          String propertyName = structure.getName(i);
          if (propertyName != null)
          {
            String propertyValue = String.valueOf(requestHeaders.get(i));
            conn.setRequestProperty(propertyName, propertyValue);
          }
        }
      }
    }

    try
    {
      if (data != null)
      {
        conn.setDoOutput(true);
        String inputString = Printer.toString(data);
        OutputStream os = conn.getOutputStream();
        os.write(inputString.getBytes(ServerConstants.BPL_CHARSET));
        os.flush();
      }

      String contentType = conn.getContentType();

      if (contentType == null ||
        contentType.contains(ServerConstants.BPL_MIMETYPE))
      {
        byte[] bytes = readResponse(conn);

        Object response;
        String responseString;
        if (bytes == null)
        {
          responseString = null;
          response = null;
        }
        else
        {
          responseString = new String(bytes, ServerConstants.BPL_CHARSET);
          response = Parser.fromString(responseString, context.getFunctions());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK)
        {
          return response;
        }
        if (response instanceof BList)
        {
          throw new BException((BList)response);
        }
        else if (response instanceof String)
        {
          throw new BException("IOException", (String)response);
        }
        else
        {
          throw new BException("IOException");
        }
      }
      return null;
    }
    finally
    {
      conn.disconnect();
    }
  }

  protected Object directCall(Context context, BList functionSetup, Object data)
    throws Exception
  {
    String tenantName = (String)functionSetup.get(TENANT);
    String moduleName = (String)functionSetup.get(MODULE);
    String functionName = (String)functionSetup.get(FUNCTION);
    BList requestHeaders = (BList)functionSetup.get(REQUEST_HEADERS);

    Module module = (Module)context.getGlobalScope();
    ModuleManager moduleManager = module.getModuleManager();
    if (tenantName == null) tenantName = module.getTenant();
    if (moduleName == null) moduleName = module.getName();

    Module targetModule =
      moduleManager.getModule(tenantName, moduleName, true);

    BList requestContext = new BList(REQUEST_CONTEXT_STRUCTURE);
    requestContext.put(REMOTE_ADDRESS, "127.0.0.1");
    requestContext.put(REMOTE_PORT, 0);
    if (requestHeaders != null)
    {
      requestContext.put(REQUEST_HEADERS, requestHeaders);
    }

    BList code = targetModule.createExteriorFunctionCall(
      functionName, requestContext, data);

    Map<String, Function> functions = moduleManager.getFunctions();

    Context targetContext = new Context(targetModule, functions);

    Object result = targetContext.evaluate(code);

    if (result instanceof BList)
    {
      result = ((BList)result).clone(true);
    }
    return result;
  }

  protected BList getFunctionSetupFromString(Context context, String surl)
  {
    BList functionInfo = new BList();
    if (surl.startsWith("http://") || surl.startsWith("https://"))
    {
      functionInfo.put(URL, surl);
    }
    else
    {
      try
      {
        int index = surl.indexOf(":");
        String serverReference = surl.substring(0, index);
        BList setup = (BList)context.getGlobalScope().get(MODULE_SETUP_VAR);
        BList remoteModules = (BList)setup.get(REMOTE_MODULES);
        String baseUrl = (String)remoteModules.get(serverReference);
        if (!baseUrl.endsWith("/") && !surl.startsWith("/")) baseUrl += "/";
        surl = baseUrl + surl.substring(index + 1);
        functionInfo.put(URL, surl);
      }
      catch (Exception ex)
      {
        throw new BException("InvalidURL", surl);
      }
    }
    return functionInfo;
  }

  protected Object getFunctionData(Context context, BList args)
    throws Exception
  {
    Object data = null;

    if (args.size() >= 3)
    {
      if (args.size() == 3)
      {
        data = context.evaluate(args.get(2));
      }
      else
      {
        BList list = new BList();
        for (int i = 2; i < args.size(); i++)
        {
          Object value = context.evaluate(args.get(i));
          String name = args.getName(i);
          if (name == null)
          {
            list.add(value);
          }
          else
          {
            list.put(name, value);
          }
        }
        data = list;
      }
    }
    return data;
  }

  protected byte[] readResponse(HttpURLConnection conn)
  {
    byte[] response = null;
    try
    {
      response = IOUtils.readBytes(conn.getInputStream());
    }
    catch (IOException ex)
    {
      InputStream errorStream = conn.getErrorStream();
      if (errorStream != null)
      {
        try
        {
          response = IOUtils.readBytes(errorStream);
        }
        catch (IOException ex2)
        {
        }
      }
    }
    return response;
  }
}
