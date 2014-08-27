package com.benstopford.coherence.bootstrap.structures.framework;

import java.util.ArrayList;
import java.util.Properties;

public class ProcessExecutor {
    public static int COHERERENCE_PROCESS_MEMORY = 128;
    private final ArrayList<Process> runningProcesses = new ArrayList<Process>();
    private Properties defaultProperties;

    public ProcessExecutor(Properties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    private String convertToMinusD(Properties props) {
        String out = "";
        for (Object p : props.keySet()) {
            String value = (String) props.get(p);
            if (!value.contains(" ") & value.trim().length() > 0)
                out += String.format("-D%s=%s ", p, value);
        }
        return out;
    }

    public Process startOutOfProcess(String config, String propertiesAdditions) {
        Process process = null;
        try {
            String command = String.format("java -javaagent:lib/SizeOf.jar -Xms%sm -Xmx%sm -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails %s %s %s com.tangosol.net.DefaultCacheServer",
                    COHERERENCE_PROCESS_MEMORY,
                    COHERERENCE_PROCESS_MEMORY,
                    convertToMinusD(properties(config)),
                    propertiesAdditions,
                    classpath());
            process = Runtime.getRuntime().exec(command);

            ProcessLogger.wrapLogging(process);
            checkForSuccesfulStart(process);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return process;
    }

    private Properties properties(String config) {
        Properties properties = defaultProperties;
        properties.put("tangosol.coherence.cacheconfig", config);
        copyIfPresent("com.benstopford.extend.port", properties);
        copyIfPresent("com.benstopford.extend.port2", properties);
        return properties;
    }

    private String classpath() {
        String sep = System.getProperty("path.separator");
        return "-cp "
                + "classes" + sep
                + "lib/coherence-utils.jar" + sep
                + "config" + sep
                + parse(System.getProperty("java.class.path"))
                + " ";
    }

    protected void copyIfPresent(String s, Properties p) {
        String value = System.getProperty(s);
        if (value != null) {
            p.put(s, value);
        }
    }

    private String parse(String property) {
        //intelij adds crap onto the classpath :(
        String[] split = property.split(" ");
        String found = "";
        for (String s : split) {
            if (s.contains("coherence")) { //hack to identify the actual entry for the classpath
                found = s;
            }
        }

        return found;
    }

    private void checkForSuccesfulStart(Process process) throws InterruptedException {
        try {
            process.exitValue();
            throw new RuntimeException("Coherence process failed to start!!");
        } catch (Exception hopedFor) {
        }
        runningProcesses.add(process);
        Thread.sleep(5000);
    }


    void killOpenCoherenceProcesses() {
        for (Process process : runningProcesses) {
            process.destroy();
            while (true) {
                try {
                    process.exitValue();
                    break;
                } catch (IllegalThreadStateException mustStillBeRunning) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
