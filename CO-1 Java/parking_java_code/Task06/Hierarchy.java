
// Task 6 - Inheritance hierarchy and constructor order

interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("Registration number required");
        }

        this.regNo = regNo;
        System.out.println("  Vehicle(" + regNo + ")");
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }

        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
        System.out.println("  TwoWheeler()");
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
        System.out.println("  Car()");
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) {
        super(regNo);
        System.out.println("  ElectricCar()");
    }

    @Override
    public double chargingFee(int kWh) {
        if (kWh < 0) {
            throw new IllegalArgumentException("kWh cannot be negative");
        }

        return kWh * 12;
    }

    @Override
    public double calculateFee(int minutes) {
        return super.calculateFee(minutes);
    }
}

// Alternative: Composition

class VehicleType {
    private final String name;
    private final double rate;

    VehicleType(String name, double rate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vehicle type required");
        }

        if (rate < 0) {
            throw new IllegalArgumentException("Rate cannot be negative");
        }

        this.name = name;
        this.rate = rate;
    }

    double rate() {
        return rate;
    }

    String name() {
        return name;
    }
}

class ComposedVehicle {
    private final String regNo;
    private final VehicleType type;

    ComposedVehicle(String regNo, VehicleType type) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("Registration number required");
        }

        if (type == null) {
            throw new IllegalArgumentException("Vehicle type required");
        }

        this.regNo = regNo;
        this.type = type;
    }

    double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }

        int hours = (minutes + 59) / 60;
        return hours * type.rate();
    }
}

public class Hierarchy {
    public static void main(String[] args) {
        System.out.println(
                "new ElectricCar(\"TN03C3\") -> constructor order:"
        );

        ElectricCar e = new ElectricCar("TN03C3");

        System.out.println(
                "fee 130 min = " + e.calculateFee(130)
                + ", charging 10 kWh = " + e.chargingFee(10)
        );

        VehicleType car = new VehicleType("CAR", 30);
        VehicleType bike = new VehicleType("BIKE", 10);
        VehicleType eBike = new VehicleType("EBIKE", 5);

        ComposedVehicle c1 =
                new ComposedVehicle("TN02B2", car);

        ComposedVehicle c2 =
                new ComposedVehicle("TN09Z9", eBike);

        System.out.println(
                "composition: car 130 min = "
                + c1.calculateFee(130)
                + ", new type e-bike (no new class) = "
                + c2.calculateFee(130)
        );

        // Benchmark: inheritance versus composition
        int n = 2_000_000;
        long sink = 0;

        Vehicle v = new Car("X");

        System.out.println("(timing runs below)");

        // Warm-up
        for (int w = 0; w < 3; w++) {
            for (int i = 0; i < n / 4; i++) {
                sink += (long) v.calculateFee(i % 600);
                sink += (long) c1.calculateFee(i % 600);
            }
        }

        long t0 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += (long) v.calculateFee(i % 600);
        }

        long t1 = System.nanoTime();

        for (int i = 0; i < n; i++) {
            sink += (long) c1.calculateFee(i % 600);
        }

        long t2 = System.nanoTime();

        System.out.printf(
                "ns per fee: inheritance %.2f | composition %.2f (sink %d)%n",
                (t1 - t0) / (double) n,
                (t2 - t1) / (double) n,
                sink
        );
    }
}
