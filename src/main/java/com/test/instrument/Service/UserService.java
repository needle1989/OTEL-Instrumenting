package com.test.instrument.Service;

import com.test.instrument.InstrumentApplication;
import com.test.instrument.Metrics.KeyLongCounter;
import com.test.instrument.Metrics.KeyObserver;
import io.prometheus.client.exporter.HTTPServer;
import org.springframework.stereotype.Service;

/**
 * @author Raven
 */
@Service
public class UserService {
    HTTPServer server = InstrumentApplication.server;
    KeyObserver keyObserver = new KeyObserver(server, "key", "this is a key num", "value");
    KeyLongCounter keyLongCounter = new KeyLongCounter(server,"test", "this is for testing", "visit");

    public String getHello(){
        keyObserver.valueCount = 3;
        keyLongCounter.authWorkBound.add(1);
        return "hello";
    }
}
