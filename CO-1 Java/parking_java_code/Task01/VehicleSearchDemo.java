
// Task 1: Vehicle Objects vs Parallel Arrays
// Campus Smart-Parking System
// Demonstrates searching, memory usage, and extensibility.

import java.util.ArrayList;
import java.util.List;

public class VehicleSearchDemo {

    // OBJECT-ORIENTED DESIGN
    static class Vehicle {
        private final String regNo;
        private final String type;
        private final int entryMinute;

        // Additional attribute
        private final boolean electric;

        public Vehicle(String regNo, String type, int entryMinute,
                       boolean electric) {
            this.regNo = regNo;
            this.type = type;
            this.entryMinute = entryMinute;
            this.electric = electric;
        }

        public String getRegNo() {
            return regNo;
        }

        public String getType() {
            return type;
        }

        public int getEntryMinute() {
            return entryMinute;
        }

        public boolean isElectric() {
            return electric;
        }
    }

    // OBJECT-ORIENTED SEARCH
    static int findObject(List<Vehicle> vehicles, String regNo) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getRegNo().equals(regNo)) {
                return i;
            }
        }
        return -1;
    }

    // PARALLEL-ARRAY SEARCH
    static int findArray(String[] regNos, String target) {
        for (int i = 0; i < regNos.length; i++) {
            if (regNos[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    // APPROXIMATE USED HEAP MEMORY
    static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
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

        System.out.println("CAMPUS SMART-PARKING SYSTEM");
        System.out.println("Vehicle Objects vs Parallel Arrays");
        System.out.println();

        // ------------------------------------------------
        // PART 1: Demonstrate both designs
        // ------------------------------------------------

        List<Vehicle> vehicles = new ArrayList<>();

        vehicles.add(new Vehicle("TN101", "CAR", 540, false));
        vehicles.add(new Vehicle("TN102", "BIKE", 560, false));
        vehicles.add(new Vehicle("TN103", "EV", 600, true));

        String[] regNos = {"TN101", "TN102", "TN103"};
        String[] types = {"CAR", "BIKE", "EV"};
        int[] entryTimes = {540, 560, 600};
        boolean[] electric = {false, false, true};

        String target = "TN103";

        int objectIndex = findObject(vehicles, target);
        int arrayIndex = findArray(regNos, target);

        System.out.println("Search demonstration");
        System.out.println("--------------------");

        System.out.println("Target registration number: " + target);
        System.out.println("Object design index: " + objectIndex);
        System.out.println("Parallel-array index: " + arrayIndex);

        if (objectIndex != -1) {
            Vehicle v = vehicles.get(objectIndex);

            System.out.println("Vehicle type: " + v.getType());
            System.out.println("Entry minute: " + v.getEntryMinute());
            System.out.println("Electric vehicle: " + v.isElectric());
        }

        if (arrayIndex != -1) {
            System.out.println("Array vehicle type: " + types[arrayIndex]);
            System.out.println("Array entry minute: "
                    + entryTimes[arrayIndex]);
            System.out.println("Array electric vehicle: "
                    + electric[arrayIndex]);
        }

        System.out.println();

        // ------------------------------------------------
        // PART 2: Search-time comparison
        // ------------------------------------------------

        int[] sizes = {1000, 10000, 100000, 500000};

        System.out.println("Search-time comparison");
        System.out.println("----------------------");

        System.out.printf("%-12s %-25s %-28s%n",
                "Vehicles", "Objects (ns/search)",
                "Parallel arrays (ns/search)");

        for (int n : sizes) {

            String[] registrations = new String[n];
            List<Vehicle> objectList = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                String registration = "TN" + i;

                registrations[i] = registration;

                objectList.add(new Vehicle(
                        registration,
                        (i % 3 == 0) ? "CAR" : "BIKE",
                        i % 1440,
                        i % 5 == 0
                ));
            }

            // Last element produces a worst-case linear search.
            String searchTarget = "TN" + (n - 1);

            // Keep the number of comparisons manageable.
            int searches = Math.max(10, 1000000 / n);

            // Warm-up to reduce JVM startup effects.
            long sink = 0;

            for (int w = 0; w < 3; w++) {
                sink += findObject(objectList, searchTarget);
                sink += findArray(registrations, searchTarget);
            }

            // Measure object search.
            long startObject = System.nanoTime();

            for (int i = 0; i < searches; i++) {
                sink += findObject(objectList, searchTarget);
            }

            long endObject = System.nanoTime();

            // Measure parallel-array search.
            long startArray = System.nanoTime();

            for (int i = 0; i < searches; i++) {
                sink += findArray(registrations, searchTarget);
            }

            long endArray = System.nanoTime();

            long objectTime =
                    (endObject - startObject) / searches;

            long arrayTime =
                    (endArray - startArray) / searches;

            System.out.printf("%-12d %-25d %-28d%n",
                    n, objectTime, arrayTime);

            // Prevent results from being entirely unused.
            if (sink == Long.MIN_VALUE) {
                System.out.println(sink);
            }
        }

        System.out.println();

        // ------------------------------------------------
        // PART 3: Approximate memory comparison
        // ------------------------------------------------

        int n = 100000;

        System.out.println("Approximate memory comparison");
        System.out.println("-----------------------------");

        // Measure the object-based representation.
        requestGC();
        long beforeObjects = usedMemory();

        List<Vehicle> objectList = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            objectList.add(new Vehicle(
                    "TN" + i,
                    "CAR",
                    i % 1440,
                    false
            ));
        }

        requestGC();
        long afterObjects = usedMemory();

        long objectMemory = Math.max(0,
                afterObjects - beforeObjects);

        // Release the object-based data before measuring arrays.
        objectList = null;
        requestGC();

        long beforeArrays = usedMemory();

        String[] arrayRegNos = new String[n];
        String[] arrayTypes = new String[n];
        int[] arrayEntryTimes = new int[n];
        boolean[] arrayElectric = new boolean[n];

        for (int i = 0; i < n; i++) {
            arrayRegNos[i] = "TN" + i;
            arrayTypes[i] = "CAR";
            arrayEntryTimes[i] = i % 1440;
            arrayElectric[i] = false;
        }

        requestGC();
        long afterArrays = usedMemory();

        long arrayMemory = Math.max(0,
                afterArrays - beforeArrays);

        System.out.println("Number of vehicles: " + n);

        System.out.printf(
                "Object design: approximately %.2f bytes/vehicle%n",
                objectMemory / (double) n
        );

        System.out.printf(
                "Parallel arrays: approximately %.2f bytes/vehicle%n",
                arrayMemory / (double) n
        );

        System.out.println();
        System.out.println(
                "Note: Heap measurements are approximate and depend "
                + "on JVM behaviour."
        );

        // ------------------------------------------------
        // PART 4: Complexity analysis
        // ------------------------------------------------

        System.out.println();
        System.out.println("THEORETICAL COMPLEXITY");
        System.out.println("----------------------");

        System.out.println(
                "Registration search using objects: O(n)"
        );

        System.out.println(
                "Registration search using arrays: O(n)"
        );

        System.out.println(
                "Accessing an attribute after locating a vehicle: O(1)"
        );

        System.out.println(
                "Appending to an ArrayList: O(1) amortized"
        );

        System.out.println(
                "Appending to a full fixed-size array: O(n) if copied"
        );

        System.out.println();
        System.out.println("Program completed.");
    }
}
