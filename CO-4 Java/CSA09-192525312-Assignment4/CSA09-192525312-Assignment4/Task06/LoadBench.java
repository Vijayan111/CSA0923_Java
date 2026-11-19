import javax.swing.table.DefaultTableModel;
import java.lang.management.ManagementFactory;
import java.util.*;

/** Load all 100,000 registrations into the JTable model at once vs one page of 50 rows. Rows are materialised as a JDBC ResultSet would (new Strings). */
public class LoadBench {
    static long alloc() { return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().getId()); }
    static long used() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }
    static final String[] COLS = {"ID", "Student ID", "Name", "E-mail", "Event", "Registered at"};
    static DefaultTableModel fill(List<Registration> rows) {
        DefaultTableModel m = new DefaultTableModel(COLS, 0);
        for (Registration r : rows) m.addRow(new Object[]{r.id(), new String(r.studentId()), new String(r.name()), new String(r.email()), new String(r.event()), r.registeredAt().toString()});
        return m;
    }
    public static void main(String[] a) throws Exception {
        InMemoryRegistrationDao dao = InMemoryRegistrationDao.seeded(100_000);
        System.out.println("100,000 registrations - work done on the Event Dispatch Thread to show them");
        System.out.println("approach                      | rows shown | time (ms)  | allocated (MB) | retained by table (MB)  [page rows: est. = rows x per-row cost of the full load]");
        for (int i = 0; i < 3; i++) fill(dao.findAll(null));                               // warm-up
        long u0 = used(); long b0 = alloc(); long t0 = System.nanoTime();
        DefaultTableModel all = fill(dao.findAll(null));
        double tAll = (System.nanoTime() - t0) / 1e6; double bAll = (alloc() - b0) / 1048576.0; double rAll = (used() - u0) / 1048576.0;
        System.out.printf("%-29s | %10d | %10.1f | %14.1f | %.1f%n", "load everything", all.getRowCount(), tAll, bAll, rAll);
        all = null;
        for (int off : new int[]{0, 50_000, 99_950}) {
            for (int i = 0; i < 2000; i++) fill(dao.findPage(null, off, 50));
            double best = 1e18; long bb = 0;
            for (int rep = 0; rep < 5; rep++) { long b = alloc(); long t = System.nanoTime(); for (int i = 0; i < 200; i++) fill(dao.findPage(null, off, 50)); best = Math.min(best, (System.nanoTime() - t) / 1e6 / 200); bb = (alloc() - b) / 200; }
            DefaultTableModel pg = fill(dao.findPage(null, off, 50));
            System.out.printf("%-29s | %10d | %10.3f | %14.3f | %.3f%n", "one page, OFFSET " + off, pg.getRowCount(), best, bb / 1048576.0, 50 * (rAll / 100_000));
        }
    }
}
