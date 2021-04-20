# Instrumenting Process for Metrics

**1852141 Li Detao**

**1854036 Wu Yue**

[TOC]

## 0 Enviroment & Requirement

* 本文档是基于[train-ticket](https://github.com/FudanSELab/train-ticket)这一微服务系统的metrics插桩文档
* 本文档中的Metrics插桩基于[OpenTelemetry](https://github.com/open-telemetry/opentelemetry-java/releases/tag/v1.1.0) Version1.1.0，截止2021.4.19为OTel的最新版本
* train-ticket系统的部署环境为实验室10.60.38.173，通过docker部署train-ticket，由Prometheus收集数据并通过Grafana可视化

## 1 Prometheus

## 2 Instrumenting Solution

### 2.1 Compose Exporter 

* OTel中提供的格式不能直接被Prometheus识别。OTel java SDK中提供了将Prometheus作为metrics exporter并通过HTTP暴露metrics的方法，本文档中的插桩方法都是基于这一原理。

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

