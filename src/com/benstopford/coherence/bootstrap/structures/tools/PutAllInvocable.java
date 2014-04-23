package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * BTS, 27-Jan-2009
 */
public class PutAllInvocable implements Invocable, PortableObject {

    private transient InvocationService service;
    private Map entriesForMember;
    private Map errors = new HashMap<Object, Throwable>();
    private String cacheName;
    private transient NamedCache cache;
    private String scheme;

    public PutAllInvocable() {
    }

    public PutAllInvocable(Map entriesForMember, String cacheName, String scheme) {
        this.entriesForMember = entriesForMember;
        this.cacheName = cacheName;
        this.scheme = scheme;
    }

    public void init(InvocationService service) {
        this.service = service;
    }

    public void run() {
        for (Object key : entriesForMember.keySet()) {
            try {
                cache().put(key, entriesForMember.get(key));
            } catch (Throwable e) {
                errors.put(key, e);
                e.printStackTrace();
            }
        }
        System.out.println("Completed invocation with error count of " + errors.size());
    }

    private NamedCache cache() {
        if (cache == null) {
            ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(scheme, getClass().getClassLoader());
            cache = factory.ensureCache(cacheName, getClass().getClassLoader());
        }
        return cache;
    }

    public Object getResult() {
        return errors;
    }

    public void readExternal(PofReader pofReader) throws IOException {
        entriesForMember = pofReader.readMap(0, new HashMap());
        errors = pofReader.readMap(1, new HashMap());
        cacheName = pofReader.readString(2);
        scheme = pofReader.readString(3);
    }

    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeMap(0, entriesForMember);
        pofWriter.writeMap(1, errors);
        pofWriter.writeString(2, cacheName);
        pofWriter.writeString(3, scheme);
    }
}