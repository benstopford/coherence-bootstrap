package com.benstopford.coherence.bootstrap.structures.uitl;

public class HeapUtils {

    public static long memoryUsedNow() throws InterruptedException {
        Runtime java = Runtime.getRuntime();
        System.gc();
        Thread.sleep(200l);
        return (java.totalMemory() - java.freeMemory());
    }

}
