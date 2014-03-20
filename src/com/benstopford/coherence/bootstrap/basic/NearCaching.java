package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.net.NamedCache;
import functional.fixtures.SizableObjectFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * BTS, 25-Jan-2008
 */
public class NearCaching extends CoherenceClusteredTest {

    @Test
    public void shouldBeAbleToNearCacheDataForInProcessReRetrieval() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-extend-enabled-cache-32001.xml");
        Thread.sleep(5000);

        NamedCache cacheWithNoNearScheme = connectOverExtend();

        cacheWithNoNearScheme.put("x", new SizableObjectFactory().buildObject(1000));

        System.out.println("Get times without near cache:");
        getFromCacheAndPrintTimings(cacheWithNoNearScheme);
        getFromCacheAndPrintTimings(cacheWithNoNearScheme);
        getFromCacheAndPrintTimings(cacheWithNoNearScheme);
        getFromCacheAndPrintTimings(cacheWithNoNearScheme);

        NamedCache cachWithNearScheme = getCache("config/extend-client-with-near-cache.xml", "foo");

        System.out.println("Get times with near cache:");
        getFromCacheAndPrintTimings(cachWithNearScheme);
        getFromCacheAndPrintTimings(cachWithNearScheme);
        getFromCacheAndPrintTimings(cachWithNearScheme);
        getFromCacheAndPrintTimings(cachWithNearScheme);
    }

    private static void getFromCacheAndPrintTimings(NamedCache cache) {
        long start;
        start = System.currentTimeMillis();
        cache.get("x");
        System.out.println("took " + (System.currentTimeMillis() - start) + " ms");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After public void tearDown() throws Exception {
        super.tearDown();
    }
}
