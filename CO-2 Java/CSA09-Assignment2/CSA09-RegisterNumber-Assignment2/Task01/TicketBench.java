import java.lang.management.ManagementFactory;
import java.util.function.Function;

public class TicketBench {
    static final com.sun.management.ThreadMXBean TM =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static long sink;

    static String[] fields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = "F" + i + "=value" + i + ";";
        return f;
    }
    static String viaPlus(String[] f)   { String s = ""; for (String x : f) s += x; return s; }
    static String viaSB(String[] f)     { StringBuilder b = new StringBuilder(); for (String x : f) b.append(x); return b.toString(); }
    static String viaSBuf(String[] f)   { StringBuffer b = new StringBuffer(); for (String x : f) b.append(x); return b.toString(); }
    static String viaJoin(String[] f)   { return String.join("", f); }
    static String viaJoiner(String[] f) { java.util.StringJoiner j = new java.util.StringJoiner(""); for (String x : f) j.add(x); return j.toString(); }
    static String viaFormat(String[] f) { return String.format("%s".repeat(f.length), (Object[]) f); }

    // runs 'tickets' tickets per run; returns {avg microseconds per run, MB allocated per run}
    static double[] measure(Function<String[], String> fn, String[] f, int tickets, int reps) {
        for (int w = 0; w < 2; w++) for (int t = 0; t < tickets; t++) sink += fn.apply(f).length();
        long time = 0, alloc = 0;
        for (int r = 0; r < reps; r++) {
            long a0 = TM.getThreadAllocatedBytes(Thread.currentThread().getId());
            long t0 = System.nanoTime();
            for (int t = 0; t < tickets; t++) sink += fn.apply(f).length();
            time += System.nanoTime() - t0;
            alloc += TM.getThreadAllocatedBytes(Thread.currentThread().getId()) - a0;
        }
        return new double[]{time / (double) reps / 1000.0, alloc / (double) reps / 1048576.0};
    }

    static void row(String name, Function<String[], String> fn, String[] f, int tickets, int reps, long est) {
        double[] m = measure(fn, f, tickets, reps);
        System.out.printf("%-14s %14.1f %12.2f %16d%n", name, m[0], m[1], est);
    }

    public static void main(String[] args) {
        int[][] workloads = {{1, 10, 20000}, {10000, 50, 5}, {1, 20000, 5}};
        String[] names = {"W1: 1 ticket x 10 fields", "W2: 10,000 tickets x 50 fields", "W3: 1 ticket x 20,000 fields"};
        for (int w = 0; w < 3; w++) {
            int tk = workloads[w][0], n = workloads[w][1], reps = workloads[w][2];
            String[] f = fields(n);
            System.out.println(names[w] + "   (avg of " + reps + " runs)");
            System.out.printf("%-14s %14s %12s %16s%n", "approach", "time(us/run)", "alloc(MB)", "est. String objs");
            row("String +",      TicketBench::viaPlus,   f, tk, reps, (long) tk * n);
            row("StringBuilder", TicketBench::viaSB,     f, tk, reps, (long) tk);
            row("StringBuffer",  TicketBench::viaSBuf,   f, tk, reps, (long) tk);
            row("String.join",   TicketBench::viaJoin,   f, tk, reps, (long) tk);
            row("StringJoiner",  TicketBench::viaJoiner, f, tk, reps, (long) tk);
            row("String.format", TicketBench::viaFormat, f, tk, reps, (long) tk);
            System.out.println();
        }
        System.out.println("Doubling test: one ticket, n fields (us per ticket)");
        System.out.printf("%8s %14s %14s%n", "n", "String +", "StringBuilder");
        double prev = 0;
        for (int n = 2500; n <= 20000; n *= 2) {
            String[] f = fields(n);
            double p = measure(TicketBench::viaPlus, f, 1, 5)[0];
            double b = measure(TicketBench::viaSB, f, 1, 5)[0];
            System.out.printf("%8d %14.1f %14.1f   %s%n", n, p, b, prev == 0 ? "" : String.format("+ grew x%.1f", p / prev));
            prev = p;
        }
        if (sink == 42) System.out.println();
    }
}
