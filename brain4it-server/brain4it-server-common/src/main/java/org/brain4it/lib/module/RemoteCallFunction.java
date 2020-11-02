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
import org.brain4it.io.IOUtils;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.net.SSLUtils;
import org.brain4it.server.ServerConstants;
import static org.brain4it.server.ServerConstants.MODULE_SETUP_VAR;

/**
 *
 * @author realor
 */
public class RemoteCallFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    String surl = (String)context.evaluate(args.get(1));

    if (!surl.startsWith("http://") && !surl.startsWith("https://"))
    {
      try
      {
        int index = surl.indexOf(":");
        String serverReference = surl.substring(0, index);
        BList setup = (BList)context.getGlobalScope().get(MODULE_SETUP_VAR);
        BList remoteModules = (BList)setup.get("remote-modules");
        String baseUrl = (String)remoteModules.get(serverReference);
        if (!baseUrl.endsWith("/") && !surl.startsWith("/")) baseUrl += "/";
        surl = baseUrl + surl.substring(index + 1);
      }
      catch (Exception ex)
      {
        throw new BException("InvalidURL", surl);
      }
    }
    URL url = new URL(surl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    SSLUtils.skipCertificateValidation(conn);
    conn.setUseCaches(false);
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(30000);
    conn.setRequestProperty("Content-Type", ServerConstants.BPL_MIMETYPE);
    try
    {
      if (args.size() >= 3)
      {
        conn.setDoOutput(true);
        Object data;
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
