import java.util.List;
import java.util.Optional;

/*
/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

* Interface class for Data Access Object using Data access object pattern
* http://www.oracle.com/technetwork/java/dataaccessobject-138824.html
* https://www.baeldung.com/java-dao-pattern
*
*
* Generic interface for mapping database data to objects.
* This separates the code for performing actual database queries from the business logic.
*
*
* */
public interface Dao<T> {

    List<T> get(String [] query);

    List<T> getAll();

    boolean save(T t);

    void update(T t, String[] params);

    void delete(T t);
}
