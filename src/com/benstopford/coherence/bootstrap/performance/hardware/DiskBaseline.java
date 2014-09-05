package com.benstopford.coherence.bootstrap.performance.hardware;


import com.benstopford.coherence.bootstrap.structures.framework.cluster.ClusterRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static junit.framework.Assert.assertTrue;


/**
 * Simple class that baselines the single thread performance of reads and writes to the local drive
 */
public class DiskBaseline {

    public static final String dir = "/tmp/foo/";
    public static final String file = dir + "data.txt";


    /**
     * Writing file of length 1,073,741,824B took 10,266ms resulting in throughput 102MB/s
     * Reading file of length 1,073,741,824B took 3,541ms resulting in throughput 296MB/s
     */
    @Test
    public void diskControlTest() throws IOException {
        run(dir + "file1");
        run(dir + "file2");
    }

    private void run(String file) throws IOException {
        long datasize = 1024L * 1024L * 1024L;//CHANGE ME!
        long write = write(file, datasize);
        long read = read(file, datasize);

        assertTrue(write != 0);
        assertTrue(write == read);
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

        System.out.printf("Writing file of length %,dB took %,dms resulting in throughput %,dMB/s\n", fileLength, took, (amount / 1024L / took));

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

        System.out.printf("Reading file of length %,dB took %,dms resulting in throughput %,dMB/s\n ", fileLength, took, (amount / 1024L / took));

        return checksum;
    }

    @Before
    public void start() {
        File dir = new File(DiskBaseline.dir);
        ClusterRunner.deleteDirectory(dir);
        dir.mkdir();
    }

    @After
    public void end() {
        File dir = new File(DiskBaseline.dir);
        ClusterRunner.deleteDirectory(dir);
        dir.mkdir();
    }

}
