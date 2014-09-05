package com.benstopford.coherence.bootstrap.structures.framework.cluster;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Push standard out/err from the a process to the console or to a file
 */
public class ProcessLogger {
    public static final LogTo loggingProfile = LogTo.fileOnly;
    public static final File file = new File("log/coherence-processes.log");
    private static BufferedWriter fileWriter;
    private static int processCounter;
    private Process process;

    public enum LogTo {
        fileOnly, fileAndConsole, consoleOnly;

        public boolean in(LogTo... options) {
            for (LogTo l : options) {
                if (l == this) return true;
            }
            return false;
        }
    }

    public static final void wrapLogging(Process p) throws Exception {
        new ProcessLogger(p);
    }

    public ProcessLogger(Process process) throws Exception {
        this.process = process;
        processCounter++;
        if (file == null || !file.exists() || fileWriter == null) {
            fileWriter = new BufferedWriter(new FileWriter(file, true));
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(loggingRunnable(process.getInputStream()));
        pool.submit(loggingRunnable(process.getErrorStream()));
    }

    private boolean stillAlive() {
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            return true;
        }
        return false;
    }

    private Runnable loggingRunnable(final InputStream in) throws FileNotFoundException {
        final BufferedReader lineReader = new BufferedReader(new InputStreamReader(in));

        return new Runnable() {
            public void run() {
                try {
                    while (true) {
                        try {
                            String name = "process-" + processCounter + ":";

                            String line = lineReader.readLine();

                            if (loggingProfile.in(LogTo.consoleOnly, LogTo.fileAndConsole)) {
                                System.out.println(name + line);
                            }
                            if (loggingProfile.in(LogTo.fileOnly, LogTo.fileAndConsole)) {
                                synchronized (fileWriter) {
                                    fileWriter.write(name + line);
                                    fileWriter.newLine();
                                    fileWriter.flush();
                                }
                            }

                            if (!stillAlive()) {
                                break;
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }
                } finally {
                    try {
                        fileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        };
    }

    public static void switchStdErrToFile() {
        if (loggingProfile.in(LogTo.fileOnly)) {
            System.out.println();
            System.out.println("**********************************************************************************");
            System.out.println("*************************COHERENCE LOGGING DISABLED*******************************");
            System.out.println("*** See ./log dir for all process logs. Change setting via ProcessLogger.LogTo ***");
            System.out.println("**********************************************************************************");
            System.out.println();

            try {
                PrintStream err = new PrintStream(new FileOutputStream("log/test-stderr.log"));
                System.setErr(err);
                System.err.println("This file contains the stderr, redirected from the test process");
                err.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
