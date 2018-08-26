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

package org.brain4it.lib.management;

import org.realor.hnap.HnapClient;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class HnapFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    if (args.size() < 3)
    {
      BList operations = new BList();
      operations.add("setSocketSettings");
      operations.add("getSocketSettings");
      operations.add("getCurrentPowerConsumition");
      operations.add("getCurrentTemperature");
      return operations;      
    }
    
    BList connection = (BList)context.evaluate(args.get(1));
    String url = Utils.toString(connection.get(0));
    String username = connection.size() > 1 ?
      Utils.toString(connection.get(1)) : null;
    String password = connection.size() > 2 ?
      Utils.toString(connection.get(2)) : null;

    HnapClient hnapClient = new HnapClient();
    hnapClient.setUrl(url);
    hnapClient.setUsername(username);
    hnapClient.setPassword(password);
    hnapClient.login();
    
    String operation = (String)context.evaluate(args.get(2));
    if (null == operation)
    {
      throw new Exception("Invalid operation");
    }
    else switch (operation)
    {
      case "setSocketSettings":
        boolean status = Utils.toBoolean(context.evaluate(args.get(3)));
        return hnapClient.setSocketSettings(status);
      case "getSocketSettings":
        return Utils.toBoolean(hnapClient.getSocketSettings());
      case "getCurrentPowerConsumition":
        return Utils.toNumber(hnapClient.getCurrentPowerConsumition());
      case "getCurrentTemperature":
        return Utils.toNumber(hnapClient.getCurrentTemperature());
      default:
        throw new Exception("Invalid operation");
    }
  }
}
