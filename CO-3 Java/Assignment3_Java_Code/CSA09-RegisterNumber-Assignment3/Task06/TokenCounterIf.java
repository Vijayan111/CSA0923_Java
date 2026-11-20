// Same counter but with "if" instead of "while"; main() uses time-outs so a hang is reported, not endless.
public class TokenCounterIf {
    private final int[] buffer = new int[5]; private int count = 0, in = 0, out = 0;
    public synchronized void put(int token) throws InterruptedException {
        if (count == buffer.length) wait();                  // BUG: not re-checked after waking
        buffer[in] = token; in = (in + 1) % buffer.length; count++; notifyAll();
    }
    public synchronized int take() throws InterruptedException {
        if (count == 0) wait();                              // BUG: not re-checked after waking
        int token = buffer[out]; out = (out + 1) % buffer.length; count--; notifyAll(); return token;
    }
    static String once(long cookDelayMs) throws InterruptedException {
        TokenCounterIf c = new TokenCounterIf(); int[] got = new int[2];
        Thread cook = new Thread(() -> { try { for (int t = 1; t <= 20; t++) { c.put(t); if (cookDelayMs > 0) Thread.sleep(cookDelayMs); } } catch (InterruptedException e) { } });
        Thread[] st = new Thread[2];
        for (int s = 0; s < 2; s++) { final int id = s; st[s] = new Thread(() -> { try { for (int k = 0; k < 10; k++) got[id] += c.take(); } catch (InterruptedException e) { } }); }
        cook.setDaemon(true); for (Thread t : st) t.setDaemon(true);
        cook.start(); for (Thread t : st) t.start();
        cook.join(1500); for (Thread t : st) t.join(1500);
        boolean hung = cook.isAlive() || st[0].isAlive() || st[1].isAlive();
        int sum = got[0] + got[1];
        return hung ? "HUNG (threads still waiting), partial sum " + sum : (sum == 210 ? "sum 210 (correct)" : "WRONG sum " + sum + ", count=" + c.count);
    }
    public static void main(String[] a) throws Exception {
        for (long delay : new long[]{0, 1}) {
            System.out.println("cook delay between tokens = " + delay + " ms, 10 runs:");
            for (int r = 1; r <= 10; r++) System.out.println("  run " + r + ": " + once(delay));
        }
    }
}
