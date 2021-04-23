package com.test.instrument.Controller;

import com.test.instrument.InstrumentApplication;
import com.test.instrument.Metrics.*;
import com.test.instrument.Service.UserService;
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


    public AuthController() throws IOException {
    }
    @Autowired
    private UserService userService;
    @RequestMapping("/hello")
    public String getHello() {
        String res = userService.getHello();
        return res;
    }
}
