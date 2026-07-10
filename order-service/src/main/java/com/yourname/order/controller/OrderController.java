package com.yourname.order.controller;

import com.yourname.order.entity.Order;
import com.yourname.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //1、创建订单
    @PostMapping("/create")
        public Order create(@RequestParam Long productId,
                            @RequestParam Integer quantity){

        return orderService.createOrder(productId,quantity);
    }


}
