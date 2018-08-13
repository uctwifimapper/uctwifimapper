import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class WifiMapperServer {

    public static void main(String [] args){

        //db
        Connection connection = null;

        if(connection == null) {

            try {

                Class.forName("org.postgresql.Driver");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }

            try {

                connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tawanda", "postgres", "tawanda");

            } catch (Exception e) {

            }

            if (connection != null) {
                System.out.println("You made it, take control your database now!");

                String query = "SELECT * FROM table2";

                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()){
                        System.out.println(resultSet.getInt(0)+" : "+resultSet.getString(1));
                    }
                }catch (Exception e){

                }

            } else {
                System.out.println("Failed to make connection!");
            }
        }

        //start server
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8800), 0);
            HttpContext context = server.createContext("/");
            context.setHandler(WifiMapperServer::handleRequest);
            server.start();
        }catch(IOException e){

        }

    }

    public static void handleRequest(HttpExchange exchange){
        try{
            System.out.println(exchange.toString());
            String response = "Hello World";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream out = exchange.getResponseBody();
            out.write(response.getBytes());
            out.close();
        }catch(IOException e){

        }
    }

}
