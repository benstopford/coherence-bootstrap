package com.benstopford.coherence.bootstrap.structures.util;

/**
 * It's tricky measuring heap sizes in tests as the garbage collector can be unpredictable.
 * To get consistent results it is best to ensure that the heap increase in your test
 * is much larger then the resident size of the heap at the start.
 * <p/>
 * So for example adding 2k of objects and measuring the size will rarely give accurate
 * results regardless of how and when you GC. Better to ensure you are adding 10+ MB of
 * objects between the start and end of the measurement.
 * <p/>
 * Prefer relatively small heaps (the default). If things become unpredictable try:
 * Ensuring the heap is small: -Xmx64m
 * Turning on verbose gc to see what is going on: -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
 * Turn off concurrent collection: -XX:+UseParallelOldGC
 */

public class HeapUtils {

    private static long start;

    public static long memoryUsedNow() throws InterruptedException {
        Runtime java = Runtime.getRuntime();
        System.gc();
        Thread.sleep(800l);
        long now = java.totalMemory() - java.freeMemory();
//        System.out.printf("Heap now: %,dB [%,d-%,d]\n", now, java.totalMemory(), java.freeMemory());
       // GcInformation.printGCInfo();
        return now;
    }

    public static void start() throws InterruptedException {
        start = memoryUsedNow();
    }

    public static long printMemoryUsed() throws InterruptedException {
        long now = memoryUsedNow();
        long used = now - start;
        System.out.printf("Heap consumed: %,dB [%,d->%,d]\n", used, start, now);
        return used;
    }


}
