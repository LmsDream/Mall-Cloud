package com.yourname.order.service;

import com.yourname.order.entity.Order;

public interface OrderService {

    Order createOrder(Long productId, Integer quantity);
}
