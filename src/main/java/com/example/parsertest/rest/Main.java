package com.example.parsertest.rest;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.example.parsertest.configuration.ConfigurationLoader;



/**
 * Main application class to configure and start the Grizzly server with Jersey.
 */
public class Main {

    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages(true, "com.example.parsertest.rest");
        // Create and start a new instance of Grizzly HTTP server
        return GrizzlyHttpServerFactory.createHttpServer(ConfigurationLoader.BASE_URI, rc);
    }

    public static void main(String[] args) {

        final HttpServer server = startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
    }
}