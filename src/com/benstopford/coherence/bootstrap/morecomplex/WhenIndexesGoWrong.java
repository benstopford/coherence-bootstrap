package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.dataobjects.Trade;
import com.benstopford.coherence.bootstrap.structures.framework.CoherenceClusteredTest;
import com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.*;

import java.io.IOException;

import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.end;
import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.start;

public class WhenIndexesGoWrong extends CoherenceClusteredTest {
    NamedCache cache = null;
    PerformanceTimer timer = new PerformanceTimer();

     /*
        Class to look at performance of indexing when it is not used properly
        I'm using:
         -Xmx8g -Xms8g
         (and tweaking these for fun) -XX:NewSize=5g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     */

    public void test() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-cache.xml");
        startOutOfProcess("config/basic-cache.xml");
        DefaultConfigurableCacheFactory factory = new DefaultConfigurableCacheFactory("config/basic-cache.xml");
        cache = factory.ensureCache("test", getClass().getClassLoader());

        for (int i = 0; i < 10000; i++) {
            cache.put(i, new Trade(i, "system", "book" + (i % 4)));
        }

        int iters = 1000;

        AndFilter filter = new AndFilter(new LikeFilter("getSystem", "not this"), new EqualsFilter("getId", 1l));

//        start();
//
//        for (int i = 0; i < iters; i++) {
//            cache.entrySet(filter);
//        }
//
//        end().printAverage(iters);

        cache.addIndex(new ReflectionExtractor("getSystem"), true, null);
        cache.addIndex(new ReflectionExtractor("getId"), true, null);

        start();

        for (int i = 0; i < iters; i++) {
            cache.entrySet(filter);
        }

        end().printAverage(iters);
        start();

        for (int i = 0; i < iters; i++) {
            cache.entrySet(new AndFilter(new NotEqualsFilter("getId", -1l), new AndFilter(new EqualsFilter("getId", 1l), new LikeFilter("getSystem", "not this"))));
        }

        end().printAverage(iters);
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
