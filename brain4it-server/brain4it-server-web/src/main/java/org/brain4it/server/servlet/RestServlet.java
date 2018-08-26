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

package org.brain4it.server.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.brain4it.lang.Executor;
import org.brain4it.server.RestService;
import org.brain4it.server.module.ModuleManager;
import org.brain4it.server.store.FileSystemStore;

/**
 *
 * @author realor
 */
public class RestServlet extends HttpServlet
{
  private static final String BASE_PATH_PARAM = "basePath";
  private static final String MULTI_TENANT_PARAM = "multiTenant";
  private static final String ACCESS_KEY_PARAM = "accessKey";
  private static final String ACCESS_KEY_FILE_PARAM = "accessKeyFile";
  private static final String LIBRARIES_PARAM = "libraries";
  private static final String MAX_WAIT_TIME_PARAM = "maxWaitTime";
  private static final String MONITOR_TIME_PARAM = "monitorTime";

  private ModuleManager moduleManager;
  private RestService restService;

  @Override
  public void init() throws ServletException
  {
    try
    {
      log("Initializing Service...");
      
      ServletContext servletContext = getServletContext();

      FileSystemStore store = new FileSystemStore();
      Properties properties = new Properties();
      String basePath = servletContext.getInitParameter(BASE_PATH_PARAM);
      if (basePath != null)
      {
        properties.setProperty(FileSystemStore.BASE_PATH, basePath);
      }
      store.init(properties);
      log("Using dir: " + store.getBasePath());
            
      String accessKey = servletContext.getInitParameter(ACCESS_KEY_PARAM);
      
      String value = servletContext.getInitParameter(ACCESS_KEY_FILE_PARAM);
      File accessKeyFile = value == null ? null : new File(value);
      
      value = servletContext.getInitParameter(MULTI_TENANT_PARAM);
      boolean multiTenant = Boolean.valueOf(value);
      
      moduleManager = new ModuleManager("Brain4it", store, 
        multiTenant, accessKey, accessKeyFile);
      
      moduleManager.addLibraries(
        servletContext.getInitParameter(LIBRARIES_PARAM));
      
      Executor.init();
      
      moduleManager.init();
      
      restService = new RestService(moduleManager);
      
      value = servletContext.getInitParameter(MAX_WAIT_TIME_PARAM);
      if (value != null)
      {
        int maxWaitTime;
        try
        {
          maxWaitTime = Integer.parseInt(value);
        }
        catch (NumberFormatException ex)
        {
          throw new NumberFormatException(MAX_WAIT_TIME_PARAM + ": " + value);
        }
        restService.setMaxWaitTime(maxWaitTime);
      }

      value = servletContext.getInitParameter(MONITOR_TIME_PARAM);
      if (value != null)
      {
        int monitorTime;
        try
        {
          monitorTime = Integer.parseInt(value);
        }
        catch (NumberFormatException ex)
        {
          throw new NumberFormatException(MONITOR_TIME_PARAM + ": " + value);
        }
        restService.setMonitorTime(monitorTime);
      }
      
      log("Initialization completed.");
    }
    catch (Exception ex)
    {
      throw new ServletException("Service initialization failure", ex);
    }
  }

  @Override
  public void destroy()
  {
    try
    {
      log("Stopping Service...");      
      moduleManager.destroy();      
      Executor.shutdown();
      log("Stop completed.");
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Service destroy failure", ex);
    }
  }

  @Override
  protected void service(HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, IOException
  {
    ServletHttpDispatcher dispatcher = 
      new ServletHttpDispatcher(request, response, restService);
    
    dispatcher.dispatch();
  }
}
