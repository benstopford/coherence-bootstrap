package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.ConfigurablePofContext;

import java.io.IOException;

public class MyPofContext extends ConfigurablePofContext {
    public MyPofContext(String sLocator) {
        super(sLocator);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void serialize(WriteBuffer.BufferOutput out, Object o) throws IOException {
        super.serialize(out, o);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Object deserialize(ReadBuffer.BufferInput in) throws IOException {
        return in;    //To change body of overridden methods use File | Settings | File Templates.
    }


}
