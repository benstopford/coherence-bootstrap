package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.FakeDatabaseCacheStore;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * BTS, 07-Dec-2007
 */
public class CacheStoreAsync extends ClusterRunner {

    @Test
    public void demoOfCacheStoreRetryCapabilityWithAsyncCacheStore() throws InterruptedException {
        NamedCache cache = getCache("config/async-cachestore.xml", "foo");

        cache.put("Key1", "Value1");
        cache.put("Key2", "Value2");
        cache.put("Key3", "Value3");
        cache.put("Key4", "Value4");
        cache.put("Key5", "Value5");
        cache.put("Key6", "Value6");

        //CacheStore should not have fired yet as async (and has artificial delay - see FakeDatabaseCacheStore.java)
        assertThat(FakeDatabaseCacheStore.keysCalled.size(),is(0));

        System.out.println("Calls to add 6 items returned to client at " + new Date());
        Thread.sleep(4 * 1000);

        //CacheStore should have completed all async calls
        assertThat(FakeDatabaseCacheStore.keysCalled.size(), is(6));
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
