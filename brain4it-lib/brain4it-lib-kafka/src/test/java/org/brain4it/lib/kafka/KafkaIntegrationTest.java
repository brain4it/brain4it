/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class KafkaIntegrationTest {

    // common
    protected KafkaLibrary klib;
    protected Context context;
    protected String prodId = "";
    protected String consId = "";
    // test 1
    protected final String topic1 = "greetings";
    protected final String messageStr = "Brain4IT <3 Kafka";
    ;
    // test 2
    protected final String[] messageArr = {"Brain4IT", "<3", "Kafka"};
    protected final String topic2 = "array-of-greetings";
    volatile ArrayList receivedMessages;
    // test 3
    protected final String topic3 = "sensors";
    protected final String messageKey = "thermometer-diff";
    protected final double messageValue = 1.5;

    public KafkaIntegrationTest() {
    }

    @Before
    public void setUp() throws Exception {
        receivedMessages = new ArrayList<String>();
        klib = new KafkaLibrary();
        context = new Context(new BList(), null);
    }

    @After
    public void tearDown() {
    }

    protected String newProducer(boolean numeric) throws Exception {
        BList prodArgs = new BList(2);
        prodArgs.add("kafka-producer");
        prodArgs.add("localhost:9092");
        if (numeric) {
            prodArgs.put("value-serializer", "DoubleSerializer");
        }
        KafkaProducerFunction prodFn = new KafkaProducerFunction(klib);
        prodId = prodFn.invoke(context, prodArgs);
        return prodId;
    }

    protected String newConsumer(boolean numeric) throws Exception {
        BList prodArgs = new BList(2);
        prodArgs.add("kafka-consumer");
        prodArgs.add("localhost:9092");
        if (numeric) {
            prodArgs.put("value-deserializer", "DoubleDeserializer");
        }
        KafkaConsumerFunction consFn = new KafkaConsumerFunction(klib);
        consId = consFn.invoke(context, prodArgs);
        return consId;
    }

    // Tests
    
    // FIXME: not working for timing issues
    // @Test
    public void simpleMessageTest() throws Exception {

        // Common
        System.out.println("invoke");

        prodId = newProducer(false);
        consId = newConsumer(false);

        // Send
        BList sendArgs = new BList(4);
        sendArgs.add("kafka-send");
        sendArgs.add(prodId);
        sendArgs.add(topic1);
        sendArgs.add(messageStr);

        KafkaSendFunction sendFn = new KafkaSendFunction(klib);
        Object sendResult = sendFn.invoke(context, sendArgs);
        assertEquals(sendResult, null);

        // Receive
        BList pollArgs = new BList(4);
        pollArgs.add("kafka-poll");
        pollArgs.add(consId);
        pollArgs.add(topic1);
        pollArgs.add(3000);

        KafkaPollFunction pollFn = new KafkaPollFunction(klib);
        BList pollResult = new BList();
        while (pollResult.size() == 0) {
            pollResult = pollFn.invoke(context, pollArgs);
        }

        // Compare input and output
        assertEquals(pollResult.get(0), messageStr);

        // Clean
        KafkaDeleteAppFunction deleteFn = new KafkaDeleteAppFunction(klib);
        BList deleteArgs = new BList(2);

        deleteArgs.add("kafka-delete");
        deleteArgs.add(consId);
        deleteFn.invoke(context, deleteArgs);

        deleteArgs.removeAll();
        deleteArgs.add("kafka-delete");
        deleteArgs.add(prodId);
        deleteFn.invoke(context, deleteArgs);
    }

    //@Test
    public void multipleMessagesTest() throws Exception {
        // Common
        System.out.println("invoke");

        prodId = newProducer(false);
        consId = newConsumer(false);

        // Create topic
        BList adminArgs = new BList(2);
        adminArgs.add("kafka-create-topics");
        adminArgs.add("localhost:9092");
        adminArgs.add(topic2);

        KafkaCreateTopicsFunction createFn = new KafkaCreateTopicsFunction(klib);
        createFn.invoke(context, adminArgs);
        //assert something on invoke result

        // Receive
        Thread receiveT = new Thread() {
            @Override
            public void run() {
                KafkaPollFunction pollFn = new KafkaPollFunction(klib);
                BList pollResults;
                int messageCount = 0, retries = 0;
                BList pollArgs = new BList(4);
                pollArgs.add("kafka-poll");
                pollArgs.add(consId);
                pollArgs.add(topic2);
                pollArgs.add(3000);
                try {
                    while (messageCount < 3) {
                        pollResults = pollFn.invoke(context, pollArgs);
                        messageCount += pollResults.size();
                        //for (String pollResult: pollResults.)
                        for (int i = 0; i < pollResults.size(); i++) {
                            receivedMessages.add((String) pollResults.get(i));
                        }
                        Thread.sleep(500);
                        assert (retries < 3);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(KafkaIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        receiveT.start();

        // Send
        Thread sendT = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    BList sendArgs = new BList(4);
                    sendArgs.add("kafka-send");
                    sendArgs.add(prodId);
                    sendArgs.add(topic2);
                    BList messageList = new BList();
                    for (String message : messageArr) {
                        messageList.add(message);
                    }
                    sendArgs.add(messageList);

                    KafkaSendFunction sendFn = new KafkaSendFunction(klib);
                    Object sendResult = sendFn.invoke(context, sendArgs);
                    assertEquals(sendResult, null);
                } catch (Exception ex) {
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
        for (int i = 0; i < messageArr.length; i++) {
            assertEquals(messageArr[i], receivedMessages.get(i));
        }

        // Clean
        KafkaDeleteAppFunction deleteFn = new KafkaDeleteAppFunction(klib);
        BList deleteArgs = new BList(2);

        deleteArgs.add("kafka-delete");
        deleteArgs.add(consId);
        deleteFn.invoke(context, deleteArgs);

        deleteArgs.removeAll();
        deleteArgs.add("kafka-delete");
        deleteArgs.add(prodId);
        deleteFn.invoke(context, deleteArgs);
    }

    @Test
    public void numericMessageTest() {

    }

    @Test
    public void MessageWithKeyTest() {

    }
}
