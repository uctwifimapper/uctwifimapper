import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
public class WifiMapperServer {

    public static void main(String [] args){

        try{

            HttpServer server = HttpServer.create(new InetSocketAddress(8800), 0);

            HttpContext context = server.createContext("/");
            context.setHandler(WifiMapperRouter::rootRequest);

            HttpContext context2 = server.createContext("/apn");
            context2.setHandler(WifiMapperRouter::apnRequest);

            server.start();

        }catch(IOException e){

        }
    }
}
