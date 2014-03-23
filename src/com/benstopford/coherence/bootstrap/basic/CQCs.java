package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.filter.LikeFilter;
import functional.fixtures.SizableObjectFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 16-Jan-2008
 */
public class CQCs extends ClusterRunner {

    @Test
    public void cqcShouldLoadSubsetToLocalProcess() throws IOException, InterruptedException {
        startCoherenceProcess("config/basic-extend-enabled-cache-32001.xml");

        NamedCache backingCache = getCache("config/extend-client-32001.xml", "foo");

        addValuesToCache(backingCache, 10);

        Filter filter = new LikeFilter(new KeyExtractor("toString"), "%1%", '/', true);

        ContinuousQueryCache cqc = new ContinuousQueryCache(backingCache, filter);

        assertEquals(2, cqc.size());
        assertEquals(10, backingCache.size());
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd) {
        for (int i = 1; i <= numberToAdd; i++) {
            cache.put("Key" + i, new SizableObjectFactory().buildObject(1000));
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
