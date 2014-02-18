package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;

/**
 * BTS, 07-Dec-2007
 */
public class CacheStoreAsync extends CoherenceClusteredTest {

    public void testDemoOfCacheStoreRetryCapabilityWithAsyncCacheStore() throws InterruptedException {
        NamedCache cache = getCache("config/async-cachestore.xml", "foo");

        cache.put("Key1", "Value");
        cache.put("Key2", "Value");
        cache.put("Key3", "Value");
        cache.put("Key4", "Value");
        cache.put("Key5", "Value");
        cache.put("Key6", "Value");

        //you should see attempts to retry based on PsedoDatabaseCacheStore.java
        Thread.sleep(5000);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
