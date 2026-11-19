import java.awt.event.ActionListener;
import java.util.*;

/** Alternative concept: one listener with an if-else chain on the command vs one listener object per button. */
public class DispatchBench {
    static volatile int sink;
    static void chain(String cmd, String[] names) {            // the original design: compare until a match
        for (int i = 0; i < names.length; i++) if (names[i].equals(cmd)) { sink += i; return; }
    }
    public static void main(String[] a) {
        System.out.println("k buttons | if-else chain (ns/event, last button) | own listener (ns/event) | extra listener objects");
        for (int k : new int[]{3, 10, 100, 1000}) {
            String[] names = new String[k]; ActionListener[] ls = new ActionListener[k];
            for (int i = 0; i < k; i++) { final int id = i; names[i] = "Button-" + i; ls[i] = e -> sink += id; }
            String cmd = new String("Button-" + (k - 1));       // worst case, not interned
            int N = 2_000_000; long best1 = Long.MAX_VALUE, best2 = Long.MAX_VALUE;
            for (int rep = 0; rep < 7; rep++) {
                long t = System.nanoTime(); for (int i = 0; i < N; i++) chain(cmd, names); best1 = Math.min(best1, System.nanoTime() - t);
                t = System.nanoTime(); for (int i = 0; i < N; i++) ls[k - 1].actionPerformed(null); best2 = Math.min(best2, System.nanoTime() - t);
            }
            System.out.printf("%-9d | %-38.1f | %-23.1f | %d%n", k, best1 / (double) N, best2 / (double) N, k);
        }
    }
}
