/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.KafkaLibrary;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author quergf
 */
public class KafkaDeleteAppFunctionTest {
    
    public KafkaDeleteAppFunctionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of invoke method, of class KafkaDeleteAppFunction.
     */
    @Test
    public void testInvoke() throws Exception {
        System.out.println("invoke");

        KafkaLibrary klib = new KafkaLibrary();
        Context context = new Context(new BList(), null);
        
        // Create producer
        BList args = new BList(2);
        args.add("kafka-producer");
        args.add("localhost:9092");
        KafkaProducerFunction producer = new KafkaProducerFunction(klib);
        String prodId = producer.invoke(context, args);
        
        // Create consumer
        args.removeAll();
        args.add("kafka-consumer");
        args.add("localhost:9092");
        KafkaConsumerFunction consumer = new KafkaConsumerFunction(klib);
        String consId = consumer.invoke(context, args);
        
        // Delete both apps
        KafkaDeleteAppFunction instance = new KafkaDeleteAppFunction(klib);
        
        args.removeAll();
        args.add("kafka-delete");
        args.add(prodId);
        assert(instance.invoke(context, args));
                
        args.removeAll();
        args.add("kafka-delete");
        args.add(consId);
        assert(instance.invoke(context, args));
    }
    
}
