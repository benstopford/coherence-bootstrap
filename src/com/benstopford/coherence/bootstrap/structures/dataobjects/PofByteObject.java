package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PofByteObject implements  PortableObject, Serializable {
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
        ByteArrayWrapper wrapper = (ByteArrayWrapper) pofReader.readObject(1);
        data = wrapper.getBytes();
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeObject(1, new ByteArrayWrapper(data));
    }


    @Override
    public String toString() {
        return "PofObject{" +
                "data=" + getInt(data) +
                '}';
    }

    public static int getInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
}
