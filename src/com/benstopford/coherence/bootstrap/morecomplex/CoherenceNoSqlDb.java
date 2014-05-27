package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer;
import com.benstopford.coherence.bootstrap.structures.framework.TestUtils;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.benstopford.coherence.bootstrap.structures.uitl.HeapUtils.memoryUsedNow;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Work in progress
 */
public class CoherenceNoSqlDb extends TestUtils {


    private int bufferEntryCount;
    private long bytesToAdd;
    private int valueSize;
    private ClassLoader cl = getClass().getClassLoader();
    ;
    private NamedCache cache;

    @Ignore @Test
    public void timeIndividualWritesSmallObject() throws Exception {
        String flash = "config/basic-cache-persistent.xml";

        cache = CacheFactory
                .getCacheFactoryBuilder()
                .getConfigurableCacheFactory(flash, cl)
                .ensureCache("stuff", cl);

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        initialiseCohernece();
        System.out.println("Running...");


        for (int runs = 0; runs < 10; runs++) {
            PerformanceTimer.start();
            for (int i = 0; i < 10; i++) {
                cache.put(i, new byte[1024]);
                PerformanceTimer.checkpoint();
            }
            PerformanceTimer.end().printAverageOfCheckpoints();
            System.out.println("----");
        }
    }

    @Ignore @Test
    public void timeBatchWriteSmallObject() throws Exception {
        String flash = "config/basic-cache-persistent.xml";

        cache = CacheFactory
                .getCacheFactoryBuilder()
                .getConfigurableCacheFactory(flash, cl)
                .ensureCache("stuff", cl);

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

    @Ignore @Test
    public void timeBatchWriteLargeObject() throws Exception {
        String flash = "config/basic-cache-persistent.xml";

        cache = CacheFactory
                .getCacheFactoryBuilder()
                .getConfigurableCacheFactory(flash, cl)
                .ensureCache("stuff", cl);

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        initialiseCohernece();
        System.out.println("Running...");


        Map data = new HashMap();
        for (int i = 0; i < 10; i++) {
            data.put(i, new byte[1024*1024]);
        }

        PerformanceTimer.start();
        for (int runs = 0; runs < 10; runs++) {
            cache.putAll(data);
            PerformanceTimer.checkpoint();
        }
        PerformanceTimer.end().printAverageOfCheckpoints();
    }

    private void initialiseCohernece() throws InterruptedException {
        //initialise
        cache.put(0, new byte[1024]);
        Thread.sleep(1000);
    }

    @After
    public void after() {
        cache.clear();
    }


    @Test
    public void shouldReadAndWriteDataUsingMinimalMemory() throws Exception {
        String flash = "config/basic-cache-persistent.xml";
//        startCoherenceProcess(flash);

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(flash, getClass().getClassLoader())
                .ensureCache("stuff", getClass().getClassLoader());

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        long start = memoryUsedNow();

        bytesToAdd = 1 * MB;
        valueSize = 8 * KB;
        bufferEntryCount = 1;

        Map buffer = new HashMap();
        PerformanceTimer.start();
        for (int i = 0; i < bytesToAdd / valueSize; i++) {
            buffer.put(i, new byte[valueSize]);
            if (buffer.size() > bufferEntryCount) {
                commitBuffer(cache, buffer);
            }
        }
        if (buffer.size() > 0) {
            commitBuffer(cache, buffer);
        }
        PerformanceTimer.end().printAverageOfCheckpoints();

        long memoryUsed = memoryUsedNow() - start;

//        assertTrue(memoryUsed < 10 * MB); //

        System.out.printf("Adding %,d KB to the cache resulted in a memory useage of %,d.\n", bytesToAdd / KB, memoryUsed / KB);

        //persistent so need to clear this guy
        cache.clear();
    }

    private void commitBuffer(NamedCache cache, Map buffer) {

        cache.putAll(buffer);
        PerformanceTimer.checkpoint();
        buffer.clear();
    }

}
