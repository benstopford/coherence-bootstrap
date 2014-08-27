package com.benstopford.coherence.bootstrap.morecomplex.sizing;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.IndexCountingInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class UseSizeOfLibraryToCountIndexSizes extends ClusterRunner {
    public static final String config = "config/basic-invocation-service-1.xml";

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

        Map<Member, Long> indexSizeSmall = invocationService.query(new IndexCountingInvocable(name, config), members);

        long total = total(indexSizeSmall);
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
