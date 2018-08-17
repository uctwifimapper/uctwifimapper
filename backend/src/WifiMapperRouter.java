import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/*

/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

* - Class responsible for processing the query received by the server.
*
* 1. If the request is a GET, it splits the query to get the column and value pairs.
* 2. If its a POST it checks values
*
*  After processing the request payload, the data is sent to (AccessPointDao)database access object which
*  performs actual query to get/save data from database
*
* - Class responsible for sending response to client.
*
*
* */

public class WifiMapperRouter {

    //For other queries not part of api, return generic response
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
    *   Method processes requests for endpoint /apn
    *   1. Split request object into column:value pair
    *   2. Send this pair to AccessPointDao for further processing
    *   3. Send response from AccessPointDao to server for forwarding to client
    *
    *   GET:  example requests /apn/location?latitude;longitude OR /apn/location?latitude;longitude;radius OR /apn/name?"name" OR /apn/bssid/""
    *   POST: /apn Json payload example {"bssid":"ee:00:8c:b8:b7:01", "ssid":"Eduroam", "location" : {"x":-34.1638945, "y":18.4208423}, "linkSpeed":200}
    * */
    public static void apnRequest(HttpExchange exchange) {

        AccessPointDao accessPointDao;

        switch (exchange.getRequestMethod()) {

            case "GET":

                String [] query = exchange.getRequestURI().getQuery().split("=", 2); //Process request payload

                accessPointDao = new AccessPointDao();

                String jsonResponse = "{\"generic\" : \"error\"}";

                if(2 == query.length) {
                    jsonResponse = new Gson().toJson(accessPointDao.get(query[0], query[1])); //Forward to AccessPointDao for further processing
                }

                sendResponse(exchange, "application/json", 200, jsonResponse); //send response to client

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
                    sendResponse(exchange, "application/json", 200, "{\"generic\":\"success\"}");
                } else {
                    sendResponse(exchange, "application/json", 400, new Gson().toJson("{\"generic\":\"error\"}"));
                }

                break;

            default:
                sendResponse(exchange, "application/json", 400, new Gson().toJson("{\"generic\":\"error\"}"));
                break;
        }
    }

    /*
    * Method responsible for sending response to client
    * 1. Set content type to Json, since response will be in Json format
    * 2. Set responce code 200 for success and 400 for error/fail
    * */
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
