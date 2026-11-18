import java.util.*;

public class PlaylistBench {
    record Song(int id) {}
    static long sink;
    static final int OPS = 1000;

    interface Op { void run(); }
    static double ns(Op op, int reps) {          // average ns per call of op (which does OPS operations)
        long t = 0;
        for (int r = 0; r < reps; r++) { long s = System.nanoTime(); op.run(); t += System.nanoTime() - s; }
        return t / (double) reps / OPS;
    }

    static List<Song> build(List<Song> l, int n) { for (int i = 0; i < n; i++) l.add(new Song(i)); return l; }

    static void suite(int n, boolean print) {
        Random rnd = new Random(1);
        Song s = new Song(-1);
        String[] ops = {"add at end", "insert front", "insert middle", "remove front", "remove middle", "get k-th", "iterate (ms)"};
        double[][] res = new double[ops.length][3];
        List<List<Song>> lists = List.of(build(new ArrayList<>(), n), build(new LinkedList<>(), n));
        for (int c = 0; c < 2; c++) {
            List<Song> l = lists.get(c);
            res[0][c] = ns(() -> { for (int i = 0; i < OPS; i++) l.add(s); }, 3);
            res[1][c] = ns(() -> { for (int i = 0; i < OPS; i++) l.add(0, s); }, 3);
            res[2][c] = ns(() -> { for (int i = 0; i < OPS; i++) l.add(l.size() / 2, s); }, 3);
            res[3][c] = ns(() -> { for (int i = 0; i < OPS; i++) l.remove(0); }, 3);
            res[4][c] = ns(() -> { for (int i = 0; i < OPS; i++) l.remove(l.size() / 2); }, 3);
            res[5][c] = ns(() -> { for (int i = 0; i < OPS; i++) sink += l.get(rnd.nextInt(l.size())).id(); }, 3);
            long t = 0;
            for (int r = 0; r < 5; r++) { long st = System.nanoTime(); for (Song x : l) sink += x.id(); t += System.nanoTime() - st; }
            res[6][c] = t / 5 / 1e6;
        }
        ArrayDeque<Song> d = new ArrayDeque<>();
        for (int i = 0; i < n; i++) d.add(new Song(i));
        res[0][2] = ns(() -> { for (int i = 0; i < OPS; i++) d.addLast(s); }, 3);
        res[1][2] = ns(() -> { for (int i = 0; i < OPS; i++) d.addFirst(s); }, 3);
        res[3][2] = ns(() -> { for (int i = 0; i < OPS; i++) d.pollFirst(); }, 3);
        long t = 0;
        for (int r = 0; r < 5; r++) { long st = System.nanoTime(); for (Song x : d) sink += x.id(); t += System.nanoTime() - st; }
        res[6][2] = t / 5 / 1e6;
        if (!print) return;
        System.out.println("playlist size n = " + n + "   (ns per operation, avg of 3 runs x " + OPS + " ops)");
        System.out.printf("%-14s %12s %12s %12s%n", "operation", "ArrayList", "LinkedList", "ArrayDeque");
        for (int i = 0; i < ops.length; i++) {
            boolean na = (i == 2 || i == 4 || i == 5);
            System.out.printf("%-14s %12.1f %12.1f %12s%n", ops[i], res[i][0], res[i][1], na ? "n/a" : String.format("%.1f", res[i][2]));
        }
        System.out.println();
    }

    static void upNext() {
        int n = 50000, cycles = 50000;
        System.out.println("Up Next queue: length " + n + ", " + cycles + " cycles of addLast, addFirst, removeFirst x2 (total ms)");
        Song s = new Song(0);
        for (int pass = 0; pass < 2; pass++) {
            List<Song> al = build(new ArrayList<>(), n), ll = build(new LinkedList<>(), n);
            ArrayDeque<Song> ad = new ArrayDeque<>(al);
            long t0 = System.nanoTime();
            for (int i = 0; i < cycles; i++) { al.add(s); al.add(0, s); al.remove(0); al.remove(0); }
            long t1 = System.nanoTime();
            for (int i = 0; i < cycles; i++) { ll.add(s); ll.add(0, s); ll.remove(0); ll.remove(0); }
            long t2 = System.nanoTime();
            for (int i = 0; i < cycles; i++) { ad.addLast(s); ad.addFirst(s); ad.removeFirst(); ad.removeFirst(); }
            long t3 = System.nanoTime();
            if (pass == 1)
                System.out.printf("ArrayList %.1f   LinkedList %.1f   ArrayDeque %.1f%n", (t1 - t0) / 1e6, (t2 - t1) / 1e6, (t3 - t2) / 1e6);
        }
        System.out.println();
    }

    static void manualArray() {
        System.out.println("Plain array with manual shifting: insert at front (ns per insert)");
        System.out.printf("%10s %16s %16s%n", "n", "manual loop", "ArrayList.add(0)");
        for (int n : new int[]{10000, 100000, 400000}) {
            Song[] a = new Song[n + OPS];
            for (int i = 0; i < n; i++) a[i] = new Song(i);
            Song s = new Song(-1);
            int[] size = {n};
            double manual = ns(() -> { for (int k = 0; k < 200; k++) { for (int i = size[0]; i > 0; i--) a[i] = a[i - 1]; a[0] = s; size[0]++; } }, 3) * OPS / 200;
            List<Song> l = build(new ArrayList<>(), n);
            double lib = ns(() -> { for (int i = 0; i < OPS; i++) l.add(0, s); }, 3);
            System.out.printf("%10d %16.1f %16.1f%n", n, manual, lib);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        suite(10000, false);                       // warm-up
        for (int n : new int[]{10000, 100000, 400000}) suite(n, true);
        upNext();
        manualArray();
        if (sink == 42) System.out.println();
    }
}
