package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;

public final class UsingAnOverflowCacheToExpireEntriesToDiskExample extends ClusterRunner {
    private static final String OVERFLOW_CONFIG_XML = "config/overflow-config.xml";
    private boolean firstEntryWasExpired = false;
    private int i = 0;
    private long start;

    @Test
    public void shouldOverflowToDisk() throws IOException, InterruptedException {
        NamedCache cache = getCache(OVERFLOW_CONFIG_XML, "test-cache");

        //force cache to start before we start timer
        cache.size();
        start = System.currentTimeMillis();

        //add test values and check that they are expired appropriately
        while (haveBeenRunningForLessThan5Secs()) {
            cache.put(++i, "some-value");
            assertNotNull("Write failed?", cache.get(i));

            if (cache.get(1) == null && !firstEntryWasExpired) {
                System.out.println("First object was expired on iteration " + i + " after " + (System.currentTimeMillis() - start) + "ms");
                firstEntryWasExpired = true;
            }
        }

        if (!firstEntryWasExpired) {
            fail("Items were not expired");
        }
    }


    private boolean haveBeenRunningForLessThan5Secs() {
        return System.currentTimeMillis() - start < 5 * 1000;
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
