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
import org.brain4it.lib.kafka.KafkaConsumerFunction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.lib.kafka.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.brain4it.lang.BList;

/**
 *
 * @author quergf
 */
public class KafkaLibrary extends Library
{

  //private final Map<String, AutoCloseable> apps = 
  //Collections.synchronizedMap(new HashMap<String, AutoCloseable>());
  protected final Map<String, AutoCloseable> apps
    = Collections.synchronizedMap(new HashMap<String, AutoCloseable>());

  @Override
  public String getName()
  {
    return "Kafka";
  }

  @Override
  public void load()
  {
    functions.put("kafka-consumer", new KafkaConsumerFunction(this));
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
    for (AutoCloseable app : apps.values())
    {
      try
      {
        if (app instanceof KafkaConsumer)
        {
          ((KafkaConsumer) app).unsubscribe();
        }
        app.close();
      }
      catch (Exception ex)
      {
        Logger.getLogger(KafkaLibrary.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public AutoCloseable getApp(String appId)
  {
    return apps.get(appId);
  }

  public static String randomId()
  {
    UUID uuid = UUID.randomUUID();
    return Long.toHexString(uuid.getMostSignificantBits())
      + Long.toHexString(uuid.getLeastSignificantBits());
  }

  public String putApp(AutoCloseable kafkaApp, String appId)
  {
    apps.put(appId, kafkaApp);
    return appId;
  }

  public AutoCloseable removeApp(String appId)
  {
    return apps.remove(appId);
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
      ArrayList arr = new ArrayList<String>();
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
