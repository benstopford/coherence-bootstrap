package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.util.ExternalizableHelper;

import java.io.Serializable;
import java.io.DataInput;
import java.io.IOException;
import java.io.DataOutput;
import java.util.List;
import java.util.ArrayList;

/**
 * BTS, 18-Apr-2008
 */
public class SizableObject implements Serializable, ExternalizableLite {
	private static final long serialVersionUID = 1L;
	List<byte[]> payload = new ArrayList<byte[]>();

    public SizableObject() {
    }

    public SizableObject(List<byte[]> payload) {
        this.payload = payload;
    }

    public void readExternal(DataInput dataInput) throws IOException {
        ExternalizableHelper.readCollection(dataInput, payload, this.getClass().getClassLoader());
    }

    public void writeExternal(DataOutput dataOutput) throws IOException {
        ExternalizableHelper.writeCollection(dataOutput, payload);
    }

    public int size(){
        return payload.size();
    }

    public Object getSelf(){
        return this;
    }
}
