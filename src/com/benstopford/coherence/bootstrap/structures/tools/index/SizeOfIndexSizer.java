package com.benstopford.coherence.bootstrap.structures.tools.index;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.currentThread;

/**
 * Support class for calculating total index sizes of a Coherence Cluster
 * <p/>
 * This class simply calls an Invocable everywhere and interprets the results
 */
public class SizeOfIndexSizer {
    public long calculateIndexSizesForSingleCache(String invocationService, String cache, String config) {
        return invoke(invocationService, config, new IndexCountingInvocable(config, cache));
    }

    public long calculateTotalIndexSize(String invocationService, String config) {
        return invoke(invocationService, config, new IndexCountingInvocable(config));
    }

    public Map<String, Long> calculateIndexSizes(String invocationServiceName, String config) {
        IndexCountingInvocable invocable = new IndexCountingInvocable(config);
        invocable.returnByCache(true);

        InvocationService service = invocationService(invocationServiceName, config);
        Set members = service.getInfo().getServiceMembers();

        return totalByCache(service.query(invocable, members));
    }

    private long invoke(String invocationServiceName, String config, IndexCountingInvocable invocable) {
        InvocationService invocationService = invocationService(invocationServiceName, config);
        Set members = invocationService.getInfo().getServiceMembers();

        return total(invocationService.query(invocable, members));
    }

    private Map<String, Long> totalByCache(Map<Member, Map<String, Long>> indexes) {
        Map<String, Long> out = new HashMap<String, Long>();

        for (Map<String, Long> indexesForMember : indexes.values()) {
            for (String cacheName : indexesForMember.keySet()) {
                Long total = out.get(cacheName);
                if (total == null)
                    total = 0L;
                total += indexesForMember.get(cacheName);
                out.put(cacheName, total);
            }
        }
        return out;
    }

    private InvocationService invocationService(String invocationServiceName, String config) {
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

}
