/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
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
public class KafkaAdminIntegrationTest {

    KafkaLibrary klib;
    Context context;
    
    public KafkaAdminIntegrationTest() {
    }

    @Before
    public void setUp() throws Exception {
        klib = new KafkaLibrary();
        context = new Context(new BList(), null);
    }

    // Admin methods
    
    protected BList createTopics(Collection<String> topics) throws Exception {
        BList adminArgs = new BList(2);
        adminArgs.add("kafka-create-topics");
        adminArgs.add("localhost:9092");
        BList topicsBList = new BList();
        for(String topic: topics)
            topicsBList.add(topic);
        adminArgs.add(topicsBList);

        KafkaCreateTopicsFunction createFn = new KafkaCreateTopicsFunction(klib);
        return createFn.invoke(context, adminArgs);
    }
    
    protected BList deleteTopics(Collection<String> topics) throws Exception {
        BList adminArgs = new BList(2);
        adminArgs.add("kafka-delete-topics");
        adminArgs.add("localhost:9092");
        BList topicsBList = new BList();
        for(String topic: topics)
            topicsBList.add(topic);
        adminArgs.add(topicsBList);

        KafkaDeleteTopicsFunction deleteFn = new KafkaDeleteTopicsFunction(klib);
        return deleteFn.invoke(context, adminArgs);
    }
    
    protected BList listTopics() throws Exception {
        BList adminArgs = new BList(2);
        adminArgs.add("kafka-list-topics");
        adminArgs.add("localhost:9092");

        KafkaListTopicsFunction deleteFn = new KafkaListTopicsFunction(klib);
        return deleteFn.invoke(context, adminArgs);
    }
    
    // Util
    protected boolean collectionHas(Collection<Object> col, Object target){
        for (Object item: col)
            if (item.equals(target))
                return true;
        return false;
    }
    protected boolean bListHas(BList list, Object target){
        for (int i = 0; i < list.size(); i++)
            if (target.equals(list.get(i)))
                return true;
        return false;
    }

    @Test
    public void adminIntegrationTest() throws Exception {
        
        // Create 3 topics
        ArrayList<String> newTopics = new ArrayList();
        newTopics.add("admin-integration-test--topic-1");
        newTopics.add("admin-integration-test--topic-2");
        newTopics.add("admin-integration-test--topic-3");
        createTopics(newTopics);
        
        // Check that they exist
        BList existingTopics;
        existingTopics = listTopics();
        
        assert(bListHas(existingTopics, "admin-integration-test--topic-1"));
        assert(bListHas(existingTopics, "admin-integration-test--topic-2"));
        assert(bListHas(existingTopics, "admin-integration-test--topic-3"));
        
        // Delete 1 topic
        ArrayList<String> rmTopics = new ArrayList();
        rmTopics.add("admin-integration-test--topic-2");
        deleteTopics(rmTopics);
        
        // Check that it doesn't exist anymore, and that the others still exist
        existingTopics = listTopics();
        
        assert(bListHas(existingTopics, "admin-integration-test--topic-1"));
        assert(! bListHas(existingTopics, "admin-integration-test--topic-2"));
        assert(bListHas(existingTopics, "admin-integration-test--topic-3"));
        
        // Try to delete an unexistant topic
        deleteTopics(rmTopics);
        assert(true);
        
        // Try to create 2 existant topic and one unexistant
        createTopics(newTopics);
        assert(true);
        
        // Clean it all
        rmTopics.clear();
        rmTopics.add("admin-integration-test--topic-1");
        rmTopics.add("admin-integration-test--topic-2");
        rmTopics.add("admin-integration-test--topic-3");
        deleteTopics(rmTopics);
    }
}
