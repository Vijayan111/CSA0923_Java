import java.nio.file.*;

/** Console test of the service layer (no window, no MySQL): normal, invalid and duplicate registrations, paging, CSV export. */
public class ServiceTest {
    static int pass = 0, total = 0;
    static void check(String name, Object expected, Object actual) {
        total++; boolean ok = String.valueOf(expected).equals(String.valueOf(actual)); if (ok) pass++;
        System.out.printf("%-4s  %-34s -> %s%n", ok ? "PASS" : "FAIL", name, actual);
    }
    public static void main(String[] a) throws Exception {
        InMemoryRegistrationDao dao = InMemoryRegistrationDao.seeded(46);
        RegistrationService s = new RegistrationService(dao);
        check("rows seeded", 46, dao.count(null));
        check("normal registration", "Registered Vijayan P for Hackathon.", s.register("192525312", "Vijayan P", "vijayan@simats.edu", "Hackathon").message());
        check("rows after register", 47, dao.count(null));
        check("duplicate same event", "Student 192525312 is already registered for Hackathon.", s.register("192525312", "Vijayan P", "vijayan@simats.edu", "Hackathon").message());
        check("rows unchanged by duplicate", 47, dao.count(null));
        check("same student, other event OK", true, s.register("192525312", "Vijayan P", "vijayan@simats.edu", "Tech Fest").ok());
        check("bad student id", "Student ID must be exactly 9 digits.", s.register("19252", "Vijayan P", "v@simats.edu", "Hackathon").message());
        check("bad name", "Name must be 2-60 letters (spaces and dots allowed).", s.register("192525313", "V1", "v@simats.edu", "Hackathon").message());
        check("bad e-mail", "Enter a valid e-mail address, e.g. name@simats.edu.", s.register("192525313", "Vijay", "vijay.simats.edu", "Hackathon").message());
        check("no event selected", "Select an event.", s.register("192525313", "Vijay", "v@simats.edu", null).message());
        check("SQL text rejected by validation", "Student ID must be exactly 9 digits.", s.register("1' OR '1'='1", "Vijay", "v@simats.edu", "Hackathon").message());
        RegistrationService.PageData p = s.page(null, 4, 10);
        check("last page row count (48 rows)", 8, p.rows().size());
        check("filter Hackathon total", dao.findAll("Hackathon").size(), s.page("Hackathon", 0, 10).total());
        Path f = Files.createTempFile("registrations", ".csv");
        int n = s.exportCsv("Hackathon", f);
        check("CSV rows written", dao.findAll("Hackathon").size(), n);
        System.out.println("      first CSV lines:"); Files.readAllLines(f).stream().limit(3).forEach(l -> System.out.println("        " + l));
        System.out.println("\n" + pass + " / " + total + " tests passed");
    }
}
