package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.NamedCache;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 07-Dec-2007
 */
public class PutAndGet extends ClusterRunner {

    @Test
    public void putAndGetFromCache() {
        NamedCache cache = getBasicCache("foo");

        cache.put("Key", "Value");

        assertEquals("Value", cache.get("Key"));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
