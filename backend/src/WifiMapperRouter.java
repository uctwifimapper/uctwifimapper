import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

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

        AccessPointDao accessPointDao;

        switch (exchange.getRequestMethod()) {

            case "GET":

                //TODO: Create method to handle payload request
                String payload = exchange.getRequestURI().getQuery();
                String[] point = payload.split(";", 3);
                Double lat = Double.parseDouble(point[0]);
                Double lon = Double.parseDouble(point[1]);
                int radius;
                if (point.length == 3 && null != point[3]) {
                    radius = Integer.parseInt(point[3]);
                }

                accessPointDao = new AccessPointDao();

                sendResponse(exchange, "application/json", 200, new Gson().toJson(accessPointDao.get("", payload)));

                break;

            case "POST":

                StringBuilder body = new StringBuilder();
                InputStreamReader reader;

                try {
                    reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8.name());
                    char[] buffer = new char[256];
                    int read;
                    try {
                        while ((read = reader.read(buffer)) != -1) {
                            body.append(buffer, 0, read);
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                accessPointDao = new AccessPointDao();

                if (accessPointDao.save(new Gson().fromJson(body.toString(), AccessPoint.class))) {
                    sendResponse(exchange, "application/json", 200, new Gson().toJson("OK"));
                } else {
                    sendResponse(exchange, "application/json", 400, new Gson().toJson("error"));

                }

                break;

            default:
                break;
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
