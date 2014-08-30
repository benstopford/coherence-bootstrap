package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An object that takes a list of fields so it can simulate a
 * much larger and more complex domain object (when viewed from pof)
 */
public class LengthyPofObject {
    public static PofSerializer serializer = new Serialiser();
    private List fields;

    public LengthyPofObject(List fields) {
        this.fields = fields;
    }

    public LengthyPofObject(Object... fields) {
        this.fields = Arrays.asList(fields);
    }

    public static class Serialiser implements PofSerializer {

        @Override
        public void serialize(PofWriter pofWriter, Object obj) throws IOException {
            LengthyPofObject o = (LengthyPofObject) obj;
            pofWriter.writeInt(0, o.fields.size());
            for (int i = 1; i <= o.fields.size(); i++) {
                pofWriter.writeObject(i, o.fields.get(i - 1));
            }
            pofWriter.writeRemainder(null);
        }

        @Override
        public Object deserialize(PofReader pofReader) throws IOException {
            LengthyPofObject o = new LengthyPofObject();
            o.fields = new ArrayList();
            int size = pofReader.readInt(0);
            for (int i = 1; i <= size; i++) {
                Object result = pofReader.readObject(i);
                o.fields.add(result);
            }
            return o;
        }
    }

}
