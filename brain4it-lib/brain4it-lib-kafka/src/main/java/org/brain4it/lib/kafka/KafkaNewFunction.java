/*
 * Brain4it
 * 
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 * 
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 * 
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *   
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *   
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/ 
 *   and 
 *   https://www.gnu.org/licenses/lgpl.txt
 */
package org.brain4it.lib.kafka;

import java.util.Properties;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.KafkaLibrary;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.consumer.KafkaConsumer;



/**
 * (kafka-new apptype servers key-serializer value-serializer)
 * 
 * @author quergf
 */
public class KafkaNewFunction implements Function {
    
    protected KafkaLibrary library;

    public KafkaNewFunction(KafkaLibrary library) {
        this.library = library;
    }


    @Override
    public Object invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 4);
        String appType = (String)context.evaluate(args.get(1));
        String servers = (String)context.evaluate(args.get(2));
        String keySerializer = (String)context.evaluate(args.get(3));
        String valueSerializer = (String)context.evaluate(args.get(4));
        
        // check params and panic if unvalid
        // fill in properties
        Properties properties = new Properties();
        properties.put("bootstrap.servers", servers);
        
        // create kafka app
        AutoCloseable app;
        switch (appType) {
            case "producer":
                //properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                properties.put("key.serializer", keySerializer);
                //properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                properties.put("value.serializer", valueSerializer);
                app = new KafkaProducer<>(properties);
                break;
                
            case "consumer":
                //properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                properties.put("key.deserializer", keySerializer);
                //properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                properties.put("value.deserializer", valueSerializer);
                app = new KafkaConsumer<>(properties);
                break;
            
            default:
                throw new Exception("Bad app-type");
        }
        
        // save the app in the shared map
        //library.apps.put(appId, app);
        return library.putApp(app);
    }
    
}
