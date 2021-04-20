package com.test.instrument.Metrics;

import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Example of using the {@link PrometheusCollector} to convert OTel metrics to Prometheus format and
 * expose these to a Prometheus instance via a {@link HTTPServer} exporter.
 *
 * <p>A {@link LongValueObserver} is used to periodically measure how many incoming messages are
 * awaiting processing. The {@link LongValueObserver} Updater gets executed every collection
 * interval.
 * @author Raven
 */
public final class KeyObserver {
    public long valueCount;
    HTTPServer prometheusPort;

    public KeyObserver(HTTPServer prometheusPort, String work_name, String desc, String unit) {
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();

        PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();

        this.prometheusPort = prometheusPort;

        Meter meter = meterProvider.get("PrometheusExample", "0.13.1");
        meter
                .longValueObserverBuilder(work_name)
                .setDescription(desc)
                .setUnit(unit)
                .setUpdater(result -> result.observe(valueCount, Labels.empty()))
                .build();
    }
}
