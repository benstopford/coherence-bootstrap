package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.*;
import net.sourceforge.sizeof.SizeOf;

import java.util.Enumeration;
import java.util.Map;

import static java.lang.Thread.currentThread;


public class IndexCountingInvocable implements Invocable {

    private transient InvocationService service;
    private transient Object result;
    private static final long serialVersionUID = -2369438253692973245L;
    private String cacheName;
    private String cacheService;
    private String config;

    public IndexCountingInvocable(String config,String cacheName) {
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
        if (cacheName != null) {
            result = size(cacheName);
        } else {
            result = getTotalSize();
        }
    }

    private long getTotalSize() {
        Enumeration serviceNames = service.getCluster().getServiceNames();
        long total = 0;
        while(serviceNames.hasMoreElements()){
            Service s = service.getCluster().getService((String) serviceNames.nextElement());
            if(s instanceof DistributedCacheService){
                Enumeration cacheNames = ((DistributedCacheService)s).getCacheNames();
                while (cacheNames.hasMoreElements()) {
                    String name = (String) cacheNames.nextElement();
                    total += size(name);
                }
            }
        }
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
}
