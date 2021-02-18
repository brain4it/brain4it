/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Structure;
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
public class KafkaSendFunctionTest {

    KafkaProducerFunction prod;
    String prodId;
    KafkaLibrary klib;
    Context context;

    public KafkaSendFunctionTest() {
        this.klib = new KafkaLibrary();
        this.context = new Context(new BList(), null);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        BList prodArgs = new BList(2);
        prodArgs.add("kafka-producer");
        prodArgs.add("localhost:9092");
        this.prod = new KafkaProducerFunction(this.klib);
        this.prodId = this.prod.invoke(this.context, prodArgs);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of invoke method, of class KafkaSendFunction.
     */
    @Test
    public void testInvokeBLists() throws Exception {
        System.out.println("invoke");

        BList sendArgs = new BList(4);
        sendArgs.add("kafka-send");
        sendArgs.add(prodId);
        
        BList sendArgTopic = new BList(1);
        sendArgTopic.add("someTopic");
        sendArgTopic.add("someTopicClone");
        sendArgs.add(sendArgTopic);
        
        BList sendArgMessage = new BList(1);
        sendArgMessage.add("- s'apuja el teló i surt un txec repartint cartes, com es diu la película?");
        sendArgMessage.add("- el cartero siempre... chequea 2 veces?");
        sendArgMessage.add("- no! vale, no és una película");
        sendArgMessage.add("- pff llavors no jugo");
        sendArgMessage.add("- És Kafka!");
        sendArgs.add(sendArgMessage);
        
        KafkaSendFunction instance = new KafkaSendFunction(klib);
        Object expResult = null;
        Object result = instance.invoke(context, sendArgs);
        assertEquals(expResult, result);
        assert (true);
    }
    
    
    @Test
    public void testInvokeStrings() throws Exception {
        System.out.println("invoke");

        BList sendArgs = new BList(4);
        sendArgs.add("kafka-send");
        sendArgs.add(prodId);
        sendArgs.add("someTopic");
        sendArgs.add("Kafka!");
        
        KafkaSendFunction instance = new KafkaSendFunction(klib);
        Object expResult = null;
        Object result = instance.invoke(context, sendArgs);
        assertEquals(expResult, result);
        assert (true);
    }

}
