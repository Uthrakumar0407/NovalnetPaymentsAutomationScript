package com.nn.utilities;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuthCallbackServer {

    public static void main(String[] args) throws Exception {
        int port = 8081; // Port to listen on
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Context for handling the OAuth callback
        server.createContext("/oauth-callback", new OAuthCallbackHandler());

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        GmailEmailRetriever.getEmailWithSubject("Invoice");

        System.out.println("Local server listening on port " + port);
    }

    static class OAuthCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "OAuth callback received. You can close this window.";

            // Send a response back to the client's browser
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            // Handle the OAuth callback logic here.
            // You should extract the authorization code from the request and proceed with token exchange.
            // After handling the callback, you can respond with a success message or HTML page.
        }
    }
}
