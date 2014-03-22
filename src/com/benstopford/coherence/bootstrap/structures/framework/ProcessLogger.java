package com.benstopford.coherence.bootstrap.structures.framework;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessLogger {

    public static final File file = new File("log/coherence-processes.log");
    private static BufferedWriter fileWriter;
    private static int processCounter;
    private Process process;

    enum LogTo {
        fileOnly, fileAndConsole, consoleOnly;

        public boolean in(LogTo... options) {
            for (LogTo l : options) {
                if (l == this) return true;
            }
            return false;
        }
    }

    public ProcessLogger(LogTo logTo, Process process) throws Exception {
        this.process = process;
        processCounter++;
        if (file==null || !file.exists() || fileWriter == null) {
            fileWriter = new BufferedWriter(new FileWriter(file, true));
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(loggingRunnable(logTo, process.getInputStream()));
        pool.submit(loggingRunnable(logTo, process.getErrorStream()));
    }

    private boolean stillAlive() {
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            return true;
        }
        return false;
    }

    private Runnable loggingRunnable(final LogTo logTo, final InputStream in) throws FileNotFoundException {
        final BufferedReader lineReader = new BufferedReader(new InputStreamReader(in));

        return new Runnable() {
            public void run() {
                try {
                    while (true) {
                        try {
                            String name = "process-" + processCounter + ":";

                            String line = lineReader.readLine();

                            if (logTo.in(LogTo.consoleOnly, LogTo.fileAndConsole)) {
                                System.out.println(name + line);
                            }
                            if (logTo.in(LogTo.fileOnly, LogTo.fileAndConsole)) {
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

}
