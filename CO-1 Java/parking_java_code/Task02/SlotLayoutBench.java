// Task 2 alternative - jagged array vs one-dimensional array vs ArrayList<String[]>
import java.util.ArrayList;
import java.util.List;

public class SlotLayoutBench {
    public static void main(String[] args) {
        int[] slotsPerFloor = {20, 30, 40, 50, 60};     // floors with different sizes
        int F = slotsPerFloor.length, total = 0;
        int[] start = new int[F + 1];                   // prefix sums, needed for the 1-D layout
        for (int f = 0; f < F; f++) { start[f] = total; total += slotsPerFloor[f]; }
        start[F] = total;

        String[][] jagged = new String[F][];
        List<String[]> list = new ArrayList<>();
        String[] flat = new String[total];
        for (int f = 0; f < F; f++) { jagged[f] = new String[slotsPerFloor[f]]; list.add(new String[slotsPerFloor[f]]); }

        // what a fixed formula floor*slotsPerFloor+slot would give for floor 4, slot 59 with S=60 : 299 (outside array of 200)
        System.out.println("total slots = " + total + "; naive formula 4*60+59 = " + (4 * 60 + 59) + " -> out of range");
        System.out.println("prefix formula start[4]+59 = " + (start[4] + 59));

        int N = 50_000_000;
        long sink = 0;
        for (int w = 0; w < 2; w++) sink += run(jagged, list, flat, start, F, slotsPerFloor, 5_000_000);   // warm up
        long t0 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % F, s = i % slotsPerFloor[f]; if (jagged[f][s] == null) sink++; }
        long t1 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % F, s = i % slotsPerFloor[f]; if (flat[start[f] + s] == null) sink++; }
        long t2 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % F, s = i % slotsPerFloor[f]; if (list.get(f)[s] == null) sink++; }
        long t3 = System.nanoTime();
        System.out.printf("locate slot, ns per access: jagged %.2f | 1-D with offsets %.2f | ArrayList<String[]> %.2f  (sink %d)%n",
                (t1 - t0) / (double) N, (t2 - t1) / (double) N, (t3 - t2) / (double) N, sink);
    }
    static long run(String[][] j, List<String[]> l, String[] fl, int[] st, int F, int[] spf, int n) {
        long s = 0;
        for (int i = 0; i < n; i++) { int f = i % F, x = i % spf[f]; if (j[f][x] == null) s++; if (fl[st[f] + x] == null) s++; if (l.get(f)[x] == null) s++; }
        return s;
    }
}
