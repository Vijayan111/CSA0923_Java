import java.lang.management.ManagementFactory;
import java.util.*;

/** Map loaded once vs "fetch every time" (simulated as a full scan of the doctor table; a real DB adds a network round trip). */
public class DoctorBench {
    static long alloc() { return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().getId()); }
    static volatile int sink;
    public static void main(String[] a) {
        System.out.println("  d     m      | Map lookup (ns/change) | scan+build list (ns/change) | Map build alloc (MB) | per-change list (KB)");
        int[][] cases = {{10, 100}, {50, 5_000}, {200, 100_000}};
        for (int[] c : cases) {
            int d = c[0], m = c[1]; List<Doctor> table = new ArrayList<>(m);
            for (int i = 0; i < m; i++) table.add(new Doctor("Dr. Name" + i, "Dept" + (i % d)));
            long before = alloc();
            DoctorDirectory dir = new DoctorDirectory(table);
            long mapBytes = alloc() - before;
            String[] depts = new String[d]; for (int i = 0; i < d; i++) depts[i] = "Dept" + i;
            int N = Math.max(2000, 2_000_000 / m); long bestMap = Long.MAX_VALUE, bestScan = Long.MAX_VALUE;
            for (int rep = 0; rep < 5; rep++) {
                long t = System.nanoTime(); for (int i = 0; i < N; i++) sink += dir.doctorsIn(depts[i % d]).size(); bestMap = Math.min(bestMap, System.nanoTime() - t);
                t = System.nanoTime();
                for (int i = 0; i < N; i++) { List<Doctor> r = new ArrayList<>(); String want = depts[i % d];
                    for (Doctor x : table) if (x.department().equals(want)) r.add(x); sink += r.size(); }
                bestScan = Math.min(bestScan, System.nanoTime() - t);
            }
            System.out.printf("%4d %8d | %22.1f | %27.1f | %15.2f | %.1f%n", d, m, bestMap / (double) N, bestScan / (double) N,
                Math.max(mapBytes, 0) / 1048576.0, (m / (double) d) * 8 / 1024);   // one ArrayList of m/d references (8 bytes each; Doctor objects already exist in the table)
            sink += dir.departments().size();
        }
    }
}
