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

import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class JmxGetFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);
   
    Object value;
    String urlString = Utils.toString(context.evaluate(args.get(1)));
    String objectString = Utils.toString(context.evaluate(args.get(2)));

    JMXServiceURL url = new JMXServiceURL(urlString);
    JMXConnector connector = JMXConnectorFactory.connect(url);
    
    try
    {
      connector.connect();
    
      MBeanServerConnection serverConn = connector.getMBeanServerConnection();
      ObjectName objectName = new ObjectName(objectString);
    
      if (args.size() <= 3)
      {
        MBeanAttributeInfo[] attributes = 
          serverConn.getMBeanInfo(objectName).getAttributes();
        BList list = new BList(attributes.length);
        for (MBeanAttributeInfo attribute : attributes)
        {
          list.add(attribute.getName());
        }
        value = list;
      }
      else
      {
        String attributeName = Utils.toString(context.evaluate(args.get(3)));
        value = convert(serverConn.getAttribute(objectName, attributeName));
      }
    }
    finally
    {
      connector.close();
    }
    return value; 
  }

  private Object convert(Object value)
  {
    if (value instanceof String || 
        value instanceof Number || 
        value instanceof Boolean) return value;

    if (value instanceof CompositeDataSupport)
    {
      CompositeDataSupport composite = (CompositeDataSupport)value;
      Set<String> keySet = composite.getCompositeType().keySet();
      BList list = new BList(keySet.size());
      for (String key : keySet)
      {
        list.put(key, composite.get(key));
      }
      value = list;
    }
    else if (value instanceof String[])
    {
      BList list = new BList();
      String array[] = (String[])value;
      for (String s : array)
      {
        list.add(s);
      }
      value = list;
    }
    else 
    {
      value = String.valueOf(value);
    }
    return value;
  }
}
