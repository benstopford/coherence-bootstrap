package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.dataobjects.SimplePofObject;
import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
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

public class Filters extends CoherenceClusteredTest {

    private DefaultConfigurableCacheFactory factory;
    private NamedCache cache;

    @Test
    public void shouldFilterResults() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache.xml");
        cache = factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, "Value" + i);
        }

        Filter filter = new LikeFilter(new KeyExtractor("toString"), "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    @Test
    public void shouldFilterResultsUsingPofExtractor() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        cache = this.factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            cache.put("Key" + i, new SimplePofObject("Value" + i, false));
        }

        ValueExtractor pofExtractor = new PofExtractor(null, new SimplePofPath(1));
        Filter filter = new LikeFilter(pofExtractor, "%1%", '/', true);

        Set set = cache.entrySet(filter);
        assertEquals(20, set.size());
    }

    @Test
    public void shouldFilterResultsUsingTwoLevelPofExtractor() {
        factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        cache = factory.ensureCache("stuff", getClass().getClassLoader());

        for (int i = 1; i <= 100; i++) {
            SimplePofObject child = new SimplePofObject("Value" + i, false);
            SimplePofObject parent = new SimplePofObject(child, false);
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
        cache.clear();
        cache.getCacheService().shutdown();
        super.tearDown();
    }

}
