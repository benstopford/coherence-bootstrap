package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;


public class SimplePofObject implements PortableObject {
    private Object data;
    private boolean logSerialisation;

    public SimplePofObject() {
    }//serialization

    public SimplePofObject(Object data) {
        this(data, true);
    }

    public SimplePofObject(Object data, boolean logSerialisation) {
        this.data = data;
        this.logSerialisation = logSerialisation;
    }

    public Object getData() {
        return data;
    }

    public void readExternal(PofReader pofReader) throws IOException {
        log("deserialising");
        data = pofReader.readObject(1);
    }

    private void log(String type) {
        if (logSerialisation) {
            System.out.println("pof is "+ type);
        }
    }

    public void writeExternal(PofWriter pofWriter) throws IOException {
        log("serialising");
        pofWriter.writeObject(1, data);
        pofWriter.writeRemainder(null);
    }
}