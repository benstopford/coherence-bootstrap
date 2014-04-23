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

        startLocalJMXServer(jmxPort);

        //two caches
        NamedCache foo = getCache("foo");
        NamedCache bar = getCache("bar");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(1));

        HeapUtils.start();

        //add one array per cache
        addValuesToCache(foo, 1024, new PofObject(new byte[10 * 1024]));//10MB
        addValuesToCache(bar, 1024, new PofObject(new byte[10 * 1024]));//10MB

        long used = HeapUtils.printMemoryUsed();

        //count the total cache size
        long size = new BinaryCacheSizeCounter().sumClusterStorageSize(jmxPort);

        assertWithinTolerance(20 * MB, size, 0.05);
        assertWithinTolerance(used, size, 0.10);
    }

    @Test
    public void shouldCountSizeOfBinaryCachesAccuratelyAcrossMultipleProcesses() throws Exception {
        int port = 40001;

        //create cluster with a JMX port open
        startBasicCacheProcessWithJMX(port);
        startBasicCacheProcess();
        startDataDisabledExtendProxy();

        //two caches
        NamedCache foo = getRemoteCache("foo");
        NamedCache bar = getRemoteCache("bar");

        //add some data
        addValuesToCache(foo, 10 * 1024, new PoJo(new byte[1024])); //10MB
        addValuesToCache(bar, 10 * 1024, new PoJo(new byte[1024])); //10MB

        //count the total index size
        long size = new BinaryCacheSizeCounter().sumClusterStorageSize(port);

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
