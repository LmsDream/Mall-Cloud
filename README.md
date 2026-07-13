# 🛒 Mall-Cloud · 微服务商城脚手架

[![JDK](https://img.shields.io/badge/JDK-1.8-green.svg)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.9-blue)](https://spring.io/projects/spring-cloud)
[![Nacos](https://img.shields.io/badge/Nacos-2.x-orange)](https://nacos.io/)
[![Seata](https://img.shields.io/badge/Seata-1.5.2-red)](https://seata.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](https://opensource.org/licenses/Apache-2.0)

---

## 📖 项目简介

**Mall-Cloud** 是一个基于 Spring Cloud 生态构建的微服务商城后端脚手架。实现了 **服务注册发现**、**远程调用**、**Redis 缓存**、**JWT 无状态鉴权** 以及 **Seata 分布式事务**。

项目采用 **JDK 1.8** 及稳定的 **Spring Boot 2.7.18** 版本，代码简洁规范，适合作为微服务项目的起步基座或简历项目展示。

**GitHub 地址**：`https://github.com/LmsDream/Mall-Cloud`

---

## 🏗️ 系统架构图

```text
客户端 (Apipost/Postman)
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│                    product-service (8081)                 │
│  ┌───────────────────────────────────────────────────┐   │
│  │  • 商品 CRUD                                      │   │
│  │  • Redis 缓存（@Cacheable + @CacheEvict）         │   │
│  │  • Shiro + JWT 无状态鉴权                         │   │
│  │  • 扣库存接口（供 Feign 调用）                    │   │
│  └───────────────────────────────────────────────────┘   │
│                         ▲                                 │
│                         │ Feign 远程调用                  │
│                         │                                 │
└─────────────────────────┼─────────────────────────────────┘
                          │
┌─────────────────────────┼─────────────────────────────────┐
│                         │                                 │
│  ┌──────────────────────┴────────────────────────────┐   │
│  │              order-service (8082)                  │   │
│  │  • 远程调用 product-service 获取商品价格           │   │
│  │  • 远程调用 product-service 扣减库存              │   │
│  │  • 计算订单总价，插入订单表                       │   │
│  │  • @GlobalTransactional（Seata 分布式事务）      │   │
│  └───────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────┘
                          │
                          ▼
              ┌─────────────────────┐
              │   Nacos (注册中心)   │
              │   Redis (缓存)       │
              │   MySQL (数据库)     │
              └─────────────────────┘
```

## 📦 技术栈

| 技术                          | 版本       | 作用                    |
| :---------------------------- | :--------- | :---------------------- |
| **JDK**                       | 1.8        | 基础运行环境            |
| **Spring Boot**               | 2.7.18     | 基础框架（内置 Tomcat） |
| **Spring Cloud**              | 2021.0.9   | 微服务生态基座          |
| **Spring Cloud Alibaba**      | 2021.0.5.0 | Nacos 服务注册发现      |
| **OpenFeign**                 | 2021.0.9   | 服务间远程调用          |
| **MyBatis-Plus**              | 3.5.7      | ORM 框架                |
| **Redis**                     | 任意稳定版 | 缓存中间件              |
| **Shiro**                     | 1.13.0     | JWT 无状态鉴权          |
| **JJWT**                      | 4.4.0      | JWT 生成与解析          |
| **Seata**                     | 1.5.2      | 分布式事务（AT 模式）   |
| **Spring Cloud LoadBalancer** | 2021.0.9   | 客户端负载均衡          |

## 🚀 快速启动（本地开发）

### 1. 前置环境

请确保已安装并启动以下服务：

| 服务             | 端口 | 启动命令                      |
| :--------------- | :--- | :---------------------------- |
| **Nacos**        | 8848 | `startup.cmd -m standalone`   |
| **Redis**        | 6379 | `redis-server`                |
| **MySQL**        | 3306 | 启动本地服务                  |
| **Seata Server** | 7091 | `seata-server.bat`（Windows） |

### 2. 数据库初始化

在 MySQL 中执行以下脚本：

sql

```
-- 创建商品库
CREATE DATABASE IF NOT EXISTS mall_db DEFAULT CHARSET=utf8mb4;
USE mall_db;

-- 商品表
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

-- Seata undo_log 表（分布式事务回滚日志）
CREATE TABLE `undo_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `branch_id` bigint(20) NOT NULL,
    `xid` varchar(100) NOT NULL,
    `context` varchar(128) NOT NULL,
    `rollback_info` longblob NOT NULL,
    `log_status` int(11) NOT NULL,
    `log_created` datetime NOT NULL,
    `log_modified` datetime NOT NULL,
    `ext` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```



### 3. 配置文件修改

修改各服务 `application.yml` 中的 MySQL 密码：

yaml

```
spring:
  datasource:
    password: 你的MySQL密码
```



### 4. 启动顺序

text

```
1. Nacos (8848)
2. Redis (6379)
3. MySQL (3306)
4. Seata Server (7091)
5. product-service (8081)
6. order-service (8082)
```



------

## 🧪 接口测试

所有接口使用 **Apipost** 或 **Postman** 测试。

| 步骤          | 方法 | URL                                       | 请求头                          | 参数                            | 预期响应                    |
| :------------ | :--- | :---------------------------------------- | :------------------------------ | :------------------------------ | :-------------------------- |
| **1. 登录**   | POST | `http://localhost:8081/api/auth/login`    | 无                              | `username=admin` `password=123` | 返回 JWT Token              |
| **2. 查商品** | GET  | `http://localhost:8081/api/product/get/1` | `Authorization: Bearer {Token}` | 无                              | 返回商品 JSON               |
| **3. 下单**   | POST | `http://localhost:8082/api/order/create`  | 无                              | `productId=1` `quantity=2`      | 返回订单详情 JSON（含总价） |

**下单成功响应示例**：

json

```
{
    "id": 1,
    "productId": 1,
    "quantity": 2,
    "totalPrice": 13998.00,
    "status": "SUCCESS",
    "createTime": "2026-07-13T14:30:00.000+00:00"
}
```



------

## 📂 项目结构

text

```
mall-cloud/
├── pom.xml                              # 父POM（版本锁）
├── .gitignore
├── README.md
│
├── product-service/                     # 商品服务（8081）
│   ├── src/main/java/com/yourname/product/
│   │   ├── ProductApplication.java      # 启动类
│   │   ├── config/shiro/
│   │   │   ├── JwtRealm.java           # JWT 认证器
│   │   │   ├── JwtToken.java           # Token 包装类
│   │   │   ├── JwtFilter.java          # 自定义过滤器（含白名单）
│   │   │   └── ShiroConfig.java        # Shiro 配置
│   │   ├── controller/
│   │   │   ├── ProductController.java  # 商品接口
│   │   │   └── AuthController.java     # 登录接口
│   │   ├── service/
│   │   │   └── ProductService.java     # 业务逻辑（缓存 + 扣库存）
│   │   ├── mapper/
│   │   │   └── ProductMapper.java
│   │   ├── entity/
│   │   │   └── Product.java
│   │   └── utils/
│   │       └── JwtUtils.java
│   └── src/main/resources/
│       └── application.yml
│
└── order-service/                       # 订单服务（8082）
    ├── src/main/java/com/yourname/order/
    │   ├── OrderApplication.java       # 启动类（@EnableFeignClients）
    │   ├── feign/
    │   │   └── ProductFeignClient.java # 远程调用接口
    │   ├── dto/
    │   │   └── ProductDTO.java
    │   ├── controller/
    │   │   └── OrderController.java
    │   ├── service/
    │   │   └── OrderService.java       # 下单业务（@GlobalTransactional）
    │   ├── entity/
    │   │   └── Order.java
    │   └── mapper/
    │       └── OrderMapper.java
    └── src/main/resources/
        └── application.yml
```

## 🔧 核心功能说明

### 1. 商品服务（product-service）

| 功能       | 实现方式                    | 说明                                                |
| :--------- | :-------------------------- | :-------------------------------------------------- |
| 商品查询   | `@Cacheable`                | 查询结果缓存到 Redis，提升性能                      |
| 扣减库存   | `@CacheEvict`               | 更新数据库后自动清除 Redis 缓存（Cache-Aside 模式） |
| JWT 鉴权   | Shiro + JWT                 | 自定义 `JwtFilter`，无状态认证                      |
| 白名单放行 | `JwtFilter.isAccessAllowed` | 登录接口和 Feign 调用路径免 Token 访问              |

### 2. 订单服务（order-service）

| 功能       | 实现方式                 | 说明                                      |
| :--------- | :----------------------- | :---------------------------------------- |
| 远程调用   | OpenFeign + LoadBalancer | 通过 Nacos 服务发现调用 `product-service` |
| 下单流程   | `@GlobalTransactional`   | Feign 扣库存 → 计算总价 → 订单落库        |
| 分布式事务 | Seata AT 模式            | 保证扣库存和插入订单的原子性              |

### 3. Seata 分布式事务

| 组件                     | 说明                                      |
| :----------------------- | :---------------------------------------- |
| **TC (Seata Server)**    | 独立部署，端口 7091，注册到 Nacos         |
| **TM (order-service)**   | 发起全局事务，标记 `@GlobalTransactional` |
| **RM (product-service)** | 参与事务的分支（扣库存）                  |
| **undo_log 表**          | 各业务库中创建，用于 AT 模式回滚          |

------

## 🐛 踩坑记录

| 问题                                          | 原因                                   | 解决方案                                            |
| :-------------------------------------------- | :------------------------------------- | :-------------------------------------------------- |
| `No Feign Client for loadBalancing defined`   | 缺少负载均衡器依赖                     | 添加 `spring-cloud-starter-loadbalancer`            |
| `EL1008E: Property 'allList' cannot be found` | `@Cacheable` 的 `key` 缺少单引号       | 改为 `key = "'allList'"`                            |
| `NotSerializableException: Product`           | Redis 序列化要求实现 `Serializable`    | `Product` 类实现 `Serializable`                     |
| `IncorrectCredentialsException`               | JWT 凭证默认比较逻辑不匹配             | 在 `JwtRealm` 中重写 `assertCredentialsMatch`       |
| 配置 `anon` 后 Feign 调用仍 401               | `JwtFilter` 在 `anon` 规则之前执行     | 在 `JwtFilter.isAccessAllowed` 中显式放行白名单路径 |
| Seata 启动报 `Could not resolve placeholder`  | `application.yml` 缺少 `security` 配置 | 从 `application.example.yml` 复制完整配置           |
| Feign 调用报 `Connection refused`             | `product-service` 未启动或端口不对     | 确认服务已启动且 Nacos 中健康实例为 1               |

------

## 📈 后续拓展方向

| 方向                      | 难度 | 简历加分点                               |
| :------------------------ | :--- | :--------------------------------------- |
| **Spring Cloud Gateway**  | ⭐⭐⭐⭐ | 统一入口、路由转发、网关层鉴权           |
| **Sentinel 熔断降级**     | ⭐⭐⭐  | 服务高可用，防止雪崩                     |
| **Docker 容器化部署**     | ⭐⭐⭐  | 编写 `Dockerfile` + `docker-compose.yml` |
| **Nacos Config 配置中心** | ⭐⭐⭐  | 配置动态刷新，无需重启服务               |
| **JMeter 压力测试**       | ⭐⭐   | 验证高并发场景下的系统稳定性             |

------

## 📝 版本记录

| 版本 | 日期       | 更新内容                                                     |
| :--- | :--------- | :----------------------------------------------------------- |
| v1.0 | 2026-07-10 | 完成 product-service + order-service，集成 Nacos、Redis、Shiro+JWT |
| v1.1 | 2026-07-13 | 集成 Seata 分布式事务，完善文档和踩坑记录                    |

------

**作者**：LmsDream
**GitHub**：`https://github.com/LmsDream`
**项目地址**：`https://github.com/LmsDream/Mall-Cloud`
**最后更新**：2026-07-13

> 💡 如果本项目对你有帮助，欢迎 Star ⭐ 支持！