/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.KafkaLibrary;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 *
 * @author quergf
 */
public class KafkaConsumerFunctionTest {
    
    public KafkaConsumerFunctionTest() {
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
     * Test of invoke method, of class KafkaProducerFunction.
     * @throws java.lang.Exception
     */
    @Test
    public void testInvoke() throws Exception {
        System.out.println("invoke");
        
        KafkaLibrary klib = new KafkaLibrary();
        Context context = new Context(new BList(), null);
        BList args = new BList(2);
        args.add("kafka-consumer");
        args.add("localhost:9092");
        KafkaConsumerFunction instance = new KafkaConsumerFunction(klib);
        String result = instance.invoke(context, args);
        assert(result.startsWith("c"));
    }
}