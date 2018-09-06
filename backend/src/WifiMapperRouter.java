import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
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

        System.out.println("Response sent: "+exchange.getRequestURI().toString() + " @ "+ LocalDate.now());

        AccessPointDao accessPointDao;

        switch (exchange.getRequestMethod()) {

            case "GET":

                String [] query = exchange.getRequestURI().getQuery().split("=", 2); //Process request payload

                accessPointDao = new AccessPointDao();

                String jsonResponse = "{\"generic\" : \"error\"}";

                if(2 == query.length) {

                    Map<String, List> map = new HashMap<>();
                    map.put("data",accessPointDao.get(query));

                    System.out.println("Map: "+new Gson().toJson(map));

                    jsonResponse = new Gson().toJson(map); //Forward to AccessPointDao for further processing
                }

                System.out.println(jsonResponse);
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

    public static void strengthRequest(HttpExchange exchange) {

        SignalStrengthDao signalStrengthDao;

        switch (exchange.getRequestMethod()) {

            case "GET":
                String [] query = exchange.getRequestURI().getQuery().split("=", 0); //Process request payload

                signalStrengthDao = new SignalStrengthDao();

                String jsonResponse = "{\"generic\" : \"error\"}";
                
                jsonResponse = new Gson().toJson(signalStrengthDao.get(query));

                System.out.println(jsonResponse);
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

                System.out.println("String Save: "+ body);

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

    /*
    * Admin Queries
    *
    * if request is for index.html
    *   load html file
    *   build response object
    *   send response to client
    *
    * */
    public static void adminRequest(HttpExchange exchange){

        System.out.println("Response sent: "+exchange.getRequestURI().toString() + " @ "+ LocalDate.now());

        String line = "";
        String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString(); //..wifimapper/backend

        //String jquery = exchange.getRequestURI().toString();//+"admin/js/jquery-3.3.1.min.js";
        String address =  "http://"+exchange.getLocalAddress().getHostName() +":"+ exchange.getLocalAddress().getPort()+exchange.getRequestURI();
        String jquery = address+"/js/jquery-3.3.1.min.js";
        String dataTableJs = "//cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js";//address+"/js/jquery.dataTables.min.js";
        String dataTableCss = "//cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css";//address+"/js/jquery.dataTables.min.css";
        String wifiMapperJs = address+"/js/wifimapper.js";
        String headerCss = address+"/js/header-login-signup.css";
        String demoCss = address+"/js/demo.css";
        String map=address+"/map";

        String head =
                "<!DOCTYPE html>"+
                        "<head><meta charset=\"UTF-8\">"+
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"+
                        "<title>WifiMapper</title>"+
                        "<script src=\""+jquery+"\"></script>"+
                        "<script src=\""+dataTableJs+"\"></script> "+
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\""+demoCss+"\">"+
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\""+headerCss+"\">"+
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\""+dataTableCss+"\">"+
                        "<link href='http://fonts.googleapis.com/css?family=Cookie' rel='stylesheet' type='text/css'>"+
                        "</head> ";

        String header = "<header class=\"header-login-signup\">\n" +
                "\n" +
                "\t<div class=\"header-limiter\">\n" +
                "\n" +
                "\t\t<h1><a href=\"#\">UCT<span>Wifi Mapper</span></a></h1>\n" +
                "\n" +
                "\t\t<nav>\n" +
                "\t\t\t<a href=\"#\" class=\"selected\">Home</a>\n" +
                "\t\t\t<a href=\""+map+"\">Map</a>\n" +
                "\t\t</nav>\n" +
                "\n" +
                "\t\t<ul>\n" +
                "\t\t\t<li><a href=\"#\">Login</a></li>\n" +
                "\t\t\t<li><a href=\"#\">Sign up</a></li>\n" +
                "\t\t</ul>\n" +
                "\n" +
                "\t</div>\n" +
                "\n" +
                "</header>";
        String body = "<body>"+header+"<div><table id=\"my-final-table\">\n" +
                "  <thead>\n" +
                "    <th>Name</th>\n" +
                "    <th>Bssid</th>\n" +
                "    <th>ssid</th>\n" +
                "    <th>Latitude</th>\n" +
                "    <th>Longitude</th>\n" +
                "    <th>Speed</th>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "  </tbody>\n" +
                "</table>\n</div></body>";
        String footer =
                "<script src=\""+wifiMapperJs+"\"></script>"+
                "</html>";

        String response = head+body+footer;
        sendResponse(exchange, "text/html", 200, response);
    }

    public static void mapRequest(HttpExchange exchange){

        String line = "";
        String response = "";
        String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString(); //..wifimapper/backend

        String address =  exchange.getRequestURI().toString(); //"http://"+exchange.getLocalAddress().getHostName() +":"+ exchange.getLocalAddress().getPort()+exchange.getRequestURI();
        System.out.println("Received "+address+" @ "+LocalDate.now());

        try {
            File resourceFile = new File(rootFolder+"/src/admin/map.html");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile)));

            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
            bufferedReader.close();

            sendResponse(exchange, "", 200, response);

        } catch (IOException e) {
            sendResponse(exchange, "text/html", 400, "");
            e.printStackTrace();
        }
    }

    public static void resourceRequest(HttpExchange exchange){

        String line = "";
        String response = "";
        String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString(); //..wifimapper/backend

        String address =  exchange.getRequestURI().toString(); //"http://"+exchange.getLocalAddress().getHostName() +":"+ exchange.getLocalAddress().getPort()+exchange.getRequestURI();
        System.out.println("Received "+address+" @ "+LocalDate.now());

        try {
            File resourceFile = new File(rootFolder+"/src"+exchange.getRequestURI());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile)));

            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
            bufferedReader.close();

            if(exchange.getRequestURI().toString().contains("css")) {
                sendResponse(exchange, "text/css", 200, response);
            }else{
                sendResponse(exchange, "application/javascript", 200, response);
            }

        } catch (IOException e) {
            sendResponse(exchange, "text/html", 400, "");
            e.printStackTrace();
        }
    }

    /*
    * Method responsible for sending response to client
    * 1. Set content type to Json, since response will be in Json format
    * 2. Set responce code 200 for success and 400 for error/fail
    * */
    private static void sendResponse(HttpExchange exchange, String contentType, int responseCode, String responseBody){

        try {
            System.out.println("Response sent: "+exchange.getRequestURI().toString() + " @ "+ LocalDate.now());
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
