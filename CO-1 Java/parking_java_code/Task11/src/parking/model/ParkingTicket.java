package parking.model;

import java.time.LocalDateTime;

public class ParkingTicket {
    private static int nextNo = 1;                    // never reused, even after exit
    private final int ticketNo;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final boolean monthlyPass;

    public ParkingTicket(Vehicle vehicle, int floor, int slot, LocalDateTime entry, boolean monthlyPass) {
        this.ticketNo = nextNo++;
        this.vehicle = vehicle; this.floor = floor; this.slot = slot;
        this.entry = entry; this.monthlyPass = monthlyPass;
    }

    public int getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalDateTime getEntry() { return entry; }
    public boolean isParkingFree(LocalDateTime exit) { return monthlyPass; }
}
