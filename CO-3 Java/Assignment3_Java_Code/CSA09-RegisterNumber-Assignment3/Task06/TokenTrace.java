public class TokenTrace {
    private final int[] buffer = new int[5]; private int count = 0, in = 0, out = 0, min = 99, max = -1;
    final StringBuilder log = new StringBuilder();
    void seen(String op) { min = Math.min(min, count); max = Math.max(max, count); if (log.length() < 330) log.append(op).append('=').append(count).append(' '); }
    synchronized void put(int t) throws InterruptedException { while (count == buffer.length) wait(); buffer[in] = t; in = (in + 1) % 5; count++; seen("P"); notifyAll(); }
    synchronized int take() throws InterruptedException { while (count == 0) wait(); int t = buffer[out]; out = (out + 1) % 5; count--; seen("T"); notifyAll(); return t; }
    public static void main(String[] a) throws Exception {
        TokenTrace c = new TokenTrace(); int[] got = new int[2];
        Thread cook = new Thread(() -> { try { for (int t = 1; t <= 20; t++) c.put(t); } catch (InterruptedException e) { } });
        Thread[] st = new Thread[2];
        for (int s = 0; s < 2; s++) { final int id = s; st[s] = new Thread(() -> { try { for (int k = 0; k < 10; k++) { got[id] += c.take(); Thread.sleep(2); } } catch (InterruptedException e) { } }); }
        cook.start(); for (Thread t : st) t.start(); cook.join(); for (Thread t : st) t.join();
        System.out.println("counter size after each operation (P=put, T=take):");
        System.out.println(c.log);
        System.out.println("smallest size seen = " + c.min + ", largest size seen = " + c.max + "  (allowed 0..5)");
        System.out.println("sum collected = " + (got[0] + got[1]));
    }
}
