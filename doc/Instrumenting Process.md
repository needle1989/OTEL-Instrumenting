# Instrumenting Process for Metrics

**1852141 Li Detao**

**1854036 Wu Yue**

[TOC]

## 0 Environment & Requirement

* 本文档是基于[train-ticket](https://github.com/FudanSELab/train-ticket)这一微服务系统的metrics插桩文档
* 本文档中的Metrics插桩基于[OpenTelemetry](https://github.com/open-telemetry/opentelemetry-java/releases/tag/v1.1.0) Version1.1.0，截止2021.4.19为OTel的最新版本
* train-ticket系统的部署环境为实验室10.60.38.173，通过docker部署train-ticket，由Prometheus (10.60.38.173:9090) 收集数据并通过Grafana (10.60.38.173:3002) 可视化
* 所有相关配置文件、jar包及数据保存在173的AIOps中

## 1 Prometheus & Grafana

### 1.1Prometheus

* 配置文件为AIOps中的prometheus.yml，每隔60s抓取一次目标端口中的数据，示例如下

  ```yaml
  global:
    scrape_interval:     60s
    evaluation_interval: 60s
   
  scrape_configs:
    - job_name: prometheus
      static_configs:
        - targets: ['localhost:9090']
          labels:
            instance: prometheus
            
    - job_name: authService
      static_configs:
        - targets: ['10.60.38.173:19091']
          labels:
            instance: auth
            
    - job_name: orderService
      static_configs:
        - targets: ['10.60.38.173:19092']
          labels:
            instance: order
  ```

* 在docker container中安装Prometheus（注意挂载配置文件的目录）

  ```
  docker run  -d \
    -p 9090:9090 \
    -v /opt/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml  \
    prom/prometheus
  ```

### 1.2 Grafana

* 创建存储信息的目录并给予777权限

  ```
  mkdir grafana-storage
  chmod 777 grafana-storage/
  ```

* 在docker container中安装Grafana（注意挂载配置文件的目录）

  ```
  docker run -d \
    -p 3003:3000 \
    --name=grafana \
    -v /opt/grafana-storage:/var/lib/grafana \
    -e "GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource" \
    grafana/grafana
  ```

## 2 Instrumenting Solution

### 2.1 Solution Process

由于该项目较为庞大，173无法直接运行全部的微服务，且项目在服务器上不方便调试，我们采取了本地调试、服务器局部部署的方案。

**本项目解决方案的基本流程如下：**

* 分析train-ticket的业务流，以单个微服务为单位完成插桩代码

* 在服务器上部署相应微服务的mongo数据库，在宿主机上暴露端口，示例如下：

  ```yaml
  version: '3'
  services:
    ts-auth-mongo:
      image: mongo
      ports:
        - 11451:27017
  ```

* 本地完成微服务的配置、调试工作，使用Postman测试接口，最后通过maven打包

* 编写相应的Dockerfile，示例如下：

  ```yaml
  FROM java:8-jre
  
  RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
  
  ADD ./ts-auth-service-1.0.jar /app/
  CMD ["java", "-Xmx200m", "-jar", "/app/ts-auth-service-1.0.jar"]
  
  EXPOSE 12349
  ```

* 将jar包和Dockerfile上传服务器，cd到相应目录打包镜像：

  ```
  docker build -t otel/auth:1.0 .
  ```

* 编写部署局部微服务的docker-compose：

  ```yaml
  version: '3'
  services:
  
    ts-auth-service:
      image: otel/auth:1.0
      restart: always
      ports:
        - 12340:12340
        - 19091:19091
      networks:
        - my-network
  
    ts-auth-mongo:
      image: mongo
      networks:
        - my-network
  
    ts-order-service:
      image: otel/order:1.0
      restart: always
      ports:
        - 12031:12031
        - 19092:19092
      networks:
        - my-network
  
    ts-order-mongo:
      image: mongo
      networks:
        - my-network
  
  networks:
      my-network:
        driver: bridge
  ```

* 部署微服务：

  ```
  docker-compose -f demo.yml up
  ```

### 2.2 Compose Implement

* OTel中提供的格式不能直接被Prometheus识别。OTel java SDK中提供了将Prometheus作为metrics exporter并通过HTTP暴露metrics的方法，本文档中的插桩方法都是基于这一原理。

* 这里以Bound Long Counter为例简述编写一个Implement的方法（相关代码如下）

  完整的Implement流程包括：

  * 构建meter provider以及由它提供的meter
  * 通过不同的builder构建相对应的的metrics，并加入名称、描述、单位等相关信息（代码中已暴露相关接口）
  * 通过Prometheus collector build方法将OTel格式转换为可供Prometheus识别的格式
  * 将编写好的metrics通过HTTP端口暴露出去供Prometheus抓取

  ```java
  public KeyLongCounter(HTTPServer prometheusPort, String work_name, String description, String unit) {
  
          SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
  
          PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();
  
          this.prometheusPort = prometheusPort;
  
          Meter meter = meterProvider.get("PrometheusExample", "0.13.1");
          
          keyCounter = meter.longCounterBuilder(work_name).setDescription(description).setUnit(unit).build();
      
          authWorkBound = keyCounter.bind(Labels.of("work_name",work_name));
      }
  ```



## 3 Business Flow for Instrumenting

### Flow 1 Order Payment

*简介：用户登录并在订单列表中选择未支付的订单支付*

涉及微服务：

| 微服务                       | 功能                           |
| ---------------------------- | ------------------------------ |
| ts-auth-service              | 用户登录                       |
| ts-verification-code-service | 验证登录                       |
| ts-order-service             | 查询用户订单（D、G开头）       |
| ts-order-other-service       | 查询用户其他订单（非D、G开头） |
| ts-inside-payment-service    | 支付订单                       |

插桩：

| 微服务                    | Instrument 类型 | 位置                                                    | 描述                                                 |
| ------------------------- | --------------- | ------------------------------------------------------- | ---------------------------------------------------- |
| ts-auth-service           | Counter         | /api/v1/users/login                                     | 收集用户登录访问量，除以时间间隔可得到访问率         |
| ts-auth-service           | Counter         | auth.service.impl.UserServiceImpl#createDefaultAuthUser | 统计创建账户数                                       |
| ts-order-service          | Counter         | order.service.OrderServiceImpl#create                   | 统计创建G、D开头订单量                               |
| ts-order-other-service    | Counter         | other.service.OrderOtherServiceImpl#create              | 统计创建非G、D开头订单量                             |
| ts-inside-payment-service | Counter         | inside_payment.service.InsidePaymentServiceImpl#pay     | 统计成功支付订单量，可以统计调用不同支付方式支付数量 |

## 4 Fault Analysis and Debugging

