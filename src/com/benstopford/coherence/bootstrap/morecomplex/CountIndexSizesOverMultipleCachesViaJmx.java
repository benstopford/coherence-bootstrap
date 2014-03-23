package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.IndexSizer;
import com.benstopford.coherence.bootstrap.structures.dataobjects.LoggingPofObject;
import com.benstopford.coherence.bootstrap.structures.dataobjects.ObjFactory;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CountIndexSizesOverMultipleCachesViaJmx extends ClusterRunner {

    @Test
    public void shouldGetIndexSizeRightOverTwoDifferentCaches() throws Exception {

        //create cluster with a JMX port open
        startBasicCacheProcessWithJMX(40001);
        startBasicCacheProcess();
        startDataDisabledExtendProxy();

        //two caches
        NamedCache foo = getRemoteCache("foo");
        NamedCache bar = getRemoteCache("bar");

        //add some data
        addValuesToCache(foo, 10*1000, new PoJo());
        addValuesToCache(bar, 10*1000, new PoJo());

        //add regular reflection indexes
        foo.addIndex(new ReflectionExtractor("getValue"), false, null);
        bar.addIndex(new ReflectionExtractor("getValue"), false, null);

        //count the total index size
        long size = new IndexSizer().sizeAllIndexes(40001);

        assertThat(size, is(319693L));
    }

    @Test
    public void shouldGetIndexSizeRightOverTwoDifferentPofCaches() throws Exception {

        //create a pof enabled cluster with a JMX port open
        startBasicCacheProcessWithJMX("config/basic-cache-with-pof.xml", 40001);
        startCoherenceProcess("config/basic-cache-with-pof.xml");
        startCoherenceProcess("config/basic-extend-enabled-cache-32001-pof.xml");

        //two caches
        NamedCache foo = getRemotePofCache("foo");
        NamedCache bar = getRemotePofCache("bar");

        //add some data
        addValuesToCache(foo, 10*1000, new LoggingPofObject(null));
        addValuesToCache(bar, 10*1000, new LoggingPofObject(null));

        //add regular reflection indexes
        foo.addIndex(new PofExtractor(null, 1), false, null);
        bar.addIndex(new PofExtractor(null, 1), false, null);

        //count the total index size
        long size = new IndexSizer().sizeAllIndexes(40001);

        //total of 10k x 1KB fields is 10,240,000B but the real index size is 20,782,776 - about double
        assertThat(size, is(20782776L));
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, ObjFactory factory) {
        Map all = new HashMap();
        for (int i = 0; i < numberToAdd; i++) {
            all.put("Key" + i, factory.createNext());
        }
        cache.putAll(all);
    }

}
