package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.CacheFactory;

/**
 * BTS, 27-Jan-2009
*/
public class SampleInvocable implements Invocable {
    
    private transient InvocationService service;
    private transient Object result;
    private static final long serialVersionUID = -2369438253692973245L;

    public SampleInvocable() {
    }

    public void init(InvocationService service) {
        this.service = service;
    }

    public void run() {
        result = CacheFactory.ensureCluster().getLocalMember().getId();
        System.out.println("Ran invocable");
    }

    public Object getResult() {
        return result;
    }
}
