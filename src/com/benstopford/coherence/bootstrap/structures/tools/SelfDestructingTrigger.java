package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

public class SelfDestructingTrigger implements com.tangosol.util.MapTrigger, PortableObject {
    public void process(Entry entry) {
        System.out.println("SelfDestructingTrigger was called for entry: " + entry);
        throw new RuntimeException("Forced Error");
    }

    public void readExternal(PofReader pofReader) throws IOException {

    }

    public void writeExternal(PofWriter pofWriter) throws IOException {

    }
}
