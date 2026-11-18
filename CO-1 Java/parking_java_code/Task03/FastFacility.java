import java.util.ArrayDeque;

public class FastFacility {
    private final String[][] slots;
    private final int[] freeCount;
    private final ArrayDeque<Integer>[] freeStack;

    @SuppressWarnings("unchecked")
    public FastFacility(int[] slotsPerFloor) {
        if (slotsPerFloor == null) {
            throw new IllegalArgumentException("Slot configuration cannot be null");
        }

        slots = new String[slotsPerFloor.length][];
        freeCount = new int[slotsPerFloor.length];
        freeStack = (ArrayDeque<Integer>[]) new ArrayDeque<?>[slotsPerFloor.length];

        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] < 0) {
                throw new IllegalArgumentException("Slot count cannot be negative");
            }

            slots[f] = new String[slotsPerFloor[f]];
            freeCount[f] = slotsPerFloor[f];
            freeStack[f] = new ArrayDeque<>();

            // Push in reverse order so the lowest slot is allocated first.
            for (int s = slotsPerFloor[f] - 1; s >= 0; s--) {
                freeStack[f].push(s);
            }
        }
    }

    /** Returns {floor, slot} of the first available slot, or null if full. */
    public int[] allocate(String vehicleNo) {
        if (vehicleNo == null || vehicleNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number cannot be empty");
        }

        for (int f = 0; f < slots.length; f++) {
            if (freeCount[f] > 0) {
                int s = freeStack[f].pop();
                slots[f][s] = vehicleNo;
                freeCount[f]--;

                return new int[]{f, s};
            }
        }

        return null;
    }

    /** Releases an occupied slot. Returns false for an invalid or free slot. */
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length) {
            return false;
        }

        if (slot < 0 || slot >= slots[floor].length) {
            return false;
        }

        if (slots[floor][slot] == null) {
            return false;
        }

        slots[floor][slot] = null;
        freeCount[floor]++;
        freeStack[floor].push(slot);

        return true;
    }

    public static void main(String[] args) {
        int[][] configs = {
            {20},
            {50, 50, 50},
            {100, 100, 100, 100, 100}
        };

        int rounds = 200_000;

        System.out.println(
            "Facility | Scan (ns/op) | Stack (ns/op)"
        );

        for (int[] cfg : configs) {
            ParkingFacility scan = new ParkingFacility(cfg);
            FastFacility fast = new FastFacility(cfg);

            int total = 0;
            for (int count : cfg) {
                total += count;
            }

            // Fill all but the last slot.
            for (int i = 0; i < total - 1; i++) {
                scan.allocate("V" + i);
                fast.allocate("V" + i);
            }

            long sink = 0;

            // Warm-up phase.
            for (int i = 0; i < 50_000; i++) {
                int[] a = scan.allocate("W");
                sink += a[1];
                scan.release(a[0], a[1]);

                int[] b = fast.allocate("W");
                sink += b[1];
                fast.release(b[0], b[1]);
            }

            long start = System.nanoTime();

            for (int i = 0; i < rounds; i++) {
                int[] result = scan.allocate("X");
                sink += result[1];
                scan.release(result[0], result[1]);
            }

            long middle = System.nanoTime();

            for (int i = 0; i < rounds; i++) {
                int[] result = fast.allocate("X");
                sink += result[1];
                fast.release(result[0], result[1]);
            }

            long end = System.nanoTime();

            System.out.printf(
                "%d slots | %d | %d (sink %d)%n",
                total,
                (middle - start) / rounds,
                (end - middle) / rounds,
                sink
            );
        }

        FastFacility test = new FastFacility(new int[]{2, 1});

        System.out.println(
            java.util.Arrays.toString(test.allocate("A")) + " " +
            java.util.Arrays.toString(test.allocate("B")) + " " +
            java.util.Arrays.toString(test.allocate("C")) + " " +
            java.util.Arrays.toString(test.allocate("D"))
        );
    }
}