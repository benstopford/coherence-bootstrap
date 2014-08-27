package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

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

    public long calculateIndexSizes(String invocationService, String config) {

        IndexCountingInvocable allCachesInvocable = new IndexCountingInvocable(config);

        return run(invocationService, config, allCachesInvocable);
    }


}
