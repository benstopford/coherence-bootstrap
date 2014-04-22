package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.IndexSizer;
import com.benstopford.coherence.bootstrap.structures.dataobjects.ByteArrayWrapper;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofByteObject;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.EqualsFilter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import static com.benstopford.coherence.bootstrap.structures.uitl.HeapUtils.memoryUsedNow;


/**
 * For predictable results it's best to add these settings to this test
 *
 * -Xmx256m -XX:+UseParallelOldGC
 *
 * In particular the concurrent collector has less predictable behaviour
 */
public class CoherenceIndexSizeMbeanIsInaccurate extends ClusterRunner {
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;
    private PofExtractor extractor;
    byte[] fieldSavedForLater = null;

    @Test
    public void compareCoherenceIndexSizeToHeapDeltasIdentical() throws Exception {
        runAll(1); //1 means all entries will have the same field value
    }

    @Test
    public void compareCoherenceIndexSizeToHeapDeltasMixed1() throws Exception {
        runAll(128); //there will be 128 unique fields with 1024/128 entries in each
    }

    @Test
    public void compareCoherenceIndexSizeToHeapDeltasMixed2() throws Exception {
        runAll(256); //there will be 1024 unique fields with 1024/1024 (1024) entries in each
    }

    @Test
    public void compareCoherenceIndexSizeToHeapDeltasMixed3() throws Exception {
        runAll(512); //there will be 512 * 1024 unique fields with 1024/(512 * 1024) entries in each
    }

    @Test
    public void compareCoherenceIndexSizeToHeapDeltasUnique() throws Exception {
        runAll(1024); //field will be completely unique
    }

    private void runAll(int cardinality) throws Exception {
        int totalBytes = 1024 * 1024;
        System.out.println("************ Running with cardinality = " + cardinality + " ***********");
        run(new byte[1024], totalBytes / 1024, cardinality);
        run(new byte[512], totalBytes / 512, cardinality);
        run(new byte[256], totalBytes / 256, cardinality);
        run(new byte[128], totalBytes / 128, cardinality);
        run(new byte[64], totalBytes / 64, cardinality);
        run(new byte[32], totalBytes / 32, cardinality);
        run(new byte[16], totalBytes / 16, cardinality);
        run(new byte[8], totalBytes / 8, cardinality);
        run(new byte[4], totalBytes / 4, cardinality);
    }

    @Test
    public void methodUsedHereForMeasuringMemoryAllocationIsAccurate() throws InterruptedException {
        List all = new ArrayList();

        int block = 5 * MB;

        long initialMemory = memoryUsedNow();
        System.out.println("Initial memory is " + initialMemory);

        while (all.size() * block <= 60 * MB) {
            all.add(new byte[block]);
            long now = memoryUsedNow();
            System.out.println("Measured memory as " + now);
            assertWithinTolerance(all.size() * block, (now - initialMemory), 0.10);
        }
    }

    private void run(byte[] data, int numberToAdd, int cardinality) throws Exception {
        extractor = new PofExtractor(ByteArrayWrapper.class, 1);

        //given
        startLocalJMXServer(40001);
        NamedCache cache = initCache("test-cache");
        cache.removeIndex(extractor); //may have created in previous test
        cache.clear();
        addValuesToCache(cache, numberToAdd, data, cardinality);

        //when
        long before = memoryUsedNow();

        cache.addIndex(extractor, false, null);

        long after = memoryUsedNow();

        //then
        boolean showLogging = false;
        long coherenceSize = new IndexSizer().sizeAllIndexes(40001, showLogging);

        System.out.printf("Ran: %,d x %sB fields [%,dKB indexable data], Cardinality of %s [%s entries in index, " +
                        "each containing %s values], Coherence MBean measured: %,dB. " +
                        "JVM increase: %,dKB. Difference: %s%%\n",
                cache.size(),
                data.length,
                cache.size() * data.length / KB,
                cardinality,
                cardinality,
                measuredCardinality(cache),
                coherenceSize,
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

    private void addValuesToCache(NamedCache cache, int numberToAdd, byte[] bytes, int cardinality) {
        Map all = new HashMap();
        HashSet uniqueFields = new HashSet();
        for (int i = 0; i < numberToAdd; i++) {
            byte[] data = overrideUniquenessBasedOnCardinality(bytes, i, cardinality);
            all.put(i, new PofByteObject(data));
            uniqueFields.add(data);
        }
        cache.putAll(all);

        doubleCheckCardinality(cache, numberToAdd, cardinality);
    }

    private void doubleCheckCardinality(NamedCache cache, double numberToAdd, int cardinality) {
        int measuredCardinality = measuredCardinality(cache);
        Assert.assertEquals((int) Math.ceil(numberToAdd / cardinality), measuredCardinality);
    }

    private int measuredCardinality(NamedCache cache) {
        return cache.keySet(new EqualsFilter(extractor, new ByteArrayWrapper(fieldSavedForLater))).size();
    }

    private byte[] overrideUniquenessBasedOnCardinality(byte[] bytes, int counter, int cardinality) {

        //create a prefix that guarantees cardinality
        byte[] counterAsBytes = ByteBuffer.allocate(4).putInt(counter % cardinality).array();

        checkLength(bytes, counterAsBytes);

        //overlay the prefix
        bytes = Arrays.copyOf(counterAsBytes, bytes.length);

        fieldSavedForLater = bytes;
        return bytes;
    }

    private void checkLength(byte[] bytes, byte[] counterAsBytes) {
        if (bytes.length < counterAsBytes.length) {
            throw new IllegalArgumentException(String.format("Can't be less that %s bytes", counterAsBytes.length));
        }
    }

    @Before
    public void before() throws Exception {
        System.gc();
        Thread.sleep(150l);
    }
}
