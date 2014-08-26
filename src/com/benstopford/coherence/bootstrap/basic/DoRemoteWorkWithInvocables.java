package com.benstopford.coherence.bootstrap.basic;

import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.benstopford.coherence.bootstrap.structures.tools.SampleInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class DoRemoteWorkWithInvocables extends ClusterRunner {
    public static final String config = "config/basic-invocation-service-1.xml";

    @Test
    public void shouldDoRemoteWork() throws InterruptedException {
        startCoherenceProcess(config);
        startCoherenceProcess(config);
        InvocationService invocationService = (InvocationService) CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory(config, classLoader)
                .ensureService("MyInvocationService1");
        assertThat(CacheFactory.getCluster().getMemberSet().size(), is(3));

        //this fella just returns the memberId but obviously could do something more useful
        SampleInvocable getMemberIdInvocable = new SampleInvocable();

        Map membersAndTheirIds = invocationService.query(getMemberIdInvocable, invocationService.getInfo().getServiceMembers());

        assertThat(membersAndTheirIds.values(), (Matcher)hasItems(1,2,3));
    }
}
