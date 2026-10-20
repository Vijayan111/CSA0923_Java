// Task 5 - static counter, static block, two facilities, object life cycle
import java.lang.ref.WeakReference;

class Facility {
    private static final int MAX_TICKETS_PER_DAY;
    private static int ticketCounter = 0;            // shared by ALL facilities: one number space
    private static int totalOccupancy = 0;
    static { MAX_TICKETS_PER_DAY = 100_000; System.out.println("[static block] Facility class loaded"); }

    private final String name;
    Facility(String name) { this.name = name; }
    int issueTicket() { totalOccupancy++; return ++ticketCounter; }
    void exit() { totalOccupancy--; }
    static int occupancy() { return totalOccupancy; }
    static int limit() { return MAX_TICKETS_PER_DAY; }
    String name() { return name; }
}

class FacilityWithOwnCounter {                       // the wrong choice when numbers must be unique
    private int counter = 0;
    private final String name;
    FacilityWithOwnCounter(String name) { this.name = name; }
    String issueTicket() { return name + "-" + (++counter); }
    int raw() { return counter; }
}

class FacilityPrefixed {                             // fix 2: per-facility counter but facility prefix in the number
    private int counter = 0;
    private final String code;
    FacilityPrefixed(String code) { this.code = code; }
    String issueTicket() { return code + "-" + (++counter); }
}

class Ticket { final int no; Ticket(int n) { no = n; } }

public class StaticDemo {
    static void modify(Ticket t, int n) { n = 99; t = new Ticket(777); }     // reassigning parameters has no effect outside
    static void modify2(int[] arr) { arr[0] = 99; }                          // changing the object does
    public static void main(String[] args) throws Exception {
        System.out.println("before first use");
        Facility north = new Facility("North"), south = new Facility("South");
        System.out.println("north ticket " + north.issueTicket() + ", south ticket " + south.issueTicket() + ", north ticket " + north.issueTicket());
        System.out.println("occupancy (static) = " + Facility.occupancy() + ", daily limit = " + Facility.limit());
        north.exit();
        System.out.println("after one exit occupancy = " + Facility.occupancy());

        FacilityWithOwnCounter n2 = new FacilityWithOwnCounter("N"), s2 = new FacilityWithOwnCounter("S");
        System.out.println("instance counters: " + n2.issueTicket() + " " + s2.issueTicket() + "   raw numbers would both be " + n2.raw() + " and " + s2.raw() + " (clash)");
        FacilityPrefixed np = new FacilityPrefixed("N"), sp = new FacilityPrefixed("S");
        System.out.println("prefixed counters: " + np.issueTicket() + " " + sp.issueTicket() + " " + np.issueTicket());

        Ticket t = new Ticket(5); int n = 5; int[] arr = {1};
        modify(t, n); modify2(arr);
        System.out.println("after modify: t.no=" + t.no + " n=" + n + " arr[0]=" + arr[0]);

        Ticket temp = new Ticket(42);
        WeakReference<Ticket> ref = new WeakReference<>(temp);
        temp = null;                                  // now unreachable: eligible for garbage collection
        System.gc(); Thread.sleep(100);
        System.out.println("ticket collected after reference dropped? " + (ref.get() == null) + "  (System.gc() is only a hint)");
    }
}
