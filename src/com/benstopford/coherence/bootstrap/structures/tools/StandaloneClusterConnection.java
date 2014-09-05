package com.benstopford.coherence.bootstrap.structures.tools;


import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * Simple utility that starts a cluster and waits (so it can be utalised elsewhere)
 */
public class StandaloneClusterConnection extends ClusterRunner {

    public static final ClassLoader classLoader = StandaloneClusterConnection.class.getClassLoader();

    public static void main(String[] args) throws Exception {

        Integer port = 34285;//put the correct port here
        System.setProperty("client.extend.port", port.toString());
        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/client.xml", classLoader)
                .ensureCache("test", classLoader);

        cache.put("key", "value");

        System.out.println(cache.size() == 1 ? "SUCCESS" : "FAILED");
    }

}
