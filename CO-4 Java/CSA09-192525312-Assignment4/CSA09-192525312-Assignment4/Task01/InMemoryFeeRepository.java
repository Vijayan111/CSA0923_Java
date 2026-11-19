import java.util.*;

/** Stand-in repository used for tests and demos; delayMs simulates the bank confirmation wait. */
public class InMemoryFeeRepository implements FeeRepository {
    public final List<String> rows = new ArrayList<>();
    private final int delayMs;
    public InMemoryFeeRepository(int delayMs) { this.delayMs = delayMs; }
    @Override public void save(String regNo, double amount) throws Exception {
        Thread.sleep(delayMs);
        rows.add(regNo + "," + amount);
    }
}
