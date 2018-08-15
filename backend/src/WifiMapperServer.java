import com.google.gson.Gson;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class WifiMapperServer {

    public static void main(String [] args){

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8800), 0);

            HttpContext context = server.createContext("/");
            context.setHandler(WifiMapperServer::rootRequest);

            HttpContext context2 = server.createContext("/apn");
            context2.setHandler(WifiMapperServer::apnRequest);

            HttpContext context3 = server.createContext("/bssid");
            context3.setHandler(WifiMapperServer::bssidRequest);

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

            String response;
            OutputStream out;

            switch (exchange.getRequestMethod()){

                case "GET":

                    String location = exchange.getRequestURI().getQuery();
                    String[] point = location.split(";", 2);
                    Double lat = Double.parseDouble(point[0]);
                    Double lon = Double.parseDouble(point[1]);

                    String query = "SELECT * FROM access_point";//WHERE ST_ClosestPoint("+new PGpoint(lat, lon)+")";
                    //ST_DWithin(coords, ST_GeomFromText('POINT(-12.5842 24.4944)',4326), 1)


                    //String query = "SELECT * FROM access_point WHERE ST_DWithin(location,'POINT("+lat+" "+lon+")' ,4326), 20)";

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    AccessPoint apn = new AccessPoint();

                    while (resultSet.next()) {

                        apn.setBssid( resultSet.getString("bssid"));
                        apn.setLinkSpeed(resultSet.getInt("link_speed"));
                        apn.setSsid(resultSet.getString("ssid"));

                        if(resultSet.getString("location") != null){
                            PGpoint pGpoint = new PGpoint();
                            apn.setLocation(pGpoint);
                        }
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();

                    response = new Gson().toJson(apn);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    out = exchange.getResponseBody();
                    out.write(response.getBytes());
                    out.close();

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

                    PGobject pGobject = new PGobject();
                    pGobject.setValue(accessPoint.getBssid());
                    pGobject.setType("macaddr");

                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO access_point VALUES (?,?,?,?)");
                    preparedStatement.setObject(1, pGobject);
                    preparedStatement.setString(2, accessPoint.getSsid());
                    preparedStatement.setObject(3, accessPoint.getLocation());
                    preparedStatement.setInt(4, accessPoint.getLinkSpeed());

                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                    response = "OK";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    out = exchange.getResponseBody();
                    out.write(response.getBytes());
                    out.close();

                    break;

                    default: break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bssidRequest(HttpExchange exchange) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/wifimapper", "postgres", "tawanda")){

            switch (exchange.getRequestMethod()){

                case "GET":

                    String bssid = exchange.getRequestURI().getQuery();
                    PGobject pGobject = new PGobject();
                    pGobject.setValue(bssid);
                    pGobject.setType("macaddr");

                    String query = "SELECT * FROM access_point WHERE bssid='"+bssid+"'";

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    AccessPoint apn = new AccessPoint();

                    while (resultSet.next()) {

                        apn.setBssid( resultSet.getString("bssid"));
                        apn.setLinkSpeed(resultSet.getInt("link_speed"));
                        apn.setSsid(resultSet.getString("ssid"));

                        if(resultSet.getString("location") != null){
                            PGpoint pGpoint = new PGpoint();
                            apn.setLocation(pGpoint);
                        }
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();

                    String response = new Gson().toJson(apn);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
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
