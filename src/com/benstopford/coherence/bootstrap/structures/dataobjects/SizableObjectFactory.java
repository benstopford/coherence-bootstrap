package com.benstopford.coherence.bootstrap.structures.dataobjects;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * BTS, 18-Apr-2008
 */
public class SizableObjectFactory {
    private static Long counter = 1l;

    public SizableObject buildObject(long sizeInKB) {
        List<byte[]> payload = new ArrayList<byte[]>();
        for (int i = 0; i < sizeInKB; i++) {
            payload.add(new byte[1000]);
        }
        return new SizableObject(payload);
    }

    public Map<Long,SizableObject> buildMapOfObjectsObject(int numberOfObjects, long sizeInKB) {
        Map<Long,SizableObject> map = new HashMap<Long,SizableObject>();
        for (int i = 0; i<numberOfObjects; i++) {
            map.put(++counter, buildObject(sizeInKB));
        }
        return map;
    }
}
