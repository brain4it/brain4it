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
package org.brain4it.lib;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.brain4it.lib.database.DatabaseRollbackFunction;
import org.brain4it.lib.database.DatabaseCommitFunction;
import org.brain4it.lib.database.DatabaseConnectFunction;
import org.brain4it.lib.database.DatabaseDisconnectFunction;
import org.brain4it.lib.database.DatabaseExecuteFunction;

/**
 *
 * @author realor
 */
public class DatabaseLibrary extends Library
{
  private final Map<String, Connection> connections = 
    Collections.synchronizedMap(new HashMap<String, Connection>());
  
  
  @Override
  public String getName()
  {
    return "Database";
  }

  @Override
  public void load()
  {
    functions.put("db-connect", new DatabaseConnectFunction(this));
    functions.put("db-disconnect", new DatabaseDisconnectFunction(this));
    functions.put("db-execute", new DatabaseExecuteFunction(this));
    functions.put("db-commit", new DatabaseCommitFunction(this));
    functions.put("db-rollback", new DatabaseRollbackFunction(this));
  }
  
  @Override
  public void unload()
  {
    for (Connection connection : connections.values())
    {
      try
      {
        connection.close();
      }
      catch (Exception ex)
      {        
      }
    }
  }
  
  public Connection getConnection(String connectionId)
  {
    return connections.get(connectionId);
  }

  public String putConnection(Connection connection)
  {
    UUID uuid = UUID.randomUUID();
    String connectionId = Long.toHexString(uuid.getMostSignificantBits()) + 
      Long.toHexString(uuid.getLeastSignificantBits());
    connections.put(connectionId, connection);
    return connectionId;
  }
  
  public Connection removeConnection(String connectionId)
  {
    return connections.remove(connectionId);
  }
}
