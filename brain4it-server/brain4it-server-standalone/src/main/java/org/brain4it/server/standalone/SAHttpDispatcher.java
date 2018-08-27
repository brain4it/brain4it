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
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import org.brain4it.server.HttpDispatcher;
import org.brain4it.server.MonitorService;
import org.brain4it.server.RestService;

/**
 *
 * @author realor
 */
public class SAHttpDispatcher extends HttpDispatcher
{
  private final HttpRequest request;
  private final HttpResponse response;
  private final RestService restService;
  private final MonitorService monitorService;
  
  public SAHttpDispatcher(HttpRequest request, HttpResponse response, 
    RestService restService, MonitorService monitorService)
  {
    this.request = request;
    this.response = response;
    this.restService = restService;
    this.monitorService = monitorService;
  }
  
  @Override
  public String getPath()
  {
    return request.getPath();
  }

  @Override
  public String getMethod()
  {
    return request.getMethod();
  }

  @Override
  protected boolean isCommitted()
  {
    return response.isCommitted();
  }
  
  @Override
  protected void setStatusCode(int code)
  {
    response.setStatusCode(code);
  }

  @Override
  protected void setStatusMessage(String message)
  {
    response.setStatusMessage(message);
  }

  @Override
  protected void setCharacterEncoding(String charset)
  {
    response.setCharacterEncoding(charset);
  }

  @Override
  protected String getRemoteAddress()
  {
    return request.getRemoteAddress();
  }

  @Override
  protected int getRemotePort()
  {
    return request.getRemotePort();
  }

  @Override
  protected String getRequestHeader(String name)
  {
    return request.getHeader(name);
  }

  @Override
  protected Collection<String> getRequestHeaderNames()
  {
    return request.getHeaderNames();
  }

  @Override
  protected void setResponseHeader(String name, String value)
  {
    response.setHeader(name, value);
  }

  @Override
  protected Collection<String> getResponseHeaderNames()
  {
    return response.getHeaderNames();
  }

  @Override
  protected Reader getRequestReader() throws IOException
  {
    return request.getReader();
  }

  @Override
  protected PrintWriter getResponseWriter()
  {
    return response.getWriter();
  }

  @Override
  protected RestService getRestService()
  {
    return restService;
  }

  @Override
  protected MonitorService getMonitorService()
  {
    return monitorService;
  }
}
