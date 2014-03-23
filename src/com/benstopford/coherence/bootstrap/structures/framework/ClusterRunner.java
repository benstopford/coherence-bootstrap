package com.benstopford.coherence.bootstrap.structures.framework;

import com.benstopford.coherence.bootstrap.structures.dataobjects.SizableObject;
import com.benstopford.coherence.bootstrap.structures.dataobjects.SizableObjectFactory;
import com.tangosol.net.*;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

/**
 * BTS, 07-Dec-2007
 * <p/>
 * EDIT 2014: This test framework predates the work done by the Coherence team to internalise this kind of testing
 * into a single JVM and multiple classloaders.Credit for this excellent pattern goes to Andrew (Orange Pheonix) Wilson
 * <p/>
 * I strongly suggest that, in your own code, you use the LittleGrid. See JK's post here:
 * http://thegridman.com/coherence/oracle-coherence-testing-with-oracle-tools/
 */
public abstract class ClusterRunner {
    public static final String LOCAL_STORAGE_FALSE = "-Dtangosol.coherence.distributed.localstorage=false";

    private ProcessExecutor executor = new ProcessExecutor(defaultProperties());
    private HashSet<CacheService> services = new HashSet<CacheService>();
    private HashSet<NamedCache> caches = new HashSet<NamedCache>();


    protected static Properties defaultProperties() {
        Properties properties = new Properties();
        properties.put("tangosol.coherence.clusteraddress", "239.255.12.30");
        properties.put("tangosol.coherence.clusterport", "1234");
        properties.put("tangosol.coherence.cluster", "benstopford.coherence.bootstrap");
        properties.put("tangosol.coherence.cacheconfig", "config/basic-cache.xml");
        properties.put("tangosol.coherence.log.level", "9");
        properties.put("tangosol.coherence.ttl", "0");
        properties.put("tangosol.coherence.management", "all");
        properties.put("tangosol.coherence.management.remote", "true");
        return properties;
    }

    @Before
    public void setUp() throws Exception {
        deleteContentsOfLogDir();
        ProcessLogger.switchStdErrToFile();
        new PersistentPortTracker().incrementExtendPort();
        System.getProperties().putAll(defaultProperties());
    }

    private void deleteContentsOfLogDir() {
        File logDir = new File("log");
        if (!logDir.exists()) {
            logDir.mkdir();
        }

        File[] logs = logDir.listFiles();
        if (logs != null) {
            for (File log : logs) {
                log.delete();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        shutdownServices();
        releaseLocalCacheResources();
        CacheFactory.getCluster().shutdown();
        CacheFactory.shutdown();
        executor.killOpenCoherenceProcesses();
        waitForAllKilledMembersToTimeOut();
    }

    private void waitForAllKilledMembersToTimeOut() throws InterruptedException {
        while (CacheFactory.ensureCluster().getMemberSet().size() > 1) {
            System.out.println("Waiting for " + CacheFactory.ensureCluster().getMemberSet().size() + " active cluster members to be deregistered");
            System.out.println(CacheFactory.ensureCluster().getMemberSet());
            Thread.sleep(1000);
        }
    }

    private void releaseLocalCacheResources() {
        for (NamedCache nc : caches) {
            nc.release();
        }
        caches.clear();
    }

    private void shutdownServices() {
        for (CacheService cs : services) {
            cs.shutdown();
        }
        services.clear();
    }

    protected NamedCache getBasicCache(String name) {
        return getCache("config/basic-cache.xml", name);
    }

    protected void startOutOfProcessWithJMX(String config) {
        executor.startOutOfProcess(config, getJMXProperties(3000));
    }

    private static String getJMXProperties(int jmx_port) {
        return getSunJMXProperties(jmx_port);
    }

    private static String getSunJMXProperties(int jmx_port) {
        return "" +
                "-Dcom.sun.management.jmxremote.ssl=false " +
                "-Dcom.sun.management.jmxremote " +
                "-Dcom.sun.management.jmxremote.authenticate=false " +
                "-Dcom.sun.management.jmxremote.port=" + jmx_port + " ";
    }


    static int prefix = 0;

    protected void addData(NamedCache cache, int mbToAdd) {
        prefix++;
        for (int i = 0; i < mbToAdd; i++) {
            SizableObject sizableObject = new SizableObjectFactory().buildObject(1000);
            cache.put(prefix + Integer.toString(i), sizableObject);
        }
        System.out.println("added " + mbToAdd + "MB");
    }

    protected NamedCache getRemoteCache() {
        return getRemoteCache("foo");
    }

    protected NamedCache getRemoteCache(String name) {
        return new DefaultConfigurableCacheFactory("config/extend-client-32001.xml").ensureCache(name, getClass().getClassLoader());
    }

    protected void startBasicCacheProcess() throws IOException, InterruptedException {
        executor.startOutOfProcess("config/basic-cache.xml", "");
    }

    protected void startBasicCacheProcessWithJMX(int port) throws IOException, InterruptedException {
        executor.startOutOfProcess("config/basic-cache.xml", getJMXProperties(port));
    }

    protected void startBasicCacheProcessWithJMX(String config, int port) throws IOException, InterruptedException {
        executor.startOutOfProcess(config, getJMXProperties(port));
    }

    protected void startDataDisabledExtendProxy() throws IOException, InterruptedException {
        executor.startOutOfProcess("config/basic-extend-enabled-cache-32001.xml", LOCAL_STORAGE_FALSE);
    }

    protected Process startCoherenceProcess(String config) {
        return executor.startOutOfProcess(config, "");
    }

    protected Process startCoherenceProcess(String config, String properties) {
        return executor.startOutOfProcess(config, properties);
    }


    protected void startExtendEnabledDataNode(boolean enableJMX, String config) throws IOException, InterruptedException {
        String jmxProps = "";
        if (enableJMX) {
            jmxProps = getJMXProperties(3000);
        }
        executor.startOutOfProcess(config, jmxProps);
    }

    protected NamedCache cacheViaExtend() {
        return getCache("config/extend-client-32001.xml", "foo");
    }

    protected NamedCache getCache(String configLocation, String cacheName) {
        ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(configLocation, getClass().getClassLoader());
        NamedCache cache = factory.ensureCache(cacheName, getClass().getClassLoader());
        addToShutdownList(cache);
        cache.size();//initialise it
        return cache;
    }

    private void addToShutdownList(NamedCache cache) {
        services.add(cache.getCacheService());
        caches.add(cache);
    }

    protected NamedCache getRemotePofCache(String name) {
        return new DefaultConfigurableCacheFactory("config/extend-client-32001-pof.xml").ensureCache(name, getClass().getClassLoader());
    }
}
