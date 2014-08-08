package com.benstopford.coherence.bootstrap.structures.tools;


import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
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
        super.setUp();

        String config = "config/basic-cache-elastic-data.xml";

        //start data nodes
        startCoherenceProcess(config);
        startCoherenceProcess(config);

        //start proxy
        startDataDisabledExtendProxy();

        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(4)); // this process will also join (to do the memberset count)

        String port = System.getProperty("com.benstopford.extend.port");

        synchronized (this){
            System.out.printf("Cluster is now running (port:%s), test away!\n", port);
            wait();
        }
    }
}
