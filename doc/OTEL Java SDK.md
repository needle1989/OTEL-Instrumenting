## 主要思想

使用OTEL的SDK进行简单插桩：将Prometheus作为metrics exporter，并通过HTTP暴露OTel定义的metrics。

以https://github.com/open-telemetry/opentelemetry-java/tree/main/examples/prometheus/src/main/java/io/opentelemetry/example/prometheus提供的example说明：

* 配置

  HTTPServer：设置Prometheus暴露数据的port；

  MeterProvider：本次插桩使用的度量。

  ```java
    static MeterProvider initializeOpenTelemetry(int prometheusPort) throws IOException {
      SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
  
      PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();
  
      server = new HTTPServer(prometheusPort);
  
      return meterProvider;
    }
  ```

  

* 应用

  使用PrometheusCollector将OTEL的metrics转换为Prometheus的格式并通过HTTP暴露

  该例中使用OTEL度量LongValueObserver，每个interval更新该度量

  在头部import使用的OTEL度量：

  ```java
  import io.opentelemetry.api.metrics.LongValueObserver;
  ```

  核心代码：

  ```java
    //设置度量为long型incomingMessageCount
    private long incomingMessageCount;
    //设置度量的详细内容
    public PrometheusExample(MeterProvider meterProvider) {
      Meter meter = meterProvider.get("PrometheusExample", "0.13.1");
      meter
          .longValueObserverBuilder("incoming.messages")
          .setDescription("No of incoming messages awaiting processing")
          .setUnit("message")
          .setUpdater(result -> result.observe(incomingMessageCount, Labels.empty()))
          .build();
    }
  ```

  

  





