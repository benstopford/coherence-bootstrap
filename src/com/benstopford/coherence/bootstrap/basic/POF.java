package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.dataobjects.SimplePofObject;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;


public class POF {

    /**
     * see config/my-pof-config
     */
    @Test
    public void putAndGetPofEncodedObject() throws IOException {

        DefaultConfigurableCacheFactory factory = new DefaultConfigurableCacheFactory("config/basic-cache-with-pof.xml");
        NamedCache cache = factory.ensureCache("stuff", getClass().getClassLoader());

        cache.put("key", new SimplePofObject("some data"));

        SimplePofObject object = (SimplePofObject) cache.get("key");

        assertEquals("some data", object.getData());
    }

}
