import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class SharedLogDemo {
    static void run(String label, Consumer<String> add, IntSupplier len) throws Exception {
        Thread[] ts = new Thread[8];
        final int[] crashed = {0};
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> {
                try { for (int k = 0; k < 200000; k++) add.accept("x"); }
                catch (RuntimeException e) { synchronized (crashed) { crashed[0]++; } }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println(label + ": expected 1600000, actual " + len.getAsInt() + ", crashed threads: " + crashed[0]);
    }
    public static void main(String[] args) throws Exception {
        for (int r = 1; r <= 5; r++) {
            System.out.println("--- run " + r);
            StringBuilder sb = new StringBuilder();
            run("StringBuilder (shared)", sb::append, sb::length);
            StringBuffer sf = new StringBuffer();
            run("StringBuffer  (shared)", sf::append, sf::length);
        }
    }
}
