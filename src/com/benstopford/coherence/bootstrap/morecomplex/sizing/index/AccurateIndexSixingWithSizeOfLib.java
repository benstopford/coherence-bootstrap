package com.benstopford.coherence.bootstrap.morecomplex.sizing.index;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.IndexCountingInvocable;
import com.benstopford.coherence.bootstrap.structures.tools.SizeOfIndexSizer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class AccurateIndexSixingWithSizeOfLib extends ClusterRunner {
    public static final String config = "config/basic-invocation-service-1.xml";
    public static final String invocationService = "MyInvocationService1";

    @Test
    public void shouldSizeSingleCache() {
        //start two remote nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);

        //Three data node coherence cluster for this test
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(3));

        NamedCache foo = getCache(config, "foo");

        //add some data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));

        //add indexes (which index the value in its entirety to keep the maths simple)
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        SizeOfIndexSizer sizer = new SizeOfIndexSizer();

        long size = sizer.calculateIndexSizesForSingleCache(invocationService, foo.getCacheName(), config);

        assertWithinTolerance(330 * 1000, size, 0.1);
    }

    @Test
    public void shouldWorkOverExtend() throws IOException, InterruptedException {

        //start two remote nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startDataDisabledExtendProxy();

        NamedCache foo = getRemoteCache("foo");

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));

        //add some data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));

        //add indexes (which index the value in its entirety to keep the maths simple)
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        SizeOfIndexSizer sizer = new SizeOfIndexSizer();

        long size = sizer.calculateIndexSizesForSingleCache(invocationService, foo.getCacheName(), config);
        System.out.println("total size is "+size);

        assertWithinTolerance(330 * 1000, size, 0.1);
    }

    @Test
    public void shouldSizeMultipleCaches() {
        //start two remote nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);

        //Three data node coherence cluster for this test
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(3));

        NamedCache foo = getCache(config, "foo");
        NamedCache bar = getCache(config, "bar");

        //add some data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));
        addValuesToCache(bar, 1000, new PoJo(Long.MAX_VALUE));

        //add indexes (which index the value in its entirety to keep the maths simple)
        foo.addIndex(new ReflectionExtractor("getData"), false, null);
        bar.addIndex(new ReflectionExtractor("getData"), false, null);

        SizeOfIndexSizer sizer = new SizeOfIndexSizer();

        long justFoo = sizer.calculateIndexSizesForSingleCache(invocationService, foo.getCacheName(), config);
        long total = sizer.calculateTotalIndexSize(invocationService, config);

        assertEquals(2 * justFoo, total);

        Map<String, Long> sizes = sizer.calculateIndexSizes(invocationService, config);

        assertThat(sizes.get("unlimited-partitioned:foo"), is(329064L));
        assertThat(sizes.get("unlimited-partitioned:bar"), is(329064L));
    }


    /**
     * Sample Output
     *
     10-long: Size of 10 entries of type Long is: 103,104, average cost per entry: 10,310
     100-long: Size of 100 entries of type Long is: 235,280, average cost per entry: 2,352
     1k-long: Size of 1000 entries of type Long is: 552,568, average cost per entry: 552
     10k-long: Size of 10000 entries of type Long is: 2,787,184, average cost per entry: 278
     100k-long: Size of 100000 entries of type Long is: 24,263,024, average cost per entry: 242
     10k-1KB: Size of 10000 entries of type byte[] is: 18,156,336, average cost per entry: 1,815
     1k-10KB: Size of 1000 entries of type byte[] is: 16,040,424, average cost per entry: 16,040
     10k-Char50: Size of 10000 entries of type char[] is: 9,917,944, average cost per entry: 991
     10k-CString50: Size of 10000 entries of type String is: 8,717,824, average cost per entry: 871
     10k-BigDecimal: Size of 10000 entries of type BigDecimal is: 9,197,032, average cost per entry: 919
     10k-DeepObject1: Size of 10000 entries of type PoJo is: 10,747,952, average cost per entry: 1,074
     10k-DeepObject5: Size of 10000 entries of type PoJo is: 11,921,640, average cost per entry: 1,192
     10k-DeepObject10: Size of 10000 entries of type PoJo is: 13,255,352, average cost per entry: 1,325
     10k-DeepObject20: Size of 10000 entries of type PoJo is: 15,446,752, average cost per entry: 1,544
     */
    @Test
    public void investigateSizingOfDifferentIndexedFields() throws Exception {
        //start two remote nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);

        //get a handle on the invocation service
        InvocationService invocationService = (InvocationService) CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(config, classLoader)
                .ensureService("MyInvocationService1");

        //Three data node coherence cluster for this test
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(3));

        run(invocationService, "10-long", 10, Long.MAX_VALUE);
        run(invocationService, "100-long", 100, Long.MAX_VALUE);
        run(invocationService, "1k-long", 1000, Long.MAX_VALUE);
        run(invocationService, "10k-long", 10 * 1000, Long.MAX_VALUE);
        run(invocationService, "100k-long", 100 * 1000, Long.MAX_VALUE);
        run(invocationService, "10k-1KB", 10 * 1000, new byte[1000]);
        run(invocationService, "1k-10KB", 1000, new byte[10 * 1000]);
        run(invocationService, "10k-Char50", 10 * 1000, new char[50]);
        run(invocationService, "10k-CString50", 10 * 1000, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        run(invocationService, "10k-BigDecimal", 10 * 1000, BigDecimal.ONE);
        run(invocationService, "10k-DeepObject1", 10 * 1000, deepObject(1));
        run(invocationService, "10k-DeepObject5", 10 * 1000, deepObject(5));
        run(invocationService, "10k-DeepObject10", 10 * 1000, deepObject(10));
        run(invocationService, "10k-DeepObject20", 10 * 1000, deepObject(20));
    }

    private Object deepObject(int depth) {
        PoJo base = new PoJo(Integer.MAX_VALUE);
        while (depth > 0) {
            base = new PoJo(base);
            depth--;
        }
        return base;
    }

    private void run(InvocationService invocationService, String name, int numberToAdd, Object objectToIndex) {
        Set members = invocationService.getInfo().getServiceMembers();

        NamedCache cache = getCache(config, name);

        //add some data
        addValuesToCache(cache, numberToAdd, new PoJo(objectToIndex));

        //add indexes (which index the value in its entirety to keep the maths simple)
        cache.addIndex(new ReflectionExtractor("getData"), false, null);

        Map<Member, Long> indexSizes = invocationService.query(new IndexCountingInvocable(config), members);

        long total = total(indexSizes);
        System.out.printf("%s: Size of %s entries of type %s is: %,d, average cost per entry: %,d\n", name, numberToAdd, objectToIndex.getClass().getSimpleName(), total, total / numberToAdd);

        cache.clear();
    }

    private long total(Map<Member, Long> map) {
        long total = 0;
        for (long s : map.values())
            total += s;
        return total;
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, Object o) {
        Map all = new HashMap();
        for (int i = 0; i < numberToAdd; i++) {
            all.put("Key" + i, o);
        }
        cache.putAll(all);
    }

}
