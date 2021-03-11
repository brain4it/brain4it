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
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

/**
 *
 * @author quergf
 */
public class KafkaSubscribeFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaSubscribeFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  @Override
  public BList invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);
    Object topicsObj;
    BList topics;
    String consumerId;

    consumerId = (String) context.evaluate(args.get(1));

    // Check arguments
    KafkaConsumer<Object, Object> cons = (KafkaConsumer<Object, Object>) library.getApp(consumerId);
    if (cons == null)
    {
      throw new java.lang.Exception("Consumer id not found");
    }

    try
    {
      topicsObj = context.evaluate(args.get(2));

      if (topicsObj instanceof BList)
      {
        topics = (BList) topicsObj;
      }
      else
      {
        topics = new BList(1);
        topics.add(topicsObj);
      }
    }
    catch (ClassCastException ex)
    {
      throw new IllegalArgumentException(ex);
    }

    // Subscribe to topics
    ArrayList<String> desiredTopics = new ArrayList<String>();
    for (Object topic : topics.toArray())
    {
      desiredTopics.add((String) topic);
    }
    cons.subscribe(desiredTopics);

    // Check currently subscribed topics
    BList currentTopics = new BList();
    for (String topic : cons.subscription())
    {
      currentTopics.add(topic);
    }

    return currentTopics;
  }

}
