// Task 9 - Demonstrating interfaces versus abstract classes

interface Vehicle {
    String getRegNo();

    double ratePerHour();
}

interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Other {
    abstract void display();
}

// A class can implement multiple interfaces.
class EV extends Other implements Vehicle, Chargeable {
    private final String regNo;

    EV(String regNo) {
        this.regNo = regNo;
    }

    @Override
    public String getRegNo() {
        return regNo;
    }

    @Override
    public double ratePerHour() {
        return 30;
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 12;
    }

    @Override
    void display() {
        System.out.println("Electric vehicle: " + regNo);
    }
}

public class VehicleAsInterface {
    public static void main(String[] args) {
        EV ev = new EV("TN03C3");

        ev.display();

        Vehicle v = ev;
        Chargeable c = ev;

        System.out.println("Registration: " + v.getRegNo());
        System.out.println("Parking rate: Rs " + v.ratePerHour());
        System.out.println("Charging fee: Rs " + c.chargingFee(10));
    }
}