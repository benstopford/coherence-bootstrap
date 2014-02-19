package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.dataobjects.SimplePofObject;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.LikeFilter;
import junit.framework.TestCase;

import java.util.Set;

public class Filters extends TestCase {

    private DefaultConfigurableCacheFactory factory;
    private NamedCache cache;

    public void testShouldFilterResults() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache.xml");
        cache = factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, "Value" + i);
        }

        Filter filter = new LikeFilter(new KeyExtractor("toString"), "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    public void testShouldFilterResultsUsingPofExtractor() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        cache = this.factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, new SimplePofObject("Value" + i, false));
        }

        ValueExtractor pofExtractor = new PofExtractor (new SimplePofPath(1));
        Filter filter = new LikeFilter(pofExtractor, "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    public void testShouldFilterResultsUsingTwoLevelPofExtractor() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        cache = factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            SimplePofObject child = new SimplePofObject("Value" + i, false);
            SimplePofObject parent = new SimplePofObject(child, false);
            cache.put("Key" + i, parent);
        }

        ValueExtractor pofExtractor = new PofExtractor (new SimplePofPath(new int[]{1,1}));
        Filter filter = new LikeFilter(pofExtractor, "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        cache.clear();
        cache.getCacheService().shutdown();
        super.tearDown();
    }

}
