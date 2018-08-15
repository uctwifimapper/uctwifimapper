import com.google.gson.Gson;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.SwitchPoint;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WifiMapperServer {

    private static final Logger logger = Logger.getLogger("WifiMapperServer");

    public static void main(String [] args){

        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Connection connection = null;

        if(connection == null) {

            try {

                connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tawanda", "postgres", "tawanda");

            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            if (connection != null) {
                System.out.println("You made it, take control your database now!");

                String query = "SELECT * FROM table2";

                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()){
                        System.out.println(resultSet.toString());
                    }

                    resultSet.close();
                    statement.close();
                }catch (Exception e){
                }

            } else {
                System.out.println("Failed to make connection!");
            }
            */
        }

        //start server
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8800), 0);

            HttpContext context = server.createContext("/");
            context.setHandler(WifiMapperServer::rootRequest);

            HttpContext context2 = server.createContext("/apn");
            context2.setHandler(WifiMapperServer::apnRequest);

            server.start();

        }catch(IOException e){

        }
    }

    public static void rootRequest(HttpExchange exchange){
        try{

            String response = "Welcome to WifiMapper.";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream out = exchange.getResponseBody();
            out.write(response.getBytes());
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void apnRequest(HttpExchange exchange) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/wifimapper", "postgres", "tawanda")){

            switch (exchange.getRequestMethod()){

                case "GET":

                    String location = exchange.getRequestURI().getQuery();
                    String[] point = location.split(";", 2);
                    Double lat = Double.parseDouble(point[0]);
                    Double lon = Double.parseDouble(point[1]);

                    String query = "SELECT * FROM access_points";//" WHERE ST_ClosestPoint(" + lat + "," + lon + ")";

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()) {

                        AccessPoint apn = new AccessPoint();
                        apn.setBssid(resultSet.getString("bssid"));
                        apn.setLinkSpeed(resultSet.getInt("link_speed"));
                        apn.setSsid(resultSet.getString("ssid"));

                        String response = new Gson().toJson(apn);

                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream out = exchange.getResponseBody();
                        out.write(response.getBytes());
                        out.close();
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();

                    break;
                case "POST":

                    StringBuilder body = new StringBuilder();
                    try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8.name())) {
                        char[] buffer = new char[256];
                        int read;
                        while ((read = reader.read(buffer)) != -1) {
                            body.append(buffer, 0, read);
                        }
                    }

                    AccessPoint accessPoint = new Gson().fromJson(body.toString(), AccessPoint.class);

                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO access_point VALUES (?,?,?,?)");
                    

                    String response = "OK";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream out = exchange.getResponseBody();
                    out.write(response.getBytes());
                    out.close();

                    break;
                    default: break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
