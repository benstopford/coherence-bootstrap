package com.benstopford.coherence.bootstrap.structures.tools.index;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.currentThread;

/**
 * Support class for calculating total index sizes of a Coherence Cluster
 * <p/>
 * This class simply calls an Invocable everywhere and interprets the results
 */
public class IndexSizer implements IndexSizerMBean {
    public static String defaultConfig = null;
    public String config = defaultConfig;

    public static final String indexSizerServiceName = "IndexSizer";

    public IndexSizer(String config) {
       this.config = config;
    }

    public IndexSizer() {
    }

    @Override
    public long getTotal() {
        return total(getIndexSizes());
    }

    @Override
    public Map<String, Long> getIndexSizes() {
        return reduce(invoke(config, new IndexSizingInvocable()));
    }


    private void validate(boolean b, String s) {
        if (!b) throw new RuntimeException(s);
    }

    private Map<Member, Map<String,Long>> invoke(String config, IndexSizingInvocable invocable) {
        InvocationService invocationService = invocationService(config);
        Map query = invocationService.query(invocable, null);
        return checkForExceptions(query);
    }

    private Map checkForExceptions(Map query) {
        for (Object o : query.values()) {
            if (o instanceof Exception) {
                throw new RuntimeException((Exception) o);
            }
        }
        return query;
    }

    private Map<String, Long> reduce(Map<Member, Map<String, Long>> indexes) {
        Map<String, Long> reduced = new HashMap<String, Long>();
        for (Map<String, Long> indexesForMember : indexes.values()) {
            for (String cacheName : indexesForMember.keySet()) {
                Long total = reduced.get(cacheName);
                if (total == null)
                    total = 0L;
                total += indexesForMember.get(cacheName);
                reduced.put(cacheName, total);
            }
        }
        return reduced;
    }

    private InvocationService invocationService(String config) {
        InvocationService service;
        if (config == null) {
            service = (InvocationService) CacheFactory.getCluster().ensureService(indexSizerServiceName, InvocationService.TYPE_DEFAULT);
            validate(service != null, "Could not reference the invocation service. This is likely because Coherence has not been initialised with a config file that includes the IndexSizer service. Check the cache config service.");
            return service;
        } else {
            service = (InvocationService) CacheFactory.getCacheFactoryBuilder()
                    .getConfigurableCacheFactory(config, currentThread().getContextClassLoader())
                    .ensureService(indexSizerServiceName);
            validate(service != null, String.format("Could not reference the invocation service. Check that the config file %s contains config for the %s Service and it is set to autostart", config, indexSizerServiceName));

        }
        return service;
    }

    private long total(Map<String, Long> map) {
        long total = 0;
        for (long s : map.values())
            total += s;
        return total;
    }


}
