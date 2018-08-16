import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WifiMapperRouter {

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

    /*
    *   GET:  /apn/location?latitude;longitude OR /apn/location?latitude;longitude;radius OR /apn/name?"name" OR /apn/bssid/""
    *   POST: /apn Json payload example {"bssid":"ee:00:8c:b8:b7:01", "ssid":"Eduroam", "location" : {"x":-34.1638945, "y":18.4208423}, "linkSpeed":200}
    * */
    public static void apnRequest(HttpExchange exchange) {

        String response;
        int responseCode;
        OutputStream responseBody;

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/wifimapper", "postgres", "tawanda")){

            switch (exchange.getRequestMethod()){

                case "GET":

                    String payload = exchange.getRequestURI().getQuery();
                    String[] point = payload.split(";", 3);
                    Double lat = Double.parseDouble(point[0]);
                    Double lon = Double.parseDouble(point[1]);
                    int radius;
                    if(point.length == 3 && null != point[3]){
                        radius = Integer.parseInt(point[3]);
                    }

                    String query = "SELECT * FROM access_point"; //WHERE ST_ClosestPoint("+new PGpoint(lat, lon)+")";
                    List<AccessPoint> apnList = new ArrayList<>();

                    try(Statement statement = connection.createStatement()) {

                        try(ResultSet resultSet = statement.executeQuery(query)) {

                            while (resultSet.next()) {

                                AccessPoint apn = new AccessPoint();

                                apn.setBssid(resultSet.getString("bssid"));
                                apn.setLinkSpeed(resultSet.getInt("link_speed"));
                                apn.setSsid(resultSet.getString("ssid"));

                                if (resultSet.getString("location") != null) {
                                    apn.setLocation(new PGpoint(resultSet.getString("location")));
                                }

                                apnList.add(apn);
                            }
                        }
                    }

                    sendResponse(exchange, "application/json", 200, new Gson().toJson(apnList));

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

                    sendResponse(exchange, "application/json", 200, new Gson().toJson("OK"));

                    break;

                default: break;
            }
        }catch (Exception e){
            e.printStackTrace();
            sendResponse(exchange, "application/json", 400, new Gson().toJson("error"));
        }
    }

    /*
    *   GET: /bssid?"ee:00:8c:b8:b7:01" OR /
    * */
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

                    sendResponse(exchange, "application/json", 200, new Gson().toJson(apn));

                    break;

                default: break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "application/json", 400, new Gson().toJson("error"));
        }
    }

    /*
    * *   GET: /apn?name="name" OR /apn?bssid="bssid" OR location="lat;lon;radius" OR spped="linkSpeed"
    *    POST: /apn Json payload
    * */
    public static void genericRequest(HttpExchange exchange){

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/wifimapper", "postgres", "tawanda")) {

            switch (exchange.getRequestMethod()) {

                case "GET":

                    String payload = exchange.getRequestURI().getQuery();
                    String[] point = payload.split("=", 3);

                    if(null != point){

                        
                    }


                    break;
                case "POST":
                    break;
                    default: break;

            }
        }catch (Exception e){

        }

    }

    private static void sendResponse(HttpExchange exchange, String contentType, int responseCode, String responseBody){

        try {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(responseCode, responseBody.getBytes().length);
            OutputStream response = exchange.getResponseBody();
            response.write(responseBody.getBytes());
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
