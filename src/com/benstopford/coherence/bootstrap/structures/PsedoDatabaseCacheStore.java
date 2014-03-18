package com.benstopford.coherence.bootstrap.structures;

import com.tangosol.net.cache.AbstractCacheStore;

import java.util.HashMap;
import java.util.Map;

/**
 * BTS, 07-Dec-2007
 */
public class PsedoDatabaseCacheStore extends AbstractCacheStore {
    public static final Map<Object, Integer> keysCalled = new HashMap<Object, Integer>();


    public PsedoDatabaseCacheStore() {
        System.out.println("constructing PsedoDatabaseCacheStore");
    }

    public void store(Object key, Object val) {
        System.out.printf("Trying to write %s to database\n", key);

        recordCall(key);

        throw new RuntimeException("something went wrong writing to database");
    }

    private void recordCall(Object key) {
        int lastCall = keysCalled.get(key)==null?0:keysCalled.get(key);
        keysCalled.put(key,++lastCall);
    }


    @Override
    public void storeAll(Map mapEntries) {
        for(Object o: mapEntries.entrySet()){
            Map.Entry entry = (Map.Entry) o;
            store(entry.getKey(), entry.getValue());
        }
    }

    public Object load(Object object) {
        return null;
    }
}
