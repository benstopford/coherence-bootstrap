package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PofObject;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.net.*;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.LikeFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class Filters extends ClusterRunner {


    @Test
    public void shouldFilterResults() {

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache.xml", classLoader)
                .ensureCache("stuff", classLoader);

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, "Value" + i);
        }

        Filter filter = new LikeFilter(new KeyExtractor("toString"), "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    @Test
    public void shouldFilterResultsUsingPofExtractor() {

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache-with-pof.xml", classLoader)
                .ensureCache("stuff", classLoader);

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, new PofObject("Value" + i));
        }

        ValueExtractor pofExtractor = new PofExtractor(null, new SimplePofPath(1));
        Filter filter = new LikeFilter(pofExtractor, "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    @Test
    public void shouldFilterResultsUsingTwoLevelPofExtractor() {

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache-with-pof.xml", classLoader)
                .ensureCache("stuff", classLoader);

        for (int i = 1; i <= 100; i++) {
            PofObject child = new PofObject("Value" + i);
            PofObject parent = new PofObject(child);
            cache.put("Key" + i, parent);
        }

        ValueExtractor pofExtractor = new PofExtractor(null, new SimplePofPath(new int[]{1, 1}));
        Filter filter = new LikeFilter(pofExtractor, "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
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
