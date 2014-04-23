package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.tools.PutAllWithErrorReporting;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PutAllThatReportsIndividualExceptions extends ClusterRunner {

    @Test
    public void shouldPut() throws InterruptedException {
        startCoherenceProcess("config/basic-invocation-service-pof-1.xml");
        startCoherenceProcess("config/basic-invocation-service-pof-1.xml");

        String cacheName = "regular-cache";
        String configPath = "config/basic-invocation-service-pof-1.xml";

        NamedCache cache = getCache(configPath, cacheName);

        InvocationService service = (InvocationService) CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(configPath, classLoader)
                .ensureService("MyInvocationService1");

        TreeMap hashMap = new TreeMap();
        hashMap.put(1, 2);
        hashMap.put(2, 3);
        hashMap.put(3, 4);
        hashMap.put(4, 5);

        PutAllWithErrorReporting invoker = new PutAllWithErrorReporting(
                service,
                (DistributedCacheService) cache.getCacheService(),
                cache.getCacheName(),
                configPath
        );
        invoker.putAll(hashMap);

        Thread.sleep(1000);
        assertEquals(4, cache.size());
        assertEquals(2, cache.get(1));
        assertEquals(3, cache.get(2));
        assertEquals(4, cache.get(3));
        assertEquals(5, cache.get(4));
    }

    @Test
    public void shouldReportFailures() {
        startCoherenceProcess("config/basic-invocation-service-pof-1.xml");
        startCoherenceProcess("config/basic-invocation-service-pof-1.xml");

        String cacheName = "break-me";
        String configPath = "config/basic-invocation-service-pof-1.xml";
        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(configPath, classLoader)
                .ensureCache(cacheName, classLoader);

        InvocationService service = (InvocationService) new DefaultConfigurableCacheFactory(configPath).ensureService("MyInvocationService1");
        Map entries = new TreeMap();
        entries.put(1, 2);
        entries.put(2, 3);
        entries.put(3, 4);
        entries.put(4, 5);

        PutAllWithErrorReporting invoker = new PutAllWithErrorReporting(
                service,
                (DistributedCacheService) cache.getCacheService(),
                cache.getCacheName(),
                configPath
        );

        Map<Object, Throwable> keyToThrowableMap = invoker.putAll(entries);

        System.out.println("test ran and return-result size is " + keyToThrowableMap.size());

        assertEquals(4, keyToThrowableMap.size());
        assertTrue(keyToThrowableMap.get(1).getMessage().contains("Forced Error"));
        assertTrue(keyToThrowableMap.get(2).getMessage().contains("Forced Error"));
        assertTrue(keyToThrowableMap.get(3).getMessage().contains("Forced Error"));
        assertTrue(keyToThrowableMap.get(4).getMessage().contains("Forced Error"));
    }


    @Before public void setUp() throws Exception {
        super.setUp();
    }

    @After public void tearDown() throws Exception {
        super.tearDown();
    }
}
