package com.benstopford.coherence.bootstrap.structures;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;


public class MyPofObject implements PortableObject {
    private Object data;
    private boolean logSerialisation;

    public MyPofObject() {
    }//serialization

    public MyPofObject(Object data) {
        this(data, true);
    }

    public MyPofObject(Object data, boolean logSerialisation) {
        this.data = data;
        this.logSerialisation = logSerialisation;
    }

    public Object getData() {
        return data;
    }

    public void readExternal(PofReader pofReader) throws IOException {
        if (logSerialisation) {
            System.out.println("pof is deserialising");
        }
        data = pofReader.readObject(1);
    }

    public void writeExternal(PofWriter pofWriter) throws IOException {
        if (logSerialisation) {
            System.out.println("pof is serialising");
        }
        pofWriter.writeObject(1, data);
    }
}
