// Task 9 - abstract class + interfaces (default and static methods), multiple interfaces
interface Chargeable {
    double RATE_PER_KWH = 12;                                   // implicitly public static final
    double chargingFee(int kWh);
    default String chargingReceipt(int kWh) { return "Charging " + kWh + " kWh = Rs " + chargingFee(kWh); }
    static boolean isValidEnergy(int kWh) { return kWh >= 0 && kWh <= 200; }
}
interface Payable {
    double amountDue();
    default boolean isFree() { return amountDue() == 0; }
}

abstract class Vehicle {                                        // shared state + constructor + partial behaviour
    private final String regNo;
    protected Vehicle(String r) { regNo = r; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();                    // what differs: abstract
    public double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }   // what is common: concrete
}
class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}
class ElectricCar extends Car implements Chargeable, Payable {   // multiple behaviours through interfaces
    private int kWh; private int minutes;
    ElectricCar(String r) { super(r); }
    void use(int minutes, int kWh) { this.minutes = minutes; this.kWh = kWh; }
    @Override public double chargingFee(int kWh) { return kWh * RATE_PER_KWH; }
    @Override public double amountDue() { return calculateFee(minutes) + chargingFee(kWh); }
}

public class AbstractionDemo {
    public static void main(String[] args) {
        ElectricCar e = new ElectricCar("TN03C3");
        e.use(130, 10);
        System.out.println(e.chargingReceipt(10));
        System.out.println("amount due = " + e.amountDue() + ", free? " + e.isFree());
        System.out.println("valid energy 250 kWh? " + Chargeable.isValidEnergy(250) + ", 40 kWh? " + Chargeable.isValidEnergy(40));
        Payable p = e; Chargeable c = e; Vehicle v = e;           // one object, three types
        System.out.println(p.amountDue() + " " + c.chargingFee(1) + " " + v.getRegNo());
        try { Object o = new Vehicle("X") { protected double ratePerHour() { return 1; } }; System.out.println("anonymous subclass created: " + ((Vehicle) o).getRegNo()); } catch (Exception ex) { }
        // new Vehicle("X");   // compile error: Vehicle is abstract
    }
}
