// Task 8 - overloading (compile time), upcasting, dynamic dispatch trace, and the instanceof alternative
import java.time.Duration;
import java.time.LocalTime;

abstract class Veh {
    abstract double rate();
    double calculateFee(int minutes) { System.out.println("   -> calculateFee(int) of " + getClass().getSimpleName() + "'s superclass chain"); return ((minutes + 59) / 60) * rate(); }
    double calculateFee(LocalTime in, LocalTime out) {
        long m = Duration.between(in, out).toMinutes();
        if (m < 0) m += 24 * 60;                          // stay passes midnight
        return calculateFee((int) m);                     // reuses the int version
    }
    double calculateFee(long minutes) { return calculateFee((int) minutes); }   // overload resolution example
}
class Bike extends Veh { double rate() { return 10; } }
class Sedan extends Veh {
    double rate() { return 30; }
    @Override double calculateFee(int minutes) { System.out.println("   -> Sedan.calculateFee(int) executed"); return super.calculateFee(minutes); }
}

public class OverloadDemo {
    public static void main(String[] args) {
        Veh v = new Sedan();                                  // declared type Veh, actual type Sedan (upcasting)
        System.out.println("call 1: v.calculateFee(130)           declared Veh, actual " + v.getClass().getSimpleName());
        System.out.println("   fee = " + v.calculateFee(130));
        System.out.println("call 2: v.calculateFee(8:30, 10:15)");
        System.out.println("   fee = " + v.calculateFee(LocalTime.of(8, 30), LocalTime.of(10, 15)));
        System.out.println("call 3: v.calculateFee(130L)  (long literal picks the long overload at compile time)");
        System.out.println("   fee = " + v.calculateFee(130L));
        short s = 45;
        System.out.println("call 4: short argument widens to int overload: " + v.calculateFee(s));

        // alternative: instanceof chain
        Veh[] fleet = { new Bike(), new Sedan(), new Bike(), new Sedan() };
        int n = 20_000_000; long sink = 0;
        for (int w = 0; w < 3; w++) for (int i = 0; i < n / 4; i++) { sink += (long) fleet[i & 3].rate(); sink += chain(fleet[i & 3]); }
        long t0 = System.nanoTime(); for (int i = 0; i < n; i++) sink += (long) fleet[i & 3].rate();
        long t1 = System.nanoTime(); for (int i = 0; i < n; i++) sink += chain(fleet[i & 3]);
        long t2 = System.nanoTime();
        System.out.printf("ns per rate lookup: dynamic dispatch %.2f | instanceof chain %.2f (sink %d)%n", (t1 - t0) / (double) n, (t2 - t1) / (double) n, sink);
    }
    static long chain(Veh v) {
        if (v instanceof Bike) return 10;
        else if (v instanceof Sedan) return 30;
        else throw new IllegalArgumentException();
    }
}
