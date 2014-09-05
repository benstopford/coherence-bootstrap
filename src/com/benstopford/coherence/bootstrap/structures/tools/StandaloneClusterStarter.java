package com.benstopford.coherence.bootstrap.structures.tools;


import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ProcessExecutor;
import com.tangosol.net.CacheFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Simple utility that starts a cluster and waits (so it can be utalised elsewhere)
 */
public class StandaloneClusterStarter extends ClusterRunner {
    public static void main(String[] args) throws Exception{
        new StandaloneClusterStarter().startServer();
    }

    private void startServer() throws Exception {

        ProcessExecutor.COHERERENCE_PROCESS_MEMORY = 800;

        super.clearDataDirectories();

        super.setUp();

        String config = "config/basic-cache-persistent-and-elastic.xml";

        //start data nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        startCoherenceProcess(config);

        //start proxy
        startDataDisabledExtendProxy();

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(8));

        String port = System.getProperty("com.benstopford.extend.port");

        synchronized (this){
            System.out.printf("Cluster is now running (port:%s), test away!\n", port);
            wait();
        }
    }
}
