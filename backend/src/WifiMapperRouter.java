import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static void rootRequest(HttpExchange exchange) {
        log(exchange, "Request");
        try {
            String response = "Welcome to WifiMapper.";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream out = exchange.getResponseBody();
            out.write(response.getBytes());
            out.close();
        } catch (IOException e) {
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

        log(exchange, "Request");

        AccessPointDao accessPointDao;

        switch (exchange.getRequestMethod()) {

            case "GET":

                String[] query = exchange.getRequestURI().getQuery().split("=", 2); //Process request payload

                accessPointDao = new AccessPointDao();

                String jsonResponse = "{\"generic\" : \"error\"}";

                if (2 == query.length) {

                    jsonResponse = new Gson().toJson(accessPointDao.get(query)); //Forward to AccessPointDao for further processing
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
                sendResponse(exchange, "application/json", 404, new Gson().toJson("{\"generic\":\"error\"}"));
                break;
        }
    }

    public static void strengthRequest(HttpExchange exchange) {

        log(exchange, "Request");

        SignalStrengthDao signalStrengthDao;

        switch (exchange.getRequestMethod()) {

            case "GET":
                String[] query = exchange.getRequestURI().getQuery().split("=", 0); //Process request payload
                signalStrengthDao = new SignalStrengthDao();
                String jsonResponse = "{\"generic\" : \"error\"}";
                jsonResponse = new Gson().toJson(signalStrengthDao.get(query));
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

                signalStrengthDao = new SignalStrengthDao();

                if (signalStrengthDao.save(new Gson().fromJson(body.toString(), SignalStrength.class))) {
                    sendResponse(exchange, "application/json", 200, "{\"generic\":\"success\"}");
                } else {
                    sendResponse(exchange, "application/json", 400, new Gson().toJson("{\"generic\":\"error\"}"));
                }
                break;
            default:
                break;

        }
    }

    /* Authenticate user and start session if correctly logged in */
    public static void loginRequest(HttpExchange exchange) {

        log(exchange, "Request");

    }

    /*
     * Admin Queries
     *
     * if request is for index.html
     *   load html file
     *   build response object
     *   send response to client
     *
     * */
    public static void adminRequest(HttpExchange exchange) {

        log(exchange, "Request");

        switch (exchange.getRequestMethod()) {

            case "GET":

                String address = "http://" + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort();// + exchange.getRequestURI();

                String jquery = address + "/resources/js/jquery-3.3.1.min.js";
                String bootstrapJs = address + "/resources/js/bootstrap.min.js";
                String wifiMapperJs = address + "/resources/js/wifimapper.js";
                String wifimapperCss = address + "/resources/css/wifimapper.css";
                String bootstrapCss = address + "/resources/css/bootstrap.min.css";
                String map = address + "/map";
                String login = address + "/login";

                SignalStrengthDao signalStrengthDao = new SignalStrengthDao();

                HashMap<String, Object> scopes = new HashMap<>();
                scopes.put("jquery", jquery);
                scopes.put("bootstrapJs", bootstrapJs);
                scopes.put("wifimapperCss", wifimapperCss);
                scopes.put("bootstrapCss", bootstrapCss);
                scopes.put("wifiMapperJs", wifiMapperJs);
                scopes.put("apnCount", signalStrengthDao.count());

                StringWriter writer = new StringWriter();
                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile("admin/index.html");
                mustache.execute(writer, scopes);
                writer.flush();
                String response = writer.toString();
                sendResponse(exchange, "text/html", 200, response);
                break;
            case "POST":
                sendResponse(exchange, "text/html", 404, "");
                break;
            default:
                sendResponse(exchange, "text/html", 404, "");
                break;
        }
    }

    public static void mapRequest(HttpExchange exchange) {

        log(exchange, "Request");

        switch (exchange.getRequestMethod()) {
            case "GET":

                String address = "http://" + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort(); //+ exchange.getRequestURI();

                String jquery = address + "/resources/js/jquery-3.3.1.min.js";
                String bootstrapJs = address + "/resources/js/bootstrap.min.js";
                String mapJs = address + "/resources/js/map.js";
                String wifimapperCss = address + "/resources/css/wifimapper.css";
                String bootstrapCss = address + "/resources/css/bootstrap.min.css";
                String map = address + "/map";
                String login = address + "/login";

                HashMap<String, Object> scopes = new HashMap<>();
                scopes.put("jquery", jquery);
                scopes.put("bootstrapJs", bootstrapJs);
                scopes.put("wifimapperCss", wifimapperCss);
                scopes.put("bootstrapCss", bootstrapCss);
                scopes.put("mapJs", mapJs);

                StringWriter writer = new StringWriter();
                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile("admin/map.html");
                mustache.execute(writer, scopes);
                writer.flush();
                String response = writer.toString();
                sendResponse(exchange, "text/html", 200, response);
                break;
            case "POST":
            default:
                sendResponse(exchange, "text/html", 404, "");
                break;
        }
    }

    /* Method returns data used on the dashboard, data used  google charts

    example of request: /admin/graphs?source=apn
    example of request: /admin/graphs?source=strength
    example of request: /admin/graphs?timestamp=X
    example of request: /admin/graphs?timestamp=X

    * Format of Json data returned:
}

    * */
    public static void graphRequest(HttpExchange exchange){

        log(exchange, "Request");

        String[] query = exchange.getRequestURI().getQuery().split("=");
        SignalStrengthDao signalStrengthDao;

        switch (exchange.getRequestMethod()) {
            case "GET":
                switch (query[0]){
                    case "apncount":
                        signalStrengthDao = new SignalStrengthDao();
                        Map<String, Integer> response = new HashMap<>();
                        response.put("count",signalStrengthDao.count());
                        sendResponse(exchange, "application/json", 200, new Gson().toJson(response));
                        break;
                    case "avgstrength":
                        signalStrengthDao = new SignalStrengthDao();
                        System.out.println("HEEEE");
                        sendResponse(exchange, "application/json", 200, new Gson().toJson(signalStrengthDao.getAvgSgtrenth()));
                        break;
                    case "monthly":
                        break;
                    case "linkspeed":
                        break;
                    case "location":
                        break;

                }
                break;
            case "POST":
                break;
            default:
                break;
        }

    }

    public static void resourceRequest(HttpExchange exchange) {

        log(exchange, "Request");

        switch (exchange.getRequestMethod()) {
            case "GET":

                String line = "";
                String response = "";
                String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString(); //..wifimapper/backend

                String address = exchange.getRequestURI().toString();

                try {
                    File resourceFile = new File(rootFolder + "/src/admin/" + exchange.getRequestURI());

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile)));

                    while ((line = bufferedReader.readLine()) != null) {
                        response += line;
                    }
                    bufferedReader.close();

                    if (exchange.getRequestURI().toString().contains("css")) {
                        sendResponse(exchange, "text/css", 200, response);
                    } else {
                        sendResponse(exchange, "application/javascript", 200, response);
                    }

                } catch (IOException e) {
                    sendResponse(exchange, "text/html", 400, "");
                    e.printStackTrace();
                }
                break;
            case "POST":
            default:
                sendResponse(exchange, "text/html", 404, "");
                break;
        }
    }

    /*
     * Method responsible for sending response to client
     * 1. Set content type to Json, since response will be in Json format
     * 2. Set responce code 200 for success and 400 for error/fail
     * */
    private static void sendResponse(HttpExchange exchange, String contentType, int responseCode, String responseBody) {

        log(exchange, ""+responseCode);

        try {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(responseCode, responseBody.getBytes().length);
            OutputStream response = exchange.getResponseBody();
            response.write(responseBody.getBytes());
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(HttpExchange exchange, String customMessage){
        String time = (DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).format(LocalDateTime.now());
        System.out.printf("%-4s %-3s %-3s %s %s", time, customMessage, exchange.getRequestMethod(), exchange.getRequestURI().toString(), "\n");
    }
}
