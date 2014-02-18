package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.filter.LikeFilter;
import functional.fixtures.SizableObjectFactory;

import java.io.IOException;

/**
 * BTS, 16-Jan-2008
 */
public class CQCs extends TestBase {

    public void testCqcShouldLoadSubsetToLocalProcess() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-extend-enabled-cache-32001.xml");

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

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
