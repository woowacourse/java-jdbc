package com.techcourse;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.stream.Stream;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        System.out.println("안녕");
    }

    private static Connector createConnector(final int port) {
        final var connector = new Connector();
        connector.setPort(port);
        connector.setProperty("bindOnInit", "false");
        return connector;
    }

    private static int defaultPortIfNull(String[] args) {
        return Stream.of(args)
                .findFirst()
                .map(Integer::parseInt)
                .orElse(DEFAULT_PORT);
    }
}
