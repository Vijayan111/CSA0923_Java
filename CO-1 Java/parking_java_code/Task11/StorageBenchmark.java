import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageBenchmark {

    record Ticket(int no, String regNo) {}

    static long usedMemory() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    static void requestGC() {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        int[] sizes = {500, 5_000, 50_000, 500_000};

        System.out.println(
            "n | Array scan ns | ArrayList scan ns | HashMap get ns"
        );

        for (int n : sizes) {
            Ticket[] arr = new Ticket[n];
            List<Ticket> list = new ArrayList<>(n);
            Map<String, Ticket> map = new HashMap<>();

            for (int i = 0; i < n; i++) {
                Ticket t = new Ticket(i, "TN" + i);
                arr[i] = t;
                list.add(t);
                map.put(t.regNo(), t);
            }

            String target = "TN" + (n - 1);
            int reps = Math.max(10, 5_000_000 / n);
            long sink = 0;

            // Warm up the methods
            for (int w = 0; w < 3; w++) {
                sink += a(arr, target);
                sink += l(list, target);
                sink += map.get(target).no();
            }

            long t0 = System.nanoTime();
            for (int i = 0; i < reps; i++) {
                sink += a(arr, target);
            }
            long t1 = System.nanoTime();

            for (int i = 0; i < reps; i++) {
                sink += l(list, target);
            }
            long t2 = System.nanoTime();

            int hashReps = reps * 200;
            for (int i = 0; i < hashReps; i++) {
                sink += map.get(target).no();
            }
            long t3 = System.nanoTime();

            System.out.printf(
                "%d | %d | %d | %d%n",
                n,
                (t1 - t0) / reps,
                (t2 - t1) / reps,
                (t3 - t2) / hashReps
            );

            // Keep the accumulated result observable
            if (sink == Long.MIN_VALUE) {
                System.out.println(sink);
            }
        }

        measureMemory();
    }

    static void measureMemory() {
        int n = 200_000;

        String[] regs = new String[n];
        for (int i = 0; i < n; i++) {
            regs[i] = "TN" + i;
        }

        requestGC();
        long m0 = usedMemory();

        Ticket[] arr = new Ticket[n];
        for (int i = 0; i < n; i++) {
            arr[i] = new Ticket(i, regs[i]);
        }

        requestGC();
        long m1 = usedMemory();

        List<Ticket> list = new ArrayList<>(n);
        for (Ticket t : arr) {
            list.add(t);
        }

        requestGC();
        long m2 = usedMemory();

        Map<String, Ticket> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(regs[i], arr[i]);
        }

        requestGC();
        long m3 = usedMemory();

        System.out.println("\nApproximate extra memory per ticket:");
        System.out.println(
            "Array + Ticket objects: " + (m1 - m0) / n + " bytes"
        );
        System.out.println(
            "ArrayList additional storage: " + (m2 - m1) / n + " bytes"
        );
        System.out.println(
            "HashMap additional storage: " + (m3 - m2) / n + " bytes"
        );

        System.out.println(
            "\nSizes: array = " + arr.length
            + ", ArrayList = " + list.size()
            + ", HashMap = " + map.size()
        );
    }

    static int a(Ticket[] arr, String r) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].regNo().equals(r)) {
                return i;
            }
        }
        return -1;
    }

    static int l(List<Ticket> list, String r) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).regNo().equals(r)) {
                return i;
            }
        }
        return -1;
    }
}