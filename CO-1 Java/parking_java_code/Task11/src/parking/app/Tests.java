package parking.app;

import java.time.LocalDateTime;
import parking.service.*;
import parking.model.*;

public class Tests {
    static int pass = 0, total = 0;
    static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 19, 9, 0);

    static void check(String name, Object expected, Object actual) {
        boolean ok = String.valueOf(expected).equals(String.valueOf(actual));
        total++; if (ok) pass++;
        System.out.printf("%-46s %-18s %-18s %s%n", name, expected, actual, ok ? "PASS" : "FAIL");
    }
    static double stay(String type, String reg, int minutes, boolean pass, int kWh) {
        ParkingService s = new ParkingService(new ParkingFacility(new int[]{5}));
        s.enter(type, reg, T0, pass);
        return s.exit(reg, T0.plusMinutes(minutes), kWh);
    }
    static String expect(Runnable r) {
        try { r.run(); return "no exception"; }
        catch (IllegalArgumentException e) { return "IllegalArgument"; }
        catch (IllegalStateException e) { return "IllegalState"; }
    }

    public static void main(String[] args) {
        System.out.printf("%-46s %-18s %-18s %s%n", "Test case", "Expected", "Actual", "Result");
        check("TwoWheeler 130 min", 30.0, stay("TWO", "A1", 130, false, 0));
        check("Car 130 min", 90.0, stay("CAR", "B2", 130, false, 0));
        check("ElectricCar 130 min + 10 kWh", 210.0, stay("EV", "C3", 130, false, 10));
        check("Zero minutes (boundary)", 0.0, stay("CAR", "B2", 0, false, 0));
        check("Exactly 60 min car (slab boundary)", 30.0, stay("CAR", "B2", 60, false, 0));
        check("61 min car (just over the boundary)", 60.0, stay("CAR", "B2", 61, false, 0));
        check("Exactly 480 min car (daily cap reached)", 240.0, stay("CAR", "B2", 480, false, 0));
        check("600 min car (cap applies)", 240.0, stay("CAR", "B2", 600, false, 0));
        check("1500 min car (1 day + 60 min)", 270.0, stay("CAR", "B2", 1500, false, 0));
        check("Monthly pass car 130 min", 0.0, stay("CAR", "B2", 130, true, 0));
        check("Monthly pass EV 130 min + 10 kWh", 120.0, stay("EV", "C3", 130, true, 10));

        ParkingService s = new ParkingService(new ParkingFacility(new int[]{1, 1}));
        ParkingTicket t1 = s.enter("CAR", "L1", T0, false);
        ParkingTicket t2 = s.enter("CAR", "L2", T0, false);
        check("Last free slot taken: ticket 2 floor/slot", "1/0", t2.getFloor() + "/" + t2.getSlot());
        check("Facility full -> third vehicle rejected", "IllegalState", expect(() -> s.enter("CAR", "L3", T0, false)));
        s.exit("L1", T0.plusMinutes(10), 0);
        ParkingTicket t3 = s.enter("CAR", "L3", T0, false);
        check("Slot reused after exit", "0/0", t3.getFloor() + "/" + t3.getSlot());
        check("Ticket numbers increase, none reused", true, t2.getTicketNo() == t1.getTicketNo() + 1 && t3.getTicketNo() == t2.getTicketNo() + 1);
        check("Unknown vehicle type 'TRUCK'", "IllegalArgument", expect(() -> s.enter("TRUCK", "Z9", T0, false)));
        check("Duplicate registration L2", "IllegalState", expect(() -> s.enter("CAR", "L2", T0, false)));
        check("Exit of vehicle not parked", "IllegalState", expect(() -> s.exit("NOPE", T0, 0)));
        ParkingFacility f = new ParkingFacility(new int[]{2});
        check("Invalid slot release(9,0)", false, f.release(9, 0));
        check("Invalid slot release(0,-1)", false, f.release(0, -1));
        int[] p = f.allocate("Q"); f.release(p[0], p[1]);
        check("Double release of the same slot", false, f.release(p[0], p[1]));

        // hand-calculated day: expected total Rs 480
        ParkingService d = new ParkingService(new ParkingFacility(new int[]{3, 3}));
        d.enter("TWO", "D1", T0, false);  d.enter("CAR", "D2", T0, false);  d.enter("EV", "D3", T0, false);
        d.enter("CAR", "D4", T0, true);   d.enter("TWO", "D5", T0, false);
        d.exit("D1", T0.plusMinutes(45), 0);    // 1 h  x 10        = 10
        d.exit("D2", T0.plusMinutes(125), 0);   // 3 h  x 30        = 90
        d.exit("D3", T0.plusMinutes(200), 15);  // 4 h  x 30 + 15x12 = 300
        d.exit("D4", T0.plusMinutes(300), 0);   // monthly pass     = 0
        d.exit("D5", T0.plusMinutes(600), 0);   // capped 8 h x 10  = 80
        check("Day collection (hand: 10+90+300+0+80)", 480.0, d.getCollection());
        check("No vehicles left inside after the day", 0, d.getActiveCount());
        System.out.println("PASSED " + pass + " of " + total);
    }
}
