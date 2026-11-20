public class ErrorCodeVsException {
    static int viaException(String s) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return -1; } }
    static int viaCheck(String s) {                       // error-code style: validate first
        if (s.isEmpty()) return -1;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return -1;
        return Integer.parseInt(s);
    }
    static long time(java.util.function.ToIntFunction<String> f, String in, int n) {
        long sink = 0; long t = System.nanoTime();
        for (int i = 0; i < n; i++) sink += f.applyAsInt(in);
        long d = System.nanoTime() - t; if (sink == 42) System.out.print(""); return d / n;
    }
    static int deep(int depth, String s) { return depth == 0 ? viaException(s) : deep(depth - 1, s) + 0; }
    public static void main(String[] a) {
        int N = 300_000;
        for (int w = 0; w < 3; w++) { time(ErrorCodeVsException::viaException, "abc", N); time(ErrorCodeVsException::viaCheck, "abc", N); }
        System.out.println("ns per call (average of " + N + " calls)");
        System.out.printf("%-38s %8s %8s%n", "case", "valid", "invalid");
        System.out.printf("%-38s %8d %8d%n", "parseInt + catch (exception)", time(ErrorCodeVsException::viaException, "1234", N), time(ErrorCodeVsException::viaException, "abc", N));
        System.out.printf("%-38s %8d %8d%n", "pre-check + error code (-1)", time(ErrorCodeVsException::viaCheck, "1234", N), time(ErrorCodeVsException::viaCheck, "abc", N));
        System.out.println();
        System.out.println("Exception thrown at different stack depths (ns per throw+catch)");
        for (int d : new int[]{1, 10, 50, 200}) {
            int n = 100_000; long t = System.nanoTime();
            for (int i = 0; i < n; i++) deep(d, "abc");
            System.out.printf("  depth %-4d %8d ns%n", d, (System.nanoTime() - t) / n);
        }
    }
}
