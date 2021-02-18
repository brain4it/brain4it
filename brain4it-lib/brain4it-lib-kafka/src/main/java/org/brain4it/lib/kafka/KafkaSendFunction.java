/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
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
     * @param args Positional arguments: - producer-id: application id
     * identifying this producer - topics: topic or list of topics to send the
     * messages - messages: list with or without elements, or string. Message
     * keys are optional in kafka
     * @return
     * @throws Exception
     */
    @Override
    public Object invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 3);
        String producerId;
        BList topics, messages;
        Object topicsObj, messagesObj;
        try {
            producerId = (String) context.evaluate(args.get(1));
            topicsObj = context.evaluate(args.get(2));
            messagesObj =  context.evaluate(args.get(3));

            if (topicsObj instanceof BList) {
                topics = (BList) topicsObj;
            } else {
                topics = new BList(1);
                topics.add(topicsObj);
            }
            if (messagesObj instanceof BList) {
                messages = (BList) messagesObj;
            } else {
                messages = new BList(1);
                messages.add(messagesObj);
            }
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
        // Check arguments
        // producer id
        KafkaProducer prod = (KafkaProducer) library.getApp(producerId);
        if (prod == null) {
            throw new java.lang.Exception("Producer id not found");
        }

        // Send all messages to all topics
        try {
            for (Object topic : topics.toArray()) {
                for (int i = 0; i < messages.size(); i++) {
                    Object key = messages.getName(i);
                    Object value = messages.get(i);

                    if (key == null) {
                        prod.send(new ProducerRecord((String) topic, value));
                    } else {
                        prod.send(new ProducerRecord((String) topic, key, value));
                    }
                }
            }
            prod.flush();
        } catch (SerializationException ex) {
            throw new IllegalArgumentException(ex);
        };

        return null;
    }

}
