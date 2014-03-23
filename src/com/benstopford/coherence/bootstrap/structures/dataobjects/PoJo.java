package com.benstopford.coherence.bootstrap.structures.dataobjects;

import java.io.Serializable;

/**
 * BTS, 16-Oct-2008
 */
public class PoJo implements Serializable, ObjFactory {
    private Integer o;

    public PoJo(int o) {
        this.o = o;
    }

    public PoJo() {
        this(-1);
    }

    public Object getValue() {
        return o;
    }


    public String toString() {
        return getValue().toString();
    }


    private static int counter = 0;
    @Override
    public Object createNext() {
        return new PoJo(counter++);
    }
}
