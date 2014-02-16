package com.benstopford.coherence.bootstrap.structures;

import java.io.Serializable;

/**
 * BTS, 16-Oct-2008
 */
public class ValueObject implements Serializable {
    private Integer o;


    public ValueObject(int o) {
        this.o = o;
    }

    public Object getValue() {
        return o;

    }


    public String toString() {
        return getValue().toString();
    }
}
