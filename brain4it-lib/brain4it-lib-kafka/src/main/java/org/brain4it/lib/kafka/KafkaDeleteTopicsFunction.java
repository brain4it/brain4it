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
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

public class KafkaDeleteTopicsFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaDeleteTopicsFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  /**
   * Generic call from Brain4IT:
   * <code>(kafka-delete-topics servers topics)</code>
   *
   * @param context Brain4IT context
   * @param args Positional arguments: bootstrap server url list or string,
   * topics list or string
   * @return BList of isDone status for each new topic
   * @throws Exception
   */
  @Override
  public BList invoke(Context context, BList args) throws Exception
  {
    // positional arguments
    Utils.checkArguments(args, 2);

    Object serversRaw = context.evaluate(args.get(1));
    String serversStr = KafkaLibrary.flattenInput(serversRaw);

    Object topicsRaw = context.evaluate(args.get(2));
    ArrayList<String> topicsList = new ArrayList<>();
    if (topicsRaw instanceof String)
    {
      topicsList.add((String) topicsRaw);
    }
    else if (topicsRaw instanceof BList)
    {
      for (Object topic : ((BList) topicsRaw).toArray())
      {
        topicsList.add((String) topic);
      }
    }

    // fill in properties
    Properties properties = new Properties();
    properties.put("bootstrap.servers", serversStr);

    AdminClient admin = KafkaAdminClient.create(properties);
    DeleteTopicsResult kresult = admin.deleteTopics(topicsList);
    if (kresult == null)
    {
      return null;
    }

    BList result = new BList();
    for (String key : kresult.values().keySet())
    {
      // wait for each topic to complete
      boolean deleted;
      try
      {
        kresult.values().get(key).get();
        deleted = true;
      }
      catch (Exception ex)
      {
        // ExecutionException if topic doesn't exist
        // Other exceptions
        deleted = false;
      }
      result.put(key, deleted);
    }

    return result;
  }

}
