package parking.model;

import java.time.LocalDateTime;

public class ParkingTicket {
    private static int nextNo = 1;                    // never reused, even after exit
    private final int ticketNo;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final Pass pass;                         // null = no pass

    public ParkingTicket(Vehicle vehicle, int floor, int slot, LocalDateTime entry, Pass pass) {
        this.ticketNo = nextNo++;
        this.vehicle = vehicle; this.floor = floor; this.slot = slot;
        this.entry = entry; this.pass = pass;
    }

    public int getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalDateTime getEntry() { return entry; }
    public boolean isParkingFree(LocalDateTime exit) { return pass != null && pass.covers(entry, exit); }
}
