package com.benstopford.coherence.bootstrap.morecomplex.recoverablecaching;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Work in progress
 */
@RunWith(Parameterized.class)
public class CoherenceNoSqlDb extends ClusterRunner {
    public static final TimeUnit unit = TimeUnit.MICROSECONDS;
    private NamedCache cache;
    public String config;

    @Parameterized.Parameters
    public static Collection configs() {
        return Arrays.asList(new Object[][]{
                {"config/basic-cache.xml", 1},
                {"config/basic-cache-persistent.xml", 2}
        });
    }

    public CoherenceNoSqlDb(String config, long ignored) {
        this.config = config;
    }

    static {
        try {
            clearBdbDirectories();
            System.out.println("BDB Directories cleared");
        } catch (IOException e) {
            System.out.println("Not all directories were cleared - " + e.getMessage());
        }
    }

    @Test
    public void timeIndividualWritesSmallObject() throws Exception {
        cache = startCluster(config);

        initialiseCohernece();
        System.out.println("Running...");

        PerformanceTimer.start();
        for (int runs = 0; runs < 1000; runs++) {
            cache.put(runs, new byte[1024]);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints(unit);
    }

//    @Test
    public void timeBatchWriteSmallObject() throws Exception {
        cache = startCluster(config);

        initialiseCohernece();
        System.out.println("Running...");


        Map data = new HashMap();
        for (int i = 0; i < 100; i++) {
            data.put(i, new byte[1024]);
        }

        PerformanceTimer.start();
        for (int runs = 0; runs < 10; runs++) {
            cache.putAll(data);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints(unit);
    }

//    @Test
    public void timeBatchWriteLargeObject() throws Exception {
        cache = startCluster(config);
        System.out.println(config);

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
        PerformanceTimer.end().printAverageOfCheckpoints(unit);
    }

    private void initialiseCohernece() throws InterruptedException {
        cache.put(0, new byte[1024]);
        Thread.sleep(1000);
    }

    @After
    public void after() {
        if (cache != null)
            cache.clear();
    }

    @Before
    public void before() throws IOException {
    }

    private NamedCache startCluster(String config) {
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        NamedCache cache = getCache(config, "stuff");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));
        cache.clear();
        return cache;
    }

    static void clearBdbDirectories() throws IOException {
        delete(new File("data/store-bdb-snapshot"));
        delete(new File("data/store-bdb-trash"));
        delete(new File("data/store-bdb-active"));
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
