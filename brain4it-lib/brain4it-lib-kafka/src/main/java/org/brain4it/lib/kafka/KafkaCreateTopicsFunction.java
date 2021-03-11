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
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.brain4it.lang.BException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

public class KafkaCreateTopicsFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaCreateTopicsFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  /**
   * Generic call from Brain4IT:
   * <code>(kafka-create-topics servers topics)</code>
   *
   * @param context Brain4IT context
   * @param args Positional arguments: bootstrap server url list or string,
   * topics list or string
   * @return named BList showing wether a topic was created (true) or was \
   * already present, or otherwise (false)
   * @throws java.lang.InterruptedException
   */
  @Override
  public BList invoke(Context context, BList args) throws BException, InterruptedException
  {
    // positional arguments
    Utils.checkArguments(args, 2);

    Object serversRaw = context.evaluate(args.get(1));
    String serversStr = KafkaLibrary.flattenInput(serversRaw);

    Object topicsRaw = context.evaluate(args.get(2));
    ArrayList<NewTopic> topicsList = new ArrayList<NewTopic>();
    if (topicsRaw instanceof String)
    {
      topicsList.add(new NewTopic((String) topicsRaw, 1, (short) 1));
    }
    else if (topicsRaw instanceof BList)
    {
      for (Object topic : ((BList) topicsRaw).toArray())
      {
        //topicsList.add(new NewTopic((String) topic, Optional.<Integer>empty(), Optional.<Short>empty()));
        topicsList.add(new NewTopic((String) topic, 1, (short) 1));
      }
    }

    // fill in properties
    Properties properties = new Properties();
    properties.put("bootstrap.servers", serversStr);

    AdminClient admin = KafkaAdminClient.create(properties);
    CreateTopicsResult kresult;
    kresult = admin.createTopics(topicsList);

    if (kresult == null)
    {
      return null;
    }

    BList result = new BList();
    for (String key : kresult.values().keySet())
    {
      // wait for each topic to complete
      boolean created;
      try
      {
        kresult.values().get(key).get();
        created = true;
      }
      catch (ExecutionException ex)
      {
        created = false;
      }
      catch (Exception ex)
      {
        // ExecutionException if topic exists
        // Other exceptions
        created = false;
      }

      result.put(key, created);
    }

    return result;
  }

}
