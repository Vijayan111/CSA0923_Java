import java.util.*;

public class PharmacyBench {
    static long sink;

    static double avgMs(Runnable r, int reps) {          // median of 'reps' runs (reduces GC noise)
        r.run(); r.run();
        double[] t = new double[reps];
        for (int i = 0; i < reps; i++) { long s = System.nanoTime(); r.run(); t[i] = (System.nanoTime() - s) / 1e6; }
        Arrays.sort(t);
        return t[reps / 2];
    }

    static void orderCheck() {
        int[] codes = {405, 102, 733, 220, 518};
        Map<Integer, Integer> h = new HashMap<>(), t = new TreeMap<>(), l = new LinkedHashMap<>();
        for (int c : codes) { h.put(c, c % 50); t.put(c, c % 50); l.put(c, c % 50); }
        System.out.println("Order check (added: 405 102 733 220 518)");
        System.out.println("HashMap        keys: " + h.keySet());
        System.out.println("TreeMap        keys: " + t.keySet() + "   <- code order");
        System.out.println("LinkedHashMap  keys: " + l.keySet() + "   <- insertion order");
        System.out.println();
    }

    static void bench(int n, boolean print) {
        Random rnd = new Random(5);
        Integer[] codes = new Integer[n];
        for (int i = 0; i < n; i++) codes[i] = 100000 + i;
        Collections.shuffle(Arrays.asList(codes), rnd);        // insertion order is NOT code order
        int[] probe = new int[100000];
        for (int i = 0; i < probe.length; i++) probe[i] = codes[rnd.nextInt(n)];

        Map<Integer, Integer> hm = new HashMap<>(), tm = new TreeMap<>(), lm = new LinkedHashMap<>();
        List<Integer> addedOrder = new ArrayList<>(Arrays.asList(codes));   // extra list needed by HashMap/TreeMap
        double[] put = new double[3];
        Map<?, ?>[] maps = {hm, tm, lm};
        for (int m = 0; m < 3; m++) {
            @SuppressWarnings("unchecked") Map<Integer, Integer> mp = (Map<Integer, Integer>) maps[m];
            long s = System.nanoTime();
            for (int c : codes) mp.put(c, c % 97);
            put[m] = (System.nanoTime() - s) / (double) n;
        }
        // sorted array + binarySearch
        int[] sc = new int[n], ss = new int[n];
        for (int i = 0; i < n; i++) sc[i] = codes[i];
        Arrays.sort(sc);
        for (int i = 0; i < n; i++) ss[i] = sc[i] % 97;

        double[] look = new double[4], repCode = new double[4], repAdd = new double[4];
        for (int m = 0; m < 3; m++) {
            @SuppressWarnings("unchecked") Map<Integer, Integer> mp = (Map<Integer, Integer>) maps[m];
            look[m] = avgMs(() -> { for (int p : probe) sink += mp.get(p); }, 7);
            // report A: all medicines in code order
            Runnable a = () -> {
                StringBuilder sb = new StringBuilder();
                if (mp instanceof TreeMap) { for (Map.Entry<Integer, Integer> e : mp.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()).append('\n'); }
                else { List<Integer> ks = new ArrayList<>(mp.keySet()); Collections.sort(ks); for (int k : ks) sb.append(k).append(':').append(mp.get(k)).append('\n'); }
                sink += sb.length();
            };
            repCode[m] = avgMs(a, 7);
            // report B: medicines in the order they were added
            Runnable b = () -> {
                StringBuilder sb = new StringBuilder();
                if (mp instanceof LinkedHashMap) { for (Map.Entry<Integer, Integer> e : mp.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()).append('\n'); }
                else { for (int k : addedOrder) sb.append(k).append(':').append(mp.get(k)).append('\n'); }
                sink += sb.length();
            };
            repAdd[m] = avgMs(b, 7);
        }
        look[3] = avgMs(() -> { for (int p : probe) sink += ss[Arrays.binarySearch(sc, p)]; }, 7);
        repCode[3] = avgMs(() -> { StringBuilder sb = new StringBuilder(); for (int i = 0; i < n; i++) sb.append(sc[i]).append(':').append(ss[i]).append('\n'); sink += sb.length(); }, 7);
        repAdd[3] = Double.NaN;
        // insertion into the sorted array (shift) - 200 new medicines
        double ins = avgMs(() -> {
            int[] c2 = Arrays.copyOf(sc, n + 200);
            int size = n;
            for (int k = 0; k < 200; k++) {
                int code = 5000 + k * 7;
                int pos = -Arrays.binarySearch(c2, 0, size, code) - 1;
                System.arraycopy(c2, pos, c2, pos + 1, size - pos);
                c2[pos] = code; size++;
            }
            sink += c2[0];
        }, 3) * 1e6 / 200;
        if (!print) return;
        System.out.println("n = " + n + " medicines   (times in ms, median of 7 runs; put in ns per entry)");
        System.out.printf("%-26s %10s %10s %14s %14s%n", "", "HashMap", "TreeMap", "LinkedHashMap", "sorted array");
        System.out.printf("%-26s %10.0f %10.0f %14.0f %14s%n", "put (ns per entry)", put[0], put[1], put[2], String.format("%.0f ns*", ins));
        System.out.printf("%-26s %10.2f %10.2f %14.2f %14.2f%n", "100,000 lookups (ms)", look[0], look[1], look[2], look[3]);
        System.out.printf("%-26s %10.2f %10.2f %14.2f %14.2f%n", "report: code order (ms)", repCode[0], repCode[1], repCode[2], repCode[3]);
        System.out.printf("%-26s %10.2f %10.2f %14.2f %14s%n", "report: added order (ms)", repAdd[0], repAdd[1], repAdd[2], "needs extra list");
        System.out.printf("%-26s %10.0f %10.0f %14.0f %14.0f%n", "ns per lookup", look[0] * 10, look[1] * 10, look[2] * 10, look[3] * 10);
        System.out.println();
    }

    public static void main(String[] args) {
        orderCheck();
        bench(10000, false);
        for (int n : new int[]{10000, 100000, 1000000}) bench(n, true);
        System.out.println("* sorted array: one insertion of a new code (binary search + arraycopy shift), ns");
        if (sink == 42) System.out.println();
    }
}
