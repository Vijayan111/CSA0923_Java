import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class DamagedFileDemo {
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("damaged", ".csv");
        Files.write(f, List.of(
            "P2001,192311001,Arun,12,4500.0",
            "P2002,192311002,Divya,7",                  // wrong number of fields
            "P2003,192311003,Karthik,9,abc",            // non-numeric amount (5 fields!)
            "",                                         // empty line
            "P2005,192311005,Meena,3,3900.0"));
        System.out.println("-- 1. original PassStore.find() (checks only the field count) --");
        PassStore s = new PassStore(f);
        for (String id : new String[]{"P2001", "P2002", "P2003", "P2005"}) System.out.printf("find(%s) -> %s%n", id, Arrays.toString(s.find(id)));
        System.out.println("   >> P2003 is returned with amount 'abc': the field-count check is NOT enough");
        System.out.println();
        System.out.println("-- 2. validating reader (PassValidator) --");
        List<String> lines = Files.readAllLines(f); int ok = 0;
        for (int i = 0; i < lines.size(); i++) {
            String why = PassValidator.check(lines.get(i));
            System.out.printf("line %d  %-34s %s%n", i + 1, lines.get(i).isEmpty() ? "(empty)" : lines.get(i), why == null ? "OK" : "REJECTED: " + why);
            if (why == null) ok++;
        }
        System.out.println(ok + " valid, " + (lines.size() - ok) + " rejected, no exception thrown, search continues");
        Files.delete(f);
    }
}
