package com.test.instrument.Controller;

import com.test.instrument.InstrumentApplication;
import com.test.instrument.Metrics.*;
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

    HTTPServer server = InstrumentApplication.server;
    KeyObserver keyObserver = new KeyObserver(server, "key", "this is a key num", "value");
    KeyLongCounter keyLongCounter = new KeyLongCounter(server,"test", "this is for testing", "visit");

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

        keyObserver.valueCount = 1;
        keyLongCounter.authWorkBound.add(1);

        /**
         * AIOps
         */
        return "hello";
    }
}
