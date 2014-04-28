package com.benstopford.coherence.bootstrap.structures.tools.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;

/**
 * Work out the total index size across all caches in a Coherence cluster
 * <p/>
 * Warning: this mechanism is not particularly accurate. In fact it's pretty lousy. I leave
 * the example here only for reference. See CoherenceIndexSizeMbeanIsInaccurate for details
 * <p/>
 * See CoherenceIndexSizeMbeanIsInaccurate which demonstrates the issue in detail.
 *
 * (Designed as a stand-alone class it's so easy to copy and paste as a util)
 */
public class IndexInfoCounter {
    private static final String jmxHost = "localhost";
    private boolean log;

    public static void main(String[] args) throws Exception {
        new IndexInfoCounter().sumIndexInfoFootprintMbean(40001);
    }

    /**
     * Returns the total bytes of all indexes in Coherence
     */
    public long sumIndexInfoFootprintMbean(int jmxPort) throws Exception {
        return sumIndexInfoFootprintMbean(jmxPort, true);
    }

    public long sumIndexInfoFootprintMbean(int jmxPort, boolean print) throws Exception {
        this.log = print;
        return doIndexQuery(jmx(jmxHost, String.valueOf(jmxPort), "'", ""));
    }

    private long doIndexQuery(MBeanServerConnection server) throws Exception {
        Map<String, Double> sizesByCache = new HashMap<String, Double>();
        String searchString = "Coherence:type=StorageManager,service=*,cache=*,*";

        Set<ObjectInstance> results = server.queryMBeans(new ObjectName(searchString), null);

        int total = results.size();
        check(total);

        for (ObjectInstance result : results) {
            String cacheName = result.getObjectName().getKeyPropertyList().get("cache");
            String[] indexInfo = (String[]) server.getAttribute(result.getObjectName(), "IndexInfo");

            Double cacheIndexSize = sizesByCache.get(cacheName);
            if (cacheIndexSize == null) {
                cacheIndexSize = 0d;
            }

            for (String index : indexInfo) {
                cacheIndexSize += extractSize(index);
            }

            sizesByCache.put(cacheName, cacheIndexSize);

            logEvery(1000, total--);
        }

        double totalSize = sum(sizesByCache);
        logf("All indexes come in at %sMB\n", totalSize / 1024 / 1024);
        return Math.round(totalSize);
    }

    private void check(int total) {
        if (total == 0) {
            throw new RuntimeException("Could not find any mbeans. Probably a connection problem or Coherence MBeans are not enabled");
        }
        log("Found " + total + " MBeans");
    }

    private double sum(Map<String, Double> sizesByCache) {
        double totalSize = 0;
        for (String s : sizesByCache.keySet()) {
            Double cacheSize = sizesByCache.get(s);
            totalSize += cacheSize;
            logf("%s:%sKB\n", s, cacheSize / 1024);
        }

        return totalSize;
    }

    private void logEvery(int n, int total) {
        if (total % n == 0) {
            log(total + " to go");
        }
    }

    private void log(String s) {
        if (log)
            System.out.println(s);
    }

    private void logf(String s, Object... args) {
        if (log)
            System.out.printf(s, args);
    }

    private double extractSize(String indexInfo) {
        String[] elems = indexInfo.split(" ");
        for (String elem : elems) {
            if (elem.startsWith("Footprint")) {
                String size = elem.split("=")[1];
                size = size.replaceAll(",", "");
                if (size.contains("MB")) {
                    size = size.replaceAll("MB", "");
                    return Double.valueOf(size) * 1024 * 1024;
                } else if (size.contains("KB")) {
                    size = size.replaceAll("KB", "");
                    return Double.valueOf(size) * 1024;
                } else {
                    return Double.valueOf(size);
                }
            }
        }
        return 0;
    }

    private MBeanServerConnection jmx(String jmxHost, String jmxPort, String jmxUser, String jmxPassword) throws Exception {
        Map<String, String[]> env = new HashMap<String, String[]>();

        String urlPath = "/jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";
        log("Connecting to " + urlPath);

        JMXServiceURL jmxUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        env.put(JMXConnector.CREDENTIALS, new String[]{jmxUser, jmxPassword});

        return JMXConnectorFactory.connect(jmxUrl, env).getMBeanServerConnection();
    }
}