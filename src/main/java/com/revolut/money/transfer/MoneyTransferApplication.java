package com.revolut.money.transfer;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.net.URI;

/**
 * The Main class of the Money Transfer RESTful application provides the ability to transfer money form one
 * Bank Account to another.
 * It will start the web server on localhost:8080
 */
public class MoneyTransferApplication {
    public static final String BASE_URI = "http://localhost:8080/transfer-service/";

    public static void main(String[] args) throws IOException {

        final HttpServer server = startServer();

        System.out.println(String.format(
                "Money Transfer Application started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...",
                BASE_URI));
        System.in.read();
        server.shutdownNow();
    }

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.revolut.money.transfer");
        rc.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
}
