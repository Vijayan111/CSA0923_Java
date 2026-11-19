import java.time.LocalDateTime;
import java.util.*;

/** Stand-in for MySQL used when no database is configured (tests, demo, off-screen screenshots). */
public class InMemoryRegistrationDao implements RegistrationDao {
    public static final List<String> EVENTS = List.of("Cultural Night", "Hackathon", "Sports Day", "Tech Fest");
    private final List<Registration> rows = new ArrayList<>(); private final Set<String> keys = new HashSet<>(); private int nextId = 1;
    public static InMemoryRegistrationDao seeded(int n) {
        InMemoryRegistrationDao d = new InMemoryRegistrationDao();
        String[] names = {"Arun Kumar", "Divya S", "Karthik R", "Meena V", "Farhan Ali", "Priya N", "Suresh M", "Lakshmi T", "Vikram D", "Anitha R"};
        for (int i = 1; i <= n; i++) {
            String sid = String.valueOf(192525000 + i), nm = names[i % names.length];
            String ev = EVENTS.get(i % EVENTS.size());
            d.rows.add(new Registration(d.nextId++, sid, nm, nm.toLowerCase().replace(' ', '.') + "@simats.edu", ev, LocalDateTime.of(2026, 11, 1 + i % 17, 9 + i % 8, (i * 7) % 60)));
            d.keys.add(sid + "|" + ev);
        }
        return d;
    }
    @Override public synchronized List<String> events() { return EVENTS; }
    @Override public synchronized boolean exists(String s, String e) { return keys.contains(s + "|" + e); }
    @Override public synchronized void insert(String s, String n, String m, String e) throws Exception {
        if (!EVENTS.contains(e)) throw new IllegalArgumentException("Unknown event: " + e);
        if (!keys.add(s + "|" + e)) throw new DuplicateRegistrationException(s, e);
        rows.add(new Registration(nextId++, s, n, m, e, LocalDateTime.now().withSecond(0).withNano(0)));
    }
    private List<Registration> filtered(String ev) { return ev == null ? rows : rows.stream().filter(r -> r.event().equals(ev)).toList(); }
    @Override public synchronized int count(String ev) { return filtered(ev).size(); }
    @Override public synchronized List<Registration> findPage(String ev, int off, int lim) {
        List<Registration> f = filtered(ev); return new ArrayList<>(f.subList(Math.min(off, f.size()), Math.min(off + lim, f.size())));
    }
    @Override public synchronized List<Registration> findAll(String ev) { return new ArrayList<>(filtered(ev)); }
}
