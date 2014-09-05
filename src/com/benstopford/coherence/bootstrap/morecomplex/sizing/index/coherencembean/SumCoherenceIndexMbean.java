package com.benstopford.coherence.bootstrap.morecomplex.sizing.index.coherencembean;

import com.benstopford.coherence.bootstrap.structures.tools.jmx.IndexInfoCounter;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofObject;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Warning - this mechanism for measuring index sizes is not accurate due to the
 * underlying footprint mbean being a guestimate of the index size.
 */
public class SumCoherenceIndexMbean extends ClusterRunner {

    private static byte[] data1K = new byte[1024];

    @Test
    public void shouldMeasureIndexSizeOverTwoJavaSerialisedCaches() throws Exception {
        int port = 40001;

        //create cluster with a JMX port open
        startBasicCacheProcessWithJMX(port);
        startBasicCacheProcess();
        startDataDisabledExtendProxy();

        //two caches
        NamedCache foo = getRemoteCache("foo");
        NamedCache bar = getRemoteCache("bar");

        //add some data
        addValuesToCache(foo, 10 * 1024, new PoJo(data1K)); //10MB
        addValuesToCache(bar, 10 * 1024, new PoJo(data1K)); //10MB

        //add regular reflection indexes
        foo.addIndex(new ReflectionExtractor("getData"), false, null);
        bar.addIndex(new ReflectionExtractor("getData"), false, null);

        //count the total index size
        long size = new IndexInfoCounter().sumIndexInfoFootprintMbean(port);

        assertWithinTolerance(size, 21286093L, 0.01);//21,286,093 for input of 20MB
    }

    @Test
    public void shouldMeasureIndexSizeOverTwoPofCaches() throws Exception {

        //create a pof enabled cluster with a JMX port open
        startBasicCacheProcessWithJMX("config/basic-cache-with-pof.xml", 40001);
        startCoherenceProcess("config/basic-cache-with-pof.xml");
        startCoherenceProcess("config/basic-extend-enabled-cache-32001-pof.xml");

        //two caches
        NamedCache foo = getRemotePofCache("foo");
        NamedCache bar = getRemotePofCache("bar");

        //add some data
        addValuesToCache(foo, 10 * 1024, new PofObject(data1K));//10MB
        addValuesToCache(bar, 10 * 1024, new PofObject(data1K));//10MB

        foo.addIndex(new PofExtractor(null, 1), false, null);
        bar.addIndex(new PofExtractor(null, 1), false, null);

        //count the total index size
        long size = new IndexInfoCounter().sumIndexInfoFootprintMbean(40001);

        assertWithinTolerance(size, 21181235L, 0.01);//20MB of data creates index of 21,181,235B
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, Object o) {
        Map all = new HashMap();
        for (int i = 0; i < numberToAdd; i++) {
            all.put("Key" + i, o);
        }
        cache.putAll(all);
    }

}
