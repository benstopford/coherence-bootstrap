package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.cache.AbstractCacheStore;

/**
 * BTS, 07-Dec-2007
 */
public class FailingCacheStore extends AbstractCacheStore {

    public FailingCacheStore() {
        System.out.println("constructing FailingCacheStore");
    }

    public void store(Object key, Object val) {
        System.out.println("FailingCacheStore: Attempting store of key: " + key + " and value: " + val);
        throw new RuntimeException("Oops!!");
    }

    public Object load(Object object) {
        return null;
    }
}
