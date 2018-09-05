import com.google.gson.Gson;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/*
*
* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati
*
* Application launcher class
*
* - Setup and start http server listening on port 8800
*
* Queries received will be routed to the corresponding handler methods
*
* The Api:
* http://localhost:8800/apn to either get data from server or to post data to server
* Any other query will return a default message
*
* */


public class WifiMapperServer {

    public static void main(String [] args){

        try{

            HttpServer server = HttpServer.create(new InetSocketAddress(8800), 0);

            HttpContext context = server.createContext("/");
            context.setHandler(WifiMapperRouter::rootRequest);

            HttpContext context2 = server.createContext("/apn");
            context2.setHandler(WifiMapperRouter::apnRequest);

            HttpContext context3 = server.createContext("/admin");
            context3.setHandler(WifiMapperRouter::adminRequest);

            HttpContext context4 = server.createContext("/admin/map");
            context4.setHandler(WifiMapperRouter::mapRequest);

            HttpContext context5 = server.createContext("/admin/resources");
            context5.setHandler(WifiMapperRouter::resourceRequest);

            server.start();

        }catch(IOException e){

        }
    }
}
