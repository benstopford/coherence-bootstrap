package com.benstopford.coherence.bootstrap.performance;


import sun.jvm.hotspot.utilities.Assert;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Simple class that baselines the single thread performance of reads and writes to the local drive
 */
public class DiskBaseline {

    public static final String file = "/tmp/data.txt";

    public static void main(String[] args) throws IOException {
        new DiskBaseline().diskControlTest();
    }

    public void diskControlTest() throws IOException {
        new File(file).delete();

        long datasize = 5 * 1024L * 1024L;//CHANGE ME!
        long write = write(file, datasize);
        long read = read("/tmp/data.txt", datasize);

        Assert.that(write == read, "checksums should match");
        Assert.that(write != 0, "checksums should not be zero");
    }

    private long write(String fileName, long amount) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        long checksum = 0;
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[4 * 1024];
        int pos = 0;
        for (long i = 0; i < amount; i++) {
            byte b = (byte) i;
            buffer[pos++] = b;
            checksum += b;
            if (pos == buffer.length) {
                file.write(buffer);
                pos = 0;
            }
        }
        long fileLength = file.length();
        file.close();
        long took = System.currentTimeMillis() - start;

        System.out.printf("Writing file of length %,dB took %,dms resulting in throughput %,dKB/s\n", fileLength, took, (amount / 1024L / took * 1000L));

        return checksum;
    }

    private long read(String fileName, long amount) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        long checksum = 0;
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[4 * 1024];
        while (file.read(buffer) > 0) {
            for (byte b : buffer)
                checksum += b;
        }
        long fileLength = file.length();
        file.close();
        long took = System.currentTimeMillis() - start;

        System.out.printf("Reading file of length %,dB took %,dms resulting in throughput %,dKB/s\n ", fileLength, took, (amount / 1024L / took * 1000L));

        return checksum;
    }

}
