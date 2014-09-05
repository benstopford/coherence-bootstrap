package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 25-Jan-2008
 */
public class ExtendProxies extends ClusterRunner {

    @Test
    public void useExtendProxy() throws IOException, InterruptedException {
        //start data enabled node
        startCoherenceProcess("config/basic-cache.xml");

        //start data disabled node as extend proxy
        startCoherenceProcess("config/basic-extend-enabled-cache-32001.xml", LOCAL_STORAGE_FALSE);

        //use extend config for this client
        ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory("config/extend-client-32001.xml", getClass().getClassLoader());
        NamedCache cache = factory.ensureCache("stuff", getClass().getClassLoader());

        //write
        cache.put("Foo", "Bar");

        //read
        assertEquals("Bar", cache.get("Foo"));
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
