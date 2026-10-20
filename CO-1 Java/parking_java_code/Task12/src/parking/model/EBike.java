package parking.model;

public class EBike extends Vehicle {
    public static final double FLAT_FEE = 15;
    public EBike(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 0; }                 // not used, flat fee instead
    @Override public double calculateFee(int minutes) { return minutes > 0 ? FLAT_FEE : 0; }
}
