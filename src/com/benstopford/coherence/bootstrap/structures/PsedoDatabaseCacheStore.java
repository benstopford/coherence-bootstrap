package com.benstopford.coherence.bootstrap.structures;

import com.tangosol.net.cache.AbstractCacheStore;

/**
 * BTS, 07-Dec-2007
 */
public class PsedoDatabaseCacheStore extends AbstractCacheStore {

    public PsedoDatabaseCacheStore() {
        System.out.println("constructing PsedoDatabaseCacheStore");
    }

    public void store(Object key, Object val) {
        System.out.printf("Trying to write %s to database\n", key);
        throw new RuntimeException("something went wrong writing to database");
    }

    public Object load(Object object) {
        return null;
    }
}
