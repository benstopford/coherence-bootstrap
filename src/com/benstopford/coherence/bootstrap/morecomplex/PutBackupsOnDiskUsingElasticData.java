package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import static com.benstopford.coherence.bootstrap.structures.util.HeapUtils.memoryUsedNow;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Demonstrates putting backups on disk using elastic data.
 * Note that the config file requires the flash-scheme to be specified
 * but ALSO the operational overides file (tangosol-coherence-override.xml)
 * must be changed to include the journaling-config data which includes
 * the file to be journaled to.
 */
public class PutBackupsOnDiskUsingElasticData extends ClusterRunner {

    @Test
    public void puttingBackupsOnDiskShouldHalveTheMemoryUsed() throws Exception {
        long bytesToAdd = 50 * MB;
        int block = 32 * KB;

        //First benchmark size with backups in memory
        NamedCache cache = startThisAndOneOtherNode("config/basic-cache.xml");

        long start = memoryUsedNow();
        for (int i = 0; i < bytesToAdd / block; i++) {
            cache.put(i, new byte[block]);
        }
        long memoryBaselineUsingRam = memoryUsedNow() - start;

        super.tearDown();
        super.setUp();

        //Next try with backups in elastic data
        cache = startThisAndOneOtherNode("config/basic-cache-backup-elastic-data.xml");

        start = memoryUsedNow();
        for (int i = 0; i < bytesToAdd / block; i++) {
            cache.put(i, new byte[block]);
        }
        long memoryUsingDisk = memoryUsedNow() - start;
        System.out.printf("With backups in RAM we used %,d MB. With backups on disk we used %,dMB.\n", memoryBaselineUsingRam / MB, memoryUsingDisk / MB);

        //When backing up to disk should use about half the memory in this process
        assertWithinTolerance(memoryBaselineUsingRam / 2, memoryUsingDisk, 0.2);
    }

    private NamedCache startThisAndOneOtherNode(String config) {
        startCoherenceProcess(config);
        NamedCache cache = getCache(config, "Foo");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(2));
        return cache;
    }
}
