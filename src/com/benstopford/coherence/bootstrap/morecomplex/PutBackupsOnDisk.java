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

public class PutBackupsOnDisk extends ClusterRunner {

    public static final int KB = 1024;
    public static final int MB = KB * KB;

    @Test
    public void shouldPutBackDataOnDisk() throws IOException, InterruptedException {

        String flash = "config/basic-cache-disk-backup.xml";
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

        assertTrue(memoryUsed < 10 * MB);

        System.out.println("Memory used writing " + memoryUsed);
    }

}
