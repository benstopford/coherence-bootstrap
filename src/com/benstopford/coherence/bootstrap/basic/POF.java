package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.MyPofObject;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import junit.framework.TestCase;

import java.io.IOException;


public class POF extends TestCase {

    /**
     * see config/my-pof-config
     */
    public void testUsingPof() throws IOException {
//        System.setProperty("tangosol.pof.config", "config/my-pof-config.xml");
        DefaultConfigurableCacheFactory factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        NamedCache cache = factory.ensureCache("stuff", getClass().getClassLoader());

        cache.put("key", new MyPofObject("some data"));

        MyPofObject object = (MyPofObject) cache.get("key");

        assertEquals("some data", object.getData());
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
