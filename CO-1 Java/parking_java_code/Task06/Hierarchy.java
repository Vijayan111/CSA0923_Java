// Task 6 - inheritance hierarchy and constructor order
interface Chargeable { double chargingFee(int kWh); }

abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; System.out.println("  Vehicle(" + regNo + ")"); }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String r) { super(r); System.out.println("  TwoWheeler()"); }
    @Override protected double ratePerHour() { return 10; }
}
class Car extends Vehicle {
    Car(String r) { super(r); System.out.println("  Car()"); }
    @Override protected double ratePerHour() { return 30; }
}
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String r) { super(r); System.out.println("  ElectricCar()"); }
    @Override public double chargingFee(int kWh) { return kWh * 12; }
    @Override public double calculateFee(int minutes) { return super.calculateFee(minutes); }   // super used for a method
}

// alternative: composition - one Vehicle class has-a VehicleType holding the rate
class VehicleType {
    private final String name; private final double rate;
    VehicleType(String n, double r) { name = n; rate = r; }
    double rate() { return rate; }
    String name() { return name; }
}
class ComposedVehicle {
    private final String regNo; private final VehicleType type;
    ComposedVehicle(String r, VehicleType t) { regNo = r; type = t; }
    double calculateFee(int minutes) { return ((minutes + 59) / 60) * type.rate(); }
}

public class Hierarchy {
    public static void main(String[] args) {
        System.out.println("new ElectricCar(\"TN03C3\") -> constructor order:");
        ElectricCar e = new ElectricCar("TN03C3");
        System.out.println("fee 130 min = " + e.calculateFee(130) + ", charging 10 kWh = " + e.chargingFee(10));

        VehicleType car = new VehicleType("CAR", 30), bike = new VehicleType("BIKE", 10), eBike = new VehicleType("EBIKE", 5);
        ComposedVehicle c1 = new ComposedVehicle("TN02B2", car), c2 = new ComposedVehicle("TN09Z9", eBike);
        System.out.println("composition: car 130 min = " + c1.calculateFee(130) + ", new type e-bike (no new class) = " + c2.calculateFee(130));

        int n = 20_000_000; long sink = 0; Vehicle v = new Car("X"); 
        System.out.println("(timing runs below)");
        for (int w = 0; w < 3; w++) for (int i = 0; i < n / 4; i++) { sink += (long) v.calculateFee(i % 600); sink += (long) c1.calculateFee(i % 600); }
        long t0 = System.nanoTime(); for (int i = 0; i < n; i++) sink += (long) v.calculateFee(i % 600);
        long t1 = System.nanoTime(); for (int i = 0; i < n; i++) sink += (long) c1.calculateFee(i % 600);
        long t2 = System.nanoTime();
        System.out.printf("ns per fee: inheritance %.2f | composition %.2f (sink %d)%n", (t1 - t0) / (double) n, (t2 - t1) / (double) n, sink);
    }
}
