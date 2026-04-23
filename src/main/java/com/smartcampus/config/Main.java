package com.smartcampus.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Smart Campus API.
 * Starts an embedded Grizzly HTTP server exposing the JAX-RS application.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig().packages("com.smartcampus");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) {
        com.smartcampus.storage.InMemoryStore.seedData();
        final HttpServer server = startServer();
        LOGGER.log(Level.INFO, "==============================================");
        LOGGER.log(Level.INFO, "Smart Campus API started at: {0}api/v1", BASE_URI);
        LOGGER.log(Level.INFO, "Press ENTER to stop the server...");
        LOGGER.log(Level.INFO, "==============================================");

        try {
            System.in.read();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error waiting for input", e);
        }
        server.shutdownNow();
    }
}
