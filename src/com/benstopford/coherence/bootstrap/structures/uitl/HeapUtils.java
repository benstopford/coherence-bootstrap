package com.benstopford.coherence.bootstrap.structures.uitl;

public class HeapUtils {

    private static long start;

    public static long memoryUsedNow() throws InterruptedException {
        Runtime java = Runtime.getRuntime();
        System.gc();
        Thread.sleep(800l);
        return (java.totalMemory() - java.freeMemory());
    }

    public static void start() throws InterruptedException {
        start = memoryUsedNow();
    }

    public static long printMemoryUsed() throws InterruptedException {
        long now = memoryUsedNow();
        long used = now - start;
        System.out.printf("Heap consumed: %,dB [%,d->%,d]\n", used,start, now);
        return used;
    }
}
