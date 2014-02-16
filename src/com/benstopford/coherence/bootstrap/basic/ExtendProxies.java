package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import java.io.IOException;

/**
 * BTS, 25-Jan-2008
 */
public class ExtendProxies extends TestBase {

    public void testWithExtendNode() throws IOException, InterruptedException {
        //start data enabled node
        startOutOfProcess("config/basic-cache.xml", "", "");

        //start data disabled node as extend proxy
        startOutOfProcess("config/basic-extend-enabled-cache.xml", "", "-Dtangosol.coherence.distributed.localstorage=false");

        //use extend config for this client
        NamedCache cache = new DefaultConfigurableCacheFactory("config/extend-config.xml")
                .ensureCache("stuff", getClass().getClassLoader());

        //write
        cache.put("Foo", "Bar");

        //read
        assertEquals("Bar", cache.get("Foo"));

    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
