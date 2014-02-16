package com.benstopford.coherence.bootstrap.basic;

import com.tangosol.net.NamedCache;
import com.benstopford.coherence.bootstrap.structures.framework.TestBase;

/**
 * BTS, 07-Dec-2007
 */
public class PutAndGet extends TestBase {

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
