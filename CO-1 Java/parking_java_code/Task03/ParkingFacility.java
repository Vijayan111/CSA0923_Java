public class ParkingFacility {
    // null means the slot is free
    private final String[][] slots;

    public ParkingFacility(int[] slotsPerFloor) {
        if (slotsPerFloor == null) {
            throw new IllegalArgumentException("Slot configuration cannot be null");
        }

        slots = new String[slotsPerFloor.length][];

        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] < 0) {
                throw new IllegalArgumentException("Slot count cannot be negative");
            }
            slots[f] = new String[slotsPerFloor[f]];
        }
    }

    /** Returns {floor, slot} of the first free slot, or null if full. */
    public int[] allocate(String vehicleNo) {
        if (vehicleNo == null || vehicleNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number cannot be empty");
        }

        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = vehicleNo;
                    return new int[]{f, s};
                }
            }
        }

        return null;
    }

    /** Releases an occupied slot. Returns false for an invalid or free slot. */
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length) {
            return false;
        }

        if (slot < 0 || slot >= slots[floor].length) {
            return false;
        }

        if (slots[floor][slot] == null) {
            return false;
        }

        slots[floor][slot] = null;
        return true;
    }

    public static void main(String[] args) {
        // Floor 0 has two slots; floor 1 has one slot.
        ParkingFacility p = new ParkingFacility(new int[]{2, 1});

        System.out.println(java.util.Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));

        System.out.println(
            p.release(0, 1) + " " +
            p.release(0, 1) + " " +
            p.release(5, 0)
        );

        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
    }
}