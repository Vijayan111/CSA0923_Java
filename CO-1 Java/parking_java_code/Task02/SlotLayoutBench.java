import java.util.ArrayList;
import java.util.List;

public class SlotLayoutBench {

    // Each floor can have a different number of slots.
    private static final int[] SLOTS_PER_FLOOR = {20, 30, 40, 50, 60};

    private static final int FLOORS = SLOTS_PER_FLOOR.length;
    private static final int TOTAL_SLOTS = 200;

    private static final int[] START = new int[FLOORS];

    // Representation 1: Jagged two-dimensional array.
    private static final String[][] jagged = new String[FLOORS][];

    // Representation 2: One-dimensional array.
    private static final String[] flat = new String[TOTAL_SLOTS];

    // Representation 3: ArrayList containing one array per floor.
    private static final List<String[]> arrayList = new ArrayList<>();

    // Used to prevent the benchmark results from being ignored.
    private static volatile int sink = 0;

    static {
        int offset = 0;

        for (int f = 0; f < FLOORS; f++) {

            START[f] = offset;

            jagged[f] = new String[SLOTS_PER_FLOOR[f]];

            arrayList.add(new String[SLOTS_PER_FLOOR[f]]);

            offset += SLOTS_PER_FLOOR[f];
        }

        if (offset != TOTAL_SLOTS) {
            throw new IllegalStateException("Incorrect total slot count");
        }
    }

    // Calculate the flattened index for a floor and slot.
    private static int flatIndex(int floor, int slot) {

        if (floor < 0 || floor >= FLOORS) {
            return -1;
        }

        if (slot < 0 || slot >= SLOTS_PER_FLOOR[floor]) {
            return -1;
        }

        return START[floor] + slot;
    }

    // Find a free slot using the jagged array.
    private static int findJagged(int floor, int slot) {

        if (floor < 0 || floor >= FLOORS) {
            return -1;
        }

        if (slot < 0 || slot >= jagged[floor].length) {
            return -1;
        }

        return jagged[floor][slot] == null ? 1 : 0;
    }

    // Find a free slot using the flattened array.
    private static int findFlat(int floor, int slot) {

        int index = flatIndex(floor, slot);

        if (index == -1) {
            return -1;
        }

        return flat[index] == null ? 1 : 0;
    }

    // Find a free slot using ArrayList<String[]>.
    private static int findArrayList(int floor, int slot) {

        if (floor < 0 || floor >= arrayList.size()) {
            return -1;
        }

        String[] floorSlots = arrayList.get(floor);

        if (slot < 0 || slot >= floorSlots.length) {
            return -1;
        }

        return floorSlots[slot] == null ? 1 : 0;
    }

    private static long benchmarkJagged(int floor, int slot, int iterations) {

        long start = System.nanoTime();

        int result = 0;

        for (int i = 0; i < iterations; i++) {
            result += findJagged(floor, slot);
        }

        long end = System.nanoTime();

        sink = result;

        return (end - start) / iterations;
    }

    private static long benchmarkFlat(int floor, int slot, int iterations) {

        long start = System.nanoTime();

        int result = 0;

        for (int i = 0; i < iterations; i++) {
            result += findFlat(floor, slot);
        }

        long end = System.nanoTime();

        sink = result;

        return (end - start) / iterations;
    }

    private static long benchmarkArrayList(
            int floor, int slot, int iterations) {

        long start = System.nanoTime();

        int result = 0;

        for (int i = 0; i < iterations; i++) {
            result += findArrayList(floor, slot);
        }

        long end = System.nanoTime();

        sink = result;

        return (end - start) / iterations;
    }

    public static void main(String[] args) {

        System.out.println("Total floors = " + FLOORS);
        System.out.println("Total slots = " + TOTAL_SLOTS);

        // Demonstrate why the naive formula fails.
        int floor = 4;
        int slot = 59;

        int naiveIndex = floor * 60 + slot;
        int correctIndex = flatIndex(floor, slot);

        System.out.println(
                "Naive formula: " + floor + "*60+" + slot
                        + " = " + naiveIndex
                        + " -> " + (naiveIndex >= TOTAL_SLOTS
                        ? "out of range" : "valid")
        );

        System.out.println(
                "Prefix formula: START[" + floor + "]+" + slot
                        + " = " + correctIndex
        );

        // Verify that the flattened index points to the same slot.
        jagged[floor][slot] = "TN04GH4444";
        flat[correctIndex] = "TN04GH4444";
        arrayList.get(floor)[slot] = "TN04GH4444";

        System.out.println("\nValidation:");

        System.out.println(
                "Jagged array: " + jagged[floor][slot]
        );

        System.out.println(
                "Flat array: " + flat[correctIndex]
        );

        System.out.println(
                "ArrayList: " + arrayList.get(floor)[slot]
        );

        // Validate invalid positions.
        System.out.println("\nInvalid position tests:");

        System.out.println("Invalid floor: " + flatIndex(5, 0));
        System.out.println("Invalid slot: " + flatIndex(4, 60));

        // Benchmark a valid position.
        int testFloor = 4;
        int testSlot = 58;
        int iterations = 5_000_000;

        // Warm-up to allow the JVM to optimize the methods.
        for (int i = 0; i < 3; i++) {
            benchmarkJagged(testFloor, testSlot, 100_000);
            benchmarkFlat(testFloor, testSlot, 100_000);
            benchmarkArrayList(testFloor, testSlot, 100_000);
        }

        long jaggedTime =
                benchmarkJagged(testFloor, testSlot, iterations);

        long flatTime =
                benchmarkFlat(testFloor, testSlot, iterations);

        long arrayListTime =
                benchmarkArrayList(testFloor, testSlot, iterations);

        System.out.println("\nBenchmark results:");
        System.out.println("Iterations per representation = " + iterations);

        System.out.println(
                "Jagged array          = " + jaggedTime + " ns/access"
        );

        System.out.println(
                "1-D array with offsets = " + flatTime + " ns/access"
        );

        System.out.println(
                "ArrayList<String[]>   = " + arrayListTime + " ns/access"
        );

        System.out.println("Benchmark sink = " + sink);
    }
}