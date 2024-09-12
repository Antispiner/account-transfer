package org.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.example.config.AppConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8090);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ResourceConfig config = new AppConfig();
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(servlet, "/*");

        server.setHandler(context);

        try {
            server.start();
            server.join();
        } finally {
            server.stop();
        }
    }
}