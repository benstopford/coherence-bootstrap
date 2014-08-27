package com.benstopford.coherence.bootstrap.structures.dataobjects;

import java.io.Serializable;

/**
 * BTS, 16-Oct-2008
 */
public class PoJo implements Serializable, ObjFactory {
    private Object data = null;

    public PoJo(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }


    public String toString() {
        return data.toString();
    }


    private static int counter = 0;
    @Override
    public Object createNext() {
        return new PoJo(counter++);
    }
}
