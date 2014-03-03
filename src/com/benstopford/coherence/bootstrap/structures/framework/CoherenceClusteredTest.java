package com.benstopford.coherence.bootstrap.structures.framework;

import com.rbs.hpcg.util.StreamGobbler;
import com.tangosol.net.*;
import functional.fixtures.SizableObjectFactory;
import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * BTS, 07-Dec-2007
 * <p/>
 * EDIT 2014: This test framework predates the work done by the Coherence team to internalise this kind of testing
 * into a single JVM and multiple classloaders.Credit for this excellent pattern goes to Andrew (Orange Pheonix) Wilson
 * <p/>
 * I strongly suggest that, in your own code, you use the LittleGrid. See JK's post here:
 * http://thegridman.com/coherence/oracle-coherence-testing-with-oracle-tools/
 */
public abstract class CoherenceClusteredTest extends TestCase {
    private static final String SEPERATOR = System.getProperty("path.separator");
    protected static final ClassLoader CLASS_LOADER = CoherenceClusteredTest.class.getClassLoader();
    protected static final String MULTICAST_ADDRESS_1 = "239.255.12.30";
    protected static final String CLUSTER_NAME = "com.rbs.hpc.gettingstarted";
    protected static final String BASIC_CACHE_XML = "config/basic-cache.xml";
    protected static final String CLUSTER_PORT = "1234";
    protected static final String LOGGING_LEVEL = "9";
    protected static final String TTL = "0";
    private final ArrayList<Process> runningProcesses = new ArrayList<Process>();


    protected static void setDefaultProperties() {
        System.setProperty("tangosol.coherence.clusteraddress", MULTICAST_ADDRESS_1);
        System.setProperty("tangosol.coherence.clusterport", CLUSTER_PORT);
        System.setProperty("tangosol.coherence.cluster", CLUSTER_NAME);
        System.setProperty("tangosol.coherence.cacheconfig", BASIC_CACHE_XML);
        System.setProperty("tangosol.coherence.log.level", LOGGING_LEVEL);
        System.setProperty("tangosol.coherence.ttl", TTL);
    }

    protected Process startOutOfProcess(String config) throws IOException, InterruptedException {
        return startOutOfProcess(config, "", "");
    }

    protected Process startOutOfProcess(String config, String classPathAdditions, String propertiesAdditions) throws IOException, InterruptedException {
        String command = "java -Xms64m -Xmx128m -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails " +
                "-Dtangosol.coherence.ttl=" + TTL + " " +
                "-Dtangosol.coherence.clusterport=" + CLUSTER_PORT + " " +
                "-Dtangosol.coherence.cluster=" + CLUSTER_NAME + " " +
                "-Dtangosol.coherence.clusteraddress=" + MULTICAST_ADDRESS_1 + " " +
                "-Dtangosol.coherence.log.level=" + LOGGING_LEVEL + " " +
                "-Dtangosol.coherence.cacheconfig=" + config + " " +
                "-Dcom.benstopford.extend.port=" + System.getProperty("com.benstopford.extend.port") + " " +
                "-Dcom.benstopford.extend.port2=" + System.getProperty("com.benstopford.extend.port2") + " " + getCoherenceJMXProperties() + " " +
                propertiesAdditions + " " +
                "-cp classes" + SEPERATOR + "lib/coherence-utils.jar" + SEPERATOR + "config" +
                classPathAdditions + SEPERATOR + parse(System.getProperty("java.class.path")) + " " +
                "com.tangosol.net.DefaultCacheServer";

        System.out.println("Spawning Coherence Process: " + config);
        Process process = Runtime.getRuntime().exec(command);
        startLogging(process);
        checkForSuccesfulStart(command, process);

        return process;
    }

    private String parse(String property) {
        //intelij adds crap onto the classpath :(
        String[] split = property.split(" ");
        String found = "";
        for(String s:split){
            if(s.contains("coherence")){ //hack to identify the actual entry for the classpath
                found = s;
            }
        }

        return found;
    }

    private void startLogging(Process process) throws FileNotFoundException {
        String name = getClass().getSimpleName() + String.valueOf(System.currentTimeMillis());
        new StreamGobbler(process, process.getErrorStream(), new File("log"), "err." + name).gobble();
        new StreamGobbler(process, process.getInputStream(), new File("log"), "out." + name).gobble();
    }

    private void checkForSuccesfulStart(String command, Process process) throws InterruptedException {
        try {
            process.exitValue();
            throw new RuntimeException("Cache server is not running " + command);
        } catch (Exception hopedFor) {
        }
        runningProcesses.add(process);
        Thread.sleep(5000);
    }


    protected void setUp() throws Exception {
//        removeCoherenceLogging();
        super.setUp();
        deleteContentsOfLogDir();
        new PersistentPortTracker().incrementExtendPort();
        setDefaultProperties();
    }

    public static void removeCoherenceLogging() {
        System.out.println();
        System.out.println("********************  COHERENCE LOGGING HAS BEEN REMOVED  *******************");
        System.out.println("**********(enable in "+CoherenceClusteredTest.class.getSimpleName()+".removeCoherenceLogging())********");
        System.out.println();
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //ignore
            }
        }));
    }

    private void deleteContentsOfLogDir() {
        File[] logs = new File("log").listFiles();
        if (logs != null) {
            for (File log : logs) {
                log.delete();
            }
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        shutdownServices();
        releaseLocalCacheResources();
        CacheFactory.getCluster().shutdown();
        CacheFactory.shutdown();
        killOpenCoherenceProcesses();
        waitForAllKilledMembersToTimeOut();
        System.out.println("log files for remote nodes are available in log directory");
    }

    private void waitForAllKilledMembersToTimeOut() throws InterruptedException {
        while (CacheFactory.ensureCluster().getMemberSet().size() > 1) {
            System.out.println("Waiting for " + CacheFactory.ensureCluster().getMemberSet().size() + " active cluster members to be deregistered");
            System.out.println(CacheFactory.ensureCluster().getMemberSet());
            Thread.sleep(1000);
        }
    }

    private void killOpenCoherenceProcesses() {
        for (Process process : runningProcesses) {
            System.out.println("killing process " + process);
            process.destroy();
            while (true) {
                try {
                    process.exitValue();
                    break;
                } catch (IllegalThreadStateException mustStillBeRunning) {
                }
            }
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
        return getCache(BASIC_CACHE_XML, name);
    }

    protected void startOutOfProcessWithJMX(String config) throws IOException, InterruptedException {
        startOutOfProcess(config, "", getJMXProperties(3000));
    }

    private static String getJMXProperties(int jmx_port) {
        return getSunJMXProperties(jmx_port);
    }

    private static String getCoherenceJMXProperties() {
        return "" +
                "-Dtangosol.coherence.management=all " +
                "-Dtangosol.coherence.management.remote=true";
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
            cache.put(prefix + Integer.toString(i), new SizableObjectFactory().buildObject(1000));
        }
        System.out.println("added " + mbToAdd + "MB");
    }

    protected NamedCache getRemoteCache() {
        return new DefaultConfigurableCacheFactory("config/extend-client-32001.xml").ensureCache("foo", getClass().getClassLoader());
    }

    protected void startBasicCacheProcess() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-cache.xml", "", "");
    }

    protected void startBasicCacheProcessWithJMX() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-cache.xml", "", getJMXProperties(3000));
    }

    protected void startDataDisabledExtendProxy() throws IOException, InterruptedException {
        startOutOfProcess("config/basic-extend-enabled-cache-32001.xml", "", "-Dtangosol.coherence.distributed.localstorage=false");
    }

    protected void startExtendEnabledDataNode(boolean enableJMX, String config) throws IOException, InterruptedException {
        String jmxProps = "";
        if (enableJMX) {
            jmxProps = getJMXProperties(3000);
        }
        startOutOfProcess(config, "", jmxProps);
    }

    HashSet<CacheService> services = new HashSet<CacheService>();
    HashSet<NamedCache> caches = new HashSet<NamedCache>();

    protected void addToShutdownList(NamedCache cache) {
        services.add(cache.getCacheService());
        caches.add(cache);
    }

    protected NamedCache getCache(String configLocation, String cacheName) {
        ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(configLocation, getClass().getClassLoader());
        NamedCache cache = factory.ensureCache(cacheName, getClass().getClassLoader());
        addToShutdownList(cache);
        cache.size();//initialise it
        return cache;
    }

    protected NamedCache connectOverExtend() {
        return getCache("config/extend-client-32001.xml", "foo");
    }

    private long start = 0;

    protected void startTimer() {
        start = System.currentTimeMillis();
    }

    protected long took() {
        long took = System.currentTimeMillis() - start;
        start = 0;
        return took;
    }

}
