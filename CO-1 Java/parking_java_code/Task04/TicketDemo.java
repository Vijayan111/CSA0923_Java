
import java.time.LocalDateTime;

class ParkingTicket {
    private static int nextNo = 1;

    private final int ticketNo;
    private final String regNo;
    private final int floor;
    private final int slot;
    private final LocalDateTime entry;
    private final boolean duplicate;

    // Main constructor
    ParkingTicket(String regNo, int floor, int slot, LocalDateTime entry) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("regNo required");
        }

        if (floor < 0 || slot < 0) {
            throw new IllegalArgumentException("bad position");
        }

        if (entry == null) {
            throw new IllegalArgumentException("entry time required");
        }

        this.ticketNo = nextNo++;
        this.regNo = regNo;
        this.floor = floor;
        this.slot = slot;
        this.entry = entry;
        this.duplicate = false;
    }

    // Overloaded constructor with constructor chaining
    ParkingTicket(String regNo, int floor, int slot) {
        this(regNo, floor, slot, LocalDateTime.now());
    }

    // Copy constructor: creates a duplicate of the existing ticket
    ParkingTicket(ParkingTicket other) {
        if (other == null) {
            throw new IllegalArgumentException("ticket required");
        }

        this.ticketNo = other.ticketNo;
        this.regNo = other.regNo;
        this.floor = other.floor;
        this.slot = other.slot;
        this.entry = other.entry;
        this.duplicate = true;
    }

    // Static factory method
    static ParkingTicket issue(String regNo, int floor, int slot) {
        return new ParkingTicket(regNo, floor, slot);
    }

    @Override
    public String toString() {
        return "Ticket#" + ticketNo + " " + regNo
                + " F" + floor + "/S" + slot
                + (duplicate ? " (DUPLICATE)" : "");
    }
}

// Alternative: default constructor and setters
class LooseTicket {
    String regNo;
    int floor = -1;
    int slot = -1;

    void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    void setFloor(int floor) {
        this.floor = floor;
    }

    void setSlot(int slot) {
        this.slot = slot;
    }
}

public class TicketDemo {
    public static void main(String[] args) {
        ParkingTicket a = new ParkingTicket(
                "TN01AB1111", 0, 0);

        ParkingTicket b = new ParkingTicket(
                "TN02CD2222", 1, 4,
                LocalDateTime.of(2026, 10, 19, 11, 30));

        ParkingTicket copy = new ParkingTicket(a);

        ParkingTicket c = ParkingTicket.issue(
                "TN03EF3333", 2, 7);

        System.out.println(a);
        System.out.println(b);
        System.out.println(copy);
        System.out.println(c);

        System.out.println("a == copy ? " + (a == copy)
                + " (two different objects)");

        ParkingTicket alias = a;

        System.out.println("a == alias ? " + (a == alias)
                + " (same object)");

        try {
            new ParkingTicket("", 0, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        LooseTicket loose = new LooseTicket();

        System.out.println("LooseTicket right after new: regNo="
                + loose.regNo + " floor=" + loose.floor
                + " (not fully initialized)");

        // Compare object creation methods
        int n = 1_000_000;
        long sink = 0;

        // Warm-up
        for (int i = 0; i < 100_000; i++) {
            sink += new ParkingTicket("X", 0, 0).hashCode() & 1;

            LooseTicket t = new LooseTicket();
            t.setRegNo("X");
            t.setFloor(0);
            t.setSlot(0);
            sink += t.floor;

            sink += ParkingTicket.issue("X", 0, 0).hashCode() & 1;
        }

        long t0 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += new ParkingTicket("X", 0, 0).hashCode() & 1;
        }

        long t1 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            LooseTicket t = new LooseTicket();
            t.setRegNo("X");
            t.setFloor(0);
            t.setSlot(0);
            sink += t.floor;
        }

        long t2 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += ParkingTicket.issue("X", 0, 0).hashCode() & 1;
        }

        long t3 = System.nanoTime();

        System.out.printf(
                "Create %d objects, ns each: constructor %.1f"
                        + " | setters %.1f | factory %.1f (sink %d)%n",
                n,
                (t1 - t0) / (double) n,
                (t2 - t1) / (double) n,
                (t3 - t2) / (double) n,
                sink
        );
    }
}
