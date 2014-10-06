package com.benstopford.coherence.bootstrap.structures.tools.index;

import com.tangosol.net.*;
import com.tangosol.util.MapIndex;
import net.sourceforge.sizeof.SizeOf;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for measuring index sizes across a Coherence Cluster
 * <p/>
 * The invocable just loops over each cache service and its respective caches measuring
 * the size of each using Java's instrumentation API
 */
public class IndexSizingInvocable implements Invocable {

    private static final long serialVersionUID = -2369438253692973245L;
    private transient InvocationService service;
    private transient Object result;

    public IndexSizingInvocable() {
    }

    public void init(InvocationService service) {
        this.service = service;
    }

    public void run() {
        try {
            result = sizeIndexesInAllDistributedCaches();
        } catch (Exception e) {
            result = e;
        }
    }

    private long sizeOf(Object indexes) {
        SizeOf.skipStaticField(true);
//        SizeOf.setMinSizeToLog(Long.MAX_VALUE);
        SizeOf.turnOffDebug();
        MapIndex theIndex = (MapIndex) indexes;
        long total = SizeOf.deepSizeOf(theIndex);

        return total;
    }

    public Object getResult() {
        return result;
    }

    public Map<String, Long> sizeIndexesInAllDistributedCaches() {
        //Loop through services => caches => indexes => size(index)

        Map<String, Long> result = new HashMap<String, Long>();
        Enumeration serviceNames = service.getCluster().getServiceNames();
        while (serviceNames.hasMoreElements()) {
            String serviceName = (String) serviceNames.nextElement();
            Service s = service.getCluster().getService(serviceName);
            if (s instanceof DistributedCacheService) {
                DistributedCacheService cacheService = (DistributedCacheService) s;
                Enumeration cacheNames = cacheService.getCacheNames();
                while (cacheNames.hasMoreElements()) {
                    String name = (String) cacheNames.nextElement();
                    BackingMapContext context = cacheService
                            .getBackingMapManager().getContext().getBackingMapContext(name);
                    Map indexes = context.getIndexMap();
                    for (Object extractor : indexes.keySet()) {
                        Object index = indexes.get(extractor);
                        long indexSize = sizeOf(index);
                        result.put(String.format("%s:%s:%s", serviceName, name, extractor), indexSize);
                    }
                }
            }
        }
        return result;
    }

}
