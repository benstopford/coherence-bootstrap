package com.benstopford.coherence.bootstrap.structures.framework;

import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class TestUtils {
    static{
        System.setProperty("tangosol.coherence.override", "config/tangosol-coherence-override.xml");
    }
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;


    public void startLocalJMXServer(int port) throws IOException {
        if (serverRunning(port)) {
            return;
        }

        String url = "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi";
        JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL(url),
                null,
                ManagementFactory.getPlatformMBeanServer()
        ).start();
    }

    private boolean serverRunning(int port) {
        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException justMeansItWasCreatedAlready) {
            return true;
        }
        return false;
    }

}
