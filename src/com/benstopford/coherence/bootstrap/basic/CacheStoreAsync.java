package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.PsedoDatabaseCacheStore;
import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;
import org.junit.Test;

/**
 * BTS, 07-Dec-2007
 */
public class CacheStoreAsync extends CoherenceClusteredTest {

    @Test
    public void testDemoOfCacheStoreRetryCapabilityWithAsyncCacheStore() throws InterruptedException {
        NamedCache cache = getCache("config/async-cachestore.xml", "foo");

        cache.put("Key1", "Value1");
        cache.put("Key2", "Value2");
        cache.put("Key3", "Value3");
        cache.put("Key4", "Value4");
        cache.put("Key5", "Value5");
        cache.put("Key6", "Value6");

        //you should see attempts to retry based on PsedoDatabaseCacheStore.java
        Thread.sleep(10 * 1000);

        System.out.println(PsedoDatabaseCacheStore.keysCalled);
        assertTrue(PsedoDatabaseCacheStore.keysCalled.size() > 0);
        assertTrue(PsedoDatabaseCacheStore.keysCalled.get("Key1") > 0);

    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
