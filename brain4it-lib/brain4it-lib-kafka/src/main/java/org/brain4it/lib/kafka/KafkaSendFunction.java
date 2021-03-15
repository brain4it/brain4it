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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

/**
 *
 * @author quergf
 */
public class KafkaSendFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaSendFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  /**
   * Generic call from Brain4IT:
   * <code>(kafka-send producer-id topics messages)</code>
   *
   * @param context
   * @param args Positional arguments: - producer-id: application id identifying
   * this producer - topics: topic or list of topics to send the messages -
   * messages: list with or without elements, or string. Message keys are
   * optional in kafka
   * @return
   * @throws Exception
   */
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 3);
    String producerId;
    BList topics, messages;
    Object topicsObj, messagesObj;
    try
    {
      producerId = (String) context.evaluate(args.get(1));
      topicsObj = context.evaluate(args.get(2));
      messagesObj = context.evaluate(args.get(3));

      if (topicsObj instanceof BList)
      {
        topics = (BList) topicsObj;
      }
      else
      {
        topics = new BList(1);
        topics.add(topicsObj);
      }
      if (messagesObj instanceof BList)
      {
        messages = (BList) messagesObj;
      }
      else
      {
        messages = new BList(1);
        messages.add(messagesObj);
      }
    }
    catch (ClassCastException ex)
    {
      throw new IllegalArgumentException(ex);
    }
    // Check arguments
    // producer id
    KafkaProducer<Object, Object> prod = library.getProducer(producerId);
    if (prod == null)
    {
      throw new java.lang.Exception("Producer id not found");
    }

    // Send all messages to all topics
    try
    {
      for (Object topic : topics.toArray())
      {
        for (int i = 0; i < messages.size(); i++)
        {
          Object key = messages.getName(i);
          Object value = messages.get(i);

          if (key == null)
          {
            prod.send(new ProducerRecord<>((String) topic, value));
          }
          else
          {
            prod.send(new ProducerRecord<>((String) topic, key, value));
          }
        }
      }
      prod.flush();
    }
    catch (SerializationException ex)
    {
      throw new IllegalArgumentException(ex);
    }

    return null;
  }

}
