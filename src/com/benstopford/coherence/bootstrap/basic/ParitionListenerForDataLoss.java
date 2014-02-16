package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.TestBase;
import com.benstopford.coherence.bootstrap.structures.MyPartitionListener;
import com.tangosol.net.*;

import java.io.IOException;
import java.io.File;

/**
 * BTS, 12-May-2008
 */
public class ParitionListenerForDataLoss extends TestBase {

    /**
     * A PartitionListener tells you that a data partition has been lost. This is really important as otherwise you
     * simply don't know if the cluster is in a good shape of not. Patition Listeners are used to detect a
     * repartitioning event. That is to say that if a node is lost, say the process dies or the Cache Service restarts
     * itself etc, Coherence will restore the data from its backup copy.
     */
    public void testDataLossShouldTriggerPartitionListenerActivation() throws IOException, InterruptedException {
        //create 2 external data enabled processes
        Process nodeToBeKilled = startOutOfProcess("config/basic-cache-with-a-partition-listener-local-storage-true.xml");
        Process nodeToBeKilled2 = startOutOfProcess("config/basic-cache-with-a-partition-listener-local-storage-true.xml");
        startOutOfProcess("config/basic-cache-with-a-partition-listener-local-storage-true.xml");

        //create local data disabled process
        NamedCache cache = getCache("config/basic-cache-with-local-storage-false.xml","foo");
        Thread.sleep(2000);

        //make sure we have clustered all 3 nodes
        assertEquals(4, cache.getCacheService().getCluster().getMemberSet().size());

        //add some data
        addData(cache, 100);

        nodeToBeKilled.destroy();
        nodeToBeKilled2.destroy();
        Thread.sleep(5000);

        assertTrue(MyPartitionListener.CLUSTER_DATA_LOSS_FLAG_FILE.exists());
    }

    protected void setUp() throws Exception {
        super.setUp();
        File flagFile = MyPartitionListener.CLUSTER_DATA_LOSS_FLAG_FILE;
        if (flagFile.exists()) {
            flagFile.delete();
            System.out.println("deleted file");
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}