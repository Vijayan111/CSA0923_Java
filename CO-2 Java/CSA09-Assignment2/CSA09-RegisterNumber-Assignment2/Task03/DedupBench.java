import java.lang.management.ManagementFactory;
import java.util.*;

public class DedupBench {
    static final com.sun.management.ThreadMXBean TM = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static long sink;

    public static void main(String[] args) {
        System.out.println("De-duplication: n unique students, 2n scans (each student scanned twice)");
        System.out.printf("%-26s %10s %14s %12s %8s%n", "structure", "n", "ns per scan", "alloc (MB)", "size");
        for (int pass = 0; pass < 2; pass++)
            for (int n : new int[]{10000, 100000, 1000000}) {
                Student[] scans = new Student[2 * n];
                for (int i = 0; i < n; i++) { scans[i] = new Student(String.valueOf(190000000 + i), "S" + i); scans[n + i] = new Student(String.valueOf(190000000 + i), "S" + i); }
                List<Student> shuffled = new ArrayList<>(Arrays.asList(scans));
                Collections.shuffle(shuffled, new Random(7));
                if (pass == 1) {
                    run("HashSet<Student>", n, shuffled, new HashSet<>(), null, null);
                    run("TreeSet + Comparator", n, shuffled, new TreeSet<>(Comparator.comparing(Student::regNo)), null, null);
                    run("HashMap<String,Student>", n, shuffled, null, new HashMap<>(), null);
                } else run("warm", n, shuffled, new HashSet<>(), null, null);
            }
    }

    static void run(String name, int n, List<Student> scans, Set<Student> set, Map<String, Student> map, Object unused) {
        System.gc();
        long a0 = TM.getThreadAllocatedBytes(Thread.currentThread().getId());
        long t0 = System.nanoTime();
        if (set != null) for (Student s : scans) set.add(s);
        else for (Student s : scans) map.putIfAbsent(s.regNo(), s);
        long t = System.nanoTime() - t0;
        long a = TM.getThreadAllocatedBytes(Thread.currentThread().getId()) - a0;
        int size = set != null ? set.size() : map.size();
        if (!name.equals("warm")) System.out.printf("%-26s %10d %14.1f %12.1f %8d%n", name, n, t / (double) scans.size(), a / 1048576.0, size);
    }

    record Student(String regNo, String name) {}      // record: equals + hashCode generated from both fields
}
