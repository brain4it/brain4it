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

package org.brain4it.manager;

import java.util.ArrayList;
import java.util.List;
import org.brain4it.client.RestClient;

/**
 *
 * @author realor
 */
public class Server
{
  private Workspace workspace;
  private String name;
  private String url;
  private String accessKey;
  private final List<Module> modules;

  public Server(Workspace workspace)
  {
    this(workspace, null, null, null);
  }

  public Server(Workspace workspace,
     String name, String url, String accessKey)
  {
    this.workspace = workspace;
    this.name = name;
    this.url = url;
    setAccessKey(accessKey);
    this.modules = new ArrayList<Module>();
  }

  public Workspace getWorkspace()
  {
    return workspace;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getAccessKey()
  {
    return accessKey;
  }

  public void setAccessKey(String accessKey)
  {
    if (accessKey != null)
    {
      accessKey = accessKey.trim();
      if (accessKey.length() == 0) accessKey = null;
    }
    this.accessKey = accessKey;
  }

  public List<Module> getModules()
  {
    return modules;
  }

  public Module getModule(String name)
  {
    int index = getModuleIndex(name);
    return index == -1 ? null: modules.get(index);
  }

  public int getModuleIndex(String name)
  {
    int index = 0;
    boolean found = false;
    while (index < modules.size() && !found)
    {
      Module module = modules.get(index);
      if (module.getName().equals(name))
      {
        found = true;
      }
      else
      {
        index++;
      }
    }
    return found ? index : -1;
  }

  public RestClient getRestClient()
  {
    return new RestClient(url, accessKey);
  }

  @Override
  public String toString()
  {
    return name;
  }
}
