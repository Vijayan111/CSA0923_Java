public class ExceptionCost {
    static class Plain extends Exception { Plain(String m) { super(m); } }
    static class NoTrace extends Exception {
        NoTrace(String m) { super(m, null, false, false); }
    }
    static Exception make(int depth, boolean trace) { return depth == 0 ? (trace ? new Plain("x") : new NoTrace("x")) : make(depth - 1, trace); }
    static long time(int depth, boolean trace, int n) {
        long t = System.nanoTime(); Exception last = null;
        for (int i = 0; i < n; i++) last = make(depth, trace);
        long d = (System.nanoTime() - t) / n; if (last == null) System.out.print(""); return d;
    }
    public static void main(String[] a) {
        for (int w = 0; w < 3; w++) { time(50, true, 50000); time(50, false, 50000); }
        System.out.println("ns to CREATE one exception object (n = 100000)");
        System.out.printf("%-8s %14s %18s%n", "depth", "with stack", "no stack trace");
        for (int d : new int[]{1, 10, 50, 200, 1000})
            System.out.printf("%-8d %14d %18d%n", d, time(d, true, 100000), time(d, false, 100000));
        Runtime rt = Runtime.getRuntime(); System.gc();
        int n = 20000; Object[] keep = new Object[n]; long before = rt.totalMemory() - rt.freeMemory();
        for (int i = 0; i < n; i++) keep[i] = make(50, true);
        long mid = rt.totalMemory() - rt.freeMemory();
        System.out.printf("%nMemory held per exception object with a 50-frame trace: about %d bytes%n", Math.max(0, (mid - before) / n));
        if (keep[0] == null) System.out.print("");
    }
}
