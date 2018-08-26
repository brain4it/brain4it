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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSingleReference;

/**
 *
 * @author realor
 */
public class Workspace
{
  private String name = "Workspace";
  private final List<Server> servers;
  
  public Workspace()
  {
    this.servers = new ArrayList<Server>();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
  
  public List<Server> getServers()
  {
    return servers;
  }
  
  public void sortServersByName()
  {
    Collections.sort(servers, new Comparator<Server>()
    {
      @Override
      public int compare(Server s1, Server s2)
      {
        return s1.getName().compareTo(s2.getName());
      }
    });
  }
  
  public Server getServer(String name)
  {
    int index = getServerIndex(name);
    return index == -1 ? null: servers.get(index);
  }
  
  public int getServerIndex(String name)
  {
    int index = 0;
    boolean found = false;
    while (index < servers.size() && !found)
    {      
      Server server = servers.get(index);
      if (server.getName().equals(name))
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
  
  public static Workspace loadWorkspace(File workspaceFile) 
    throws IOException, ParseException
  {
    Workspace workspace = new Workspace();    
    Parser parser = new Parser(new FileReader(workspaceFile));
    BList workspaceList = (BList)parser.parse();
    workspace.setName((String)workspaceList.get(1));
    for (int i = 2; i < workspaceList.size(); i++)
    {
      BList serverList = (BList)workspaceList.get(i);
      Server server = new Server(workspace);
      server.setName((String)serverList.get(1));
      server.setUrl((String)serverList.get(2));
      server.setAccessKey((String)serverList.get(3));
      workspace.getServers().add(server);
      for (int j = 4; j < serverList.size(); j++)
      {
        BList moduleList = (BList)serverList.get(j);
        Module module = new Module(server);
        module.setName((String)moduleList.get(1));
        module.setAccessKey((String)moduleList.get(2));
        if (moduleList.size() > 3)
        {
          module.setMetadata((BList)moduleList.get(3));
        }
        server.getModules().add(module);
      }
    }
    return workspace;
  }
  
  public static void saveWorkspace(Workspace workspace, File workspaceFile)
    throws IOException
  {
    BList workspaceList = new BList();
    workspaceList.add(new BSingleReference("workspace"));
    workspaceList.add(workspace.getName());
    for (Server server : workspace.getServers())
    {
      BList serverList = new BList();
      serverList.add(new BSingleReference("server"));
      serverList.add(server.getName());
      serverList.add(server.getUrl());
      serverList.add(server.getAccessKey());
      workspaceList.add(serverList);
      for (Module module : server.getModules())
      {
        BList moduleList = new BList();
        moduleList.add(new BSingleReference("module"));
        moduleList.add(module.getName());
        moduleList.add(module.getAccessKey());
        if (module.getMetadata() != null)
        {
          moduleList.add(module.getMetadata());
        }
        serverList.add(moduleList);
      }
    }
    workspaceFile.getParentFile().mkdirs();
    Writer writer = new FileWriter(workspaceFile);
    try
    {
      Printer printer = new Printer(writer);
      printer.print(workspaceList);
    }
    finally
    {
      writer.close();
    }    
  }
}
