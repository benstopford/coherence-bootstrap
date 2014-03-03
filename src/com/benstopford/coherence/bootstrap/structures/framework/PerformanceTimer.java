package com.benstopford.coherence.bootstrap.structures.framework;

import java.text.DecimalFormat;

public class PerformanceTimer {
    public enum TimeFormat {ns, us, ms, s}

    private static long start;

    public static void start() {
        start = System.nanoTime();
    }

    public static Took end() {
        return new Took(System.nanoTime() - start);
    }

    public static class Took {
        long tookInNs;

        public Took(long ns) {
            this.tookInNs = ns;
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
    }
}
