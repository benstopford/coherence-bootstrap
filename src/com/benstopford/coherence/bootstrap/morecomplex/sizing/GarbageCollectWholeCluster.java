package com.benstopford.coherence.bootstrap.morecomplex.sizing;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.jmx.ClusterGC;
import com.benstopford.coherence.bootstrap.structures.util.GcInformation;
import com.sun.management.GarbageCollectorMXBean;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GarbageCollectWholeCluster extends ClusterRunner {

    @Test
    public void shouldCollectLocalJvmViaJmx() throws Exception {
        startLocalJMXServer(10001);

        long initialGcCount = GcInformation.getOldGenBean().getCollectionCount();

        long gcCount = 10;
        for (int i = 0; i < gcCount; i++) {
            new ClusterGC().run(new String[]{"localhost:10001"});
        }

        assertThat(GcInformation.getOldGenBean().getCollectionCount() - initialGcCount, is(gcCount));
    }

    @Test
    public void shouldCollectMultipleRemoteJvmsViaJmx() throws Exception {
        startBasicCacheProcessWithJMX(10002);
        startBasicCacheProcessWithJMX(10003);
        startDataDisabledExtendProxy();
        System.gc();


        long intial10002 = getOldGenCollectionCount("10002");
        long intial10003 = getOldGenCollectionCount("10003");

        new ClusterGC().run(new String[]{"localhost:10002"});
        assertThat(getOldGenCollectionCount("10002"), is(intial10002 + 1));
        assertThat(getOldGenCollectionCount("10003"), is(intial10003));

        new ClusterGC().run(new String[]{"localhost:10003"});
        assertThat(getOldGenCollectionCount("10002"), is(intial10002 + 1));
        assertThat(getOldGenCollectionCount("10003"), is(intial10003 + 1));

        new ClusterGC().run(new String[]{"localhost:10002","localhost:10003"});
        assertThat(getOldGenCollectionCount("10002"), is(intial10002 + 2));
        assertThat(getOldGenCollectionCount("10003"), is(intial10003 + 2));
    }



    private long getOldGenCollectionCount(String port) throws Exception {
        long collectionCount = -1;
        List<GarbageCollectorMXBean> beans = new ClusterGC().getGCMbean("localhost", port);
        for (GarbageCollectorMXBean bean : beans) {
            if (isOldGen(bean)) {
                collectionCount = bean.getCollectionCount();
            }
        }
        return collectionCount;
    }

    private boolean isOldGen(GarbageCollectorMXBean bean) {
        return Arrays.asList(bean.getMemoryPoolNames()).contains("PS Old Gen");
    }


}
