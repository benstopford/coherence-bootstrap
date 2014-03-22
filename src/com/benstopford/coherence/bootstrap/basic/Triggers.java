package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.fail;

public class Triggers extends ClusterRunner {

    @Test
    public void shouldPut() throws InterruptedException {

        String cacheName = "break-me";
        NamedCache cache = getCache("config/basic-invocation-service-pof-1.xml", cacheName);

        try {
            cache.put("key", "value");
            fail("trigger was supposed to deny update no break-me cache");
        } catch (Exception expected) {
            System.out.println("Got what we were looking for " + expected.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setDefaultProperties();
        startOutOfProcess("config/basic-invocation-service-pof-1.xml");
        startOutOfProcess("config/basic-invocation-service-pof-1.xml");
        System.out.println(CacheFactory.ensureCluster().getMemberSet());
    }

    @Test
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
