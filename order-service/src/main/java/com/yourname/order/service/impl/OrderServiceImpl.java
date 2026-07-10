package com.yourname.order.service.impl;

import com.yourname.order.dto.ProductDto;
import com.yourname.order.entity.Order;
import com.yourname.order.feign.ProductFeignClient;
import com.yourname.order.mapper.OrderMapper;
import com.yourname.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order createOrder(Long productId, Integer quantity) {
        //1、远程调用获取商品信息（主要获取商品单价）
        ProductDto product = productFeignClient.getById(productId);
        if (product == null){
            throw new RuntimeException("商品不存在，ID："+productId);
        }
        //2、远程调用商品服务扣减库存
        String deductResult = productFeignClient.deduct(productId, quantity);
        log.info("扣减库存结果：{}"+deductResult);
        //3、计算商品总价（单价x数量）
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(quantity));
        //4、构建订单实体
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setStatus("SUCCESS");
        order.setCreateTime(new Date());
        //5、插入订单表（本地事务）
        int rows = orderMapper.insert(order);
        if (rows<=0){
            throw new RuntimeException("订单插入失败！");
        }
        log.info("订单创建成功，订单ID：{}，总价：{}",order.getId(),totalPrice);
        //这里可以插入订单表
        return order;
    }
}
