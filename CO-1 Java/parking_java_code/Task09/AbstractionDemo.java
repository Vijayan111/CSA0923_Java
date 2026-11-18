// Task 9 - Abstract class, interfaces, default and static methods

interface Chargeable {
    double RATE_PER_KWH = 12;

    double chargingFee(int kWh);

    default String chargingReceipt(int kWh) {
        return "Charging " + kWh + " kWh = Rs " + chargingFee(kWh);
    }

    static boolean isValidEnergy(int kWh) {
        return kWh >= 0 && kWh <= 200;
    }
}

interface Payable {
    double amountDue();

    default boolean isFree() {
        return amountDue() == 0;
    }
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException(
                    "Registration number is required");
        }
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException(
                    "Parking minutes cannot be negative");
        }

        int hours = minutes / 60;
        if (minutes % 60 != 0) {
            hours++;
        }

        return hours * ratePerHour();
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

class ElectricCar extends Car implements Chargeable, Payable {
    private int kWh;
    private int minutes;

    ElectricCar(String regNo) {
        super(regNo);
    }

    void use(int minutes, int kWh) {
        if (minutes < 0) {
            throw new IllegalArgumentException(
                    "Parking minutes cannot be negative");
        }

        if (!Chargeable.isValidEnergy(kWh)) {
            throw new IllegalArgumentException(
                    "Energy must be between 0 and 200 kWh");
        }

        this.minutes = minutes;
        this.kWh = kWh;
    }

    @Override
    public double chargingFee(int kWh) {
        if (!Chargeable.isValidEnergy(kWh)) {
            throw new IllegalArgumentException(
                    "Energy must be between 0 and 200 kWh");
        }

        return kWh * RATE_PER_KWH;
    }

    @Override
    public double amountDue() {
        return calculateFee(minutes) + chargingFee(kWh);
    }
}

public class AbstractionDemo {
    public static void main(String[] args) {
        ElectricCar e = new ElectricCar("TN03C3");
        e.use(130, 10);

        System.out.println(e.chargingReceipt(10));

        System.out.println(
                "amount due = " + e.amountDue()
                + ", free? " + e.isFree());

        System.out.println(
                "valid energy 250 kWh? "
                + Chargeable.isValidEnergy(250)
                + ", 40 kWh? "
                + Chargeable.isValidEnergy(40));

        Payable p = e;
        Chargeable c = e;
        Vehicle v = e;

        System.out.println(
                p.amountDue() + " "
                + c.chargingFee(1) + " "
                + v.getRegNo());

        // An anonymous subclass can implement an abstract method.
        Vehicle anonymous = new Vehicle("TN99X9") {
            @Override
            protected double ratePerHour() {
                return 1;
            }
        };

        System.out.println(
                "anonymous subclass created: "
                + anonymous.getRegNo());

        // new Vehicle("X"); // Compilation error: Vehicle is abstract.
    }
}