import java.lang.management.*;

public class Wallet {
    static class Account { final int id; int balance; Account(int id, int b) { this.id = id; balance = b; } }
    static void pause(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { } }

    static void transferUnsafe(Account from, Account to, int amt) {        // locks in argument order
        synchronized (from) {
            pause(100);                                                    // widen the window
            synchronized (to) { from.balance -= amt; to.balance += amt; }
        }
    }
    static void transferOrdered(Account from, Account to, int amt) {       // always lock lower id first
        Account first = from.id < to.id ? from : to, second = first == from ? to : from;
        synchronized (first) {
            pause(100);
            synchronized (second) { from.balance -= amt; to.balance += amt; }
        }
    }
    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 1000), b = new Account(2, 1000);
        System.out.println("== Part A1: unsafe transfers  T1: A->B 100   T2: B->A 50 ==");
        Thread t1 = new Thread(() -> transferUnsafe(a, b, 100), "T1"), t2 = new Thread(() -> transferUnsafe(b, a, 50), "T2");
        t1.setDaemon(true); t2.setDaemon(true); t1.start(); t2.start();
        t1.join(1000); t2.join(1000);
        System.out.println("after 1 s: T1 state=" + t1.getState() + ", T2 state=" + t2.getState());
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        System.out.println("deadlock detected: " + (ids != null) + (ids == null ? "" : ", threads involved = " + ids.length));
        if (ids != null) for (ThreadInfo ti : mx.getThreadInfo(ids))
            System.out.println("  " + ti.getThreadName() + " is BLOCKED waiting for " + ti.getLockName().replaceAll("Wallet\\$Account@", "Account@") + " held by " + ti.getLockOwnerName());

        Account c = new Account(1, 1000), d = new Account(2, 1000);
        System.out.println("\n== Part A2: ordered locking, same two transfers ==");
        Thread u1 = new Thread(() -> transferOrdered(c, d, 100), "T1"), u2 = new Thread(() -> transferOrdered(d, c, 50), "T2");
        long t0 = System.nanoTime(); u1.start(); u2.start(); u1.join(); u2.join();
        System.out.printf("both finished in %d ms; balances: A=%d, B=%d (total %d)%n", (System.nanoTime() - t0) / 1_000_000, c.balance, d.balance, c.balance + d.balance);
    }
}
