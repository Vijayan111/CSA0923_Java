import java.util.List;

/** DATA layer contract. The service layer depends on this interface only. event == null means "all events". */
public interface RegistrationDao {
    List<String> events() throws Exception;
    boolean exists(String studentId, String event) throws Exception;
    void insert(String studentId, String name, String email, String event) throws Exception;   // DuplicateRegistrationException if the pair exists
    int count(String event) throws Exception;
    List<Registration> findPage(String event, int offset, int limit) throws Exception;
    List<Registration> findAll(String event) throws Exception;
}
