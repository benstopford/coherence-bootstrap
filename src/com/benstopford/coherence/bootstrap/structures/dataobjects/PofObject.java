package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

public class PofObject implements ObjFactory, PortableObject {
    public static final Serialiser serialiser = new Serialiser();
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
        serialiser.deserialize(pofReader);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        serialiser.serialize(pofWriter, this);
    }

    public static class Serialiser implements PofSerializer {
        public Serialiser() {
        }

        @Override
        public void serialize(PofWriter pofWriter, Object o) throws IOException {
            pofWriter.writeObject(1, ((PofObject) o).getData());
        }

        @Override
        public Object deserialize(PofReader pofReader) throws IOException {
            return new PofObject(pofReader.readObject(1));
        }
    }
}
