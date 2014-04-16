package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

public class PofByteObject implements  PortableObject {
    protected byte[] data;

    public PofByteObject() {
    }//serialization



    public PofByteObject(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }


    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        data = pofReader.readByteArray(1);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeByteArray(1, data);
    }
}
