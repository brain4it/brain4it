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
 * Software Foundation; either version 3 of the License, or (at your option) 
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

import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.brain4it.lib.KafkaLibrary;

/**
 * (kafka-commit consumer-id)
 *
 * Commits the offsets for the specified Kafka consumer.
 *
 * @author kfiertek
 */
public class KafkaCommitFunction implements Function
{
  protected KafkaLibrary library;

  public KafkaCommitFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  /**
   * Generic call from Brain4IT:
   * <code>(kafka-commit consumer-id)</code>
   *
   * @param context Brain4IT context
   * @param args Positional arguments: consumer-id (string)
   * @return null on success
   * @throws Exception if consumer-id is invalid or commit fails
   */
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    // positional arguments
    Utils.checkArguments(args, 1);
    Object consumerIdRaw = context.evaluate(args.get(1));
    if (!(consumerIdRaw instanceof String)) {
      throw new IllegalArgumentException("consumer-id must be a string");
    }
    String consumerId = (String) consumerIdRaw;

    // Retrieve consumer from shared map
    KafkaConsumer<Object, Object> consumer = library.getConsumer(consumerId);
    if (consumer == null) {
      throw new IllegalArgumentException("Invalid or closed consumer-id: " + consumerId);
    }

    try {
      // Commit offsets synchronously
      consumer.commitSync();
      return "true"; // Brain4it functions return null for void operations
    } catch (Exception e) {
      throw new RuntimeException("Failed to commit offsets for consumer: " + consumerId, e);
    }
  }
}