package parking.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import parking.model.*;
import parking.service.*;

public class Tests12 {
    static int pass = 0, total = 0;
    static final LocalDate D = LocalDate.of(2026, 10, 19);
    static LocalDateTime at(int h, int m) { return D.atTime(h, m); }

    static void check(String name, Object expected, Object actual) {
        boolean ok = String.valueOf(expected).equals(String.valueOf(actual));
        total++; if (ok) pass++;
        System.out.printf("%-48s %-14s %-14s %s%n", name, expected, actual, ok ? "PASS" : "FAIL");
    }
    static double stay(String type, LocalDateTime in, LocalDateTime out, Pass p, int kWh) {
        ParkingService s = new ParkingService(new ParkingFacility(new int[]{3}));
        s.enter(type, "R1", in, p);
        return s.exit("R1", out, kWh);
    }
    public static void main(String[] args) {
        System.out.printf("%-48s %-14s %-14s %s%n", "Test case", "Expected", "Actual", "Result");
        check("E-bike 130 min (flat Rs 15)", 15.0, stay("EBIKE", at(10, 0), at(12, 10), null, 0));
        check("E-bike 1 min", 15.0, stay("EBIKE", at(10, 0), at(10, 1), null, 0));
        check("E-bike 0 min", 0.0, stay("EBIKE", at(10, 0), at(10, 0), null, 0));
        check("Van 10:00-12:00 (both hours peak)", 120.0, stay("VAN", at(10, 0), at(12, 0), null, 0));
        check("Van 08:00-09:00 (starts before 9)", 30.0, stay("VAN", at(8, 0), at(9, 0), null, 0));
        check("Van 09:00-10:00 (starts exactly 9:00)", 60.0, stay("VAN", at(9, 0), at(10, 0), null, 0));
        check("Van 16:00-17:00 (starts 16:00)", 60.0, stay("VAN", at(16, 0), at(17, 0), null, 0));
        check("Van 17:00-18:00 (starts exactly 17:00)", 30.0, stay("VAN", at(17, 0), at(18, 0), null, 0));
        check("Van 08:30-10:30 (blocks 8:30 and 9:30)", 90.0, stay("VAN", at(8, 30), at(10, 30), null, 0));
        check("Van 16:30-18:00 (blocks 16:30 and 17:30)", 90.0, stay("VAN", at(16, 30), at(18, 0), null, 0));
        check("Van 0 min", 0.0, stay("VAN", at(10, 0), at(10, 0), null, 0));
        check("Van 08:00-16:00 (8 h cap, 1 h off-peak + 7 peak)", 450.0, stay("VAN", at(8, 0), at(16, 0), null, 0));
        check("Car 09:00-12:10 unchanged by the new rule", 120.0, stay("CAR", at(9, 0), at(12, 10), null, 0));
        check("Visitor one-day pass, car 10 h same day", 0.0, stay("CAR", at(8, 0), at(18, 0), new OneDayPass(D), 0));
        check("Visitor one-day pass for another day", 240.0, stay("CAR", at(8, 0), at(18, 0), new OneDayPass(D.plusDays(1)), 0));
        check("One-day pass, stay crosses midnight", 270.0, stay("CAR", at(23, 0), D.plusDays(1).atTime(0, 0).plusMinutes(1500 - 60 - 0), new OneDayPass(D), 0));
        check("One-day pass EV: parking free, charging 10 kWh", 120.0, stay("EV", at(10, 0), at(12, 0), new OneDayPass(D), 10));
        ParkingService ms = new ParkingService(new ParkingFacility(new int[]{1}));
        ms.enter("CAR", "M1", at(9, 0), true);
        check("Old monthly pass still free (boolean API)", 0.0, ms.exit("M1", at(14, 0), 0));

        // 5 floors with different slot counts
        int[] floors = {20, 30, 40, 50, 60};
        ParkingFacility f = new ParkingFacility(floors);
        int[] last = null; int allocated = 0;
        for (int i = 0; i < 200; i++) { last = f.allocate("V" + i); if (last != null) allocated++; }
        check("5 floors capacity", 200, f.capacity());
        check("200th vehicle gets floor 4 slot 59", "4/59", last[0] + "/" + last[1]);
        check("201st vehicle rejected (null)", null, f.allocate("EXTRA"));
        System.out.println("PASSED " + pass + " of " + total);
    }
}
