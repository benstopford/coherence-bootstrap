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

    /**
     * Sample Output
     *
     ************ Running with cardinality = 1024 (unique) ***********
     Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 1 values], Coherence MBean measured: 1,111,491B. JVM increase: 1,194,120B. Difference: 7%
     Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 2 values], Coherence MBean measured: 589,824B. JVM increase: 1,036,512B. Difference: 76%
     Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 4 values], Coherence MBean measured: 327,680B. JVM increase: 990,928B. Difference: 202%
     Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 8 values], Coherence MBean measured: 196,608B. JVM increase: 1,006,832B. Difference: 412%
     Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 16 values], Coherence MBean measured: 131,072B. JVM increase: 1,499,512B. Difference: 1044%
     Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 32 values], Coherence MBean measured: 98,304B. JVM increase: 2,943,848B. Difference: 2895%
     Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 64 values], Coherence MBean measured: 81,920B. JVM increase: 5,224,656B. Difference: 6278%
     Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 128 values], Coherence MBean measured: 81,920B. JVM increase: 11,464,032B. Difference: 13894%
     Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 256 values], Coherence MBean measured: 65,536B. JVM increase: 19,811,552B. Difference: 30130%

     ************ Running with cardinality = 512 ***********
     Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 2 values], Coherence MBean measured: 557,056B. JVM increase: 1,275,872B. Difference: 129%
     Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 4 values], Coherence MBean measured: 294,912B. JVM increase: 558,928B. Difference: 90%
     Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 8 values], Coherence MBean measured: 163,840B. JVM increase: 1,228,736B. Difference: 650%
     Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 16 values], Coherence MBean measured: 98,304B. JVM increase: 817,696B. Difference: 732%
     Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 32 values], Coherence MBean measured: 65,536B. JVM increase: 883,000B. Difference: 1247%
     Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 64 values], Coherence MBean measured: 49,152B. JVM increase: 3,162,544B. Difference: 6334%
     Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 128 values], Coherence MBean measured: 40,960B. JVM increase: 5,095,888B. Difference: 12341%
     Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 256 values], Coherence MBean measured: 40,960B. JVM increase: 10,196,616B. Difference: 24794%
     Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 512 values], Coherence MBean measured: 32,768B. JVM increase: 21,426,392B. Difference: 65288%

     ************ Running with cardinality = 256 ***********
     Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 4 values], Coherence MBean measured: 278,528B. JVM increase: 1,126,040B. Difference: 304%
     Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 8 values], Coherence MBean measured: 147,456B. JVM increase: 356,176B. Difference: 142%
     Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 16 values], Coherence MBean measured: 81,920B. JVM increase: 848,800B. Difference: 936%
     Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 32 values], Coherence MBean measured: 49,152B. JVM increase: 776,896B. Difference: 1481%
     Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 64 values], Coherence MBean measured: 32,768B. JVM increase: 857,672B. Difference: 2517%
     Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 128 values], Coherence MBean measured: 24,576B. JVM increase: 2,843,808B. Difference: 11471%
     Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 256 values], Coherence MBean measured: 20,480B. JVM increase: 4,734,568B. Difference: 23018%
     Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 512 values], Coherence MBean measured: 20,480B. JVM increase: 11,227,408B. Difference: 54721%
     Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 1024 values], Coherence MBean measured: 16,384B. JVM increase: 19,893,600B. Difference: 121321%

     ************ Running with cardinality = 128 ***********
     Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 8 values], Coherence MBean measured: 139,264B. JVM increase: 423,096B. Difference: 204%
     Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 16 values], Coherence MBean measured: 73,728B. JVM increase: 344,064B. Difference: 367%
     Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 32 values], Coherence MBean measured: 40,960B. JVM increase: 502,232B. Difference: 1126%
     Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 64 values], Coherence MBean measured: 24,576B. JVM increase: 693,296B. Difference: 2721%
     Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 128 values], Coherence MBean measured: 16,384B. JVM increase: 1,245,056B. Difference: 7499%
     Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 256 values], Coherence MBean measured: 12,288B. JVM increase: 2,529,064B. Difference: 20482%
     Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 512 values], Coherence MBean measured: 10,240B. JVM increase: 5,224,896B. Difference: 50924%
     Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 1024 values], Coherence MBean measured: 10,240B. JVM increase: 10,527,264B. Difference: 102705%
     Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 2048 values], Coherence MBean measured: 8,192B. JVM increase: 21,150,776B. Difference: 258088%

     ************ Running with cardinality = 1 ***********
     Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 1024 values], Coherence MBean measured: 1,085B. JVM increase: 809,088B. Difference: 74470%
     Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 2048 values], Coherence MBean measured: 576B. JVM increase: 185,592B. Difference: 32121%
     Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 4096 values], Coherence MBean measured: 320B. JVM increase: 624,992B. Difference: 195210%
     Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 8192 values], Coherence MBean measured: 192B. JVM increase: 1,162,096B. Difference: 605158%
     Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 16384 values], Coherence MBean measured: 128B. JVM increase: 1,093,784B. Difference: 854419%
     Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 32768 values], Coherence MBean measured: 96B. JVM increase: 2,909,912B. Difference: 3031058%
     Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 65536 values], Coherence MBean measured: 80B. JVM increase: 4,893,936B. Difference: 6117320%
     Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 131072 values], Coherence MBean measured: 80B. JVM increase: 11,620,696B. Difference: 14525770%
     Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 262144 values], Coherence MBean measured: 64B. JVM increase: 19,847,904B. Difference: 31012250%
     *
     */


    @Test
    public void compareCoherenceIndexSizeToHeapDeltasUnique() throws Exception {
        runAll(1024); //field will be completely unique
    }

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
    public void compareCoherenceIndexSizeToHeapDeltasUniqueAgain() throws Exception {
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
                        "JVM increase: %,dB. Difference: %s%%\n",
                cache.size(),
                data.length,
                cache.size() * data.length / KB,
                cardinality,
                cardinality,
                measuredCardinality(cache),
                coherenceSize,
                (after - before),
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
