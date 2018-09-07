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


            HttpContext root = server.createContext("/");
            root.setHandler(WifiMapperRouter::mapRequest);

            HttpContext apn = server.createContext("/apn");
            apn.setHandler(WifiMapperRouter::apnRequest);

            HttpContext strength = server.createContext("/strength");
            strength.setHandler(WifiMapperRouter::strengthRequest);

            HttpContext login = server.createContext("/login");
            login.setHandler(WifiMapperRouter::loginRequest);

            HttpContext resource = server.createContext("/resources");
            resource.setHandler(WifiMapperRouter::resourceRequest);

            HttpContext admin = server.createContext("/admin");
            admin.setHandler(WifiMapperRouter::adminRequest);

            HttpContext map = server.createContext("/admin/map");
            map.setHandler(WifiMapperRouter::mapRequest);

            server.start();

        }catch(IOException e){

        }
    }
}
