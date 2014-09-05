package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.NamedCache;
import com.tangosol.util.AbstractMapListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * Also see intermediate.SimulatingLostUpdatesInClientListenersExample
 * BTS, 25-Jan-2008
 */
public class MapListeners extends ClusterRunner {
    int notificationCount = 0;

    @Test
    public void mapListenerShouldBeCalledWhenEntryInserted() throws IOException, InterruptedException {

        startBasicCacheProcess();
        startDataDisabledExtendProxy();

        NamedCache cache = getRemoteCache();

        cache.addMapListener(new AbstractMapListener() {
            public void entryInserted(com.tangosol.util.MapEvent mapEvent) {
                System.out.println("Entry was inserted " + mapEvent.getNewValue());
                notificationCount++;
            }

            public void entryUpdated(com.tangosol.util.MapEvent mapEvent) {
                System.out.println("Entry was udpated to" + mapEvent.getNewValue());
                notificationCount++;
            }
        });

        cache.put("Foo", "1");
        cache.put("Foo", "2");

        Thread.sleep(2000); //it's async

        assertEquals(2, notificationCount);
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
