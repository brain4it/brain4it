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

import java.util.Properties;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaProducerFunction implements Function
{

  protected KafkaLibrary library;

  public KafkaProducerFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  /**
   * Generic call from Brain4IT:
   * <code>(kafka-producer servers key-serializer value-serializer)</code>
   *
   * @param context Brain4IT context
   * @param args Positional arguments: bootstrap server url list, key-serializer
   * classname, value-serializer classname,
   * @return
   * @throws Exception
   */
  @Override
  public String invoke(Context context, BList args) throws Exception
  {
    // positional arguments
    Utils.checkArguments(args, 1);
    Object serversRaw = context.evaluate(args.get(1));
    // named and optional arguments
    String keySerializer = (String) context.evaluate(args.get("key-serializer"));
    String valueSerializer = (String) context.evaluate(args.get("value-serializer"));
    // fill optional arguments with default values if not provided
    if (keySerializer == null)
    {
      keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
    }
    if (valueSerializer == null)
    {
      valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";
    }

    // servers.
    // Throws an exception if conditions are nor met or casting fails
    String serversStr = KafkaLibrary.flattenInput(serversRaw);

    // serializers
    // Expect either:
    // - complete classnames like "org.apache.kafka.common.serialization.StringSerializer"
    // - classname base like "StringSerializer", that we complete assuming its package
    if (!keySerializer.contains("."))
    {
      keySerializer = "org.apache.kafka.common.serialization." + keySerializer;
    }
    if (!valueSerializer.contains("."))
    {
      valueSerializer = "org.apache.kafka.common.serialization." + valueSerializer;
    }

    // fill in properties
    Properties properties = new Properties();
    properties.put("bootstrap.servers", serversStr);

    properties.put("key.serializer", keySerializer);
    properties.put("value.serializer", valueSerializer);
    KafkaProducer app = new KafkaProducer<>(properties);

    // save the app in the shared map
    return library.putApp(app, "p" + KafkaLibrary.randomId());
  }

}
