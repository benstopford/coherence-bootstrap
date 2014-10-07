package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.tools.MyCustomIndex;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.tools.index.IndexSizer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.LikeFilter;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Example of creating a custom index This example creates a low memory index
 * which only includes the backward map, discarding the forward lookup.
 */
public class CustomIndexExample {
    public static final String CONFIG = "config/basic-index-sizer.xml";

    @Test
    public void createLowMemoryIndex() {

        //Add some sample values to a cache: note we have 10k objects with 100 unique field values
        NamedCache cache = getCache();
        for (int i = 1; i <= 10000; i++) {
            cache.put("Key" + i, new PoJo("value" + (i%100)));
        }

        //Add a regular index
        final ReflectionExtractor extractor = new ReflectionExtractor("getData");
        cache.addIndex(extractor, false, null);

        //Check that the query evaluates to the expected number
        int size = cache.entrySet(new LikeFilter(extractor, "%1%", '/', true)).size();
        assertEquals(1900, size);

        //Measure the size of the index
        long regularIndexSize = new IndexSizer(CONFIG).getTotal();

        System.out.println("---");

        cache.removeIndex(extractor);

        //Create a custom index which uses a ConditionalIndex (for convenience) to
        //create the reverse lookup but not the forward one
        //thus taking a fraction the space (depending on cardinality of the index)
        MyCustomIndex customIndex = new MyCustomIndex(extractor);

        //add the custom index
        cache.addIndex(customIndex, false, null);

        //we still query with the same value extractor
        size = cache.entrySet(new LikeFilter(extractor, "%1%", '/', true)).size();
        assertEquals(1900, size);

        //check our custom index was called
        assertThat(customIndex.getIndexContentsCount, is(1));

        long customIndexSize = new IndexSizer().getTotal();

        //our index should be much smaller
        assertTrue(customIndexSize < regularIndexSize);
        System.out.printf("[%,d][%,d]->[%.2f%% smaller]\n", regularIndexSize, customIndexSize,(double) customIndexSize/regularIndexSize*100);
    }

    private NamedCache getCache() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        return CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(CONFIG, classloader)
                .ensureCache("stuff", classloader);
    }

}
