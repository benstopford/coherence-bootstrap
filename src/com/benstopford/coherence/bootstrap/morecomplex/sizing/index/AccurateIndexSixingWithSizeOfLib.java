package com.benstopford.coherence.bootstrap.morecomplex.sizing.index;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PoJo;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.index.IndexSizeJMXBean;
import com.benstopford.coherence.bootstrap.structures.tools.index.IndexSizer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class AccurateIndexSixingWithSizeOfLib extends ClusterRunner {
    public static final String dataNodeConfig = "config/basic-index-sizer.xml";

    @Test
    public void shouldSizeSingleCache() {

        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);
        NamedCache foo = getCache(dataNodeConfig, "foo");
        assertClusterStarted();

        //add some indexed data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizer sizer = new IndexSizer(dataNodeConfig);

        long size = sizer.getTotal();

        assertThat(size, is(329064L));
    }

    @Test
    public void shouldFailIfNoConfigSpecified() throws IOException, InterruptedException {

        NamedCache foo = getCache(dataNodeConfig, "foo");

        //add some data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));

        //add indexes (which index the value in its entirety to keep the maths simple)
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizer sizer = new IndexSizer();

        try {
            sizer.getTotal();
        } catch (Exception e) {
            assertThat(e.getMessage(), is("java.lang.IllegalStateException: Service \"unlimited-partitioned\" has been started by a different configurable cache factory."));
        }
    }


    @Test
    public void shouldWorkOverExtend() throws IOException, InterruptedException {

        //start two remote nodes
        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);
        startDataDisabledExtendProxy();
        NamedCache foo = getRemoteCache("foo");

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));

        //add indexed data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizer sizer = new IndexSizer();

        long size = sizer.getTotal();

        assertThat(size, is(318696L));
    }

    @Test
    public void shouldWorkOverExtendWithDefaultConfig() throws IOException, InterruptedException {

        System.setProperty("tangosol.coherence.cacheconfig", dataNodeConfig);

        //start two remote nodes
        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);
        startDataDisabledExtendProxy();
        NamedCache foo = getRemoteCache("foo");

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4));

        //add some indexed data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));
        foo.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizer sizer = new IndexSizer();

        Map<String, Long> sizes = sizer.getIndexSizes();

        assertThat(sizes.get("unlimited-partitioned:foo"), is(318696L));
    }

    @Test
    public void shouldSizeMultipleCaches() {
        //start two remote nodes
        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);

        //Three data node coherence cluster for this test
        assertClusterStarted();

        NamedCache foo = getCache(dataNodeConfig, "foo");
        NamedCache bar = getCache(dataNodeConfig, "bar");

        //add some data
        addValuesToCache(foo, 1000, new PoJo(Long.MAX_VALUE));
        addValuesToCache(bar, 1000, new PoJo(Long.MAX_VALUE));

        //add indexes (which index the value in its entirety to keep the maths simple)
        foo.addIndex(new ReflectionExtractor("getData"), false, null);
        bar.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizer sizer = new IndexSizer(dataNodeConfig);

        Map<String, Long> sizes = sizer.getIndexSizes();
        System.out.println(sizes);

        assertThat(sizes.get("unlimited-partitioned:foo"), is(329064L));
        assertThat(sizes.get("unlimited-partitioned:bar"), is(329064L));
    }


    /**
     * Sample Output
     * <p/>
     * 10-long: Size of 10 entries of type Long is: 103,104, average cost per entry: 10,310
     * 100-long: Size of 100 entries of type Long is: 235,280, average cost per entry: 2,352
     * 1k-long: Size of 1000 entries of type Long is: 552,568, average cost per entry: 552
     * 10k-long: Size of 10000 entries of type Long is: 2,787,184, average cost per entry: 278
     * 100k-long: Size of 100000 entries of type Long is: 24,263,024, average cost per entry: 242
     * 10k-1KB: Size of 10000 entries of type byte[] is: 18,156,336, average cost per entry: 1,815
     * 1k-10KB: Size of 1000 entries of type byte[] is: 16,040,424, average cost per entry: 16,040
     * 10k-Char50: Size of 10000 entries of type char[] is: 9,917,944, average cost per entry: 991
     * 10k-CString50: Size of 10000 entries of type String is: 8,717,824, average cost per entry: 871
     * 10k-BigDecimal: Size of 10000 entries of type BigDecimal is: 9,197,032, average cost per entry: 919
     * 10k-DeepObject1: Size of 10000 entries of type PoJo is: 10,747,952, average cost per entry: 1,074
     * 10k-DeepObject5: Size of 10000 entries of type PoJo is: 11,921,640, average cost per entry: 1,192
     * 10k-DeepObject10: Size of 10000 entries of type PoJo is: 13,255,352, average cost per entry: 1,325
     * 10k-DeepObject20: Size of 10000 entries of type PoJo is: 15,446,752, average cost per entry: 1,544
     */
    @Test
    public void investigateSizingOfDifferentIndexedFields() throws Exception {
        //start two remote nodes
        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);

        //Three data node coherence cluster for this test
        assertClusterStarted();

        run("10-long", 10, Long.MAX_VALUE);
        run("100-long", 100, Long.MAX_VALUE);
        run("1k-long", 1000, Long.MAX_VALUE);
        run("10k-long", 10 * 1000, Long.MAX_VALUE);
        run("100k-long", 100 * 1000, Long.MAX_VALUE);
        run("10k-1KB", 10 * 1000, new byte[1000]);
        run("1k-10KB", 1000, new byte[10 * 1000]);
        run("10k-Char50", 10 * 1000, new char[50]);
        run("10k-CString50", 10 * 1000, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        run("10k-BigDecimal", 10 * 1000, BigDecimal.ONE);
        run("10k-DeepObject1", 10 * 1000, deepObject(1));
        run("10k-DeepObject5", 10 * 1000, deepObject(5));
        run("10k-DeepObject10", 10 * 1000, deepObject(10));
        run("10k-DeepObject20", 10 * 1000, deepObject(20));
    }

    private Object deepObject(int depth) {
        PoJo base = new PoJo(Integer.MAX_VALUE);
        while (depth > 0) {
            base = new PoJo(base);
            depth--;
        }
        return base;
    }

    private void run(String name, int numberToAdd, Object objectToIndex) {

        NamedCache cache = getCache(dataNodeConfig, name);

        //add some data
        addValuesToCache(cache, numberToAdd, new PoJo(objectToIndex));

        //add indexes (which index the value in its entirety to keep the maths simple)
        cache.addIndex(new ReflectionExtractor("getData"), false, null);

        long total = new IndexSizer(dataNodeConfig).getTotal();

        System.out.printf("%s: Size of %s entries of type %s is: %,d, average cost per entry: %,d\n", name, numberToAdd, objectToIndex.getClass().getSimpleName(), total, total / numberToAdd);

        cache.clear();
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, Object o) {
        Map all = new HashMap();
        for (int i = 0; i < numberToAdd; i++) {
            all.put("Key" + i, o);
        }
        cache.putAll(all);
    }

    @Test
    public void shouldRegisterBean() throws Exception {
        System.setProperty("tangosol.coherence.cacheconfig", dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);
        startCoherenceProcess(dataNodeConfig);
        startDataDisabledExtendProxy();
        NamedCache cache = getRemoteCache("cache1");
        startLocalJMXServer(40001);
        assertClusterStarted();

        //add some data
        addValuesToCache(cache, 10000, new PoJo("foo"));

        //add indexes (which index the value in its entirety to keep the maths simple)
        cache.addIndex(new ReflectionExtractor("getData"), false, null);
        cache.addIndex(new ReflectionExtractor("toString"), false, null);

        cache = getRemoteCache("cache2");

        //add some data
        addValuesToCache(cache, 100, new PoJo("foo"));

        //add indexes (which index the value in its entirety to keep the maths simple)
        cache.addIndex(new ReflectionExtractor("getData"), false, null);

        IndexSizeJMXBean.register();

        String searchString = "com.noracle.coherence:type=IndexSizer,*";

        MBeanServerConnection server = jmx(40001);
        Set<ObjectInstance> results = server.queryMBeans(new ObjectName(searchString), null);


        for (ObjectInstance result : results) {
            long total = (Long)server.getAttribute(result.getObjectName(), "Total");
            assertThat(total, is(5816744L));
        }
    }


    private MBeanServerConnection jmx(int jmxPort) throws Exception {
        Map<String, String[]> env = new HashMap<String, String[]>();
        String urlPath = "/jndi/rmi://localhost:" + jmxPort + "/jmxrmi";
        JMXServiceURL jmxUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        return JMXConnectorFactory.connect(jmxUrl, env).getMBeanServerConnection();
    }

}
