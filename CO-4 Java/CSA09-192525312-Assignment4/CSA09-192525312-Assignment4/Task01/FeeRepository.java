/** Data-access contract: the service depends on this interface, never on JDBC directly. */
public interface FeeRepository {
    void save(String regNo, double amount) throws Exception;
}
