package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.cache.AbstractCacheStore;

/**
 * BTS, 07-Dec-2007
 */
public class TestCacheStore extends AbstractCacheStore {
    public static boolean WAS_CALLED;

    public void store(Object key, Object val) {
        System.out.println("store called for key: " + key + " value: " + val);
        WAS_CALLED = true;
    }

    public Object load(Object object) {
        return null;
    }
}
