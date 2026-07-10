# 📘 项目配置文档：Spring Cloud 微服务商城脚手架

> **适用版本**：JDK 1.8 | Spring Boot 2.7.18 | Spring Cloud 2021.0.9 | Nacos 2.x

---

## 一、项目概述

本项目是一个基于 **Spring Cloud + Nacos + OpenFeign** 的分布式微服务商城后端脚手架，包含商品服务（`product-service`）和订单服务（`order-service`）。集成 **Redis** 缓存和 **Shiro + JWT** 无状态鉴权，实现服务注册发现、远程调用、缓存加速和接口安全管控。

---

## 二、环境与工具清单

| 组件 | 版本 / 说明 |
| :--- | :--- |
| **JDK** | 1.8（必须） |
| **Maven** | 3.6+ |
| **MySQL** | 5.7.4+ |
| **Redis** | 任意稳定版（默认端口 6379） |
| **Nacos** | 2.x 系列（推荐 2.5.2） |
| **开发工具** | IDEA / Eclipse / Apipost（接口测试） |

---

## 三、核心技术栈及版本锁定

所有版本已在父工程 `pom.xml` 中统一锁定，子模块继承即可。

| 技术 | 版本 | 作用 |
| :--- | :--- | :--- |
| **Spring Boot** | 2.7.18 | 基础容器（内置 Tomcat） |
| **Spring Cloud** | 2021.0.9 | 微服务生态基座 |
| **Spring Cloud Alibaba** | 2021.0.5.0 | Nacos 服务注册发现 |
| **MyBatis-Plus** | 3.5.7 | ORM 简化 SQL 操作 |
| **Shiro** | 1.13.0 | 权限框架（适配 JDK8 最终版） |
| **JJWT (java-jwt)** | 4.4.0 | JWT Token 生成与解析 |
| **Spring Cloud LoadBalancer** | 内置 | Feign 客户端负载均衡 |

---

## 四、项目结构树

```text
mall-cloud/
├── pom.xml                          # 父POM（版本锁）
├── product-service/
│   ├── pom.xml
│   ├── src/main/java/com/yourname/product/
│   │   ├── ProductApplication.java         # 启动类（@EnableCaching + @EnableDiscoveryClient）
│   │   ├── config/shiro/
│   │   │   ├── JwtRealm.java               # 认证器（重写凭证匹配）
│   │   │   ├── JwtToken.java               # Token包装类
│   │   │   ├── JwtFilter.java              # 请求拦截器（取Header）
│   │   │   └── ShiroConfig.java            # Shiro核心配置（无状态Session）
│   │   ├── controller/
│   │   │   ├── ProductController.java      # 商品接口（@RequiresAuthentication）
│   │   │   └── AuthController.java         # 登录接口（生成Token）
│   │   ├── service/
│   │   │   └── ProductService.java         # 业务逻辑（@Cacheable + 扣库存）
│   │   ├── mapper/
│   │   │   └── ProductMapper.java          # MP Mapper
│   │   ├── entity/
│   │   │   └── Product.java                # 实体（implements Serializable）
│   │   └── utils/
│   │       └── JwtUtils.java               # JWT工具类
│   └── src/main/resources/
│       └── application.yml
└── order-service/
    ├── pom.xml
    ├── src/main/java/com/yourname/order/
    │   ├── OrderApplication.java           # 启动类（@EnableFeignClients）
    │   ├── feign/
    │   │   └── ProductFeignClient.java     # 远程调用接口（@FeignClient）
    │   ├── controller/
    │   │   └── OrderController.java
    │   └── service/
    │       └── OrderService.java
    └── src/main/resources/
        └── application.yml
```

## 五、父工程核心配置（pom.xml）
```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<properties>
    <java.version>1.8</java.version>
    <spring-cloud.version>2021.0.9</spring-cloud.version>
    <spring-cloud-alibaba.version>2021.0.5.0</spring-cloud-alibaba.version>
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <jwt.version>4.4.0</jwt.version>
</properties>
```
## 六、各服务配置文件详情
1. product-service 配置（application.yml）
```
server:
  port: 8081

spring:
  application:
    name: product-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mall_db?useSSL=false&characterEncoding=utf8
    username: root
    password: 123456          # ⚠️ 替换为自己的密码
  redis:
    host: localhost
    port: 6379
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```
2. order-service 配置（application.yml）
```
server:
  port: 8082

spring:
  application:
    name: order-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mall_db?useSSL=false&characterEncoding=utf8
    username: root
    password: 123456
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```
3. gateway-service 配置（application.yml）
```
server:
  port: 8080   # 统一入口

spring:
  application:
    name: gateway-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: product-route
          uri: lb://product-service   # lb:// 表示从 Nacos 负载均衡取服务
          predicates:
            - Path=/api/product/**
        - id: order-route
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

## 七、数据库初始化脚本（MySQL 5.7）
```
CREATE DATABASE IF NOT EXISTS mall_db DEFAULT CHARSET=utf8mb4;
USE mall_db;

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO product (name, price, stock) VALUES 
('华为 Mate 60', 6999.00, 100),
('小米 14 Ultra', 5999.00, 50),
('OPPO Find X7', 4999.00, 80);
```

## 八、服务启动顺序（严格按此顺序）

| 顺序 | 服务             | 启动命令 / 方式                                                                 | 验证方式                                      |
| ---- | ---------------- | ------------------------------------------------------------------------------ | --------------------------------------------- |
| 1    | Nacos            | Windows: `startup.cmd -m standalone`<br>Mac/Linux: `sh startup.sh -m standalone` | 访问 `http://localhost:8848/nacos`            |
| 2    | Redis            | `redis-server`                                                                 | `redis-cli ping` 返回 `PONG`                   |
| 3    | MySQL            | 启动本地服务                                                                    | 确保 3306 端口可用                            |
| 4    | product-service  | 运行 `ProductApplication.main()`                                                | 控制台打印 `Started ProductApplication`        |
| 5    | order-service    | 运行 `OrderApplication.main()`                                                  | 控制台打印 `Started OrderApplication`          |
| 6    | gateway-service  | 运行 `GatewayApplication.main()`                                                | 控制台打印 `Started GatewayApplication`        |

## 九、接口测试用例（全部走网关8080端口）

| 步骤                 | 方法 | URL（网关入口）                              | 请求头（Headers）                          | 请求参数                  | 预期响应                          |
| -------------------- | ---- | ------------------------------------------- | ------------------------------------------ | ------------------------- | --------------------------------- |
| 1.登录               | POST | `http://localhost:8080/api/auth/login`      | `Content-Type: application/json`            | `username=admin`<br>`password=123` | 返回 JWT Token 字符串             |
| 2.查商品（带Token）  | GET  | `http://localhost:8080/api/product`         | `Authorization: Bearer {上一步Token}`       | 无                        | 返回 3 条商品 JSON                |
| 3.查商品（不带Token）| GET  | `http://localhost:8080/api/product`         | 无                                          | 无                        | 401 Unauthorized（拦截生效）|
| 4.下单（扣库存）| POST | `http://localhost:8080/api/order/create`    | `Authorization: Bearer {Token}`             | `productId=1`<br>`quantity=2` | 返回“下单成功！扣库存成功” |

## 十、核心代码片段说明

1. 网关 JWT 全局过滤器（JwtGlobalFilter.java）

```
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 登录接口放行
        // 2. 从 Header 取 Authorization: Bearer {token}
        // 3. 调用 JwtUtils 解析，失败返回 401
        // 4. 成功则将用户名放入请求头，转发给下游
    }
    @Override
    public int getOrder() { return -1; } // 最高优先级
}
```
2. 商品服务扣库存（ProductService.java）
```
@Cacheable(value = "product", key = "'allList'")  // 固定字符串加单引号
public List<Product> findAll() { ... }

@Transactional
public boolean deductStock(Long id, Integer quantity) {
    // 查库 -> 校验库存 -> 扣减 -> update
}
```
3. 订单服务远程调用（ProductFeignClient.java）
```
@FeignClient(name = "product-service")
public interface ProductFeignClient {
    @PutMapping("/api/product/deduct/{id}/{quantity}")
    String deduct(@PathVariable("id") Long id, @PathVariable("quantity") Integer quantity);
}
```
## 十一、常见问题与解决方案（踩坑记录）

| 报错信息 | 原因 | 解决方案 |
| ---- | ---- | ---- |
| `No Feign Client for loadBalancing defined` | 缺少负载均衡器依赖 | 在 `order-service/pom.xml` 中添加 `spring-cloud-starter-loadbalancer` |
| `EL1008E: Property 'allList' cannot be found` | `@Cacheable` 的 `key` 缺少单引号 | 改为 `key = "'allList'"` |
| `NotSerializableException: Product` | Redis 序列化要求实现 `Serializable` | `Product` 类增加 `implements Serializable` |
| `Client not connected, current status:STARTING` | Nacos 未启动或连接超时 | 启动 Nacos 或在配置增加 `timeout: 10000` |
| Gateway 启动报 `spring-boot-starter-web` 冲突 | Gateway 不能和 web 依赖共存 | 检查 `gateway-service/pom.xml`，确保没有引入 `spring-boot-starter-web` |
| 网关路由 404 | 路由断言路径写错，或下游服务未注册到 Nacos | 检查 `application.yml` 中的 Path 是否与 Controller 映射一致，检查 Nacos 服务列表 |

## 十二、架构总览图
```
[客户端]
    │
    ▼
(gateway-service 端口 8080)
    │
    ├── 1. JWT 全局鉴权（放行 /login，拦截其他）
    │
    ├── 2. 路由转发
    │       ├── /api/product/** → product-service (8081)
    │       └── /api/order/**   → order-service (8082)
    │
    ▼
[业务服务]
    ├── product-service: 查商品（Redis缓存）、扣库存（MySQL）
    └── order-service: 远程调用 product-service 扣库存，生成订单
```

## 十三、后续扩展建议
- 配置中心：引入 Nacos Config，统一管理多环境配置。
- 熔断降级：引入 Sentinel 或 Resilience4j，在网关层做限流。
- 链路追踪：引入 Sleuth + Zipkin，追踪分布式调用链。
- 容器化部署：编写 `Dockerfile`，将服务打包成镜像并编排。

# 🛒 Mall-Cloud · 微服务商城脚手架

[![JDK](https://img.shields.io/badge/JDK-1.8-green.svg)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.9-blue)](https://spring.io/projects/spring-cloud)
[![Nacos](https://img.shields.io/badge/Nacos-2.x-orange)](https://nacos.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](https://opensource.org/licenses/Apache-2.0)

## 📖 项目简介

**Mall-Cloud** 是一个基于 Spring Cloud 生态构建的微服务商城后端脚手架。实现了 **网关统一鉴权**、**服务注册发现**、**远程调用**、**Redis 缓存** 及 **缓存一致性维护**。

项目采用 **JDK 1.8** 及稳定的 **Spring Boot 2.7.18** 版本，代码简洁规范，适合作为微服务项目的起步基座或简历项目展示。

## 🏗️ 系统架构

```text
客户端 (Apipost/Postman)
        │
        ▼
 Gateway (8080) —— JWT 全局鉴权过滤器
        │
        ├── /api/product/** → Product Service (8081)
        │                     ├── CRUD + Redis Cache
        │                     └── Cache-Aside 模式（@CacheEvict）
        │
        └── /api/order/**   → Order Service (8082)
                              ├── Feign 远程调用 Product
                              ├── 分布式事务（预留 Seata 接口）
                              └── 订单落库 (MySQL)

# 项目名称：mall-cloud（微服务商城脚手架）

## 技术栈
- Spring Boot 2.7.18
- Spring Cloud 2021.0.9
- Nacos 2.x
- Redis
- Shiro 1.13.0 + JWT

## 启动顺序
1. Nacos
2. Redis
3. MySQL
4. product-service (8081)
5. order-service (8082)