package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.dataobjects.LengthyPofObject;
import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofHelper;
import com.tangosol.io.pof.SimplePofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.extractor.PofExtractor;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class PofEfficiency {
    public static int objectCount;
    static byte[] data = new byte[50];
    private SimplePofContext context = null;

    enum Type {start, end, random}

    ;

    /**
     * Break-Even Points (on my machine) for objects with different numbers of fields
     * - for objects of 5 fields the break even point is deserialising 2 fields with pof
     * - for objects of 20 fields the break even point is deserialising 4 fields with pof
     * - for objects of 50 fields the break even point is deserialising 5 fields with pof
     * - for objects of 100 fields the break even point is deserialising 7 fields with pof
     * - for objects of 200 fields the break even point is deserialising 9 fields with pof
     * <p/>
     * Varying Field Size:
     * - the size of the field (adjusted with fieldPadding) doesn't affect performance much
     * <p/>
     * Look at performance of pof-extractors in comparison to deserilising the whole object
     * It is best to use the below memory settings and a larger objectCount to get reliable results:
     * -Xmx8g -Xms8g
     * (and tweaking these for fun) -XX:NewSize=5g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     */
    @Test
    public void compareFullObjectDeserialisationWithPullingDataFromStreamWithPof() throws InterruptedException {
        data = new byte[64];
        objectCount = 100000; //TODO: Set to ~1,000,000 for accurate test - just set low for memory/time reasons
        int fieldCount = 50;
        int numberOfFieldsToExract = 5; //5 is the approximate break even point for a 50 field object

        List<Binary> cache = listOfBinaryObjectsOfSize(createPofObject(fieldCount));

        //warm up JVM
        testFullObjectDeserialiation(cache, false, fieldCount);
        gc();

        testFullObjectDeserialiation(cache, true, fieldCount);
        gc();

        testPofExtractionOfNAttributes(cache, numberOfFieldsToExract, fieldCount, Type.start);
        gc();

        testPofExtractionOfNAttributes(cache, numberOfFieldsToExract, fieldCount, Type.end);
        gc();

        testPofExtractionOfNAttributes(cache, numberOfFieldsToExract, fieldCount, Type.random);
    }

    private List<Binary> listOfBinaryObjectsOfSize(LengthyPofObject pofObject) {
        List<Binary> cache = new ArrayList<Binary>();

        //create a 'cache' of n binary versions of that object
        context = new SimplePofContext();
        for (int i = 0; i < objectCount; i++) {
            context.registerUserType(2001, LengthyPofObject.class, LengthyPofObject.serializer);
            Binary binary = ExternalizableHelper.toBinary(pofObject, context);
            cache.add(binary);
        }
        return cache;
    }


    public void testFullObjectDeserialiation(List<Binary> data, boolean print, int fieldCount) {
        start();
        for (Binary b : data) {
            ExternalizableHelper.fromBinary(b, context);
        }
        Took end = end();

        if (print)
            System.out.printf("On average full deserialisation of a %s field object took %,dns\n", fieldCount, end.average(data.size()));
    }

    public void testPofExtractionOfNAttributes(List<Binary> cache, int numberOfFieldsToExract, int fieldCount, Type entryPoint) {

        int[] randomPofIndexes = new int[fieldCount];
        Random random = new Random();
        for (int i = 0; i < randomPofIndexes.length; i++) {
            randomPofIndexes[i] = random.nextInt(fieldCount);
        }

        //PofExtract some number of fields from the start/end of stream
        start();
        for (Binary b : cache) {
            PofValue value = PofValueParser.parse(b, context);
            for (int i = 1; i <= numberOfFieldsToExract; i++) {
                if (entryPoint == Type.end) {
                    extract(value, fieldCount - i);
                } else if (entryPoint == Type.start) {
                    extract(value, i);
                } else if (entryPoint == Type.random) {
                    int ind = randomPofIndexes[i];
                    extract(value, ind);
                }
            }
        }
        Took took = end();
        System.out.printf("On average pof extraction of %s %s fields of %s took %,dns\n",
                entryPoint == Type.end ? "last" : entryPoint == Type.start ? "first" : "random",
                numberOfFieldsToExract, fieldCount, took.average(Long.valueOf(cache.size())));
    }

    private void extract(PofValue value, int index) {
        PofExtractor pofExtractor = new PofExtractor(null, new SimplePofPath(index));
        byte[] byteArray = pofExtractor.getNavigator().navigate(value).getByteArray();
        if (byteArray.length != data.length) fail("oops");
    }

    private LengthyPofObject createPofObject(int numberOfFields) {
        Object[] fields = new Object[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            fields[i] = data;
        }
        return new LengthyPofObject(fields);
    }


    /**
     * Ignore GC via -Xms8g -Xmx8g -XX:NewSize=4g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     */
    @Test
    public void howMuchSlowerIsPullingDataFromTheEndOfTheStreamRatherThanTheStart() throws InterruptedException, IOException {
        int maxFieldsToTraverse = 1024;
        int iterationsInTest = 1 * 100 * 1000;

        List<Object> fields = new ArrayList<Object>();
        for (int i = 1; i <= maxFieldsToTraverse; i++) {
            fields.add(i);
        }

        LengthyPofObject obj = new LengthyPofObject(fields);

        System.out.println("\n***Test WITHOUT PofValueParsing for each extraction***");
        for (int pofFieldPosition = 1; pofFieldPosition <= maxFieldsToTraverse; pofFieldPosition = pofFieldPosition * 2) {
            measurePofNavigationTime(
                    pofFieldPosition,
                    obj,
                    iterationsInTest,
                    false);
            gc();
        }

        System.out.println("\n***Test WITH PofValueParsing for each extraction***");
        for (int pofFieldPosition = 1; pofFieldPosition <= maxFieldsToTraverse; pofFieldPosition = pofFieldPosition * 2) {
            measurePofNavigationTime(
                    pofFieldPosition,
                    obj,
                    iterationsInTest, true);
            gc();
        }


        System.out.println("\n***Test baseline using just skipPackedInts calls***");
        for (int pofFieldPosition = 1; pofFieldPosition <= maxFieldsToTraverse; pofFieldPosition = pofFieldPosition * 2) {
            measurePofNavigationTimeWithSkipsOnly(
                    pofFieldPosition,
                    obj,
                    iterationsInTest);
            gc();
        }
    }

    private void gc() throws InterruptedException {
        System.gc();
        Thread.sleep(1000);
    }

    private void measurePofNavigationTime(int path, LengthyPofObject o, int count, boolean includeParsing) {
        SimplePofPath simplePofPath = new SimplePofPath(path);

        SimplePofContext context = new SimplePofContext();
        context.registerUserType(2001, LengthyPofObject.class, LengthyPofObject.serializer);
        PofExtractor pofExtractor = new PofExtractor(Integer.class, simplePofPath);

        Binary b = ExternalizableHelper.toBinary(o, context);

        start();
        int i = count, total = 0;
        PofValue value = PofValueParser.parse(b, context);
        while (i-- > 0) {
            if (includeParsing) value = PofValueParser.parse(b, context);
            total += (Integer) pofExtractor.getNavigator().navigate(value).getValue();
        }
        end().printAverage(count, NANOSECONDS, "Average extraction time for navigator " + simplePofPath.toString() + " is ");

        assertThat(total, is(count * path));
    }

    /**
     * This mechanism does the parsing manually (as a control)
     */
    private void measurePofNavigationTimeWithSkipsOnly(int fieldPosition, LengthyPofObject o, int count) throws IOException {

        SimplePofContext context = new SimplePofContext();
        context.registerUserType(2001, LengthyPofObject.class, LengthyPofObject.serializer);
        Binary b = ExternalizableHelper.toBinary(o, context);

        start();
        int i = count;
        int total = 0;
        while (i-- > 0) {
            ReadBuffer.BufferInput stream = b.getBufferInput();
            PofHelper.skipPackedInts(stream, 6); //skip object header with ints for: header,type,version,pofid,type,fieldcount
            PofHelper.skipPackedInts(stream, (fieldPosition - 1) * 3); //skip to the field we want (each field contains id,type,value)

            stream.readPackedInt();//pofid
            stream.readPackedInt();//type
            total += stream.readPackedInt();
        }
        end().printAverage(count, NANOSECONDS, "Average extraction time for field position " + fieldPosition + " is ");

        assertThat(total, is(count * fieldPosition));
    }
}


