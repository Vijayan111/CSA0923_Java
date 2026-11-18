import java.util.*;

public class TriageStudy {
    static long sink;
    static final Comparator<Patient> RULE = Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival);

    public static void main(String[] args) {
        // 1. hand-checkable instance (8 patients)
        String[] nm = {"Ravi", "Meena", "John", "Asha", "Kumar", "Lata", "Dev", "Priya"};
        int[] sv = {3, 1, 3, 5, 1, 2, 2, 1};
        PriorityQueue<Patient> pq = new PriorityQueue<>(RULE);
        for (int i = 0; i < nm.length; i++) pq.add(new Patient(nm[i], sv[i], i));
        System.out.println("Hand-worked order : Meena Kumar Priya Lata Dev Ravi John Asha");
        System.out.println("printing the queue: " + pq);
        System.out.println("   ^ internal heap order, not treatment order");
        StringBuilder sb = new StringBuilder();
        Patient p;
        while ((p = pq.poll()) != null) sb.append(p.name).append(' ');
        System.out.println("poll() order      : " + sb.toString().trim());

        // 2. growth
        System.out.println("\nPriorityQueue cost per operation (ns, median of 5 runs)");
        System.out.printf("%10s %12s %12s%n", "n", "add", "poll");
        for (int pass = 0; pass < 2; pass++)
            for (int n : new int[]{10000, 100000, 1000000}) {
                double[] a = new double[5], q = new double[5];
                for (int r = 0; r < 5; r++) {
                    Random rnd = new Random(r);
                    Patient[] ps = new Patient[n];
                    for (int i = 0; i < n; i++) ps[i] = new Patient("P" + i, 1 + rnd.nextInt(5), i);
                    PriorityQueue<Patient> h = new PriorityQueue<>(RULE);
                    long t0 = System.nanoTime();
                    for (Patient x : ps) h.add(x);
                    long t1 = System.nanoTime();
                    while (h.poll() != null) sink++;
                    long t2 = System.nanoTime();
                    a[r] = (t1 - t0) / (double) n; q[r] = (t2 - t1) / (double) n;
                }
                Arrays.sort(a); Arrays.sort(q);
                if (pass == 1) System.out.printf("%10d %12.0f %12.0f%n", n, a[2], q[2]);
            }

        // 3. starvation and an aging rule: key = arrival + severity * W
        System.out.println("\nStarvation test: Asha (S5) waits; one S1 patient arrives and one is served every tick");
        for (int W : new int[]{0, 50}) {
            Comparator<Patient> c = W == 0 ? RULE
                    : Comparator.comparingLong((Patient x) -> x.arrival + (long) x.severity * W).thenComparingLong(x -> x.arrival);
            PriorityQueue<Patient> h = new PriorityQueue<>(c);
            h.add(new Patient("Asha", 5, 0));
            long servedAt = -1;
            for (long t = 1; t <= 2000 && servedAt < 0; t++) {
                h.add(new Patient("S1-" + t, 1, t));
                if (h.poll().name.equals("Asha")) servedAt = t;
            }
            System.out.println((W == 0 ? "strict rule        : " : "aging rule (W=50) : ") + (servedAt < 0 ? "Asha NOT served in 2000 ticks" : "Asha served at tick " + servedAt));
        }

        // 4. alternative designs
        System.out.println("\nAlternative designs: n admits then n serves (ns per operation)");
        System.out.printf("%8s | %-22s | %-22s | %-22s%n", "n", "PriorityQueue add/poll", "sorted LinkedList a/s", "5 FIFO queues add/poll");
        for (int n : new int[]{10000, 50000, 100000}) {
            Random rnd = new Random(9);
            Patient[] ps = new Patient[n];
            for (int i = 0; i < n; i++) ps[i] = new Patient("P" + i, 1 + rnd.nextInt(5), i);
            double[] pqT = run(ps, 0), llT = run(ps, 1), qqT = run(ps, 2);
            System.out.printf("%8d | %10.0f %11.0f | %10.0f %11.0f | %10.0f %11.0f%n", n, pqT[0], pqT[1], llT[0], llT[1], qqT[0], qqT[1]);
        }
        if (sink == 42) System.out.println();
    }

    static double[] run(Patient[] ps, int kind) {
        double[] best = {Double.MAX_VALUE, Double.MAX_VALUE};
        for (int rep = 0; rep < 4; rep++) {
            long t0, t1, t2;
            int n = ps.length;
            if (kind == 0) {
                PriorityQueue<Patient> h = new PriorityQueue<>(RULE);
                t0 = System.nanoTime(); for (Patient x : ps) h.add(x);
                t1 = System.nanoTime(); while (h.poll() != null) sink++;
                t2 = System.nanoTime();
            } else if (kind == 1) {
                LinkedList<Patient> l = new LinkedList<>();
                t0 = System.nanoTime();
                for (Patient x : ps) {
                    ListIterator<Patient> it = l.listIterator();
                    while (it.hasNext()) { if (RULE.compare(it.next(), x) > 0) { it.previous(); break; } }
                    it.add(x);
                }
                t1 = System.nanoTime(); while (l.pollFirst() != null) sink++;
                t2 = System.nanoTime();
            } else {
                @SuppressWarnings("unchecked") ArrayDeque<Patient>[] qs = new ArrayDeque[6];
                for (int i = 1; i <= 5; i++) qs[i] = new ArrayDeque<>();
                t0 = System.nanoTime(); for (Patient x : ps) qs[x.severity].addLast(x);
                t1 = System.nanoTime();
                for (int i = 0; i < n; i++) { for (int s = 1; s <= 5; s++) if (!qs[s].isEmpty()) { sink += qs[s].pollFirst().severity; break; } }
                t2 = System.nanoTime();
            }
            best[0] = Math.min(best[0], (t1 - t0) / (double) ps.length);
            best[1] = Math.min(best[1], (t2 - t1) / (double) ps.length);
        }
        return best;
    }
}
