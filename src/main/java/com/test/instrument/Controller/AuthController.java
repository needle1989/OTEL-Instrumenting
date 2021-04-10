package com.test.instrument.Controller;

import com.test.instrument.Metrics.*;
import io.opentelemetry.api.metrics.MeterProvider;
import io.prometheus.client.exporter.HTTPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Raven
 */
@RestController
@RequestMapping("/v1")
public class AuthController {
    /**
     * AIOps
     */

    HTTPServer server = new HTTPServer(19090);
    KeyObserver keyObserver = new KeyObserver(server);
    KeyCounter keyCounter = new KeyCounter(server);

    public AuthController() throws IOException {
    }
    /**
     * AIOps
     */
    @Autowired
    @RequestMapping("/hello")
    public String getHello() {
        /**
         * AIOps
         */

        keyObserver.incomingMessageCount = 1;
        keyCounter.authWorkBound.add(1);

        /**
         * AIOps
         */
        return "hello";
    }
}
