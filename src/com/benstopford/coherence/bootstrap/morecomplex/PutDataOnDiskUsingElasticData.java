package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import java.io.IOException;

import static com.benstopford.coherence.bootstrap.structures.uitl.HeapUtils.memoryUsedNow;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PutDataOnDiskUsingElasticData extends ClusterRunner {

    @Test
    public void shouldPutDataOnDisk() throws IOException, InterruptedException {

        String flash = "config/basic-cache-elastic-data.xml";
        startCoherenceProcess(flash);
        NamedCache cache = getCache(flash, "Foo");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(2));

        long start = memoryUsedNow();

        long bytesToAdd = 512 * MB;
        long block = 32 * KB;
        for (int i = 0; i < bytesToAdd / (block); i++) {
            cache.put(i, new byte[(byte)block]);
        }

        long memoryUsed = memoryUsedNow() - start;

        assertTrue(memoryUsed < 10 * MB); //

        System.out.printf("Adding %,d KB to the cache resulted in a memory useage of %,d.\n", bytesToAdd/KB, memoryUsed/KB);
    }

}
