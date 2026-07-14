package com.yourname.order.feign;

import com.yourname.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "product-service",fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductFeignClient {

    //1、扣库存
    @PutMapping("/api/product/deduct/{productId}/{quantity}")
    String deduct(@PathVariable("productId") Long productId,
                  @PathVariable("quantity") Integer quantity);
    //2、根据商品id查询商品价格
    @GetMapping("/api/product/get/{id}")
    ProductDto getById(@PathVariable("id") Long id);

}
