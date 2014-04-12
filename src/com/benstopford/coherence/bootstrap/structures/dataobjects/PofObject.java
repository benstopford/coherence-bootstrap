package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

public class PofObject implements ObjFactory, PortableObject {
    protected Object data;

    public PofObject() {
    }//serialization


    public PofObject(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public Object createNext() {
        return new PofObject(new byte[1024]);
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        data = pofReader.readObject(1);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeObject(1, data);
    }

}
