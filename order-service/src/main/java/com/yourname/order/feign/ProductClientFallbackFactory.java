package com.yourname.order.feign;

import com.yourname.order.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductFeignClient> {

    @Override
    public ProductFeignClient create(Throwable cause) {

        return new ProductFeignClient() {
            @Override
            public ProductDto getById(Long id) {
                log.error("远程调用 ProductFeignClient#getById 失败，商品ID:{}",id,cause);
                //降级返回一个默认商品（兜底数据）
                ProductDto fallback = new ProductDto();
                fallback.setId(id);
                fallback.setName("商品信息获取失败，请稍后再试");
                fallback.setPrice(BigDecimal.ZERO);
                fallback.setStock(0);
                return fallback;
            }
            @Override
            public String deduct(Long productId, Integer quantity) {
                log.error("远程调用 ProductFeignClient#deduct 失败，商品ID：{}，数量：{}",productId,quantity,cause);
                return "扣减库存失败，请稍后再试";
            }
        };
    }
}
