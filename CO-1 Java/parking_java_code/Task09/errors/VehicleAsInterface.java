interface Vehicle {
    private final String regNo;              // an interface cannot hold instance state
    Vehicle(String regNo) { }                // an interface cannot have a constructor
    double ratePerHour();
}
abstract class Chargeable {                  // a class can extend only ONE abstract class
    abstract double chargingFee(int kWh);
}
abstract class Other { }
class EV extends Chargeable, Other { double chargingFee(int k) { return 0; } }
