package parking.service;

import java.time.LocalDateTime;
import parking.model.*;

public class ParkingService {
    private final ParkingFacility facility;
    private final ParkingTicket[] active;             // active tickets in an array, searched linearly
    private int activeCount = 0;
    private double collection = 0;

    public ParkingService(ParkingFacility facility) {
        this.facility = facility;
        this.active = new ParkingTicket[facility.capacity()];
    }

    private int find(String regNo) {
        for (int i = 0; i < activeCount; i++) if (active[i].getVehicle().getRegNo().equals(regNo)) return i;
        return -1;
    }

    public ParkingTicket enter(String type, String regNo, LocalDateTime time, boolean monthlyPass) {
        return enter(type, regNo, time, monthlyPass ? new MonthlyPass() : null);      // old signature still works
    }

    public ParkingTicket enter(String type, String regNo, LocalDateTime time, Pass pass) {
        Vehicle v = VehicleFactory.create(type, regNo);                 // unknown type -> IllegalArgumentException
        if (find(regNo) >= 0) throw new IllegalStateException("duplicate registration: " + regNo);
        int[] pos = facility.allocate(regNo);
        if (pos == null) throw new IllegalStateException("facility full");
        ParkingTicket t = new ParkingTicket(v, pos[0], pos[1], time, pass);
        active[activeCount++] = t;
        return t;
    }

    /** Releases the slot and returns the total amount charged (parking + charging). */
    public double exit(String regNo, LocalDateTime time, int kWh) {
        int i = find(regNo);
        if (i < 0) throw new IllegalStateException("no active ticket for " + regNo);
        ParkingTicket t = active[i];
        if (!facility.release(t.getFloor(), t.getSlot())) throw new IllegalStateException("slot already free");
        active[i] = active[--activeCount]; active[activeCount] = null;
        double fee = t.isParkingFree(time) ? 0 : t.getVehicle().calculateFee(t.getEntry(), time);
        if (t.getVehicle() instanceof Chargeable c && kWh > 0) fee += c.chargingFee(kWh);
        collection += fee;
        return fee;
    }

    public double getCollection() { return collection; }
    public int getActiveCount() { return activeCount; }
}
