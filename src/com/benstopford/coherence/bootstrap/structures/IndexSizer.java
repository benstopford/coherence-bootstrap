package com.benstopford.coherence.bootstrap.structures;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;

/**
 * Work out the total index size across all caches in a Coherence cluster
 */
public class IndexSizer {
    private static final String jmxHost = "localhost";
    private static final String jmxUser = "jmxadmin";
    private static final String jmxPassword = "password";

    public static void main(String[] args) throws Exception {
        new IndexSizer().sizeAllIndexes(40001);
    }

    /**
     * Returns the total bytes of all indexes in Coherence
     */
    public long sizeAllIndexes(int jmxPort) throws Exception {
        return doIndexQuery(jmx(jmxHost, String.valueOf(jmxPort), jmxUser, jmxPassword));
    }

    private long doIndexQuery(MBeanServerConnection server) throws Exception {
        Map<String, Double> sizesByCache = new HashMap<String, Double>();

        Set<ObjectInstance> results = server.queryMBeans(
                new ObjectName("Coherence:type=StorageManager,service=*,cache=*,*")
                , null
        );
        int total = results.size();
        System.err.println("Found " + total + " MBeans");

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

            log(total--);
        }

        return Math.round(printResults(sizesByCache));
    }

    private double printResults(Map<String, Double> sizesByCache) {
        double totalSize = 0;
        for (String s : sizesByCache.keySet()) {
            Double cacheSize = sizesByCache.get(s);
            totalSize += cacheSize;
            System.out.printf("%s:%sKB\n", s, cacheSize / 1024);
        }
        System.out.printf("All indexes come in at %sMB\n", totalSize / 1024 / 1024);
        return totalSize;
    }

    private void log(int total) {
        if (total % 1000 == 0) {
            System.out.println(total + " to go");
        }
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
        String urlPath = "/jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";
        System.out.println("Connecting to " + urlPath);
        JMXServiceURL jmxUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        Map<String, String[]> env = new HashMap<String, String[]>();
        env.put(JMXConnector.CREDENTIALS, new String[]{jmxUser, jmxPassword});
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl, env);
        return jmxConnector.getMBeanServerConnection();
    }
}