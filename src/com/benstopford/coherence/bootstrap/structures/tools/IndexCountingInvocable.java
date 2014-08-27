package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.*;
import net.sourceforge.sizeof.SizeOf;

import java.util.Map;

/**
 * BTS, 27-Jan-2009
*/
public class IndexCountingInvocable implements Invocable {

    private transient InvocationService service;
    private transient Object result;
    private static final long serialVersionUID = -2369438253692973245L;
    private String cacheName;
    private String config;

    public IndexCountingInvocable(String name, String config) {
        this.config = config;
        this.cacheName = name;
    }

    public void init(InvocationService service) {
        this.service = service;
    }

    public void run() {

        NamedCache cache = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(config, this.getClass().getClassLoader())
                .ensureCache(cacheName, this.getClass().getClassLoader());

        DistributedCacheService cacheService = (DistributedCacheService) cache.getCacheService();

        int memberId = cacheService.getCluster().getLocalMember().getId();

        BackingMapContext context = cacheService
                .getBackingMapManager().getContext().getBackingMapContext(cacheName);
        Map indexes = context.getIndexMap();

        SizeOf.skipStaticField(true);
        SizeOf.setMinSizeToLog(Long.MAX_VALUE);
        SizeOf.turnOffDebug();

        //calculate object size
        long l = SizeOf.deepSizeOf(indexes);

//        System.out.printf("%s: Member:%s backing map size: %,d,number of indexes:%s, total index size:%,d\n",cacheName,memberId,context.getBackingMap().size(),indexes.size(),l);

        result = l;
    }

    public Object getResult() {
        return result;
    }
}
