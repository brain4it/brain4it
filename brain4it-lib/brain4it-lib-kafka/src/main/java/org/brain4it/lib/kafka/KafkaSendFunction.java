/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brain4it.lib.kafka;

import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
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
     * <code>(kafka-send app-id topic message)</code>
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception 
     */
    @Override
    public Object invoke(Context context, BList args) throws Exception {
        Utils.checkArguments(args, 4);
        Object serversRaw = context.evaluate(args.get(1));
        String keySerializer = (String) context.evaluate(args.get(2));
        String valueSerializer = (String) context.evaluate(args.get(3));
    }

}
