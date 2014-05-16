package com.benstopford.coherence.bootstrap.structures.tools.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.management.GarbageCollectorMXBean;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * (Designed as a stand-alone class, so easy to copy and paste as a util)
 */
public class ClusterGC {

    public static final String search = "Coherence:type=Cache,service=*,name=*,nodeId=*,tier=back";

    public static void main(String[] args) throws Exception {
        new ClusterGC().run(new String[]{"localhost:40001"});
    }

    public void run(String[] hosts) throws Exception {
        run(hosts, 0l);
    }

    public void run(String[] hosts, long sleepMs) throws Exception {
        for (String host : hosts) {
            if (sleepMs > 0)
                Thread.sleep(sleepMs);
            String[] split = host.split(":");
            MBeanServerConnection jmx = jmx(split[0], split[1], "", "");
            ObjectName memoryMXBean = new ObjectName("java.lang:type=Memory");
            jmx.invoke(memoryMXBean, "gc", null, null);
        }
    }


    private MBeanServerConnection jmx(String jmxHost, String jmxPort, String jmxUser, String jmxPassword) throws Exception {
        Map<String, String[]> env = new HashMap<String, String[]>();

        String urlPath = "/jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";

        JMXServiceURL jmxUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        env.put(JMXConnector.CREDENTIALS, new String[]{jmxUser, jmxPassword});

        return JMXConnectorFactory.connect(jmxUrl, env).getMBeanServerConnection();
    }

    public List<GarbageCollectorMXBean> getGCMbean(String host, String port) throws Exception {
        ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        MBeanServerConnection mbs = jmx(host, port, "", "");
        List<GarbageCollectorMXBean> all = new ArrayList<GarbageCollectorMXBean>();
        for (ObjectName name : mbs.queryNames(gcName, null)) {
            GarbageCollectorMXBean gc = ManagementFactory.newPlatformMXBeanProxy(mbs, name.getCanonicalName(), GarbageCollectorMXBean.class);
            all.add(gc);
        }
        return all;
    }


}
