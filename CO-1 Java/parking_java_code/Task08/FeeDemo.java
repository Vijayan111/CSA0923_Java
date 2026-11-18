// Task 9 - Inheritance, method overriding, interfaces, and equals()

import java.util.Objects;

interface Chargeable {
    double chargingFee(int kWh);
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

    /**
     * Charges for every started hour.
     */
    public double calculateFee(int minutes) {

        if (minutes < 0) {
            throw new IllegalArgumentException(
                    "Minutes cannot be negative");
        }

        int hours = minutes / 60;

        if (minutes % 60 != 0) {
            hours++;
        }

        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Vehicle)) {
            return false;
        }

        Vehicle other = (Vehicle) o;

        return regNo.equals(other.regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNo);
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

class ElectricCar extends Car implements Chargeable {

    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {

        if (kWh < 0) {
            throw new IllegalArgumentException(
                    "Charging units cannot be negative");
        }

        return kWh * 12;
    }
}

public class FeeDemo {

    public static void main(String[] args) {

        Vehicle[] parked = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        for (Vehicle v : parked) {

            double fee = v.calculateFee(130);

            if (v instanceof Chargeable) {
                Chargeable c = (Chargeable) v;
                fee += c.chargingFee(10);
            }

            System.out.println(
                    v.getClass().getSimpleName()
                    + " " + v.getRegNo()
                    + " -> Rs " + fee);
        }

        Car car = new Car("TN02B2");
        ElectricCar electricCar =
                new ElectricCar("TN02B2");

        System.out.println(
                "Same registration number: "
                + car.equals(electricCar));
    }
}