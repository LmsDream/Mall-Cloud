package com.yourname.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient    //注册到nacos
@MapperScan("com.yourname.product.mapper")
@EnableCaching            //开启redis缓存（必须加）
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class,args);
        System.out.println("✅ product-service (JDK8) 启动成功！");
    }


}
