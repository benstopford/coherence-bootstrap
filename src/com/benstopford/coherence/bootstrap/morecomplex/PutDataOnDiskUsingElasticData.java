package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static com.benstopford.coherence.bootstrap.structures.util.HeapUtils.memoryUsedNow;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PutDataOnDiskUsingElasticData extends ClusterRunner {

    @Test
    public void shouldPutDataOnDiskWithoutEatingMuchRAM() throws IOException, InterruptedException {

        String flash = "config/basic-cache-elastic-data.xml";
        startCoherenceProcess(flash);
        NamedCache cache = getCache(flash, "Foo");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(2));

        long start = memoryUsedNow();

        long bytesToAdd = 512 * MB;
        int block = 32 * KB;
        write(cache, bytesToAdd, block);

        long memoryUsed = memoryUsedNow() - start;

        assertTrue(memoryUsed < 10 * MB);

        long contents = clusterContains(cache);
        assertTrue("Expected the cache to contain "+bytesToAdd+" but contained "+contents, contents==bytesToAdd);

        System.out.printf("Adding %,d KB to the cache resulted in a memory usage of %,d.\n", bytesToAdd / KB, memoryUsed / KB);
    }

    private void write(NamedCache cache, long bytesToAdd, int block) {
        for (int i = 0; i < bytesToAdd / block; i++) {
            cache.put(i, new byte[block]);
            if (i % 1000 == 0) System.out.println((double) i * block / bytesToAdd * 100 + "% complete");
        }
    }

    private long clusterContains(NamedCache cache) {
        Iterator iterator = cache.keySet().iterator();
        long total = 0;
        while(iterator.hasNext()){
            Object next = cache.get(iterator.next());
            byte[] data = (byte[]) next;
            total+=data.length;
        }
        return total;
    }
}
