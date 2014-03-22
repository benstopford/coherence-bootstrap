package com.benstopford.coherence.bootstrap.structures.framework;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessLogger {
    public static final LogTo loggingProfile = LogTo.fileOnly;
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
            System.out.println("                       _____      _                                    _                       _               _____  _           _     _          _ \n                      / ____|    | |                                  | |                     (_)             |  __ \\(_)         | |   | |        | |\n                     | |     ___ | |__   ___ _ __ ___ _ __   ___ ___  | |     ___   __ _  __ _ _ _ __   __ _  | |  | |_ ___  __ _| |__ | | ___  __| |\n                     | |    / _ \\| '_ \\ / _ \\ '__/ _ \\ '_ \\ / __/ _ \\ | |    / _ \\ / _` |/ _` | | '_ \\ / _` | | |  | | / __|/ _` | '_ \\| |/ _ \\/ _` |\n                     | |___| (_) | | | |  __/ | |  __/ | | | (_|  __/ | |___| (_) | (_| | (_| | | | | | (_| | | |__| | \\__ \\ (_| | |_) | |  __/ (_| |\n                      \\_____\\___/|_| |_|\\___|_|  \\___|_| |_|\\___\\___| |______\\___/ \\__, |\\__, |_|_| |_|\\__, | |_____/|_|___/\\__,_|_.__/|_|\\___|\\__,_|\n                                                                                   __/ | __/ |         __/ |                                        \n                                                                                   |___/ |___/         |___/                                         ");
            System.out.println("                             ******************** See ./log dir for all process logs. Change setting via ProcessLogger.LogTo *******************");
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
