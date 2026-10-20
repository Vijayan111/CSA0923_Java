// Task 1 - vehicles as objects versus parallel arrays: search and per-vehicle memory
import java.util.ArrayList;
import java.util.List;

public class VehicleSearchDemo {
    static class Vehicle {                          // object design
        final String regNo; final String type; final int entryMinute;
        Vehicle(String r, String t, int e) { regNo = r; type = t; entryMinute = e; }
    }

    static long used() {
        System.gc(); System.gc();
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    public static void main(String[] args) {
        int[] sizes = {1_000, 10_000, 100_000, 1_000_000};
        System.out.println("n | object search (ns/search) | parallel-array search (ns/search)");
        for (int n : sizes) {
            String[] reg = new String[n]; String[] type = new String[n]; int[] entry = new int[n];
            List<Vehicle> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                reg[i] = "TN" + i; type[i] = (i % 3 == 0) ? "CAR" : "BIKE"; entry[i] = i % 1440;
                list.add(new Vehicle(reg[i], type[i], entry[i]));
            }
            String target = "TN" + (n - 1);          // worst case: last element
            int searches = Math.max(5, 2_000_000 / n);
            long sink = 0;
            for (int w = 0; w < 3; w++) { sink += findObj(list, target) + findArr(reg, target); }   // warm up
            long t0 = System.nanoTime();
            for (int s = 0; s < searches; s++) sink += findObj(list, target);
            long t1 = System.nanoTime();
            for (int s = 0; s < searches; s++) sink += findArr(reg, target);
            long t2 = System.nanoTime();
            System.out.printf("%d | %d | %d   (sink %d)%n", n, (t1 - t0) / searches, (t2 - t1) / searches, sink);
        }

        // approximate heap per vehicle (strings are shared by both designs and counted in both)
        int n = 200_000;
        long b0 = used();
        List<Vehicle> objs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) objs.add(new Vehicle("TN" + i, "CAR", i % 1440));
        long b1 = used();
        String[] r = new String[n]; String[] t = new String[n]; int[] e = new int[n];
        for (int i = 0; i < n; i++) { r[i] = "TN" + i; t[i] = "CAR"; e[i] = i % 1440; }
        long b2 = used();
        System.out.println("approx bytes per vehicle, objects        : " + (b1 - b0) / n);
        System.out.println("approx bytes per vehicle, parallel arrays: " + (b2 - b1) / n);
        System.out.println(objs.size() + r.length + t.length + e.length > 0 ? "" : "");
    }
    static int findObj(List<Vehicle> l, String reg) {
        for (int i = 0; i < l.size(); i++) if (l.get(i).regNo.equals(reg)) return i;
        return -1;
    }
    static int findArr(String[] regs, String reg) {
        for (int i = 0; i < regs.length; i++) if (regs[i].equals(reg)) return i;
        return -1;
    }
}
