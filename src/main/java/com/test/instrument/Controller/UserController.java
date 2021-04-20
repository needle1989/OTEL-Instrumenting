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
@RequestMapping("/v2")
public class UserController {
    /**
     * AIOps
     */

    HTTPServer server = InstrumentApplication.server;
    KeyLongCounter keyLongCounter = new KeyLongCounter(server,"user", "this is a test 2", "visit");

    public UserController() throws IOException {
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

        keyLongCounter.authWorkBound.add(1);

        /**
         * AIOps
         */
        return "hello";
    }
}
