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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.KafkaLibrary;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author quergf
 */
public class KafkaIntegrationTest
{

  // common
  protected KafkaLibrary klib;
  protected Context context;
  protected String prodId = "";
  protected String consId = "";
  // test 1
  protected final String topic1 = "greetings";
  protected final String messageStr = "Brain4IT <3 Kafka";
  protected volatile BList pollResult;
  // test 2
  protected final String[] messageArr =
  {
    "Brain4IT", "<3", "Kafka"
  };
  protected final String topic2 = "array-of-greetings";
  protected volatile ArrayList receivedMessages;
  // test 3
  protected final String topic3 = "meaning-of-life";
  protected final double messageNum = 42.0;
  // test 4
  protected final String topic4 = "sensors";
  protected final String messageKey = "thermometer-diff";
  protected final double messageValue = 1.5;

  public KafkaIntegrationTest()
  {
  }

  @Before
  public void setUp() throws Exception
  {
    receivedMessages = new ArrayList<String>();
    klib = new KafkaLibrary();
    context = new Context(new BList(), null);
  }

  @After
  public void tearDown() throws Exception
  {
    deleteTopic(topic1);
    deleteTopic(topic2);
    deleteTopic(topic3);
    deleteTopic(topic4);
  }

  // Apps functions
  protected String newProducer(boolean numeric) throws Exception
  {
    BList prodArgs = new BList(2);
    prodArgs.add("kafka-producer");
    prodArgs.add("localhost:9092");
    if (numeric)
    {
      prodArgs.put("value-serializer", "DoubleSerializer");
    }
    KafkaProducerFunction prodFn = new KafkaProducerFunction(klib);
    prodId = prodFn.invoke(context, prodArgs);
    return prodId;
  }

  protected String newConsumer(boolean numeric) throws Exception
  {
    BList prodArgs = new BList(2);
    prodArgs.add("kafka-consumer");
    prodArgs.add("localhost:9092");
    if (numeric)
    {
      prodArgs.put("value-deserializer", "DoubleDeserializer");
    }
    KafkaConsumerFunction consFn = new KafkaConsumerFunction(klib);
    consId = consFn.invoke(context, prodArgs);
    return consId;
  }

  protected void deleteApp(String appId) throws Exception
  {
    KafkaDeleteAppFunction deleteFn = new KafkaDeleteAppFunction(klib);
    BList deleteArgs = new BList(2);

    deleteArgs.add("kafka-delete");
    deleteArgs.add(appId);
    deleteFn.invoke(context, deleteArgs);
  }

  // Topics functions
  protected void newTopic(String topic) throws Exception
  {
    BList adminArgs = new BList(2);
    adminArgs.add("kafka-create-topics");
    adminArgs.add("localhost:9092");
    adminArgs.add(topic);

    KafkaCreateTopicsFunction createFn = new KafkaCreateTopicsFunction(klib);
    createFn.invoke(context, adminArgs);

  }

  protected void deleteTopic(String topic) throws Exception
  {
    BList adminArgs = new BList(2);
    adminArgs.add("kafka-delete-topics");
    adminArgs.add("localhost:9092");
    adminArgs.add(topic);

    KafkaDeleteTopicsFunction deleteFn = new KafkaDeleteTopicsFunction(klib);
    deleteFn.invoke(context, adminArgs);
  }

  // Subscribe consumer to topic
  protected void subscribeTopics(String cons, String topic) throws Exception
  {
    BList subArgs = new BList(3);
    subArgs.add("kafka-subscribe");
    subArgs.add(cons);
    subArgs.add(topic);
    KafkaSubscribeFunction subFn = new KafkaSubscribeFunction(klib);
    BList subResult = subFn.invoke(context, subArgs);
    assertEquals(topic, (String) subResult.get(0));
  }

  // Tests //
  @Test
  public void simpleMessageTest() throws Exception
  {

    // Common
    System.out.println("simpleMessageTest");

    // Create topic
    newTopic(topic1);

    // Applications
    prodId = newProducer(false);
    consId = newConsumer(false);

    // Subscribe consumer to topic
    subscribeTopics(consId, topic1);

    // Receive
    Thread receiveT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList pollArgs = new BList(3);
          pollArgs.add("kafka-poll");
          pollArgs.add(consId);
          pollArgs.add(3000);

          KafkaPollFunction pollFn = new KafkaPollFunction(klib);
          pollResult = new BList();

          while (pollResult.size() == 0)
          {
            pollResult = pollFn.invoke(context, pollArgs);
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    receiveT.start();
    Thread.sleep(2000);

    // Send
    Thread sendT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList sendArgs = new BList(4);
          sendArgs.add("kafka-send");
          sendArgs.add(prodId);
          sendArgs.add(topic1);
          sendArgs.add(messageStr);

          KafkaSendFunction sendFn = new KafkaSendFunction(klib);
          Object sendResult = sendFn.invoke(context, sendArgs);
          assertEquals(sendResult, null);
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    sendT.start();

    // synchronize
    sendT.join();
    receiveT.join();

    // Compare input and output
    assertEquals(messageStr, ((BList) pollResult.get(topic1)).get(0));

    // Clean
    deleteApp(consId);
    deleteApp(prodId);
  }

  @Test
  public void multipleMessagesTest() throws Exception
  {
    // Common
    System.out.println("multipleMessageTest");

    // Create topic
    newTopic(topic2);

    // Applications
    prodId = newProducer(false);
    consId = newConsumer(false);

    // Subscribe consumer to topic
    subscribeTopics(consId, topic2);

    // Receive
    Thread receiveT = new Thread()
    {
      @Override
      public void run()
      {
        KafkaPollFunction pollFn = new KafkaPollFunction(klib);
        BList pollResults;
        int messageCount = 0, retries = 0;
        BList pollArgs = new BList(3);
        pollArgs.add("kafka-poll");
        pollArgs.add(consId);
        pollArgs.add(3000);
        try
        {
          while (messageCount < 3)
          {
            pollResults = pollFn.invoke(context, pollArgs);
            messageCount += pollResults.size();
            //for (String pollResult: pollResults.)
            for (int i = 0; i < ((BList) pollResults.get(topic2)).size(); i++)
            {
              receivedMessages.add((String) ((BList) pollResults.get(topic2)).get(i));
            }
            Thread.sleep(500);
            assert (retries < 3);
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    receiveT.start();
    Thread.sleep(2000);

    // Send
    Thread sendT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList sendArgs = new BList(4);
          sendArgs.add("kafka-send");
          sendArgs.add(prodId);
          sendArgs.add(topic2);
          BList messageList = new BList();
          for (String message : messageArr)
          {
            messageList.add(message);
          }
          sendArgs.add(messageList);

          KafkaSendFunction sendFn = new KafkaSendFunction(klib);
          Object sendResult = sendFn.invoke(context, sendArgs);
          assertEquals(sendResult, null);
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    sendT.start();

    // Synchronize
    sendT.join();
    receiveT.join();

    // Compare input and output
    assertEquals(messageArr.length, receivedMessages.size());
    for (int i = 0; i < messageArr.length; i++)
    {
      assertEquals(messageArr[i], receivedMessages.get(i));
    }

    // Clean
    deleteApp(consId);
    deleteApp(prodId);
  }

  @Test
  public void numericMessageTest() throws Exception
  {

    // Common
    System.out.println("numericMessageTest");

    // Create topic
    newTopic(topic3);

    // Applications
    prodId = newProducer(true);
    consId = newConsumer(true);

    // Subscribe consumer to topic
    subscribeTopics(consId, topic3);

    // Receive
    Thread receiveT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList pollArgs = new BList(3);
          pollArgs.add("kafka-poll");
          pollArgs.add(consId);
          pollArgs.add(3000);

          KafkaPollFunction pollFn = new KafkaPollFunction(klib);
          pollResult = new BList();

          while (pollResult.size() == 0)
          {
            pollResult = pollFn.invoke(context, pollArgs);
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    receiveT.start();
    Thread.sleep(2000);

    // Send
    Thread sendT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList sendArgs = new BList(4);
          sendArgs.add("kafka-send");
          sendArgs.add(prodId);
          sendArgs.add(topic3);
          sendArgs.add(messageNum);

          KafkaSendFunction sendFn = new KafkaSendFunction(klib);
          Object sendResult = sendFn.invoke(context, sendArgs);
          assertEquals(sendResult, null);
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    sendT.start();

    // synchronize
    sendT.join();
    receiveT.join();

    // Compare input and output
    assertEquals(messageNum, ((BList) pollResult.get(topic3)).get(0));

    // Clean
    deleteApp(consId);
    deleteApp(prodId);

  }

  @Test
  public void messageWithKeyTest() throws Exception
  {

    // Common
    System.out.println("messageWithKeyTest");

    // Create topic
    newTopic(topic4);

    // Applications
    prodId = newProducer(true);
    consId = newConsumer(true);

    // Subscribe consumer to topic
    subscribeTopics(consId, topic4);

    // Receive
    Thread receiveT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList pollArgs = new BList(3);
          pollArgs.add("kafka-poll");
          pollArgs.add(consId);
          pollArgs.add(3000);

          KafkaPollFunction pollFn = new KafkaPollFunction(klib);
          pollResult = new BList();

          while (pollResult.size() == 0)
          {
            pollResult = pollFn.invoke(context, pollArgs);
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    receiveT.start();
    Thread.sleep(2000);

    // Send
    Thread sendT = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          BList sendArgs = new BList(4);
          sendArgs.add("kafka-send");
          sendArgs.add(prodId);
          sendArgs.add(topic4);

          BList messageList = new BList();
          messageList.put(messageKey, messageValue);
          sendArgs.add(messageList);

          KafkaSendFunction sendFn = new KafkaSendFunction(klib);
          Object sendResult = sendFn.invoke(context, sendArgs);
          assertEquals(sendResult, null);
        }
        catch (Exception ex)
        {
          Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    sendT.start();

    // synchronize
    sendT.join();
    receiveT.join();

    // Compare input and output
    assertEquals(messageValue, ((BList) pollResult.get(topic4)).get(messageKey));

    // Clean
    deleteApp(consId);
    deleteApp(prodId);
  }
}
