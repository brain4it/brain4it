/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

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
public class KafkaProducerFunctionTest {
    
    public KafkaProducerFunctionTest() {
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
     */
    @Test
    public void testInvoke() throws Exception {
        System.out.println("invoke");
        
        KafkaLibrary klib = new KafkaLibrary();
        Context context = new Context(new BList(), null);
        BList args = new BList(2);
        args.add("kafka-producer");
        args.add("localhost:9092");
        KafkaProducerFunction instance = new KafkaProducerFunction(klib);
        String expResult = "";
        String result = instance.invoke(context, args);
        assert(result instanceof String);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
