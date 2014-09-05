package com.benstopford.coherence.bootstrap.performance;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ProcessExecutor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.end;
import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.start;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Work in progress
 */
public class ElasticDataWithRecoverableCachingPerf extends ClusterRunner {


    private int originalMem;

    /**
     * Mac with 8GB ram
     * Putting 16GB into cache (2 processes, 250m each, 10kb values, putall(1000)
     * writing the data (elastic): 361,223ms
     * reading 5000 entries taken at random: 5,390ms
     *
     * @throws java.io.IOException
     * @throws InterruptedException
     */

    @Ignore
    @Test
    public void readAndWriteVeryLargeDataset() throws IOException, InterruptedException {
        clearDataDirectories();

        originalMem = ProcessExecutor.COHERERENCE_PROCESS_MEMORY;
        ProcessExecutor.COHERERENCE_PROCESS_MEMORY = 350;

        String flash = "config/basic-cache-persistent-and-elastic.xml";
        startCoherenceProcess(flash);
        startCoherenceProcess(flash);
        startDataDisabledExtendProxy();

        NamedCache cache = getRemoteCache("foo");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));

        long bytesToAdd = 4 * 1024 * MB;
        int block = 10 * KB;

        start();
        writeBatch(cache, bytesToAdd, block, 1000);
        end().printMs(String.format("Write [%,d]:", bytesToAdd));

        start();
        read(cache, 5000L, cache.size());
        end().printMs(String.format("Read 1024 from [%,d][%,d]:", bytesToAdd, cache.size()));

        long contents = clusterContains(cache);
        assertTrue("Expected the cache to contain " + bytesToAdd * 4 + " but contained " + contents, contents == bytesToAdd);
    }

//    @Test
//    public void deleteMe() throws IOException, InterruptedException {
//
//        original = ProcessExecutor.COHERERENCE_PROCESS_MEMORY;
//        ProcessExecutor.COHERERENCE_PROCESS_MEMORY = 1024;
//
//        String flash = "config/basic-cache-persistent-and-elastic.xml";
//        startCoherenceProcess(flash);
//        startCoherenceProcess(flash);
//        startDataDisabledExtendProxy();
//        System.out.println("*****************************started at ************************");
//        System.out.println(new Date());
//        Thread.sleep(5000);
//
////        NamedCache cache = getRemoteCache("foo");
////        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));
//
//        while (true) {
//            try {
//                NamedCache cache = getRemoteCache("foo");
//                System.out.println(cache.size());
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                System.out.println(e.getMessage());
//            }
//        }
//
//    }

    @After
    public void tearDown() {
        if (originalMem > 0)
            ProcessExecutor.COHERERENCE_PROCESS_MEMORY = originalMem;
    }


    private void read(NamedCache cache, long count, long numberSpace) {

        List<Integer> keys = new ArrayList<Integer>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int key = (int) Math.round(random.nextDouble() * numberSpace);
            keys.add(key);
        }

        if (count > cache.size()) throw new RuntimeException("oops");
        for (Object k : keys) {
            Object o = cache.get(k);
            if (o == null) throw new RuntimeException("oops");
            if (count-- == 0) break;
        }
    }


    private void addAnotherNode(String flash, NamedCache cache) throws InterruptedException {
        clear(cache);

        startCoherenceProcess(flash);

        System.out.println("process started");
        while (CacheFactory.getCluster().getMemberSet().size() < 3) {
            System.out.println("waiting for member to join");
            Thread.sleep(100);
        }
        System.out.println("member joined, pausing");
        Thread.sleep(1000);
        System.out.println("pause complete");
    }

    private void clear(NamedCache cache) {
        System.out.println("clearing cache");
        Set keys = cache.keySet();//clear seems to bring whole dataset to memory :(
        for (Object k : keys) {
            cache.remove(k);
        }
        System.out.println("cleared");
        System.out.println("starting coherence");
    }


    private void write(NamedCache cache, long bytesToAdd, int block) {
        write(cache, bytesToAdd, block, 0);
    }

    private void write(NamedCache cache, long bytesToAdd, int block, int from) {
        long keysToAdd = bytesToAdd / block;
        for (int i = from; i < from + keysToAdd; i++) {
            cache.put(i, new byte[block]);
            if (i % 1000 == 0) System.out.println((double) (i - from) * block / bytesToAdd * 100 + "% complete");
        }
    }

    private void writeBatch(NamedCache cache, long bytesToAdd, int block, int batchSize) {
        long keysToAdd = bytesToAdd / block;
        Map map = new HashMap();
        for (int i = 0; i < keysToAdd; i++) {
            map.put(i, new byte[block]);
            if (i % batchSize == 0) {
                cache.putAll(map);
                map.clear();
            }
            if (i % 1000 == 0) System.out.println((double) i * block / bytesToAdd * 100 + "% complete");
        }
        cache.putAll(map);
    }


    private long clusterContains(NamedCache cache) {
        Iterator iterator = cache.keySet().iterator();
        long total = 0;
        while (iterator.hasNext()) {
            Object next = cache.get(iterator.next());
            byte[] data = (byte[]) next;
            total += data.length;
        }
        return total;
    }

}
