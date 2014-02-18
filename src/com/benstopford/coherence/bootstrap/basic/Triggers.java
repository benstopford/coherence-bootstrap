package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class Triggers extends CoherenceClusteredTest {
    
    public void testShouldPut() throws InterruptedException {

        String cacheName = "break-me";
        NamedCache cache = getCache("config/basic-invocation-service-pof-1.xml", cacheName);

        try {
            cache.put("key", "value");
            fail("trigger was supposed to deny update no break-me cache");
        } catch (Exception expected) {
            System.out.println("Got what we were looking for "+expected.getMessage());
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        setDefaultProperties();
        startOutOfProcess("config/basic-invocation-service-pof-1.xml");
        startOutOfProcess("config/basic-invocation-service-pof-1.xml");
        System.out.println(CacheFactory.ensureCluster().getMemberSet());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
