package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Vehicle {
    public static final int MAX_BILLABLE_HOURS_PER_DAY = 8;     // daily maximum
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("registration number required");
        this.regNo = regNo;
    }

    public final String getRegNo() { return regNo; }

    protected abstract double ratePerHour();

    /** Started hours are charged in full; at most 8 hours per 24-hour day. */
    protected int billableHours(int minutes) {
        if (minutes <= 0) return 0;
        int days = minutes / (24 * 60);
        int rest = minutes % (24 * 60);
        int restHours = (rest + 59) / 60;
        return days * MAX_BILLABLE_HOURS_PER_DAY + Math.min(restHours, MAX_BILLABLE_HOURS_PER_DAY);
    }

    public double calculateFee(int minutes) { return billableHours(minutes) * ratePerHour(); }

    public double calculateFee(LocalDateTime in, LocalDateTime out) {
        return calculateFee((int) Duration.between(in, out).toMinutes());
    }

    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
    @Override public boolean equals(Object o) { return o instanceof Vehicle v && regNo.equals(v.regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
}
