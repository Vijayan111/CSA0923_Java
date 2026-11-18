import java.util.*;

public class PlaylistRedesign {
    record Song(int id) {}
    static long sink;

    /** Gap-buffer playlist: the free space ("gap") is kept at the editing cursor. */
    static class GapPlaylist {
        private Song[] a;
        private int gapStart, gapEnd;      // gap = a[gapStart .. gapEnd-1]
        long moves;                        // elements moved (operation count)

        GapPlaylist(int capacity) { a = new Song[Math.max(capacity, 16)]; gapEnd = a.length; }
        int size() { return a.length - (gapEnd - gapStart); }
        Song get(int i) { return a[i < gapStart ? i : i + (gapEnd - gapStart)]; }

        private void moveGap(int pos) {
            if (pos < gapStart) {
                int k = gapStart - pos;
                System.arraycopy(a, pos, a, gapEnd - k, k);
                gapStart -= k; gapEnd -= k; moves += k;
            } else if (pos > gapStart) {
                int k = pos - gapStart;
                System.arraycopy(a, gapEnd, a, gapStart, k);
                gapStart += k; gapEnd += k; moves += k;
            }
        }
        private void grow() {
            Song[] b = new Song[a.length * 2];
            System.arraycopy(a, 0, b, 0, gapStart);
            int tail = a.length - gapEnd;
            System.arraycopy(a, gapEnd, b, b.length - tail, tail);
            gapEnd = b.length - tail; a = b; moves += size();
        }
        void add(int i, Song s) {
            if (gapStart == gapEnd) grow();
            moveGap(i);
            a[gapStart++] = s;
        }
        Song remove(int i) {
            moveGap(i);
            Song s = a[gapEnd]; a[gapEnd++] = null;
            return s;
        }
    }

    // workload: 'ops' inserts; cursor moves by at most 'step' songs between edits (step=-1: fully random position)
    static int[] positions(int n, int ops, int step, long seed) {
        Random r = new Random(seed);
        int[] p = new int[ops];
        int cur = n / 2;
        for (int i = 0; i < ops; i++) {
            int size = n + i;
            cur = step < 0 ? r.nextInt(size + 1) : Math.max(0, Math.min(size, cur + r.nextInt(2 * step + 1) - step));
            p[i] = cur;
        }
        return p;
    }

    static boolean verify() {
        Random r = new Random(11);
        List<Song> ref = new ArrayList<>();
        GapPlaylist g = new GapPlaylist(4);
        for (int i = 0; i < 20000; i++) {
            if (ref.isEmpty() || r.nextInt(3) != 0) { int pos = r.nextInt(ref.size() + 1); Song s = new Song(i); ref.add(pos, s); g.add(pos, s); }
            else { int pos = r.nextInt(ref.size()); if (ref.remove(pos) != g.remove(pos)) return false; }
            if (g.size() != ref.size()) return false;
        }
        for (int i = 0; i < ref.size(); i++) if (ref.get(i) != g.get(i)) return false;
        return true;
    }

    static double median(double[] t) { Arrays.sort(t); return t[t.length / 2]; }

    public static void main(String[] args) {
        System.out.println("Correctness: GapPlaylist == ArrayList after 20,000 random add/remove: " + verify());
        System.out.println();
        int ops = 10000;
        System.out.println("Original (ArrayList.add(i, x)) vs redesign (GapPlaylist) - " + ops + " inserts, median of 5 runs");
        System.out.printf("%-18s %8s | %10s %14s | %10s %14s | %s%n", "cursor pattern", "n", "ArrayList", "elems moved", "GapPlaylist", "elems moved", "speed-up");
        for (int pass = 0; pass < 2; pass++)
            for (int step : new int[]{5, -1})
                for (int n : new int[]{10000, 100000, 400000}) {
                    if (pass == 0 && n != 10000) continue;          // warm-up pass: smallest size only
                    int[] pos = positions(n, ops, step, 3);
                    Song s = new Song(0);
                    double[] ta = new double[5], tg = new double[5];
                    long movedA = 0, movedG = 0;
                    for (int r = 0; r < 5; r++) {
                        List<Song> l = new ArrayList<>(n + ops);
                        for (int i = 0; i < n; i++) l.add(s);
                        long t0 = System.nanoTime();
                        long m = 0;
                        for (int i = 0; i < ops; i++) { l.add(pos[i], s); m += (n + i) - pos[i]; }
                        ta[r] = (System.nanoTime() - t0) / 1e6; movedA = m;
                        GapPlaylist g = new GapPlaylist(n + ops);
                        for (int i = 0; i < n; i++) g.add(i, s);
                        g.moves = 0;
                        t0 = System.nanoTime();
                        for (int i = 0; i < ops; i++) g.add(pos[i], s);
                        tg[r] = (System.nanoTime() - t0) / 1e6; movedG = g.moves;
                        sink += l.size() + g.size();
                    }
                    if (pass == 1)
                        System.out.printf("%-18s %8d | %8.2f ms %14d | %8.2f ms %14d | x%.1f%n", step < 0 ? "random position" : "local (+-5 songs)", n,
                                median(ta), movedA, median(tg), movedG, median(ta) / median(tg));
                }
        System.out.println();
        System.out.println("Extra memory: GapPlaylist keeps its gap (up to capacity - size) free; ArrayList also over-allocates (about 1.5x on growth).");
        if (sink == 42) System.out.println();
    }
}
