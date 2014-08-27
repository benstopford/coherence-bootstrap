package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.currentThread;

public class SizeOfIndexSizer {
    public long calculateIndexSizesForSingleCache(String invocationService, String cache, String config) {

        IndexCountingInvocable singleCacheInvocable = new IndexCountingInvocable(config, cache);

        return run(invocationService, config, singleCacheInvocable);
    }

    private long run(String invocationServiceName, String config, IndexCountingInvocable invocable) {
        InvocationService invocationService = getInvocationService(invocationServiceName, config);
        Set members = invocationService.getInfo().getServiceMembers();

        Map<Member, Long> indexes = invocationService.query(invocable, members);

        return total(indexes);
    }

    private Map<String, Long> getCacheBreakdown(String invocationServiceName, String config, IndexCountingInvocable invocable) {
        invocable.returnByCache(true);

        InvocationService invocationService = getInvocationService(invocationServiceName, config);

        Set members = invocationService.getInfo().getServiceMembers();

        Map<Member, Map<String, Long>> indexes = invocationService.query(invocable, members);

        return totalByCache(indexes);
    }

    private Map<String, Long> totalByCache(Map<Member, Map<String, Long>> indexes) {
        Map<String, Long> out = new HashMap<String, Long>();

        for(Map<String, Long> indexesForMember: indexes.values()){
            for(String cacheName: indexesForMember.keySet()){
                Long total = out.get(cacheName);
                if(total==null)
                    total = 0L;
                total+=indexesForMember.get(cacheName);
                out.put(cacheName,total);
            }
        }
        return out;
    }

    private InvocationService getInvocationService(String invocationServiceName, String config) {
        return (InvocationService) CacheFactory.getCacheFactoryBuilder()
                    .getConfigurableCacheFactory(config, currentThread().getContextClassLoader())
                    .ensureService(invocationServiceName);
    }

    private long total(Map<Member, Long> map) {
        long total = 0;
        for (long s : map.values())
            total += s;
        return total;
    }


    public long calculateTotalIndexSize(String invocationService, String config) {

        IndexCountingInvocable allCachesInvocable = new IndexCountingInvocable(config);

        return run(invocationService, config, allCachesInvocable);
    }

    public Map<String, Long> calculateIndexSizes(String invocationService, String config) {

        IndexCountingInvocable allCachesInvocable = new IndexCountingInvocable(config);
        return getCacheBreakdown(invocationService, config, allCachesInvocable);
    }
}
