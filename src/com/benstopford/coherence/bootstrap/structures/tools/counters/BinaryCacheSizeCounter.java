package com.benstopford.coherence.bootstrap.structures.tools.counters;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Counts the binary size of data in a Coherence cluster
 * (assumes that you are using POF so that units are in Binary)
 *
 * (Designed as a stand-alone class it's so easy to copy and paste as a util)
 */
public class BinaryCacheSizeCounter {
    private static final String jmxHost = "localhost";

    public static void main(String[] args) throws Exception {
        new BinaryCacheSizeCounter().sumClusterStorageSize(40001);
    }


    public long sumClusterStorageSize(int jmxPort) throws Exception {
        MBeanServerConnection server = jmx(jmxHost, String.valueOf(jmxPort), "'", "");
        Map<String, Double> sizesByCache = new HashMap<String, Double>();
        String searchString = "Coherence:type=Cache,service=*,name=*,nodeId=*,tier=back";//service=unlimited-partitioned

        Set<ObjectInstance> results = server.queryMBeans(new ObjectName(searchString), null);

        int total = results.size();
        check(total);

        for (ObjectInstance result : results) {
            ObjectName objectName = result.getObjectName();
            String cacheName = objectName.getKeyProperty("name");
            int units = (Integer) server.getAttribute(objectName, "Units");


            Double unitTotal = sizesByCache.get(cacheName);
            if (unitTotal == null) {
                unitTotal = 0d;
            }
            unitTotal += units;

            sizesByCache.put(cacheName, unitTotal);

            logEvery(1000, total--);
        }

        double totalSize = sum(sizesByCache);
        return Math.round(totalSize);
    }


    private void check(int total) {
        if (total == 0) {
            throw new RuntimeException("Could not find any mbeans. Probably a connection problem or Coherence MBeans are not enabled");
        }
        System.out.println("Found " + total + " MBeans");
    }

    private double sum(Map<String, Double> sizesByCache) {
        double totalSize = 0;
        for (String s : sizesByCache.keySet()) {
            Double cacheSize = sizesByCache.get(s);
            totalSize += cacheSize;
            System.out.printf("Cache %s Total:%sKB\n", s, cacheSize / 1024);
        }
        System.out.printf("Total of all caches: %sKB\n", totalSize / 1024);
        return totalSize;
    }

    private void logEvery(int n, int total) {
        if (total % n == 0) {
            System.out.println(total + " to go");
        }
    }

    private MBeanServerConnection jmx(String jmxHost, String jmxPort, String jmxUser, String jmxPassword) throws Exception {
        Map<String, String[]> env = new HashMap<String, String[]>();

        String urlPath = "/jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";
        System.out.println("Connecting to " + urlPath);

        JMXServiceURL jmxUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        env.put(JMXConnector.CREDENTIALS, new String[]{jmxUser, jmxPassword});

        return JMXConnectorFactory.connect(jmxUrl, env).getMBeanServerConnection();
    }
}