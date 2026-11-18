class SafeTicket {
    private int durationMinutes;
    private int slot;
    private final int slotCount;

    SafeTicket(int slotCount) {
        if (slotCount <= 0) {
            throw new IllegalArgumentException("slotCount must be positive");
        }
        this.slotCount = slotCount;
    }

    void setDurationMinutes(int m) {
        if (m < 0) {
            throw new IllegalArgumentException(
                "duration cannot be negative: " + m
            );
        }
        durationMinutes = m;
    }

    void setSlot(int s) {
        if (s < 0 || s >= slotCount) {
            throw new IllegalArgumentException("invalid slot " + s);
        }
        slot = s;
    }

    int getDurationMinutes() {
        return durationMinutes;
    }

    int getSlot() {
        return slot;
    }
}

class OpenTicket {
    int durationMinutes;
    int slot;
}

public class EncapsulationDemo {

    static double fee(int minutes, double rate) {
        if (minutes < 0) {
            throw new IllegalArgumentException(
                "duration cannot be negative"
            );
        }
        if (rate < 0) {
            throw new IllegalArgumentException(
                "rate cannot be negative"
            );
        }
        return ((minutes + 59L) / 60) * rate;
    }

    public static void main(String[] args) {
        SafeTicket s = new SafeTicket(20);

        try {
            s.setDurationMinutes(-30);
        } catch (IllegalArgumentException e) {
            System.out.println("safe: " + e.getMessage());
        }

        try {
            s.setSlot(25);
        } catch (IllegalArgumentException e) {
            System.out.println("safe: " + e.getMessage());
        }

        OpenTicket o = new OpenTicket();
        o.durationMinutes = -120;
        o.slot = 25;

        System.out.println(
            "open: duration " + o.durationMinutes
            + " -> fee Rs " + feeForDisplay(o.durationMinutes, 30)
            + ", slot " + o.slot
        );

        Integer a = 127, b = 127, c = 128, d = 128;

        System.out.println(
            "Integer 127 == 127 : " + (a == b)
            + " (cached objects)"
        );

        System.out.println(
            "Integer 128 == 128 : " + (c == d)
            + " (usually different objects)"
        );

        System.out.println("Integer 128 equals : " + c.equals(d));

        Integer slotA = 200, slotB = 200;

        System.out.println(
            "slot numbers 200 == 200 : " + (slotA == slotB)
            + ", equals : " + slotA.equals(slotB)
        );

        Integer maybe = null;

        try {
            int x = maybe;
        } catch (NullPointerException e) {
            System.out.println("unboxing null -> NullPointerException");
        }

        long t0 = System.nanoTime();
        long sum = 0;

        for (int i = 0; i < 50_000_000; i++) {
            sum += i;
        }

        long t1 = System.nanoTime();
        Long boxed = 0L;

        for (int i = 0; i < 50_000_000; i++) {
            boxed += i;
        }

        long t2 = System.nanoTime();

        System.out.printf(
            "sum with long: %d ms | with boxed Long: %d ms (%d %d)%n",
            (t1 - t0) / 1_000_000,
            (t2 - t1) / 1_000_000,
            sum,
            boxed
        );
    }

    static String feeForDisplay(int minutes, double rate) {
        try {
            return String.valueOf(fee(minutes, rate));
        } catch (IllegalArgumentException e) {
            return "invalid duration";
        }
    }
}