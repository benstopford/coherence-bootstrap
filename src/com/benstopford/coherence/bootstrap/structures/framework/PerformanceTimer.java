package com.benstopford.coherence.bootstrap.structures.framework;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PerformanceTimer {

    public enum TimeFormat {ns, us, ms, s}

    private static long start;
    private static List<Long> checkpoints = new ArrayList<Long>();

    public static void start() {
        start = System.nanoTime();
        checkpoints.clear();
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
            return tookInNs / 1000;
        }

        public long ms() {
            return tookInNs / 1000 / 1000;
        }

        public long s() {
            return tookInNs;
        }

        public long took(TimeFormat format) {
            switch (format) {
                case ns:
                    return ns();
                case us:
                    return us();
                case ms:
                    return ms();
                case s:
                    return s();
            }
            return -1;
        }

        public double average(long iterations, TimeFormat f) {
            return took(f) / iterations;
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
            return print(format(prefix, TimeFormat.ns));
        }

        public Took printUs(String prefix) {
            return print(format(prefix, TimeFormat.us));
        }

        public Took printMs(String prefix) {
            return print(format(prefix, TimeFormat.ms));
        }

        public Took printS(String prefix) {
            return print(format(prefix, TimeFormat.s));
        }

        public Took print(TimeFormat format, String prefix) {
            took(format);
            return print(prefix + s());
        }

        public Took printAverage(long iterations) {
            return printAverage(iterations, TimeFormat.us);
        }

        public Took printAverage(long iterations, TimeFormat f) {
            return printAverage(iterations, f, String.format("The average over %s iterations was: ", iterations));
        }

        public Took printAverage(long iterations, TimeFormat f, String prefex) {
            print(format(prefex, average(iterations, f), f));
            return this;
        }

        public Took printAverageOfCheckpoints() {
            if (checkpoints.length == 0) {
                throw new IllegalArgumentException("This method requires that you collected checkpoints during your run.");
            }
            Double mean = mean();
            Double sd = standardDeviation(mean);

            DecimalFormat f = decimalFormat(TimeFormat.ns);

            System.out.println("Checkpoints: "+listDeltas());

            System.out.printf("Average(%s checkpoints): %sns\n"
                    , checkpoints.length
                    , f.format(mean));
            System.out.printf("Standard Deviation: %sns\n", f.format(sd));
            return this;
        }

        private Double standardDeviation(Double mean) {
            double totalDeltaSq = 0;
            long previous = start;
            for (long checkpoint : checkpoints) {
                double deltaSq = deltaSq(mean, previous, checkpoint);
                totalDeltaSq += deltaSq;
                previous = checkpoint;
            }
            return Math.sqrt(totalDeltaSq / (checkpoints.length));
        }

        private double deltaSq(Double mean, long previous, long next) {
            long valueNs = next - previous;
            double delta = mean - valueNs;
            return delta * delta;
        }

        private Double mean() {
            long total = 0;
            long previous = start;
            for (long checkpoint : checkpoints) {
                long delta = checkpoint - previous;
                total += delta;
                previous = checkpoint;
            }
            return ((double) total) / (checkpoints.length);
        }

        private List<Long> listDeltas() {
            List<Long> deltas = new ArrayList<Long>(checkpoints.length);
            long previous = start;
            for (long checkpoint : checkpoints) {
                deltas.add(checkpoint - previous);
            }
            return deltas;
        }

        private String format(String prefix, TimeFormat f) {
            return format(prefix, Double.valueOf(took(f)), f);
        }

        private String format(String prefix, double took, TimeFormat format) {
            DecimalFormat decFormat = decimalFormat(format);

            if (prefix.contains("%")) {
                return (String.format(prefix.replace("%", "%s" + format), decFormat.format(took)));
            } else {
                return (prefix + decFormat.format(took) + format);
            }
        }

        private DecimalFormat decimalFormat(TimeFormat format) {
            DecimalFormat decFormat = null;
            switch (format) {
                case ns:
                    decFormat = new DecimalFormat("#,###");
                    break;
                case us:
                    decFormat = new DecimalFormat("#,###.0");
                    break;
                case ms:
                    decFormat = new DecimalFormat("#,###.0");
                    break;
                case s:
                    decFormat = new DecimalFormat("#,###.0");
            }
            return decFormat;
        }

        private Took print(String s) {
            System.out.println(s);
            return this;
        }

        public String toString() {
            return format("Took: ", TimeFormat.ns);
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

        end.printAverage(10, TimeFormat.ns);
        //==> The average over 10 iterations was: 20,643,600ns

        end.printAverage(10, TimeFormat.us, "The av time over 10 cycles of % was a great result");
        //==> The av time over 10 cycles of 20,586.0us was a great result

        end.printAverage(10).printUs("The total was:");
        //==> The average over 10 iterations was: 20,643.0us
        //==> Total:206,437.0us


        System.out.println("****Using Checkpoints****");

        start();
        for (int i = 0; i < 5; i++) {
            Thread.sleep(5);
            PerformanceTimer.checkpoint();
        }
        end().printAverageOfCheckpoints();
        //=> Average(5 checkpoints): 5,395,400ns
        //=> Standard Deviation: 228,499ns
    }
}