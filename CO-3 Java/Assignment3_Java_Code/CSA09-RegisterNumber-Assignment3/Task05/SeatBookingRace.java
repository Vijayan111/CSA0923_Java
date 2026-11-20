import java.util.concurrent.locks.LockSupport;

public class SeatBookingRace {
    static class Screen {
        final boolean[] booked = new boolean[10]; int made = 0;
        final boolean sync, slowCheck;
        Screen(boolean sync, boolean slowCheck) { this.sync = sync; this.slowCheck = slowCheck; }
        void book(int seat) {
            if (sync) { synchronized (this) { doBook(seat); } } else doBook(seat);
        }
        private void doBook(int seat) {
            if (!booked[seat]) {                                   // CHECK
                if (slowCheck) { try { Thread.sleep(1); } catch (InterruptedException e) { } }   // e.g. payment/DB call
                booked[seat] = true; made++;                       // ACT
            }
        }
    }
    static int run(boolean sync, boolean slow) throws InterruptedException {
        Screen s = new Screen(sync, slow);
        Thread[] u = new Thread[100];
        for (int i = 0; i < 100; i++) { final int seat = i % 10; u[i] = new Thread(() -> s.book(seat)); u[i].start(); }
        for (Thread t : u) t.join();
        return s.made;
    }
    public static void main(String[] a) throws Exception {
        System.out.println("Seats sold over 10 runs (correct answer = 10 every time)");
        String[] label = {"A: no sync, plain check-then-act", "B: no sync, 1 ms between check and act", "C: synchronized"};
        boolean[][] mode = {{false, false}, {false, true}, {true, true}};
        for (int m = 0; m < 3; m++) {
            StringBuilder sb = new StringBuilder(); int wrong = 0, extra = 0;
            for (int r = 0; r < 10; r++) { int n = run(mode[m][0], mode[m][1]); sb.append(String.format("%4d", n)); if (n != 10) { wrong++; extra += n - 10; } }
            System.out.printf("%-42s%s   wrong runs=%d, extra seats sold=%d%n", label[m], sb, wrong, extra);
        }
    }
}
