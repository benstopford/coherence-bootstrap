package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.*;
import net.sourceforge.sizeof.SizeOf;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.currentThread;

/**
 * To run this invocable you need to add -javaagent:lib/SizeOf.jar to the command line
 * SizeOf.jar is taken from http://sizeof.sourceforge.net/
 */
public class IndexCountingInvocable implements Invocable {

    private transient InvocationService service;
    private transient Object result;
    private static final long serialVersionUID = -2369438253692973245L;
    private String cacheName;
    private String config;
    private boolean returnByCache;

    public IndexCountingInvocable(String config, String cacheName) {
        this.config = config;
        this.cacheName = cacheName;
    }

    public IndexCountingInvocable(String config) {
        this.config = config;
    }

    public void init(InvocationService service) {
        this.service = service;
    }

    public void run() {

        if (returnByCache) {
            result = getSizeByCache();
        } else if (cacheName != null) {
            result = size(cacheName);
        } else {
            result = getTotalSize();
        }
    }

    private long getTotalSize() {
        Collection<Long> sizes = getSizeByCache().values();
        long total = 0;
        for (Long l : sizes)
            total += l;
        return total;
    }

    private long size(String name) {
        NamedCache cache = factory()
                .ensureCache(name, this.getClass().getClassLoader());

        DistributedCacheService cacheService = (DistributedCacheService) cache.getCacheService();

        BackingMapContext context = cacheService
                .getBackingMapManager().getContext().getBackingMapContext(name);
        Map indexes = context.getIndexMap();

        return sizeOf(indexes);
    }

    private ConfigurableCacheFactory factory() {
        return CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(config, currentThread().getContextClassLoader());
    }

    private long sizeOf(Map indexes) {
        SizeOf.skipStaticField(true);
        SizeOf.setMinSizeToLog(Long.MAX_VALUE);
        SizeOf.turnOffDebug();
        return SizeOf.deepSizeOf(indexes);
    }

    public Object getResult() {
        return result;
    }

    public void returnByCache(boolean returnByCache) {
        this.returnByCache = returnByCache;
    }

    public Map<String, Long> getSizeByCache() {
        Map<String, Long> out = new HashMap<String, Long>();
        Enumeration serviceNames = service.getCluster().getServiceNames();
        long total = 0;
        while (serviceNames.hasMoreElements()) {
            String serviceName = (String) serviceNames.nextElement();
            Service s = service.getCluster().getService(serviceName);
            if (s instanceof DistributedCacheService) {
                Enumeration cacheNames = ((DistributedCacheService) s).getCacheNames();
                while (cacheNames.hasMoreElements()) {
                    String name = (String) cacheNames.nextElement();
                    out.put(String.format("%s:%s", serviceName, name), size(name));
                }
            }
        }
        return out;
    }
}
