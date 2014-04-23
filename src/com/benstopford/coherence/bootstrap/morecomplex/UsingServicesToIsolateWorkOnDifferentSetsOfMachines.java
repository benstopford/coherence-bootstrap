package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.tools.SampleInvocable;
import com.benstopford.coherence.bootstrap.structures.framework.ClusterRunner;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * BTS, 03-Sep-2008
 */
public class UsingServicesToIsolateWorkOnDifferentSetsOfMachines extends ClusterRunner {


    @Test
    public void splitMachinesIntoTwoSetsWithServices() throws IOException, InterruptedException {

        // Five Coherence processes (including this one)
        startCoherenceProcess("config/basic-invocation-service-1.xml");
        startCoherenceProcess("config/basic-invocation-service-1.xml");
        startCoherenceProcess("config/basic-invocation-service-2.xml");
        startCoherenceProcess("config/basic-invocation-service-2.xml");

        SampleInvocable getMemberIdInvocable = new SampleInvocable();

        //Invocation service 1 is only running on 3 of the 5 nodes
        InvocationService service1 = getService("MyInvocationService1");
        Map membersVisited1 = service1.query(getMemberIdInvocable, service1.getInfo().getServiceMembers());

        System.out.printf("Service 1 is distributed on the %s members: %s\n", service1.getInfo().getServiceMembers().size(), membersVisited1.values());


        //Invocation service 1 is running on a different 3 of the 5 nodes
        InvocationService service2 = getService("MyInvocationService2");
        Map membersVisited2 = service2.query(getMemberIdInvocable, service2.getInfo().getServiceMembers());

        System.out.printf("Service 1 is distributed on the %s members: %s\n", service2.getInfo().getServiceMembers().size(), membersVisited2.values());


        assertEquals(membersVisited1.toString(), 3, membersVisited1.size());
        assertEquals(membersVisited2.toString(), 3, membersVisited2.size());
    }

    private InvocationService getService(String name) {
        return (InvocationService) CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("config/basic-invocation-service-1-and-2.xml", classLoader)
                .ensureService(name);
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
