package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.util.MapTriggerListener;

public class TriggerFactory {

    public static MapTriggerListener createTriggerListener(String sCacheName) {
        System.out.println("Initialising Triggers");
        return new MapTriggerListener(new SelfDestructingTrigger());
    }
}
