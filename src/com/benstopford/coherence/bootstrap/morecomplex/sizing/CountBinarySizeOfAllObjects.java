package com.benstopford.coherence.bootstrap.morecomplex.sizing;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofObject;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.counters.BinaryCacheSizeCounter;
import com.benstopford.coherence.bootstrap.structures.uitl.HeapUtils;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CountBinarySizeOfAllObjects extends ClusterRunner {

    @Test
    public void shouldCountSizeOfBinaryCachesAccuratelyInProcess() throws Exception {
        int jmxPort = 10001;

        //Given two caches, clustered in this jvm
        startLocalJMXServer(jmxPort);
        NamedCache foo = getCache("foo");
        NamedCache bar = getCache("bar");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        HeapUtils.start();

        //When we add 10MB to each cache
        addValuesToCache(foo, 1024, new PofObject(new byte[10 * 1024]));//10MB
        addValuesToCache(bar, 1024, new PofObject(new byte[10 * 1024]));//10MB

        long heapIncrease = HeapUtils.printMemoryUsed();

        //call the utility
        long measured = new BinaryCacheSizeCounter().sumClusterStorageSize(jmxPort);

        //Then the JMX Units Sizer utility should be within 5%. Should be within 10% of memory consumption
        assertWithinTolerance(20 * MB, measured, 0.05);
        assertWithinTolerance(heapIncrease, measured, 0.10);
    }

    @Test
    public void shouldCountSizeOfBinaryCachesAccuratelyAcrossMultipleProcesses() throws Exception {
        int port = 40001;

        //Given a cluster in three JVMs
        startBasicCacheProcessWithJMX(port);
        startBasicCacheProcess();
        startDataDisabledExtendProxy();

        //with two caches
        NamedCache foo = getRemoteCache("foo");
        NamedCache bar = getRemoteCache("bar");

        //When we add 20MB of data
        addValuesToCache(foo, 10 * 1024, new PoJo(new byte[1024])); //10MB
        addValuesToCache(bar, 10 * 1024, new PoJo(new byte[1024])); //10MB

        //call the utility
        long size = new BinaryCacheSizeCounter().sumClusterStorageSize(port);

        //Then our sizing utility should get the size within 10%
        double fudgeFactorForObjectWrappers = 1.3;
        assertWithinTolerance((long)(20 * MB * fudgeFactorForObjectWrappers), size, 0.10);
    }

    private NamedCache getCache(String name) {
        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache-with-pof.xml", this.getClass().getClassLoader())
                .ensureCache(name, this.getClass().getClassLoader());
        return cache;
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, Object o) {
        Map all = new HashMap();
        for (int i = 0; i < numberToAdd; i++) {
            all.put("Key" + i, o);
        }
        cache.putAll(all);
    }
}
