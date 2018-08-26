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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The Console class lets you execute commands in a Brain4it server  
 * with a REPL (Read-Eval-Print-Loop) interface.
 * 
 * @author realor
 */
public class Console
{
  protected final RestClient restClient;
  protected String prompt = "> ";
  protected String charset = "ISO-8859-1";

  public Console()
  {
    restClient = new RestClient();
  }

  public Console(String serverUrl, String accessKey, String path)
  {
    restClient = new RestClient(serverUrl, accessKey);
    restClient.setPath(path);
    restClient.setMethod("POST");
  }

  public RestClient getRestClient()
  {
    return restClient;
  }

  public void run() throws IOException
  {
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(System.in, charset));
    try
    {
      System.out.print(prompt);
      String line = reader.readLine();
      while (line != null && line.length() > 0)
      {
        try
        {
          restClient.setDataString(line);
          String result = restClient.send();
          System.out.println(result);
        }
        catch (Exception ex)
        {
          String message = ex.getMessage();
          if (message == null) message = ex.toString();
          System.out.println(message);
        }
        System.out.print(prompt);
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
  }

  public static void main(String[] args) throws Exception
  {
    if (args.length < 3)
    {
      System.out.println(
        "Arguments: <url> [<accessKey>] [<path>]");
    }
    else
    {
      String url = args[0];
      String accessKey = args.length >= 2 ? args[1] : null;
      String path = args.length >= 3 ? args[2] : null;
      Console console = new Console(url, accessKey, path);
      console.run();
    }
  }
}
