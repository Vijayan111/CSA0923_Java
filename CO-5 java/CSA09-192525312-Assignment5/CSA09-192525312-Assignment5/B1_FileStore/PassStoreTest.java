import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class PassStoreTest {
    static int pass = 0, total = 0;
    static void check(String name, String expected, String actual) {
        total++; boolean ok = expected.equals(actual); if (ok) pass++;
        System.out.printf("%-30s %-40s %s%n", name, actual, ok ? "PASS" : "FAIL (expected " + expected + ")");
    }
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore s = new PassStore(f);
        s.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        s.add("P1003", "192311003", "Divya", 7, 3800);
        s.add("P1004", "192311004", "Karthik", 12, 4100);
        System.out.println("File content:");
        Files.readAllLines(f).forEach(l -> System.out.println("   " + l));
        System.out.println();
        check("T1 add + find (normal)", "[P1003, 192311003, Divya, 7, 3800.0]", Arrays.toString(s.find("P1003")));
        check("T2 find (ID not present)", "null", Arrays.toString(s.find("P9999")));
        check("T3 find damaged line's ID", "null", Arrays.toString(s.find("P1002")));
        check("T4 find AFTER damaged line", "[P1004, 192311004, Karthik, 12, 4100.0]", Arrays.toString(s.find("P1004")));
        List<String[]> r12 = s.listByRoute(12);
        check("T5 list route 12 (2 passes)", "[P1001, P1004]", r12.stream().map(x -> x[0]).toList().toString());
        check("T6 list route 99 (none)", "[]", s.listByRoute(99).toString());
        System.out.println("\n" + pass + " of " + total + " tests passed");
        Files.delete(f);
    }
}
