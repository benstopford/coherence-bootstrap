package com.benstopford.coherence.bootstrap.structures.util;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Modified version of the below sample class adding support for multiple collectors https://blogs.oracle.com/poonam/entry/how_to_programmatically_obtain_gc
 */
public class GcInformation {
    private static volatile GarbageCollectorMXBean[] gcMBean;

    public GcInformation() {
    }

    // initialize the GC MBean field
    private static void initGCMBean() {
        if (gcMBean == null) {
            synchronized (GcInformation.class) {
                if (gcMBean == null) {
                    gcMBean = beans();
                }
            }
        }
    }

    // get the GarbageCollectorMXBean MBean from the
    // platform MBean server
    public static GarbageCollectorMXBean getOldGenBean() {
        GarbageCollectorMXBean[] beans = beans();
        for(GarbageCollectorMXBean bean: beans){
            for(Old type: Old.values()) {
               if( bean.getName().replace(" ","").contains(type.name())){
                   return bean;
               }
            }
        }
        throw new RuntimeException("Could not the appropriate collector in the list: "+beans);
    }
    public static GarbageCollectorMXBean getYoungGenBean() {
        GarbageCollectorMXBean[] beans = beans();
        for(GarbageCollectorMXBean bean: beans){
            for(Young type: Young.values()) {
               if( bean.getName().contains(type.name())){
                   return bean;
               }
            }
        }
        throw new RuntimeException("Could not the appropriate collector in the list: "+beans);
    }

    private static GarbageCollectorMXBean[] beans() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Iterator<ObjectName> names = server.queryNames(new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*"), null).iterator();
            GarbageCollectorMXBean[] result = new GarbageCollectorMXBean[2];
            result[0] =
                    ManagementFactory.newPlatformMXBeanProxy(server,
                            names.next().getCanonicalName(), GarbageCollectorMXBean.class);
            result[1] =
                    ManagementFactory.newPlatformMXBeanProxy(server,
                            names.next().getCanonicalName(), GarbageCollectorMXBean.class);

            return result;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    enum Young {
        Copy,
        ParNew,
        PSScavenge,
    }

    enum Old {
        MarkSweepCompact,
        ConcurrentMarkSweep,
        PSMarkSweep
    }


    public GarbageCollectorMXBean[] gcBean() {
        initGCMBean();
        return gcMBean;
    }

    public static boolean printGCInfo() {
        // initialize GC MBean
        initGCMBean();
        for (GarbageCollectorMXBean bean : gcMBean) {
            try {
                GcInfo gci = bean.getLastGcInfo();
                long id = gci.getId();
                long startTime = gci.getStartTime();
                long endTime = gci.getEndTime();
                long duration = gci.getDuration();

                if (startTime == endTime) {
                    return false;   // no gc
                }
                System.out.println("GC ID: " + id);
                System.out.println("Start Time: " + startTime);
                System.out.println("End Time: " + endTime);
                System.out.println("Duration: " + duration);
                Map mapBefore = gci.getMemoryUsageBeforeGc();
                Map mapAfter = gci.getMemoryUsageAfterGc();

                System.out.println("Before GC Memory Usage Details....");
                Set memType = mapBefore.keySet();
                Iterator it = memType.iterator();
                while (it.hasNext()) {
                    String type = (String) it.next();
                    System.out.println(type);
                    MemoryUsage mu1 = (MemoryUsage) mapBefore.get(type);
                    System.out.print("Initial Size: " + mu1.getInit());
                    System.out.print(" Used: " + mu1.getUsed());
                    System.out.print(" Max: " + mu1.getMax());
                    System.out.print(" Committed: " + mu1.getCommitted());
                    System.out.println(" ");
                }

                System.out.println("After GC Memory Usage Details....");
                memType = mapAfter.keySet();
                it = memType.iterator();
                while (it.hasNext()) {
                    String type = (String) it.next();
                    System.out.println(type);
                    MemoryUsage mu2 = (MemoryUsage) mapAfter.get(type);
                    System.out.print("Initial Size: " + mu2.getInit());
                    System.out.print(" Used: " + mu2.getUsed());
                    System.out.print(" Max: " + mu2.getMax());
                    System.out.print(" Committed: " + mu2.getCommitted());
                    System.out.println(" ");
                }
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception exp) {
                throw new RuntimeException(exp);
            }
        }
        return true;
    }


    public static void main(String[] args) {
        // Print the last gc information
        boolean ret = printGCInfo();
    }
}