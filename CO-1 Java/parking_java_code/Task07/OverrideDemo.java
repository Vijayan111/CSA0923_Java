// Task 7 - overriding, final, toString/equals/hashCode; alternative: one method with a switch on a type field
import java.util.HashSet;
import java.util.Set;

abstract class Vehicle {
    private final String regNo;
    public static final int MINUTES_PER_HOUR = 60;            // final constant
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public final String getRegNo() { return regNo; }          // final method: no subclass may change identity access
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) { return ((minutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR) * ratePerHour(); }
    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
    @Override public boolean equals(Object o) { return o instanceof Vehicle v && regNo.equals(v.regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String r) { super(r); }
    @Override protected double ratePerHour() { return 10; }
}
class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}
final class ElectricCar extends Car {                          // final class: cannot be extended further
    ElectricCar(String r) { super(r); }
    @Override public double calculateFee(int minutes) { return super.calculateFee(minutes) + 0; }
}

// alternative: single method with a switch on a type field
class TypedVehicle {
    final String regNo; final String type;
    TypedVehicle(String r, String t) { regNo = r; type = t; }
    double calculateFee(int minutes) {
        int hours = (minutes + 59) / 60;
        switch (type) {
            case "TWO": return hours * 10;
            case "CAR": return hours * 30;
            case "EV":  return hours * 30;
            default: throw new IllegalArgumentException("unknown type " + type);   // easy to forget a new type
        }
    }
}

public class OverrideDemo {
    public static void main(String[] args) {
        Vehicle[] vs = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
        for (Vehicle v : vs) System.out.println(v + " fee(130) = " + v.calculateFee(130));
        Vehicle a = new Car("TN02B2"), b = new Car("TN02B2");
        System.out.println("a == b: " + (a == b) + ", a.equals(b): " + a.equals(b) + ", same hashCode: " + (a.hashCode() == b.hashCode()));
        Set<Vehicle> set = new HashSet<>(); set.add(a); set.add(b);
        System.out.println("HashSet size after adding both: " + set.size());
        System.out.println("equals(null) = " + a.equals(null) + ", equals(\"TN02B2\") = " + a.equals("TN02B2"));
        TypedVehicle t = new TypedVehicle("TN05X5", "EBIKE");
        try { t.calculateFee(60); } catch (IllegalArgumentException e) { System.out.println("switch version: " + e.getMessage() + " (found only at run time)"); }

        int n = 20_000_000; long sink = 0; TypedVehicle tv = new TypedVehicle("X", "CAR");
        for (int w = 0; w < 3; w++) for (int i = 0; i < n / 4; i++) { sink += (long) vs[i % 3].calculateFee(i % 600); sink += (long) tv.calculateFee(i % 600); }
        long t0 = System.nanoTime(); for (int i = 0; i < n; i++) sink += (long) vs[i % 3].calculateFee(i % 600);
        long t1 = System.nanoTime(); for (int i = 0; i < n; i++) sink += (long) tv.calculateFee(i % 600);
        long t2 = System.nanoTime();
        System.out.printf("ns per fee: overriding %.2f | switch on type %.2f (sink %d)%n", (t1 - t0) / (double) n, (t2 - t1) / (double) n, sink);
    }
}
