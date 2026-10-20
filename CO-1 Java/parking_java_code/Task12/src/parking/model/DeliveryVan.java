package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DeliveryVan extends Vehicle {
    private static final double CAR_RATE = 30;                              // same value as Car (duplicated)
    private static final LocalTime PEAK_START = LocalTime.of(9, 0), PEAK_END = LocalTime.of(17, 0);

    public DeliveryVan(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return CAR_RATE; }

    /** Every started hour costs double the car rate if the hour STARTS between 09:00 and 17:00 (17:00 excluded). */
    @Override public double calculateFee(LocalDateTime in, LocalDateTime out) {
        int minutes = (int) Duration.between(in, out).toMinutes();
        int blocks = billableHours(minutes);                                // same daily cap rule as every vehicle
        double fee = 0;
        for (int i = 0; i < blocks; i++) {
            LocalTime start = in.plusMinutes(i * 60L).toLocalTime();
            boolean peak = !start.isBefore(PEAK_START) && start.isBefore(PEAK_END);
            fee += peak ? 2 * CAR_RATE : CAR_RATE;
        }
        return fee;
    }
}
