import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** B4.1 migration (file -> database, validated, batched, one transaction, failure report) and B4.2 metadata. */
public class Migrate {
    public static void main(String[] a) throws Exception {
        Path legacy = Files.createTempFile("legacy_passes", ".csv"), db = Files.createTempFile("transport", ".sqlite");
        PassGen.passes(legacy, 20000, 11);
        List<String> lines = new ArrayList<>(Files.readAllLines(legacy));         // plant known faults so the report can be checked
        lines.set(99, "P000100,19230010");                                         // short line
        lines.set(499, "P000500,192300500,Ravi,12,free");                          // non-numeric amount
        lines.set(1999, "");                                                       // empty line
        lines.set(2999, "P000001,192303000,Meena,5,4100.0");                       // duplicate pass ID
        lines.set(7999, "P008000,192308000,Anitha,177,3900.0");                     // route out of range
        lines.set(11999, "P012000,19230,Divya,4,3900.0");                          // bad register number
        Files.write(legacy, lines);

        List<String[]> good = new ArrayList<>(); List<String> report = new ArrayList<>(); Set<String> seen = new HashSet<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(legacy)) {
            lineNo++;
            String why = PassValidator.check(line);
            if (why == null) { String id = line.split(",")[0]; if (!seen.add(id)) why = "duplicate pass ID " + id; }
            if (why == null) good.add(line.split(",", -1)); else report.add(String.format("line %-6d %-40s %s", lineNo, line.isEmpty() ? "(empty)" : line, why));
        }
        System.out.println("read " + lineNo + " lines: " + good.size() + " valid, " + report.size() + " failed validation");
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + db)) {
            try (Statement st = con.createStatement()) { st.execute(Files.readString(Path.of(System.getProperty("schema", "schema_sqlite.sql")))); }
            long t = System.nanoTime();
            int loaded = PassLoader.loadAll(con, good);
            double ms = (System.nanoTime() - t) / 1e6;
            int inDb; try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) { rs.next(); inDb = rs.getInt(1); }
            System.out.printf("loaded %d rows in one transaction (batches of 1000) in %.0f ms; SELECT COUNT(*) = %d  -> %s%n%n", loaded, ms, inDb, inDb == good.size() ? "MATCH" : "MISMATCH");
            System.out.println("FAILURE REPORT (rows NOT migrated - to be fixed by hand):");
            report.forEach(r -> System.out.println("  " + r));
            System.out.println();
            DatabaseMetaData md = con.getMetaData();
            System.out.println("== B4.2 DatabaseMetaData ==");
            System.out.println("database product : " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            System.out.println("driver           : " + md.getDriverName() + " " + md.getDriverVersion());
            System.out.println("JDBC version     : " + md.getJDBCMajorVersion() + "." + md.getJDBCMinorVersion());
            System.out.println("URL              : jdbc:sqlite:<file>");
            System.out.println("supports transactions: " + md.supportsTransactions() + "   batch updates: " + md.supportsBatchUpdates());
            try (ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"})) { while (rs.next()) System.out.println("table            : " + rs.getString("TABLE_NAME")); }
            try (ResultSet rs = md.getColumns(null, null, "bus_pass", "%")) { while (rs.next()) System.out.printf("   column %-9s %s%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME")); }
            try (ResultSet rs = md.getPrimaryKeys(null, null, "bus_pass")) { while (rs.next()) System.out.println("   primary key: " + rs.getString("COLUMN_NAME")); }
        }
        Files.delete(legacy); Files.delete(db);
    }
}
