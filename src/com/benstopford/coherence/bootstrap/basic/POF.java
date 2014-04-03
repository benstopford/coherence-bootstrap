package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.dataobjects.LoggingPofObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;


public class POF {
    ClassLoader classLoader = getClass().getClassLoader();

    /**
     * see config/my-pof-config
     */
    @Test
    public void putAndGetPofEncodedObject() throws IOException {

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache-with-pof.xml", classLoader)
                .ensureCache("stuff", classLoader);

        cache.put("key", new LoggingPofObject("some data"));

        LoggingPofObject object = (LoggingPofObject) cache.get("key");

        assertEquals("some data", object.getData());
    }

}
