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
package org.brain4it.lib.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.lib.DatabaseLibrary;

/**
 *
 * @author realor
 */
public class DatabaseExecuteFunction extends DatabaseFunction
{
  public DatabaseExecuteFunction(DatabaseLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String connectionId = (String)context.evaluate(args.get(1));
    Connection connection = library.getConnection(connectionId);
    if (connection != null)
    {
      Object result = null;
      String sql = (String)context.evaluate(args.get(2));
      
      ArrayList<String> parameterNames = new ArrayList<String>();
      String xsql = parse(sql, parameterNames);      
      PreparedStatement statement = connection.prepareStatement(xsql);
      try
      {
        BList parameterValues = (BList)context.evaluate(args.get("parameters"));      
        if (parameterNames.size() > 0 && parameterValues != null)
        {
          setParameters(statement, parameterNames, parameterValues);
        }
        Object value = context.evaluate(args.get("max-rows"));
        if (value instanceof Number)
        {
          statement.setMaxRows(((Number)value).intValue());        
        }
        boolean isSelect = statement.execute();
        if (isSelect)
        {
          result = toList(statement.getResultSet());
        }
        else
        {
          result = statement.getUpdateCount();
        }
      }
      finally
      {
        statement.close();
      }
      return result;
    }
    return null;
  }

  private String parse(String sql, ArrayList<String> parameterNames)
  {
    char ch;
    StringBuilder sqlBuffer = new StringBuilder();
    StringBuilder parameterBuffer = new StringBuilder();
    
    int state = 0;
    for (int i = 0; i < sql.length(); i++)
    {
      ch = sql.charAt(i);
      switch (state)
      {
        case 0: // expecting parameter
          if (ch == '{') state = 1;
          else
          {
            sqlBuffer.append(ch);
            if (ch == '\'') state = 2;
            else if (ch == '"') state = 3;
          }
          break;
        case 1: // inside {...}
          if (ch == '}')
          {
            parameterNames.add(parameterBuffer.toString());
            parameterBuffer.setLength(0);
            sqlBuffer.append("?");
            state = 0;
          }
          else parameterBuffer.append(ch);
          break;
        case 2: // inside '...'
          sqlBuffer.append(ch);
          if (ch == '\'') state = 0;
          break;
        case 3: // inside "..."
          sqlBuffer.append(ch);
          if (ch == '"') state = 0;
          break;
      }
    }
    return sqlBuffer.toString();
  }
  
  private void setParameters(PreparedStatement statement, 
    ArrayList<String> parameterNames, BList parameterValues)
    throws SQLException
  {
    for (int i = 1; i <= parameterNames.size(); i++)
    {
      String parameterName = parameterNames.get(i - 1);

      Object value = parameterValues.get(parameterName);
      if (value instanceof Boolean || 
          value instanceof Number || 
          value instanceof String)
      {
        statement.setObject(i, value);        
      }
      else
      {
        statement.setNull(i, Types.VARCHAR);
      }
    }
  }
  
  private BList toList(ResultSet resultSet) throws SQLException
  {
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columns = metaData.getColumnCount();
    Structure structure = new Structure(columns, columns);
    for (int i = 0; i < columns; i++)
    {
      structure.putName(i, metaData.getColumnName(i + 1));      
    }
    BList table = new BList();
    while (resultSet.next())
    {
      BList row = new BList(structure);
      table.add(row);
      for (int i = 0; i < columns; i++)
      {
        int type = metaData.getColumnType(i + 1);
        switch (type)
        {
          case Types.NULL:
            row.put(i, null);
            break;
          case Types.BOOLEAN:
            row.put(i, (Boolean)resultSet.getBoolean(i + 1));
            break;
          case Types.INTEGER:
            row.put(i, (Integer)resultSet.getInt(i + 1));
            break;
          case Types.FLOAT:
          case Types.DECIMAL:
          case Types.NUMERIC:
          case Types.BIGINT:
          case Types.REAL:
            row.put(i, (Double)resultSet.getDouble(i + 1));
            break;
          default:
            row.put(i, resultSet.getString(i + 1));
            break;
        }
      }
    }
    return table;
  }
}
