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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.brain4it.server.HttpDispatcher;
import org.brain4it.server.MonitorService;
import org.brain4it.server.RestService;
import static org.brain4it.server.ServerConstants.URL_CHARSET;

/**
 *
 * @author realor
 */
public class ServletHttpDispatcher extends HttpDispatcher
{
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final RestService restService;
  private final MonitorService monitorService;
  
  public ServletHttpDispatcher(
    HttpServletRequest request, HttpServletResponse response, 
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
    try
    {
      String uri = request.getRequestURI();
      String contextPath = request.getContextPath();
      String servletPath = request.getServletPath();
      int prefixLength = contextPath.length() + servletPath.length();
      String path = URLDecoder.decode(uri.substring(prefixLength), URL_CHARSET);
      return path;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
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
    response.setStatus(code);
  }

  @Override
  protected void setStatusMessage(String message)
  {
  }

  @Override
  protected void setCharacterEncoding(String charset)
  {
    response.setCharacterEncoding(charset);
  }

  @Override
  protected String getRemoteAddress()
  {
    return request.getRemoteAddr();
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
    return Collections.list(request.getHeaderNames());
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
  protected PrintWriter getResponseWriter() throws IOException
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
