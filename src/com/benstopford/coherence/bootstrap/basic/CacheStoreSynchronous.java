package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.TestCacheStore;
import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * BTS, 07-Dec-2007
 */
public class CacheStoreSynchronous extends CoherenceClusteredTest {

    @Test
    public void cacheStore() {

        NamedCache cache = getCache("config/synch-cachestore.xml", "foo");

        cache.put("Key1", "Value");

        assertTrue(TestCacheStore.WAS_CALLED);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
