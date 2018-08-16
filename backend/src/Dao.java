import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(long id);

    List<T> get(String column, String value);

    List<T> getAll();

    boolean save(T t);

    void update(T t, String[] params);

    void delete(T t);
}
