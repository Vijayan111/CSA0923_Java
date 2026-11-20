import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {
    void insert(T t) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    int update(T t) throws SQLException;
    int delete(K id) throws SQLException;
}
