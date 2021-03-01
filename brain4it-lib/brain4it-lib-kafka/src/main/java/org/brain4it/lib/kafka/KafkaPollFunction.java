/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
public class KafkaPollFunction implements Function {

    protected KafkaLibrary library;

    public KafkaPollFunction(KafkaLibrary library) {
        this.library = library;
    }

    @Override
    public BList invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 2);
        String consumerId = (String) context.evaluate(args.get(1));
        Long timeout = new Long((Integer) context.evaluate(args.get(2)));

        // Check arguments
        KafkaConsumer cons = (KafkaConsumer) library.getApp(consumerId);
        if (cons == null) {
            throw new java.lang.Exception("Consumer id not found");
        }

        // Build BList of records, where:
        // - BList name is Kafka records's id
        // - BList element is Kafka record's value
        BList result = new BList();
        ConsumerRecords records = cons.poll(timeout);
        Set<String> topics = cons.subscription();

        for (String topic : topics) {
            BList topicRecords = new BList();
            for (ConsumerRecord record : (Iterable<ConsumerRecord>) records.records(topic)) {
                if (record.key() != null) {
                    topicRecords.put(record.key(), record.value());
                } else {
                    topicRecords.add(record.value());
                }
            }
            result.put(topic, topicRecords);
        }

        return result;
    }

}