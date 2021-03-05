/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import java.util.Set;
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
    KafkaConsumer cons = (KafkaConsumer) library.getApp(consumerId);
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
    ArrayList<String> desiredTopics = new ArrayList();
    for (Object topic : topics.toArray())
    {
      desiredTopics.add((String) topic);
    }
    cons.subscribe(desiredTopics);

    // Check currently subscribed topics
    BList currentTopics = new BList();
    for (String topic : (Set<String>) cons.subscription())
    {
      currentTopics.add(topic);
    }

    return currentTopics;
  }

}
