package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.dataobjects.ComplexPofObject;
import com.tangosol.io.pof.SimplePofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.extractor.PofExtractor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.*;
import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.TimeFormat.ns;


public class PofEfficiency {
    public static int objectCount;
    static byte[] padding = new byte[10];

    enum Type {start, end, random};

    /**
     * Look at performance of pof-extractors in comparison to deserilising the whole object
     * It is best to use the below memory settings and a larger objectCount to get reliable results:
     * -Xmx8g -Xms8g
     * (and tweaking these for fun) -XX:NewSize=5g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     */
    @Test
    public void whenDoesPofExtractionStopsBeingMoreEfficient() throws InterruptedException {

        padding = new byte[64];
        objectCount = 1000000; //TODO: Set to ~1,000,000 for accurate test
        int fieldCount = 50;

        //warm up JVM
        testFullObjectDeserialiation(fieldCount, false);
        gc();

        testFullObjectDeserialiation(fieldCount, true);
        gc();

        testPofExtractionOfNAttributes(fieldCount, 5, Type.start);
        gc();

        testPofExtractionOfNAttributes(fieldCount, 5, Type.end);
        gc();

        testPofExtractionOfNAttributes(fieldCount, 5, Type.random);


        System.out.println("----Break Even Points (on my machine) for objects with different numbers of fields----");
        System.out.println("- for objects of 5 fields the break even point is deserialising 2 fields with pof");
        System.out.println("- for objects of 20 fields the break even point is deserialising 4 fields with pof");
        System.out.println("- for objects of 50 fields the break even point is deserialising 5 fields with pof");
        System.out.println("- for objects of 100 fields the break even point is deserialising 7 fields with pof");
        System.out.println("- for objects of 200 fields the break even point is deserialising 9 fields with pof");
        System.out.println("----Varying Field Size----");
        System.out.println("- the size of the field (adjusted with fieldPadding) doesn't affect performance much");
        System.out.println("----Varying Field Size----");
        System.out.println("----Conclusion----");
        System.out.println("Using pof extractors in filters and for indexing is a good idea but if you are doing complex operations in the cache that require access to a broad range of fields it may actually be more efficient to deserialise the whole object");
    }


    public static void testFullObjectDeserialiation(int numberOfFieldsOnObject, boolean print) {
        SimplePofContext context = new SimplePofContext();
        List<Binary> data = new ArrayList<Binary>();

        //create a test object
        ComplexPofObject o = createPofObject(numberOfFieldsOnObject);

        //create a 'cache' of n binary versions of that object
        for (int i = 0; i < objectCount; i++) {
            context.registerUserType(2001, ComplexPofObject.class, ComplexPofObject.serializer);
            Binary binary = ExternalizableHelper.toBinary(o, context);
            data.add(binary);
        }

        //deserialise them all
        start();
        for (Binary b : data) {
            ExternalizableHelper.fromBinary(b, context);
        }
        Took end = end();

        Double d = Double.valueOf(end.average(data.size(), ns));

        if (print)
            System.out.printf("On average full deserialisation of a %s field object took %sns\n", numberOfFieldsOnObject, d);
    }

    public static void testPofExtractionOfNAttributes(int numberOfFieldsOnObject, int numberOfFieldsToExract, Type entryPoint) {
        SimplePofContext context = new SimplePofContext();
        List<Binary> data = new ArrayList<Binary>();

        //create a test object
        ComplexPofObject o = createPofObject(numberOfFieldsOnObject);

        //create a 'cache' of n binary versions of that object
        for (int i = 0; i < objectCount; i++) {
            context.registerUserType(2001, ComplexPofObject.class, ComplexPofObject.serializer);
            Binary binary = ExternalizableHelper.toBinary(o, context);
            data.add(binary);
        }

        int[] randomPofIndexes = new int[numberOfFieldsToExract];
        Random random = new Random();
        for (int i = 0; i < randomPofIndexes.length; i++) {
            randomPofIndexes[i] = random.nextInt(numberOfFieldsOnObject);
        }


        //PofExtract some number of fields from the start/end of stream
        start();
        for (Binary b : data) {
            for (int i = 0; i < numberOfFieldsToExract; i++) {
                if (entryPoint == Type.end) {
                    extract(context, b, numberOfFieldsOnObject - i);
                } else if (entryPoint == Type.start) {
                    extract(context, b, i);
                } else if (entryPoint == Type.random) {
                    extract(context, b, randomPofIndexes[i]);
                }
            }
        }
        Took took = end();
        System.out.printf("On average pof extraction of %s %s fields of %s took %sns\n",
                entryPoint == Type.end ? "last" : entryPoint == Type.start ? "first" : "random",
                numberOfFieldsToExract, numberOfFieldsOnObject, took.average(data.size(), ns));
    }

    private static void extract(SimplePofContext context, Binary b, int index) {
        PofExtractor pofExtractor = new PofExtractor(null, new SimplePofPath(index));
        PofValue value = PofValueParser.parse(b, context);
        pofExtractor.getNavigator().navigate(value).getValue();
    }

    private static ComplexPofObject createPofObject(int numberOfFields) {
        Object[] fields = new Object[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            fields[i] = String.valueOf(padding) + i;
        }
        return new ComplexPofObject(fields);
    }


    /**
     * Ignore GC via -Xms8g -Xmx8g -XX:NewSize=4g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     */
    @Test
    public void howMuchSlowerIsPullingDataFromTheEndOfTheStreamRatherThanTheStart() throws InterruptedException {

        List<Integer> fields = new ArrayList<Integer>();
        for (int i = 0; i < 1024; i++) {
            fields.add(Integer.MAX_VALUE);
        }

        ComplexPofObject o = new ComplexPofObject(fields);

        for (int pofFieldPosition = 1; pofFieldPosition <= 1024; pofFieldPosition = pofFieldPosition * 2) {
            serialisationTime(new SimplePofPath(pofFieldPosition), o);
            gc();
        }
    }

    private void gc() throws InterruptedException {
        System.gc();
        Thread.sleep(1000);
    }

    private void serialisationTime(SimplePofPath navigator, ComplexPofObject o) {
        int total = 1 * 1000 * 1000, count = total; //10 million is best, reduced to keep test times down

        SimplePofContext context = new SimplePofContext();
        context.registerUserType(2001, ComplexPofObject.class, ComplexPofObject.serializer);
        PofExtractor pofExtractor = new PofExtractor(ComplexPofObject.class, navigator);

        Binary b = ExternalizableHelper.toBinary(o, context);

        start();
        while (count-- > 0) {
            PofValue value = PofValueParser.parse(b, context);
            pofExtractor.getNavigator().navigate(value).getValue();
        }
        end().printAverage(total, TimeFormat.ns, "Average extraction time for navigator " + navigator.toString() + " is ");
    }



}


