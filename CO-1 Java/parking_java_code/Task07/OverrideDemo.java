
// Task 7 - Overriding, final, toString, equals, hashCode
// Alternative: one method with a switch on a type field

import java.util.HashSet;
import java.util.Set;

abstract class Vehicle {
    private final String regNo;

    public static final int MINUTES_PER_HOUR = 60;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException(
                    "Registration number required");
        }

        this.regNo = regNo;
    }

    public final String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }

        int hours = (minutes + MINUTES_PER_HOUR - 1)
                / MINUTES_PER_HOUR;

        return hours * ratePerHour();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + regNo + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Vehicle)) {
            return false;
        }

        Vehicle v = (Vehicle) o;
        return regNo.equals(v.regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

final class ElectricCar extends Car {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        return super.calculateFee(minutes);
    }
}

// Alternative: one class using a switch on the vehicle type
class TypedVehicle {
    private final String regNo;
    private final String type;

    TypedVehicle(String regNo, String type) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException(
                    "Registration number required");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle type required");
        }

        this.regNo = regNo;
        this.type = type;
    }

    double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }

        int hours = (minutes + 59) / 60;

        switch (type) {
            case "TWO":
                return hours * 10;

            case "CAR":
            case "EV":
                return hours * 30;

            default:
                throw new IllegalArgumentException(
                        "unknown type " + type);
        }
    }
}

public class OverrideDemo {
    public static void main(String[] args) {
        Vehicle[] vehicles = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        // Demonstrate overriding and polymorphism
        for (Vehicle v : vehicles) {
            System.out.println(
                    v + " fee(130) = " + v.calculateFee(130));
        }

        // Demonstrate equals and hashCode
        Vehicle a = new Car("TN02B2");
        Vehicle b = new Car("TN02B2");

        System.out.println(
                "a == b: " + (a == b)
                + ", a.equals(b): " + a.equals(b)
                + ", same hashCode: "
                + (a.hashCode() == b.hashCode()));

        Set<Vehicle> set = new HashSet<>();
        set.add(a);
        set.add(b);

        System.out.println(
                "HashSet size after adding both: " + set.size());

        System.out.println(
                "equals(null) = " + a.equals(null)
                + ", equals(\"TN02B2\") = "
                + a.equals("TN02B2"));

        // Demonstrate the switch alternative
        TypedVehicle tv =
                new TypedVehicle("TN05X5", "EBIKE");

        try {
            tv.calculateFee(60);
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "switch version: " + e.getMessage()
                    + " (found at run time)");
        }

        // Compare inheritance and switch-based calculations
        int n = 2_000_000;
        long sink = 0;

        // Warm-up
        for (int w = 0; w < 3; w++) {
            for (int i = 0; i < n / 4; i++) {
                sink += (long) vehicles[i % 3]
                        .calculateFee(i % 600);

                sink += (long) new TypedVehicle("X", "CAR")
                        .calculateFee(i % 600);
            }
        }

        TypedVehicle car = new TypedVehicle("X", "CAR");

        long t0 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += (long) vehicles[i % 3]
                    .calculateFee(i % 600);
        }

        long t1 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += (long) car.calculateFee(i % 600);
        }

        long t2 = System.nanoTime();

        System.out.printf(
                "ns per fee: overriding %.2f | switch on type %.2f (sink %d)%n",
                (t1 - t0) / (double) n,
                (t2 - t1) / (double) n,
                sink);
    }
}
