package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.benstopford.coherence.bootstrap.structures.ParallelSumAggregator;
import com.benstopford.coherence.bootstrap.structures.SumAggregator;
import com.tangosol.net.NamedCache;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.NotFilter;

import java.io.Serializable;

/**
 * BTS, 07-Dec-2007
 */
public class Aggregators extends TestBase implements Serializable
{

    public void testSimpleAggregation() throws InterruptedException {
        NotFilter grabEverything = new NotFilter(new EqualsFilter("toString", "it won't be this"));
        NamedCache cache = getBasicCache("foo");

        //add simple data
        for(int i = 0;i<1000; i++){
            cache.put("key:"+i, 10);
        }

        //create a simple (non-parallel) aggregator
        startTimer();
        Integer value = (Integer) cache.aggregate(grabEverything, new SumAggregator());
        long entryAggregator = took();


        //run again with a parallel aggregator
        startTimer();
        Integer value2 = (Integer) cache.aggregate(grabEverything, new ParallelSumAggregator(new SumAggregator()));
        long parallelAggregator = took();

        System.out.printf("EntryAggregator found sum of %s in %sms\n", value, entryAggregator);
        System.out.printf("ParallelAggregator found sum of %s in %sms\n", value2, parallelAggregator);

        assertEquals(value.intValue(), 10000);
        assertEquals(value2.intValue(), 10000);
        assertTrue("parallel aggregator should be faster", parallelAggregator<entryAggregator);
    }


    protected void setUp() throws Exception {
        removeCoherenceLogging();
        super.setUp();
        setDefaultProperties();
        startBasicCacheProcess();
        startBasicCacheProcess();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }
}