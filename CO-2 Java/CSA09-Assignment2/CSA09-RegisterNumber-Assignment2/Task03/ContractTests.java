import java.util.*;

public class ContractTests {
    static int equalsCalls;

    // Version 1: the junior developer's class (equals only)
    static class BadStudent {
        final String regNo, name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { equalsCalls++; return o instanceof BadStudent b && regNo.equals(b.regNo); }
    }
    // Version 2: corrected (equals + hashCode on the same field)
    static class GoodStudent {
        final String regNo, name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { equalsCalls++; return o instanceof GoodStudent g && regNo.equals(g.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    // Version 3: mutable key used in hashCode
    static class MutableStudent {
        String regNo; final String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof MutableStudent m && regNo.equals(m.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }

    static void report(String test, int predicted, int actual, String why) {
        System.out.printf("%-34s predicted=%d  actual=%d  %s%n", test, predicted, actual, predicted == actual ? "MATCH" : "DIFFERENT");
        System.out.println("    " + why + "   [equals() calls: " + equalsCalls + "]");
        equalsCalls = 0;
    }

    public static void main(String[] args) {
        // Test 1: each student scanned once (HashSet appears to work)
        Set<BadStudent> t1 = new HashSet<>();
        t1.add(new BadStudent("101", "Arun")); t1.add(new BadStudent("102", "Divya")); t1.add(new BadStudent("103", "Kiran"));
        report("T1 distinct scans, no hashCode", 3, t1.size(), "no duplicates were scanned, so nothing is exposed");

        // Test 2: same students scanned twice (exposes duplicates)
        Set<BadStudent> t2 = new HashSet<>();
        for (int gate = 1; gate <= 2; gate++) { t2.add(new BadStudent("101", "Arun")); t2.add(new BadStudent("102", "Divya")); t2.add(new BadStudent("103", "Kiran")); }
        report("T2 repeat scans, no hashCode", 6, t2.size(), "identity hash codes differ, so equals() is never reached");

        Set<GoodStudent> t2b = new HashSet<>();
        for (int gate = 1; gate <= 2; gate++) { t2b.add(new GoodStudent("101", "Arun")); t2b.add(new GoodStudent("102", "Divya")); t2b.add(new GoodStudent("103", "Kiran")); }
        report("T2 repeat scans, with hashCode", 3, t2b.size(), "same hash -> same bucket -> equals() true -> duplicate rejected");

        // Test 3 (own): field used in hashCode changed after insertion
        Set<MutableStudent> t3 = new HashSet<>();
        MutableStudent m = new MutableStudent("201", "Meena");
        t3.add(m);
        m.regNo = "999";
        System.out.println("T3 after regNo changed: contains(m) = " + t3.contains(m) + "  (predicted false)");
        t3.add(m);
        report("T3 mutate key, add again", 2, t3.size(), "object sits in the old bucket, lookup uses the new hash");

        // Test 4 (own): TreeSet whose compareTo is inconsistent with equals
        Comparator<GoodStudent> byName = Comparator.comparing(s -> s.name);
        TreeSet<GoodStudent> ts = new TreeSet<>(byName);
        ts.add(new GoodStudent("301", "Ravi")); ts.add(new GoodStudent("302", "Ravi"));
        Set<GoodStudent> hs = new HashSet<>(List.of(new GoodStudent("301", "Ravi"), new GoodStudent("302", "Ravi")));
        report("T4 TreeSet by name (2 diff regNo)", 1, ts.size(), "TreeSet treats compare()==0 as duplicate: it says 1 (HashSet says " + hs.size() + ")");
    }
}
