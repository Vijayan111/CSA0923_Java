import java.util.*;

public class ComparatorTests {
    static class TeamC extends Team implements Comparable<TeamC> {      // alternative: natural ordering inside Team
        TeamC(String n, int p, int g) { super(n, p, g); }
        @Override public int compareTo(TeamC o) {
            int c = Integer.compare(o.points, points);
            if (c != 0) return c;
            c = Integer.compare(o.goalDiff, goalDiff);
            return c != 0 ? c : name.compareTo(o.name);
        }
    }

    static final Comparator<Team> GOOD = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()).thenComparing(t -> t.name);
    static final Comparator<Team> SUBTRACT = (a, b) -> b.points - a.points;                 // faulty: overflow
    static final Comparator<Team> NO_TIEBREAK = (a, b) -> Integer.compare(b.points, a.points); // faulty: ignores ties

    // wildcard / bounded type helpers
    static <T> void addAll(Leaderboard<T> lb, List<? extends T> src) { for (T t : src) lb.add(t); }
    static <T extends Comparable<? super T>> T best(List<? extends T> list) { return Collections.min(list); }

    static List<Team> dataset1() {
        return new ArrayList<>(List.of(new Team("Lions", 12, 5), new Team("Wolves", 12, 5), new Team("Hawks", 12, 2),
                new Team("Sharks", 9, 3), new Team("Tigers", 9, 3), new Team("Bears", 9, 3), new Team("Foxes", 6, -2), new Team("Owls", 6, -2)));
    }

    public static void main(String[] args) {
        System.out.println("Data set 1 (ties at every level), comparator chain:");
        List<Team> d = dataset1(); d.sort(GOOD);
        System.out.println(d);
        List<Team> shuffled = dataset1(); Collections.reverse(shuffled); shuffled.sort(GOOD);
        System.out.println("same result after reversing the input: " + d.toString().equals(shuffled.toString()));

        System.out.println("\nData set 2a - faulty comparator that ignores the tie-breakers (input order decides):");
        List<Team> a = dataset1(); a.sort(NO_TIEBREAK);
        List<Team> b = dataset1(); Collections.reverse(b); b.sort(NO_TIEBREAK);
        System.out.println(a); System.out.println(b);
        System.out.println("same result for both inputs: " + a.toString().equals(b.toString()));

        System.out.println("\nData set 2b - faulty comparator using subtraction (points near int limits):");
        List<Team> c = new ArrayList<>(List.of(new Team("A", 2_000_000_000, 0), new Team("B", -2_000_000_000, 0), new Team("C", 5, 0)));
        c.sort(SUBTRACT);
        System.out.println("subtraction : " + c);
        c.sort(NO_TIEBREAK);
        System.out.println("Integer.compare: " + c);
        Random r = new Random(3);
        System.out.println("\nWildcard: List<? extends T> into Leaderboard<Team>, bounded min:");
        Leaderboard<Team> lb = new Leaderboard<>(GOOD);
        List<TeamC> sub = List.of(new TeamC("Zebras", 3, 0), new TeamC("Ants", 3, 0), new TeamC("Kites", 7, 1));
        addAll(lb, sub);
        System.out.println(lb.ranking() + "   best by Comparable: " + best(sub));

        System.out.println("\nSorting 500,000 random teams (ms, median of 5 runs):");
        int n = 500000;
        List<Team> base = new ArrayList<>(); List<TeamC> baseC = new ArrayList<>();
        for (int i = 0; i < n; i++) { int p = r.nextInt(30), g = r.nextInt(40) - 20; String nm = "T" + r.nextInt(100000);
            base.add(new Team(nm, p, g)); baseC.add(new TeamC(nm, p, g)); }
        System.out.printf("%-34s %8.1f%n", "one sort, Comparator chain", med(() -> new ArrayList<>(base).sort(GOOD)));
        System.out.printf("%-34s %8.1f%n", "one sort, Comparable.compareTo", med(() -> Collections.sort(new ArrayList<>(baseC))));
        System.out.printf("%-34s %8.1f%n", "three stable sorts (name,gd,pts)", med(() -> {
            List<Team> l = new ArrayList<>(base);
            l.sort(Comparator.comparing((Team t) -> t.name));
            l.sort(Comparator.comparingInt((Team t) -> t.goalDiff).reversed());
            l.sort(Comparator.comparingInt((Team t) -> t.points).reversed()); }));
    }
    static double med(Runnable r) {
        r.run();
        double[] t = new double[5];
        for (int i = 0; i < 5; i++) { long s = System.nanoTime(); r.run(); t[i] = (System.nanoTime() - s) / 1e6; }
        Arrays.sort(t); return t[2];
    }
}
