// Task 4 - constructors, this, chaining, copy constructor; alternatives: setters and static factory
import java.time.LocalDateTime;

class ParkingTicket {
    private static int nextNo = 1;
    private final int ticketNo;
    private final String regNo;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final boolean duplicate;

    ParkingTicket(String regNo, int floor, int slot, LocalDateTime entry) {      // main constructor
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        if (floor < 0 || slot < 0) throw new IllegalArgumentException("bad position");
        this.ticketNo = nextNo++;
        this.regNo = regNo; this.floor = floor; this.slot = slot; this.entry = entry;
        this.duplicate = false;
    }
    ParkingTicket(String regNo, int floor, int slot) {                           // overloaded, chained
        this(regNo, floor, slot, LocalDateTime.of(2026, 10, 19, 9, 0));
    }
    ParkingTicket(ParkingTicket other) {                                         // copy constructor: reissue
        this.ticketNo = other.ticketNo;                                          // same number, never a new one
        this.regNo = other.regNo; this.floor = other.floor; this.slot = other.slot; this.entry = other.entry;
        this.duplicate = true;
    }
    static ParkingTicket issue(String regNo, int floor, int slot) {              // static factory alternative
        return new ParkingTicket(regNo, floor, slot);
    }
    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + regNo + " F" + floor + "/S" + slot + (duplicate ? " (DUPLICATE)" : "");
    }
}

// alternative 1: default constructor + setters -> object can exist in an invalid state
class LooseTicket {
    String regNo; int floor = -1, slot = -1;
    void setRegNo(String r) { regNo = r; }
    void setFloor(int f) { floor = f; }
    void setSlot(int s) { slot = s; }
}

public class TicketDemo {
    public static void main(String[] args) {
        ParkingTicket a = new ParkingTicket("TN01AB1111", 0, 0);
        ParkingTicket b = new ParkingTicket("TN02CD2222", 1, 4, LocalDateTime.of(2026, 10, 19, 11, 30));
        ParkingTicket copy = new ParkingTicket(a);
        ParkingTicket c = ParkingTicket.issue("TN03EF3333", 2, 7);
        System.out.println(a); System.out.println(b); System.out.println(copy); System.out.println(c);
        System.out.println("a == copy ? " + (a == copy) + "   (two references, two heap objects)");
        ParkingTicket alias = a;
        System.out.println("a == alias ? " + (a == alias) + "   (same heap object)");
        try { new ParkingTicket("", 0, 0); } catch (IllegalArgumentException e) { System.out.println("rejected: " + e.getMessage()); }

        LooseTicket l = new LooseTicket();
        System.out.println("LooseTicket right after new: regNo=" + l.regNo + " floor=" + l.floor + " (invalid but usable)");

        // cost of creating n tickets: constructor vs setters vs factory
        int n = 5_000_000; long sink = 0;
        for (int w = 0; w < 2; w++) for (int i = 0; i < 1_000_000; i++) { sink += new ParkingTicket("X", 0, 0).hashCode() & 1; LooseTicket t = new LooseTicket(); t.setRegNo("X"); sink += t.floor; sink += ParkingTicket.issue("X", 0, 0).hashCode() & 1; }
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += new ParkingTicket("X", 0, 0).hashCode() & 1;
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) { LooseTicket t = new LooseTicket(); t.setRegNo("X"); t.setFloor(0); t.setSlot(0); sink += t.floor; }
        long t2 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += ParkingTicket.issue("X", 0, 0).hashCode() & 1;
        long t3 = System.nanoTime();
        System.out.printf("create %d tickets, ns each: constructor chain %.1f | default+setters %.1f | static factory %.1f (sink %d)%n",
            n, (t1 - t0) / (double) n, (t2 - t1) / (double) n, (t3 - t2) / (double) n, sink);
    }
}
