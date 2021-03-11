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

import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
public class KafkaPollFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaPollFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  @Override
  public BList invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);
    String consumerId = (String) context.evaluate(args.get(1));
    Long timeout = new Long((Integer) context.evaluate(args.get(2)));

    // Check arguments
    KafkaConsumer<Object, Object> cons = (KafkaConsumer<Object, Object>) library.getApp(consumerId);
    if (cons == null)
    {
      throw new java.lang.Exception("Consumer id not found");
    }

    // Build BList of records, where:
    // - BList name is Kafka records's id
    // - BList element is Kafka record's value
    BList result = new BList();
    ConsumerRecords<Object, Object> records = cons.poll(timeout);
    Set<String> topics = cons.subscription();

    for (String topic : topics)
    {
      BList topicRecords = new BList();
      for (ConsumerRecord<Object, Object> record : records.records(topic))
      {
        if (record.key() != null)
        {
          topicRecords.put(record.key(), record.value());
        }
        else
        {
          topicRecords.add(record.value());
        }
      }
      result.put(topic, topicRecords);
    }

    return result;
  }

}
