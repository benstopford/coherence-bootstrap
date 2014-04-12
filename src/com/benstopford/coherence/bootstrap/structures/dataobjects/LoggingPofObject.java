package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;


public class LoggingPofObject extends PofObject implements PortableObject, ObjFactory {
    private boolean logSerialisation;

    public LoggingPofObject() {
    }//serialization

    public LoggingPofObject(Object data) {
        this(data, true);
    }

    public LoggingPofObject(Object data, boolean logSerialisation) {
        super(data);
        this.logSerialisation = logSerialisation;
    }

    public void readExternal(PofReader pofReader) throws IOException {
        super.readExternal(pofReader);
        logSerialisation = pofReader.readBoolean(2);

        log("deserialising:" + getData());
    }

    public void writeExternal(PofWriter pofWriter) throws IOException {
        super.writeExternal(pofWriter);
        pofWriter.writeBoolean(2, logSerialisation);

        log("serialising:" + getData());
    }

    private void log(String type) {
        if (logSerialisation) {
            System.out.println("pof is " + type);
        }
    }

    @Override
    public Object createNext() {
        return new LoggingPofObject(new byte[1024], false);
    }
}
