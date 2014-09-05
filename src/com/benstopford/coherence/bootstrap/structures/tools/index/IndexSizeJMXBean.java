package com.benstopford.coherence.bootstrap.structures.tools.index;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class IndexSizeJMXBean {

    public static void register() throws Exception {

        String objectName = "com.noracle.coherence:type=IndexSizer";

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        // Construct the ObjectName for the Hello MBean we will register
        ObjectName mbeanName = new ObjectName(objectName);

        IndexSizer mbean = new IndexSizer();

        server.registerMBean(mbean, mbeanName);

        Set<ObjectInstance> instances = server.queryMBeans(new ObjectName(objectName), null);

        ObjectInstance instance = (ObjectInstance) instances.toArray()[0];

        System.out.println("Class Name:t" + instance.getClassName());
        System.out.println("Object Name:t" + instance.getObjectName());

    }
}
