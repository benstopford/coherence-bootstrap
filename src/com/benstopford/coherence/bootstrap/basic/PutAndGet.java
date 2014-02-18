package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;

/**
 * BTS, 07-Dec-2007
 */
public class PutAndGet extends CoherenceClusteredTest {

    public void testPutAndGetFromCache() {
        NamedCache cache = getBasicCache("foo");

        cache.put("Key", "Value");

        assertEquals("Value", cache.get("Key"));
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
