package com.benstopford.coherence.bootstrap.structures;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;


public class MyPofObject implements PortableObject {
    private String data;

    public MyPofObject() {
    }//serialization

    public MyPofObject(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void readExternal(PofReader pofReader) throws IOException {
        System.out.println("pof is deserialising");
        data = pofReader.readString(1);
    }

    public void writeExternal(PofWriter pofWriter) throws IOException {
        System.out.println("pof is serialising");
        pofWriter.writeString(1, data);
    }
}
