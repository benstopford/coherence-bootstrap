package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 20-Feb-2008
 */
public class HopBetweenCaches extends ClusterRunner implements Serializable {

    @Test
    public void shouldBeAbleToAcceessDifferentCacheFromAnEntryProcessorIfTheyAreInDifferentCacheServices() {
        //*****NB - this is only safe to do if the two caches are in different cache services*****
        //*****NB - note that it may work using a single cache service with multiple threads but there is the potential for deadlock*****

        NamedCache cache1 = getCache("config/basic-cache-on-different-cache-service.xml", "cache1");
        NamedCache cache2 = getBasicCache("cache2");

        cache1.invoke("Key", new PutInAnotherCacheEP());

        assertEquals("Entry Processors rock!!", cache2.get("Key2"));
    }

    private class PutInAnotherCacheEP extends AbstractProcessor {
        public Object process(InvocableMap.Entry entry) {

            NamedCache cache2 = getBasicCache("cache2");
            cache2.put("Key2", "Entry Processors rock!!");

            return null;
        }
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
