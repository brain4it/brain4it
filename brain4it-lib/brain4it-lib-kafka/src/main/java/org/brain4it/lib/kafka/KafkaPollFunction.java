/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;

/**
 *
 * @author quergf
 */
public class KafkaPollFunction implements Function{
    
    
    protected KafkaLibrary library;

    public KafkaPollFunction(KafkaLibrary library) {
        this.library = library;
    }

    @Override
    public BList invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 3);
        String consumerId = (String) context.evaluate(args.get(1));
        String topic = (String) context.evaluate(args.get(2));
        Long timeout = new Long((Integer) context.evaluate(args.get(3)));
        
        // Check arguments
        KafkaConsumer cons = (KafkaConsumer) library.getApp(consumerId);
        if (cons == null) {
            throw new java.lang.Exception("Consumer id not found");
        }
        
        // Subscribe to topics
        cons.subscribe(asList(topic));
        
        // Build BList of records, where:
        // - BList name is Kafka records's id
        // - BList element is Kafka record's value
        BList result = new BList();
        ConsumerRecords records = cons.poll(timeout);
        for (ConsumerRecord record: (Iterable<ConsumerRecord>) records.records(topic)) {
            if (record.key() != null)
                result.put(record.key(), record.value());
            else
                result.add(record.value());
        }
        
        return result;
    }
    
}
