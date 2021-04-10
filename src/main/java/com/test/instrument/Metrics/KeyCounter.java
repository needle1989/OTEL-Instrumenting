package com.test.instrument.Metrics;

import io.opentelemetry.api.metrics.*;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

/**
 * @author Raven
 */
public class KeyCounter {
    private final HTTPServer prometheusPort;
    private final MeterProvider meterSdkProvider = GlobalMetricsProvider.get();
    private final Meter sampleMeter =
            meterSdkProvider.get("io.opentelemetry.example.metrics", "0.13.1");
    public final LongCounter directoryCounter =
            sampleMeter
                    .longCounterBuilder("directories_search_count")
                    .setDescription("Counts directories accessed while searching for files.")
                    .setUnit("unit")
                    .build();
    public final BoundLongCounter authWorkBound = directoryCounter.bind(Labels.of("work_name","some_work"));
    public KeyCounter(HTTPServer prometheusPort) throws IOException {
        this.prometheusPort = prometheusPort;
        PrometheusCollector.builder().setMetricProducer((SdkMeterProvider) meterSdkProvider).buildAndRegister();

        final BoundLongCounter authWorkBound = directoryCounter.bind(Labels.of("work_name","some_work"));
    }
}
