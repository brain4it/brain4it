/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

/**
 *
 * @author quergf
 */
public class KafkaSendFunction implements Function {

    protected KafkaLibrary library;

    public KafkaSendFunction(KafkaLibrary library) {
        this.library = library;
    }

    /**
     * Generic call from Brain4IT:
     * <code>(kafka-send producer-id topics messages)</code>
     * 
     * @param context
     * @param args Positional arguments:
     *   - producer-id: application id identifying this producer
     *   - topics: topic or list of topics to send the messages
     *   - messages: list with or without elements, or string. Message keys are optional in kafka
     * @return
     * @throws Exception 
     */
    @Override
    public Object invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 3);
        String producerId = (String) context.evaluate(args.get(1));
        BList topics = (BList) context.evaluate(args.get(2));
        Object messagesRaw = context.evaluate(args.get(3));
        
       // Check arguments
       // producer id
        KafkaProducer prod = (KafkaProducer) library.getApp(producerId);
        if (prod == null) {
            throw new java.lang.Exception("producer id not found");
        }
        // messages
        BList messages = new BList(1);
        if (messagesRaw instanceof BList) {
            messages = (BList) messagesRaw;
        } else {
            messages.add(messagesRaw);
        }
        
        
        // Send all messages to all topics
        for (String topic: (String[])topics.toArray()) {
            for (int i = 0; i < messages.size(); i++) {
                Object key = messages.getName(i);
                Object value = messages.get(i);
                
                if (key == null) {
                    prod.send(new ProducerRecord(topic, value));
                } else {
                    prod.send(new ProducerRecord(topic, key, value));
                }
            }
        }
        
        return null;
    }

}
