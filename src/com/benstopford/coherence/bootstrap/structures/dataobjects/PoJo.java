package com.benstopford.coherence.bootstrap.structures.dataobjects;

import java.io.Serializable;

/**
 * BTS, 16-Oct-2008
 */
public class PoJo implements Serializable {
    private Integer o;


    public PoJo(int o) {
        this.o = o;
    }

    public Object getValue() {
        return o;

    }


    public String toString() {
        return getValue().toString();
    }
}
