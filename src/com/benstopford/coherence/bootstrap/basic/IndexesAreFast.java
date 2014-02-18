package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.benstopford.coherence.bootstrap.structures.ValueObject;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This test shows how to use indexes and how they are much faster.
 * BTS, 25-Jan-2008
 */
public final class IndexesAreFast extends CoherenceClusteredTest {
    private final List<Object> valuesSentToClient1 = new ArrayList<Object>();
    private final List<Object> valuesSentToClient2 = new ArrayList<Object>();

    public void testIsUsingAnEntrySetWithAnIndexFasterAndDoesItScaleBetterAsTheClusterSizeIncreases() throws IOException, InterruptedException {

        startOutOfProcess("config/basic-cache.xml", "", "");
        startOutOfProcess("config/basic-cache.xml", "", "");
        startOutOfProcess("config/basic-cache.xml", "", "");
        startOutOfProcess("config/basic-cache.xml", "", "");
        startOutOfProcess("config/basic-cache.xml", "", "");

        System.out.println("\n\n******* Timings without index *******");
        NamedCache cache = getBasicCache("foo");
        addValuesToCache(cache, 10);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 100);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 1000);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 10000);
        getFromCacheAndPrintTimings(cache, 100);

        cache.clear();
        cache.addIndex(new ReflectionExtractor("getValue"), false, null);

        System.out.println("\n\n******* Timings with index *******");
        addValuesToCache(cache, 10);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 100);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 1000);
        getFromCacheAndPrintTimings(cache, 100);
        addValuesToCache(cache, 10000);
        getFromCacheAndPrintTimings(cache, 100);

        System.out.println("\n\n");
    }

    private static void getFromCacheAndPrintTimings(NamedCache cache, int number) {
        int total = 0;

        long start = System.nanoTime();
        for (int i = 0; i < number; i++) {
            cache.entrySet(new EqualsFilter("getValue", i));
        }
        long took = System.nanoTime() - start;

        System.out.printf("Selecting from %,d objects took %,dus\n", cache.size(), took / 1000);
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd) {
        List value = new ArrayList();
        for (int i = 0; i < numberToAdd; i++) {
            ValueObject valueObject = new ValueObject(i);
            value.add(i);
            cache.put("Key" + i, valueObject);
        }
    }

    protected void setUp() throws Exception {
        System.setProperty("tangosol.coherence.distributed.localstorage", "false");
        valuesSentToClient1.clear();
        valuesSentToClient2.clear();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        System.clearProperty("tangosol.coherence.distributed.localstorage");
        super.tearDown();
    }
}
