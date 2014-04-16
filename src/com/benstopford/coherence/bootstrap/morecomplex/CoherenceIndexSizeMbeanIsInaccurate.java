package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.IndexSizer;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofByteObject;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.PofExtractor;
import junit.framework.Assert;
import org.junit.Test;

import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

public class CoherenceIndexSizeMbeanIsInaccurate extends ClusterRunner{
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;

    @Test
    public void compareCoherenceIndexSizeToHeapDeltas() throws Exception {
        int iterations = 1024 * 1024;
        run(new byte[1024], iterations / 1024);
        run(new byte[512], iterations / 512);
        run(new byte[256], iterations / 256);
        run(new byte[128], iterations / 128);
        run(new byte[64], iterations / 64);
        run(new byte[32], iterations / 32);
        run(new byte[16], iterations / 16);
        run(new byte[8], iterations / 8);
        run(new byte[4], iterations / 4);
        System.out.println("***************************");
    }

    @Test
    public void methodUsedHereForMeasuringMemoryAllocationIsAccurate() throws InterruptedException {
        List allBytes = new ArrayList();
        int block = 5;

        long initialMemory = memoryUsedNow();

        while (allBytes.size() * block <= 60) {
            allBytes.add(new byte[block * MB]);
            assertWithinTolerance(allBytes.size() * block * MB, (memoryUsedNow() - initialMemory), 0.1);
        }
    }

    private void run(byte[] data, int numberToAdd) throws Exception {
        PofExtractor firstField = new PofExtractor(null, 1);

        //given
        startLocalJMXServer(40001);
        NamedCache cache = initCache("cache");
        cache.removeIndex(firstField); //may have created in previous test
        cache.clear();
        addValuesToCache(cache, numberToAdd, data);

        //when
        long before = memoryUsedNow();

        cache.addIndex(firstField, false, null);

        long after = memoryUsedNow();

        //then
        long coherenceSize = new IndexSizer().sizeAllIndexes(40001, false);

        System.out.printf("With %,d x %sB fields [%,dKB of indexed data] Coherence reported an index size of %,dKB. " +
                        "The JVM increased by %,dKB. %% difference %s%%\n",
                cache.size(),
                data.length,
                cache.size() * data.length / KB,
                coherenceSize / KB,
                (after - before) / KB,
                Math.abs((100 - Math.round((double) (after - before) / coherenceSize * 100)))
        );
    }

    private void startLocalJMXServer(int port) throws IOException {
        if (serverRunning(port)) {
            return;
        }

        String url = "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi";
        JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL(url),
                null,
                ManagementFactory.getPlatformMBeanServer()
        ).start();
    }

    private boolean serverRunning(int port) {
        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException justMeansItWasCreatedAlready) {
            return true;
        }
        return false;
    }

    private NamedCache initCache(String name) {
        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-cache-with-pof.xml", this.getClass().getClassLoader())
                .ensureCache(name, this.getClass().getClassLoader());
        return cache;
    }

    private long memoryUsedNow() throws InterruptedException {
        Runtime java = Runtime.getRuntime();
        System.gc();
        Thread.sleep(200l);
        return (java.totalMemory() - java.freeMemory());
    }

    private void addValuesToCache(NamedCache cache, int numberToAdd, byte[] bytes) {
        Map all = new HashMap();
        HashSet uniqueFields = new HashSet();
        for (int i = 0; i < numberToAdd; i++) {
            // Ensure that all values will be different
            // so that index created will not just be for one value
            byte[] data = makeByteFieldUniqueUsingCounterOverlay(bytes, i);
            all.put(i, new PofByteObject(data));
            uniqueFields.add(data);
        }
        cache.putAll(all);
        Assert.assertEquals(uniqueFields.size(), numberToAdd);//check all fields in PofByteObject are unique
    }

    private byte[] makeByteFieldUniqueUsingCounterOverlay(byte[] bytes, int counter) {
        byte[] counterAsBytes = ByteBuffer.allocate(4).putInt(counter).array();

        if (bytes.length < counterAsBytes.length) {
            throw new IllegalArgumentException(String.format("Can't be less that %s bytes", counterAsBytes.length));
        }

        byte[] bytesOut = new byte[bytes.length];
        for (int i = 0; i < counterAsBytes.length; i++) {
            bytesOut[i] = counterAsBytes[i];
        }

        return bytesOut;
    }

}
