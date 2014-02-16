package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.benstopford.coherence.bootstrap.structures.SleepingProcessor;
import com.tangosol.net.NamedCache;
import com.tangosol.util.processor.AbstractProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BTS, 01-May-2009
 * - change the thread counts in the config files to make the multithreaded invocation fast or slow
 */
public class MultiThreadedExtendClientExample extends TestBase {
    private AtomicInteger atomicI = new AtomicInteger();

    public void testShouldBeFasterMultithreaded() throws IOException, InterruptedException {

        //Start two nodes, one data enabled the other the extend proxy
        startOutOfProcess("config/basic-cache-threaded.xml");
        startOutOfProcess("config/basic-extend-enabled-threaded-cache.xml", "", "-Dtangosol.coherence.distributed.localstorage=false ");
        Thread.sleep(1000);

        NamedCache cache = getCache("config/extend-config.xml", "foo");

        System.out.println("--------Single----------");

        runSingleThreaded(cache, 10);

        cache.clear();

        System.out.println("--------Multi----------");

        runMultiThreaded(cache, 10);
    }


    private void runSingleThreaded(NamedCache cache, int numberToAdd) {
        AbstractProcessor ep = new SleepingProcessor();
        final long start = System.currentTimeMillis();

        for (int i = 1; i <= numberToAdd; i++) {
            cache.invoke("Key" + i, ep);
        }

        long end = System.currentTimeMillis();
        System.out.println("Synchronous execution took " + (end - start));
    }


    private void runMultiThreaded(final NamedCache cache, int numberToAdd) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        long start = System.currentTimeMillis();

        executorService.invokeAll(createProcessorTasks(numberToAdd, cache), 60, TimeUnit.SECONDS);

        long took = System.currentTimeMillis() - start;
        System.out.println("Threaded execution took " + took);
    }


    private Collection<? extends Callable<Object>> createProcessorTasks(int numberToAdd, final NamedCache cache) {
        final AbstractProcessor ep = new SleepingProcessor();
        ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (int i = 0; i < numberToAdd; i++) {
            final int j = i;
            tasks.add(Executors.callable(new Runnable() {
                public void run() {
                    cache.invoke("Key" + j, ep);
                }
            }));
        }
        return tasks;
    }


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}