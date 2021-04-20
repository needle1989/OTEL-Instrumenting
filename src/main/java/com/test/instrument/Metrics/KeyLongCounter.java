package com.test.instrument.Metrics;

import io.opentelemetry.api.metrics.*;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Example of using the {@link PrometheusCollector} to convert OTel metrics to Prometheus format and
 * expose these to a Prometheus instance via a {@link HTTPServer} exporter.
 *
 * <p>A {@link KeyLongCounter} is used to periodically measure how many incoming messages are
 * awaiting processing. The {@link KeyLongCounter} Updater gets executed every time a add operation is called.
 * @author Raven
 */
public final class KeyLongCounter {
    HTTPServer prometheusPort;
    public final LongCounter keyCounter;
    public final BoundLongCounter authWorkBound;

    public KeyLongCounter(HTTPServer prometheusPort, String work_name, String description, String unit) {

        SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();

        PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();

        this.prometheusPort = prometheusPort;

        Meter meter = meterProvider.get("PrometheusExample", "0.13.1");
        keyCounter = meter.longCounterBuilder(work_name).setDescription(description).setUnit(unit).build();
        authWorkBound = keyCounter.bind(Labels.of("work_name",work_name));
    }
}


