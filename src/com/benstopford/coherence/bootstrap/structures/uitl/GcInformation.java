package com.benstopford.coherence.bootstrap.structures.uitl;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GcInformation {

    private static final String GC_BEAN_NAME =
            "java.lang:type=GarbageCollector,name=ConcurrentMarkSweep";
    private static volatile GarbageCollectorMXBean gcMBean;

    public GcInformation() {
    }

    // initialize the GC MBean field
    private static void initGCMBean() {
        if (gcMBean == null) {
            synchronized (GcInformation.class) {
                if (gcMBean == null) {
                    gcMBean = getGCMBean();
                }
            }
        }
    }
    // get the GarbageCollectorMXBean MBean from the
    // platform MBean server
    public static GarbageCollectorMXBean getGCMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            GarbageCollectorMXBean bean =
                    ManagementFactory.newPlatformMXBeanProxy(server,
                            GC_BEAN_NAME, GarbageCollectorMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    public static boolean printGCInfo() {
        // initialize GC MBean
        initGCMBean();
        try {
            GcInfo gci = gcMBean.getLastGcInfo();
            while(gci==null){
                gci =  getGCMBean().getLastGcInfo();
                Thread.sleep(1000);
                System.out.println("Waiting for bean "+getGCMBean());
            }
            long id = gci.getId();
            long startTime = gci.getStartTime();
            long endTime = gci.getEndTime();
            long duration = gci.getDuration();

            if (startTime == endTime) {
                return false;   // no gc
            }
            System.out.println("GC ID: "+id);
            System.out.println("Start Time: "+startTime);
            System.out.println("End Time: "+endTime);
            System.out.println("Duration: "+duration);
            Map mapBefore = gci.getMemoryUsageBeforeGc();
            Map mapAfter = gci.getMemoryUsageAfterGc();

            System.out.println("Before GC Memory Usage Details....");
            Set memType = mapBefore.keySet();
            Iterator it = memType.iterator();
            while(it.hasNext()) {
                String type = (String)it.next();
                System.out.println(type);
                MemoryUsage mu1 = (MemoryUsage) mapBefore.get(type);
                System.out.print("Initial Size: "+mu1.getInit());
                System.out.print(" Used: "+ mu1.getUsed());
                System.out.print(" Max: "+mu1.getMax());
                System.out.print(" Committed: "+mu1.getCommitted());
                System.out.println(" ");
            }

            System.out.println("After GC Memory Usage Details....");
            memType = mapAfter.keySet();
            it = memType.iterator();
            while(it.hasNext()) {
                String type = (String)it.next();
                System.out.println(type);
                MemoryUsage mu2 = (MemoryUsage) mapAfter.get(type);
                System.out.print("Initial Size: "+mu2.getInit());
                System.out.print(" Used: "+ mu2.getUsed());
                System.out.print(" Max: "+mu2.getMax());
                System.out.print(" Committed: "+mu2.getCommitted());
                System.out.println(" ");
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
        return true;
    }


    public static void main(String[] args) {
        // Print the last gc information
        boolean ret = printGCInfo();
    }
}