package com.benstopford.coherence.bootstrap.morecomplex.sizing.index;

import com.benstopford.coherence.bootstrap.structures.dataobjects.ByteArrayWrapper;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofByteObject;
import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.index.IndexSizer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.EqualsFilter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.benstopford.coherence.bootstrap.structures.uitl.HeapUtils.memoryUsedNow;


/**
 * Sample results (should be taken as a sanity check only:
 *
 ************ Running with cardinality = 1024 (repeated again at end of run for comparison) ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 1 values], SizeOf measured: 1,398,720B. JVM increase: -209,512B. Difference: 115%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 2 values], SizeOf measured: 1,025,536B. JVM increase: 935,968B. Difference: 9%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 4 values], SizeOf measured: 1,100,456B. JVM increase: 572,920B. Difference: 48%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 8 values], SizeOf measured: 1,472,616B. JVM increase: 838,936B. Difference: 43%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 16 values], SizeOf measured: 2,584,728B. JVM increase: 1,580,736B. Difference: 39%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 32 values], SizeOf measured: 4,380,904B. JVM increase: 3,038,808B. Difference: 31%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 64 values], SizeOf measured: 8,022,304B. JVM increase: 5,095,376B. Difference: 36%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 128 values], SizeOf measured: 16,415,912B. JVM increase: 11,496,416B. Difference: 30%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 256 values], SizeOf measured: 31,136,960B. JVM increase: 19,828,920B. Difference: 36%

 ************ Running with cardinality = 1 ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 1024 values], SizeOf measured: 250,832B. JVM increase: -1,425,368B. Difference: 668%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 2048 values], SizeOf measured: 401,384B. JVM increase: 1,700,480B. Difference: 324%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 4096 values], SizeOf measured: 738,192B. JVM increase: 1,213,456B. Difference: 64%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 8192 values], SizeOf measured: 1,241,296B. JVM increase: 1,544,784B. Difference: 24%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 16384 values], SizeOf measured: 2,418,880B. JVM increase: 2,378,760B. Difference: 2%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 32768 values], SizeOf measured: 4,248,200B. JVM increase: 2,804,096B. Difference: 34%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 65536 values], SizeOf measured: 7,905,968B. JVM increase: 4,919,176B. Difference: 38%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 131072 values], SizeOf measured: 16,321,608B. JVM increase: 11,067,752B. Difference: 32%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 1 [1 entries in index, each containing 262144 values], SizeOf measured: 31,028,808B. JVM increase: 19,735,160B. Difference: 36%

 ************ Running with cardinality = 128 ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 8 values], SizeOf measured: 395,312B. JVM increase: -1,247,312B. Difference: 416%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 16 values], SizeOf measured: 477,432B. JVM increase: 470,872B. Difference: 1%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 32 values], SizeOf measured: 781,728B. JVM increase: 943,104B. Difference: 21%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 64 values], SizeOf measured: 1,271,984B. JVM increase: 673,232B. Difference: 47%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 128 values], SizeOf measured: 2,438,032B. JVM increase: 2,020,280B. Difference: 17%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 256 values], SizeOf measured: 4,263,304B. JVM increase: 2,611,648B. Difference: 39%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 512 values], SizeOf measured: 7,922,448B. JVM increase: 5,560,336B. Difference: 30%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 1024 values], SizeOf measured: 16,333,664B. JVM increase: 10,532,840B. Difference: 36%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 128 [128 entries in index, each containing 2048 values], SizeOf measured: 31,040,864B. JVM increase: 21,134,544B. Difference: 32%

 ************ Running with cardinality = 256 ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 4 values], SizeOf measured: 536,624B. JVM increase: -1,595,928B. Difference: 397%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 8 values], SizeOf measured: 556,616B. JVM increase: 305,688B. Difference: 45%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 16 values], SizeOf measured: 828,144B. JVM increase: 462,048B. Difference: 44%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 32 values], SizeOf measured: 1,298,608B. JVM increase: 773,016B. Difference: 40%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 64 values], SizeOf measured: 2,459,872B. JVM increase: 113,008B. Difference: 95%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 128 values], SizeOf measured: 4,281,048B. JVM increase: 3,218,904B. Difference: 25%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 256 values], SizeOf measured: 7,934,736B. JVM increase: 4,914,720B. Difference: 38%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 512 values], SizeOf measured: 16,348,328B. JVM increase: 11,067,840B. Difference: 32%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 256 [256 entries in index, each containing 1024 values], SizeOf measured: 31,055,528B. JVM increase: 19,679,552B. Difference: 37%

 ************ Running with cardinality = 512 ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 2 values], SizeOf measured: 819,248B. JVM increase: -623,344B. Difference: 176%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 4 values], SizeOf measured: 708,168B. JVM increase: 1,878,552B. Difference: 165%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 8 values], SizeOf measured: 914,160B. JVM increase: 679,264B. Difference: 26%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 16 values], SizeOf measured: 1,351,856B. JVM increase: 876,192B. Difference: 35%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 32 values], SizeOf measured: 2,510,584B. JVM increase: 1,752,408B. Difference: 30%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 64 values], SizeOf measured: 4,309,712B. JVM increase: 2,337,832B. Difference: 46%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 128 values], SizeOf measured: 7,973,160B. JVM increase: 5,291,192B. Difference: 34%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 256 values], SizeOf measured: 16,370,856B. JVM increase: 10,546,248B. Difference: 36%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 512 [512 entries in index, each containing 512 values], SizeOf measured: 31,078,056B. JVM increase: 21,172,656B. Difference: 32%

 ************ Running with cardinality = 1024 ***********
 Ran: 1,024 x 1024B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 1 values], SizeOf measured: 1,398,344B. JVM increase: 14,120B. Difference: 99%
 Ran: 2,048 x 512B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 2 values], SizeOf measured: 1,025,120B. JVM increase: 1,168,496B. Difference: 14%
 Ran: 4,096 x 256B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 4 values], SizeOf measured: 1,100,040B. JVM increase: 893,120B. Difference: 19%
 Ran: 8,192 x 128B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 8 values], SizeOf measured: 1,472,200B. JVM increase: 1,024,432B. Difference: 30%
 Ran: 16,384 x 64B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 16 values], SizeOf measured: 2,584,312B. JVM increase: 1,727,136B. Difference: 33%
 Ran: 32,768 x 32B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 32 values], SizeOf measured: 4,380,904B. JVM increase: 2,996,896B. Difference: 32%
 Ran: 65,536 x 16B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 64 values], SizeOf measured: 8,022,304B. JVM increase: 5,087,680B. Difference: 37%
 Ran: 131,072 x 8B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 128 values], SizeOf measured: 16,415,912B. JVM increase: 11,374,704B. Difference: 31%
 Ran: 262,144 x 4B fields [1,024KB indexable data], Cardinality of 1024 [1024 entries in index, each containing 256 values], SizeOf measured: 31,136,960B. JVM increase: 19,819,992B. Difference: 36%

 */

/**
 * For predictable results it's best to add these settings to this test
 *
 * -Xmx256m -XX:+UseParallelOldGC ( and of course -javaagent:lib/SizeOf.jar  )
 *
 * In particular the concurrent collector has less predictable behaviour
 */
public class ShowInstrumentationInvocableIsAccurate extends ClusterRunner {
    public static final String config = "config/basic-invocation-service-pof-1.xml";

    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;
    private ValueExtractor extractor;
    byte[] fieldSavedForLater = null;

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


    private void run(byte[] data, int numberToAdd, int cardinality) throws Exception {
        extractor = new PofExtractor(ByteArrayWrapper.class, 1);

        //given
        NamedCache cache = initCache("test-cache");
        cache.removeIndex(extractor); //may have created in previous test
        cache.clear();
        addValuesToCache(cache, numberToAdd, Arrays.copyOf(data, data.length), cardinality);

        //when
        long before = memoryUsedNow();

        cache.addIndex(extractor, false, null);

        long after = memoryUsedNow();

        //then
        IndexSizer sizer = new IndexSizer(config);
        long coherenceSize = sizer.getTotal();

        System.out.printf("Ran: %,d x %sB fields [%,dKB indexable data], Cardinality of %s [%s entries in index, " +
                        "each containing %s values], SizeOf measured: %,dB. " +
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

    private NamedCache initCache(String name) {
        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(config, this.getClass().getClassLoader())
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
