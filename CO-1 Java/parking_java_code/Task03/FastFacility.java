// Task 3 alternative - free count per floor + stack of free slot numbers per floor
import java.util.ArrayDeque;

public class FastFacility {
    private final String[][] slots;
    private final int[] freeCount;
    private final ArrayDeque<Integer>[] freeStack;

    @SuppressWarnings("unchecked")
    public FastFacility(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        freeCount = new int[slotsPerFloor.length];
        freeStack = new ArrayDeque[slotsPerFloor.length];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
            freeCount[f] = slotsPerFloor[f];
            freeStack[f] = new ArrayDeque<>();
            for (int s = slotsPerFloor[f] - 1; s >= 0; s--) freeStack[f].push(s);   // lowest slot on top
        }
    }
    public int[] allocate(String vehicleNo) {            // O(F): find a floor with space, then O(1) pop
        for (int f = 0; f < slots.length; f++) {
            if (freeCount[f] > 0) {
                int s = freeStack[f].pop();
                slots[f][s] = vehicleNo; freeCount[f]--;
                return new int[]{f, s};
            }
        }
        return null;
    }
    public boolean release(int floor, int slot) {        // O(1)
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length) return false;
        if (slots[floor][slot] == null) return false;
        slots[floor][slot] = null; freeCount[floor]++; freeStack[floor].push(slot);
        return true;
    }

    public static void main(String[] args) {
        int[][] configs = {{20}, {50, 50, 50}, {100, 100, 100, 100, 100}};
        int rounds = 200_000;
        System.out.println("facility | scan allocate+release (ns) | stack allocate+release (ns)");
        for (int[] cfg : configs) {
            ParkingFacility a = new ParkingFacility(cfg);
            FastFacility b = new FastFacility(cfg);
            int total = 0; for (int c : cfg) total += c;
            int last = cfg.length - 1;
            for (int i = 0; i < total - 1; i++) { a.allocate("V" + i); b.allocate("V" + i); }   // all but one full
            long sink = 0;
            for (int w = 0; w < 50_000; w++) { sink += a.allocate("W")[1]; a.release(last, cfg[last] - 1); sink += b.allocate("W")[1]; b.release(last, cfg[last] - 1); }
            long t0 = System.nanoTime();
            for (int i = 0; i < rounds; i++) { int[] r = a.allocate("X"); sink += r[1]; a.release(r[0], r[1]); }
            long t1 = System.nanoTime();
            for (int i = 0; i < rounds; i++) { int[] r = b.allocate("X"); sink += r[1]; b.release(r[0], r[1]); }
            long t2 = System.nanoTime();
            System.out.printf("%d slots | %d | %d  (sink %d)%n", total, (t1 - t0) / rounds, (t2 - t1) / rounds, sink);
        }
        FastFacility t = new FastFacility(new int[]{2, 1});
        System.out.println(java.util.Arrays.toString(t.allocate("A")) + " " + java.util.Arrays.toString(t.allocate("B"))
            + " " + java.util.Arrays.toString(t.allocate("C")) + " " + java.util.Arrays.toString(t.allocate("D")));
    }
}
