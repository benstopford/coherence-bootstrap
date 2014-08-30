package com.benstopford.coherence.bootstrap.structures.framework;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.*;

public class PerformanceTimer {
    private static long start;
    private static List<Long> checkpoints = new ArrayList<Long>();

    public static void start() {
        start = System.nanoTime();
        checkpoints.clear();
    }

    public static void start(String text, Object... amount) {
        System.out.printf(text+"\n", amount);
        start();
    }

    public static long startTime(){
        return start;
    }

    public static long currentDuration(TimeUnit unit){
        long took = System.nanoTime() - start;
        long convert = unit.convert(took, NANOSECONDS);
        return convert;
    }

    public static void checkpoint() {
        checkpoints.add(System.nanoTime());
    }

    public static Took end() {
        if (checkpoints.size() > 0) {
            return new Took(start, System.nanoTime(), checkpoints.toArray(new Long[]{}));
        } else {
            return new Took(System.nanoTime() - start);
        }
    }


    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static BlockingQueue<String> printQueue = new LinkedBlockingQueue<String>();

    static {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String message = printQueue.take();
                        System.out.printf(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public static void progress(String message, Object... args) {
        printQueue.offer(String.format(message + "\r", args));
    }


    public static class Took {
        long tookInNs;
        long start;
        long end;
        Long[] checkpoints;

        public Took(long ns) {
            this.tookInNs = ns;
        }

        public Took(long start, long end, Long[] checkpoints) {
            this.start = start;
            this.end = end;
            tookInNs = end - start;
            this.checkpoints = checkpoints;
        }

        public long ns() {
            return tookInNs;
        }

        public long us() {
            return us(tookInNs);
        }

        public long ms() {
            return ms(tookInNs);
        }

        public long s() {
            return s(tookInNs);
        }

        private long us(long ns) {
            return MICROSECONDS.convert(ns, NANOSECONDS);
        }

        private long ms(long ns) {
            return MILLISECONDS.convert(ns, NANOSECONDS);
        }

        private long s(long ns) {
            return SECONDS.convert(ns, NANOSECONDS);
        }

        public long took(TimeUnit format) {
            return took(format, tookInNs);
        }

        public long took(TimeUnit format, long took) {
            return format.convert(took, NANOSECONDS);
        }

        public long average(long iterations) {
            return Math.round(tookInNs / iterations);
        }

        public Took printNs() {
            return printNs("Took: ");
        }

        public Took printUs() {
            return printUs("Took: ");
        }

        public Took printMs() {
            return printMs("Took: ");
        }

        public Took printS() {
            return printS("Took: ");
        }

        public Took printNs(String prefix) {
            return print(format(prefix, tookInNs, NANOSECONDS));
        }

        public Took printUs(String prefix) {
            return print(format(prefix, tookInNs, MICROSECONDS));
        }

        public Took printMs(String prefix) {
            return print(format(prefix, tookInNs, MILLISECONDS));
        }

        public Took printS(String prefix) {
            return print(format(prefix, tookInNs, SECONDS));
        }

        public Took print(TimeUnit format, String prefix) {
            took(format);
            return print(prefix + s());
        }

        public Took printAverage(long iterations) {
            return printAverage(iterations, MICROSECONDS);
        }

        public Took printAverage(long iterations, TimeUnit f) {
            return printAverage(iterations, f, String.format("The average over %s iterations was: ", iterations));
        }

        public Took printAverage(long iterations, TimeUnit f, String prefex) {
            long average = average(iterations);
            print(format(prefex, average, f));
            return this;
        }

        public Took printAverageOfCheckpoints(TimeUnit f) {
            if (checkpoints.length == 0) {
                throw new IllegalArgumentException("This method requires that you collected checkpoints during your run.");
            }
            long mean = mean();
            long sd = standardDeviation(mean);

            System.out.println("Elapsed time:" + format(tookInNs, f));
//            System.out.println("Checkpoints: " + prettyPrintDeltas(f));
            System.out.printf("Average(%s checkpoints): %s\n", checkpoints.length, format(mean, f));
            System.out.printf("Standard Deviation: %s\n", format(sd, f));
            return this;
        }

        private long standardDeviation(long mean) {
            double totalDeltaSq = 0;
            long previous = start;
            for (long checkpoint : checkpoints) {
                double deltaSq = deltaSq(mean, previous, checkpoint);
                totalDeltaSq += deltaSq;
                previous = checkpoint;
            }
            return Math.round(Math.sqrt(totalDeltaSq / (checkpoints.length)));
        }

        private double deltaSq(long mean, long previous, long next) {
            long valueNs = next - previous;
            double delta = mean - valueNs;
            return delta * delta;
        }

        private long mean() {
            long total = 0;
            long previous = start;
            for (long checkpoint : checkpoints) {
                long delta = checkpoint - previous;
                total += delta;
                previous = checkpoint;
            }
            return Math.round(((double) total) / (checkpoints.length));
        }

        private List<String> prettyPrintDeltas(TimeUnit f) {
            List<String> deltas = new ArrayList<String>(checkpoints.length);
            long previous = start;
            for (long checkpoint : checkpoints) {
                long deltaNs = checkpoint - previous;
                String pretty = format(deltaNs, f);
                deltas.add(pretty);
                previous = checkpoint;
            }
            return deltas;
        }

        private String format(long took, TimeUnit tu) {
            return format("", took, tu);
        }

        private String format(String prefix, long ns, TimeUnit tu) {
            DecimalFormat decFormat = decimalFormat();
            long took = tu.convert(ns, NANOSECONDS);

            if (prefix.contains("%")) {
                return (String.format(prefix.replace("%", "%s" + little(tu)), decFormat.format(took)));
            } else {
                return (prefix + decFormat.format(took) + little(tu));
            }
        }

        private String little(TimeUnit tu) {
            if (tu == NANOSECONDS) {
                return "ns";
            }
            if (tu == MICROSECONDS) {
                return "us";
            }
            if (tu == MILLISECONDS) {
                return "ms";
            }
            if (tu == SECONDS) {
                return "s";
            }
            throw new RuntimeException(tu.toString() + " not supported");
        }

        private DecimalFormat decimalFormat() {
            return new DecimalFormat("#,###");
        }

        private Took print(String s) {
            System.out.println(s);
            return this;
        }

        public String toString() {
            return "Took: " + format(tookInNs, NANOSECONDS);
        }

        public void printAverageOfCheckpoints() {
            printAverageOfCheckpoints(MILLISECONDS);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        PerformanceTimer.start();
        Thread.sleep(205);

        Took end = PerformanceTimer.end();

        System.out.println(end);
        //==> Took: 205,866,000ns

        System.out.println(end.ms());
        //==>205

        end.printUs();
        //==> Took: 205,866.0us

        end.printAverage(10, NANOSECONDS);
        //==> The average over 10 iterations was: 20,643,600ns

        end.printAverage(10, MICROSECONDS, "The av time over 10 cycles of % was a great result");
        //==> The av time over 10 cycles of 20,586.0us was a great result

        end.printAverage(10).printUs("The total was:");
        //==> The average over 10 iterations was: 20,643.0us
        //==> Total:206,437.0us

        System.out.println("\n****Using Checkpoints****");

        start();
        for (int i = 0; i < 5; i++) {
            Thread.sleep(5);
            PerformanceTimer.checkpoint();
        }
        end().printAverageOfCheckpoints(MICROSECONDS);
        //Elapsed time:24,647us
        //Checkpoints: [4,651us, 4,987us, 5,005us, 4,996us, 4,999us]
        //Average(5 checkpoints): 4,927us
        //Standard Deviation: 138us
    }
}