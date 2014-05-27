package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer;
import com.benstopford.coherence.bootstrap.structures.framework.TestUtils;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Work in progress
 */
public class CoherenceNoSqlDb extends TestUtils {
    private ClassLoader cl = getClass().getClassLoader();
    private NamedCache cache;
    public static final String flash = "config/basic-cache-persistent.xml";

    @Test
    public void timeIndividualWritesSmallObject() throws Exception {
        cache = cache();

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        initialiseCohernece();
        System.out.println("Running...");

        PerformanceTimer.start();
        for (int runs = 0; runs < 100; runs++) {
            cache.put(runs, new byte[1024]);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints();
    }

    @Test
    public void timeBatchWriteSmallObject() throws Exception {
        cache = cache();

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        initialiseCohernece();
        System.out.println("Running...");


        Map data = new HashMap();
        for (int i = 0; i < 10; i++) {
            data.put(i, new byte[1024]);
        }

        PerformanceTimer.start();
        for (int runs = 0; runs < 10; runs++) {
            cache.putAll(data);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints();
    }

    @Test
    public void timeBatchWriteLargeObject() throws Exception {
        cache = cache();

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        initialiseCohernece();
        System.out.println("Running...");


        Map data = new HashMap();
        for (int i = 0; i < 10; i++) {
            data.put(i, new byte[1024 * 1024]);
        }

        PerformanceTimer.start();
        for (int runs = 0; runs < 10; runs++) {
            cache.putAll(data);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints();
    }

    private void initialiseCohernece() throws InterruptedException {
        cache.put(0, new byte[1024]);
        Thread.sleep(1000);
    }

    @After
    public void after() {
        cache.clear();
    }

    @Before
    public void before() {
        cache().clear();
    }

    private NamedCache cache() {
        return CacheFactory
                .getCacheFactoryBuilder()
                .getConfigurableCacheFactory(flash, cl)
                .ensureCache("stuff", cl);
    }
}
