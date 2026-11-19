import javax.swing.AbstractAction;
import java.awt.event.*;
import java.lang.management.ManagementFactory;

/** Bytes allocated per listener object for each way of writing the Convert action. */
public class ListenerCost {
    static volatile Object[] sink = new Object[1024];
    static long alloc() { return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().getId()); }
    void convert() { }
    class Named implements ActionListener { public void actionPerformed(ActionEvent e) { convert(); } }
    interface Maker { Object make(); }
    static void measure(String name, Maker m) {
        for (int i = 0; i < 200_000; i++) sink[i & 1023] = m.make();               // warm-up
        int N = 1_000_000; long b = alloc(); for (int i = 0; i < N; i++) sink[i & 1023] = m.make();
        System.out.printf("%-42s %6.1f bytes per object%n", name, (alloc() - b) / (double) N);
    }
    public static void main(String[] a) {
        ListenerCost o = new ListenerCost();
        System.out.println("Allocated bytes per listener (64-bit HotSpot, compressed oops)");
        measure("(a) frame implements ActionListener", () -> o);                                  // the existing frame object: nothing new
        measure("(b) named inner class instance", () -> o.new Named());
        measure("(c) anonymous inner class instance", () -> new ActionListener() { public void actionPerformed(ActionEvent e) { o.convert(); } });
        measure("(d) lambda e -> convert() capturing this", () -> (ActionListener) e -> o.convert());
        measure("    lambda that captures nothing", () -> (ActionListener) e -> { });
        measure("    method reference o::convert", () -> (ActionListener) e -> o.convert());
        measure("    shared Action (AbstractAction subclass)", () -> new AbstractAction("Convert") { public void actionPerformed(ActionEvent e) { o.convert(); } });
    }
}
