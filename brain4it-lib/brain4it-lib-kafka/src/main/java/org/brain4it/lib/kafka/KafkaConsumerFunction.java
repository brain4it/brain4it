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

import java.util.Properties;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * (kafka-consumer servers key-deserializer value-deserializer)
 *
 * @author quergf
 */
public class KafkaConsumerFunction implements Function
{
  protected KafkaLibrary library;

  public KafkaConsumerFunction(KafkaLibrary library)
  {
    this.library = library;
  }

  @Override
  public String invoke(Context context, BList args) throws Exception
  {
    // positional arguments
    Utils.checkArguments(args, 1);
    Object serversRaw = context.evaluate(args.get(1));
    if (serversRaw == null) {
      throw new IllegalArgumentException("servers cannot be null");
    }

    // named and optional arguments
    Object keyDeserializer = context.evaluate(args.get("key-deserializer"));
    Object valueDeserializer = context.evaluate(args.get("value-deserializer"));
    Object consumerGroupId = context.evaluate(args.get("group-id"));
    Object autoCommit = context.evaluate(args.get("enable-auto-commit"));
    Object autoCommitInterval = context.evaluate(args.get("auto-commit-interval"));
    Object autoOffsetReset = context.evaluate(args.get("auto-offset-reset"));
    Object maxPollRecords = context.evaluate(args.get("max-poll-records"));
    Object maxPollInterval = context.evaluate(args.get("max-poll-interval"));
    Object sessionTimeout = context.evaluate(args.get("session-timeout"));
    Object heartbeatInterval = context.evaluate(args.get("heartbeat-interval"));

    // fill in defaults
    if (keyDeserializer == null)
    {
      keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    }
    if (valueDeserializer == null)
    {
      valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    }
    if (autoCommit == null)
    {
      autoCommit = false; // Ensure boolean
    }
    if (autoCommitInterval == null)
    {
      autoCommitInterval = 5000; // Kafka default
    }
    if (autoOffsetReset == null)
    {
      autoOffsetReset = "latest"; // Your requirement
    }
    if (maxPollRecords == null)
    {
      maxPollRecords = 10; // Your requirement
    }
    if (maxPollInterval == null)
    {
      maxPollInterval = 300000; // Reduced to Kafka default
    }
    if (sessionTimeout == null)
    {
      sessionTimeout = 10000; // Kafka default
    }
    if (heartbeatInterval == null)
    {
      heartbeatInterval = 3000; // Kafka default
    }

    // Consumer groups
    String appId = KafkaLibrary.CONSUMER_SUFFIX + KafkaLibrary.randomId();
    if (consumerGroupId == null)
    {
      consumerGroupId = appId;
    }

    // servers
    String serversStr = KafkaLibrary.flattenInput(serversRaw);
    if (serversStr == null || serversStr.trim().isEmpty()) {
      throw new IllegalArgumentException("servers cannot be empty");
    }

    // deserializers: prepend package if no dots
    if (keyDeserializer instanceof String && !((String) keyDeserializer).contains("."))
    {
      keyDeserializer = "org.apache.kafka.common.serialization." + keyDeserializer;
    }
    if (valueDeserializer instanceof String && !((String) valueDeserializer).contains("."))
    {
      valueDeserializer = "org.apache.kafka.common.serialization." + valueDeserializer;
    }

    // validate numeric properties
    int commitIntervalMs = toInt(autoCommitInterval, "auto.commit.interval.ms");
    int pollIntervalMs = toInt(maxPollInterval, "max.poll.interval.ms");
    int pollRecords = toInt(maxPollRecords, "max.poll.records");
    int sessionTimeoutMs = toInt(sessionTimeout, "session.timeout.ms");
    int heartbeatIntervalMs = toInt(heartbeatInterval, "heartbeat.interval.ms");

    // validate heartbeat vs session timeout
    if (heartbeatIntervalMs >= sessionTimeoutMs) {
      throw new IllegalArgumentException("heartbeat.interval.ms (" + heartbeatIntervalMs +
          ") must be less than session.timeout.ms (" + sessionTimeoutMs + ")");
    }

    // fill in properties
    Properties properties = new Properties();
    properties.put("bootstrap.servers", serversStr);
    properties.put("group.id", consumerGroupId.toString());
    properties.put("key.deserializer", keyDeserializer.toString());
    properties.put("value.deserializer", valueDeserializer.toString());
    properties.put("enable.auto.commit", autoCommit); // Ensure boolean true/false
    properties.put("auto.commit.interval.ms", String.valueOf(commitIntervalMs));
    properties.put("auto.offset.reset", autoOffsetReset.toString());
    properties.put("max.poll.interval.ms", String.valueOf(pollIntervalMs));
    properties.put("max.poll.records", String.valueOf(pollRecords));
    properties.put("session.timeout.ms", String.valueOf(sessionTimeoutMs));
    properties.put("heartbeat.interval.ms", String.valueOf(heartbeatIntervalMs));

    try {
      KafkaConsumer<Object, Object> app = new KafkaConsumer<>(properties);
      return library.putConsumer(app, appId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to construct Kafka consumer", e);
    }
  }

  private int toInt(Object value, String propertyName) {
    if (value == null) {
      throw new IllegalArgumentException(propertyName + " cannot be null");
    }
    try {
      if (value instanceof Number) {
        return ((Number) value).intValue();
      } else if (value instanceof String) {
        return Integer.parseInt((String) value);
      } else {
        throw new IllegalArgumentException(propertyName + " must be a number or numeric string, got: " + value);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid " + propertyName + ": " + value, e);
    }
  }
}