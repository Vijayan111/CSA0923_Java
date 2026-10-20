// Task 11 alternative - active tickets in array vs ArrayList vs HashMap keyed by registration number
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageBenchmark {
    record Ticket(int no, String regNo) { }
    static long used() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    public static void main(String[] args) {
        int[] sizes = {500, 5_000, 50_000, 500_000};
        System.out.println("n | array scan ns | ArrayList scan ns | HashMap get ns | (find the LAST inserted ticket)");
        for (int n : sizes) {
            Ticket[] arr = new Ticket[n]; List<Ticket> list = new ArrayList<>(); Map<String, Ticket> map = new HashMap<>();
            for (int i = 0; i < n; i++) { Ticket t = new Ticket(i, "TN" + i); arr[i] = t; list.add(t); map.put(t.regNo(), t); }
            String target = "TN" + (n - 1);
            int reps = Math.max(10, 5_000_000 / n); long sink = 0;
            for (int w = 0; w < 3; w++) sink += a(arr, target) + l(list, target) + (map.get(target).no());
            long t0 = System.nanoTime(); for (int i = 0; i < reps; i++) sink += a(arr, target);
            long t1 = System.nanoTime(); for (int i = 0; i < reps; i++) sink += l(list, target);
            long t2 = System.nanoTime(); int hreps = reps * 200; for (int i = 0; i < hreps; i++) sink += map.get(target).no();
            long t3 = System.nanoTime();
            System.out.printf("%d | %d | %d | %d  (sink %d)%n", n, (t1 - t0) / reps, (t2 - t1) / reps, (t3 - t2) / hreps, sink);
        }
        int n = 200_000;
        String[] regs = new String[n]; for (int i = 0; i < n; i++) regs[i] = "TN" + i;       // shared strings, not counted
        long m0 = used(); Ticket[] arr = new Ticket[n]; for (int i = 0; i < n; i++) arr[i] = new Ticket(i, regs[i]);
        long m1 = used(); List<Ticket> list = new ArrayList<>(n); for (int i = 0; i < n; i++) list.add(arr[i]);
        long m2 = used(); Map<String, Ticket> map = new HashMap<>(); for (int i = 0; i < n; i++) map.put(regs[i], arr[i]);
        long m3 = used();
        System.out.println("approx extra bytes per ticket: array reference+ticket " + (m1 - m0) / n + " | ArrayList adds " + (m2 - m1) / n + " | HashMap adds " + (m3 - m2) / n);
        System.out.println(arr.length + list.size() + map.size() > 0 ? "" : "");
    }
    static int a(Ticket[] arr, String r) { for (int i = 0; i < arr.length; i++) if (arr[i].regNo().equals(r)) return i; return -1; }
    static int l(List<Ticket> l, String r) { for (int i = 0; i < l.size(); i++) if (l.get(i).regNo().equals(r)) return i; return -1; }
}
