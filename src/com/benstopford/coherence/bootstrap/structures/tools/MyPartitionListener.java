package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.partition.PartitionListener;
import com.tangosol.net.partition.PartitionEvent;

import java.io.File;
import java.io.IOException;

/**
 * BTS, 12-May-2008
 */
public class MyPartitionListener implements PartitionListener {
    public static final File CLUSTER_DATA_LOSS_FLAG_FILE = new File("CLUSTER_DATA_LOSS_DETECTED");

    public void onPartitionEvent(PartitionEvent partitionEvent) {
        if (partitionEvent.getId() == PartitionEvent.PARTITION_LOST) {
            System.err.println("Oops - a parition has been lost. Time to health check your cluster.");
            System.err.println(partitionEvent.toString());

            try {
                CLUSTER_DATA_LOSS_FLAG_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
