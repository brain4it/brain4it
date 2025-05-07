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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.lib.kafka.*;
import java.util.UUID;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.brain4it.lang.BList;

/**
 *
 * @author quergf
 */
public class KafkaLibrary extends Library
{

  public static String PRODUCER_SUFFIX = "p";
  public static String CONSUMER_SUFFIX = "c";

  protected final Map<String, KafkaConsumer<Object, Object>> consumers
    = Collections.synchronizedMap(new HashMap<String, KafkaConsumer<Object, Object>>());

  protected final Map<String, KafkaProducer<Object, Object>> producers
    = Collections.synchronizedMap(new HashMap<String, KafkaProducer<Object, Object>>());

  @Override
  public String getName()
  {
    return "Kafka";
  }

  @Override
  public void load()
  {
    functions.put("kafka-consumer", new KafkaConsumerFunction(this));
    functions.put("kafka-commit", new KafkaCommitFunction(this));
    functions.put("kafka-producer", new KafkaProducerFunction(this));
    functions.put("kafka-send", new KafkaSendFunction(this));
    functions.put("kafka-poll", new KafkaPollFunction(this));
    functions.put("kafka-delete-app", new KafkaDeleteAppFunction(this));
    functions.put("kafka-create-topics", new KafkaCreateTopicsFunction(this));
    functions.put("kafka-delete-topics", new KafkaDeleteTopicsFunction(this));
    functions.put("kafka-list-topics", new KafkaListTopicsFunction(this));
    functions.put("kafka-subscribe", new KafkaSubscribeFunction(this));
  }

  @Override
  public void unload()
  {
    for (KafkaConsumer<Object, Object> consumer : consumers.values())
    {
      // KafkaConsumer is not thread safe
      synchronized (consumer)
      {
        consumer.unsubscribe();
        consumer.close();
      }
    }

    for (KafkaProducer<Object, Object> producer : producers.values())
    {
      producer.close();
    }
  }

  public KafkaConsumer<Object, Object> getConsumer(String consId)
  {
    return consumers.get(consId);
  }

  public KafkaProducer<Object, Object> getProducer(String prodId)
  {
    return producers.get(prodId);
  }

  public static String randomId()
  {
    UUID uuid = UUID.randomUUID();
    return Long.toHexString(uuid.getMostSignificantBits())
      + Long.toHexString(uuid.getLeastSignificantBits());
  }

  public String putConsumer(KafkaConsumer<Object, Object> consumer, String consId)
  {
    consumers.put(consId, consumer);
    return consId;
  }

  public String putProducer(KafkaProducer<Object, Object> producer, String prodId)
  {
    producers.put(prodId, producer);
    return prodId;
  }

  public AutoCloseable removeApp(String appId)
  {
    if (appId.startsWith(CONSUMER_SUFFIX))
    {
      return consumers.remove(appId);
    }
    if (appId.startsWith(PRODUCER_SUFFIX))
    {
      return producers.remove(appId);
    }
    return null;
  }

  /**
   * Converts an unknown input to a string representations, usable as a
   * "property's" value.
   *
   * @param input. Expects either BList with strings or String
   * @return String representation of `input`, separating fields by ','
   * @throws ClassCastException
   */
  public static String flattenInput(Object input) throws ClassCastException
  {
    String str;
    if (input instanceof BList)
    {
      ArrayList<String> arr = new ArrayList<>();
      for (Object element : ((BList) input).toArray())
      {
        arr.add((String) element);
      }
      str = String.join(",", arr);
    }
    else if (input instanceof String)
    {
      str = (String) input;
    }
    else
    {
      throw new java.lang.ClassCastException("`servers` is not a list of strings nor a string");
    }
    return str;
  }
}
