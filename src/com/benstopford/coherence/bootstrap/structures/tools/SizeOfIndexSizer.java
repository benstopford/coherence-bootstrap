package com.benstopford.coherence.bootstrap.structures.tools;

import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;

import java.util.Map;
import java.util.Set;

public class SizeOfIndexSizer {
    public long calculateIndexSizesForSingleCache(InvocationService invocationService, NamedCache cache, String config) {
        Set members = invocationService.getInfo().getServiceMembers();

        Map<Member, Long> indexes = invocationService.query(new IndexCountingInvocable(cache.getCacheName(), config), members);

        return total(indexes);
    }

    private long total(Map<Member, Long> map) {
        long total = 0;
        for (long s : map.values())
            total += s;
        return total;
    }


}
