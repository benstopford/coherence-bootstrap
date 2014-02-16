package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.benstopford.coherence.bootstrap.structures.TestCacheStore;
import com.tangosol.net.NamedCache;

/**
 * BTS, 07-Dec-2007
 */
public class CacheStoreSynchronous extends TestBase {
    public void testCacheStore() {

        NamedCache cache = getCache("config/synchronous-cachestore.xml","foo");

        cache.put("Key1", "Value");

        assertTrue(TestCacheStore.WAS_CALLED);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
