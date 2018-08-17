import java.sql.*;

/*
*  Using singleton pattern.
*  Only one instance of connection to database exists
* */
public class Database {

    private static Connection connection;

    //prevent class instantiation
    private Database(){}

    public static Connection getConnection(){

        if(null != connection){
            return connection;
        }

        return getConnection("jdbc:postgresql://127.0.0.1:5432/wifimapper", "postgres", "tawanda");
    }

    private static Connection getConnection(String db, String user, String password) {

        try {

            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(db, user, password);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static void close(){
        if (null != connection) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
