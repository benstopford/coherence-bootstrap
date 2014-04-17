package com.benstopford.coherence.bootstrap.structures.dataobjects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class used to put byte arrays into Coherence as it doesn't appear to support equality
 * operations on byte arrays natively
 */
public class ByteArrayWrapper implements PortableObject{

    private byte[] data;

    public ByteArrayWrapper() {
    }


    public ByteArrayWrapper(byte[] data) {
        this.data = data;
    }


    @Override
    public void readExternal(PofReader pofReader) throws IOException {
      data =   pofReader.readByteArray(1);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeByteArray(1, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteArrayWrapper that = (ByteArrayWrapper) o;

        if (!Arrays.equals(data, that.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? Arrays.hashCode(data) : 0;
    }

    @Override
    public String toString() {
        return "ByteArrayWrapper{" +
                "data=" + PofByteObject.getInt(data) +
                '}';
    }

    public byte[] getBytes() {
        return data;
    }
}
