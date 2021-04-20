package com.test.instrument;

import io.prometheus.client.exporter.HTTPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author Raven
 */
@SpringBootApplication
public class InstrumentApplication {

    public static HTTPServer server;

    public static void main(String[] args) throws IOException {
        SpringApplication.run(InstrumentApplication.class, args);
        server = new HTTPServer(19090);
    }

}
