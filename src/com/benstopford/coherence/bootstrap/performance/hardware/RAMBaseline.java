package com.benstopford.coherence.bootstrap.performance.hardware;


import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.end;
import static com.benstopford.coherence.bootstrap.structures.framework.PerformanceTimer.start;
import static junit.framework.Assert.assertTrue;


/**
 * Simple class that baselines the single thread performance of reads and writes to the local drive
 */
public class RAMBaseline {

    public static final String dir = "/tmp/foo/";
    public static final String file = dir + "data.txt";

    /**
     * Writing buffer of length 1,073,741,824B took 1,693ms resulting in throughput 619MB/s
     * Reading file of length 1,073,741,824B took 1,256ms resulting in throughput 834KB/s
     */
    @Test
    public void ramControlTest() throws IOException {
        run();
        run();
    }

    private void run() throws IOException {
        ArrayList<byte[]> list = new ArrayList();

        long datasize = 10 * 1024 * 1024L; //change to 1GB+
        int page = 8;//change to 512B

        long write = write(list, datasize, page);
        long read = read(list, page);

        assertTrue(write != 0);
        assertTrue(write == read);
    }

    private long write(ArrayList list, long datasize, int page) throws IOException {
        long checksum = 0;
        start();
        for (int i = 0; i < datasize; i = i + page) {
            byte[] e = new byte[page];
            for (int j = 0; j < e.length; j++) {
                e[j] = 1;
                checksum += e[j];
            }
            list.add(e);
        }
        long took = end().ms();

        print(list, page, took, "Writing");
        return checksum;
    }

    private long read(ArrayList<byte[]> list, int page) throws IOException {
        long checksum = 0;
        start();
        for (byte[] arr : list) {
            for (byte b : arr)
                checksum += b;
        }
        long took = end().ms();

        print(list, page, took, "Reading");

        return checksum;
    }

    private void print(ArrayList<byte[]> list, int page, long took, String str) {
        System.out.printf(str + " list of length %,d entries, each of %,dB [%,dMB] took %,dms resulting in throughput %,dMB/s\n ", list.size(), page, (list.size() * page / 1024), took, took == 0 ? 0 : ((list.size() * page) / 1024L / took));
    }

}
