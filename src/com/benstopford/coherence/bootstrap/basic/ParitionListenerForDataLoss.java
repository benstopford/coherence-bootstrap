package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.tools.MyPartitionListener;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.NamedCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * BTS, 12-May-2008
 */
public class ParitionListenerForDataLoss extends ClusterRunner {

    /**
     * A PartitionListener tells you that a data partition has been lost. This is really important as otherwise you
     * simply don't know if the cluster is in a good shape of not. Patition Listeners are used to detect a
     * repartitioning event. That is to say that if a node is lost, say the process dies or the Cache Service restarts
     * itself etc, Coherence will restore the data from its backup copy.
     */
    @Test
    public void dataLossShouldTriggerPartitionListenerActivation() throws IOException, InterruptedException {
        String dataEnabledConfig = "config/basic-cache-with-a-partition-listener-local-storage-true.xml";

        //create 3 external data enabled processes
        Process nodeToBeKilled = startCoherenceProcess(dataEnabledConfig);
        Process nodeToBeKilled2 = startCoherenceProcess(dataEnabledConfig);
        startCoherenceProcess(dataEnabledConfig);

        //Ensure this VM is data disabled for this cache service
        NamedCache cache = getCache("config/basic-cache-with-local-storage-false.xml","foo");
        Thread.sleep(2000);

        //make sure we have clustered all 4 nodes
        assertEquals(4, cache.getCacheService().getCluster().getMemberSet().size());

        //add some data
        addData(cache, 100);

        nodeToBeKilled.destroy();
        nodeToBeKilled2.destroy();
        Thread.sleep(5000);

        assertTrue(MyPartitionListener.CLUSTER_DATA_LOSS_FLAG_FILE.exists());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        File flagFile = MyPartitionListener.CLUSTER_DATA_LOSS_FLAG_FILE;
        if (flagFile.exists()) {
            flagFile.delete();
            System.out.println("deleted file");
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}